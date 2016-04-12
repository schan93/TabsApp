package com.test.tabs.tabs.com.tabs.activity;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.batch.android.Batch;
import com.batch.android.Config;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.firebase.client.Firebase;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.tabs.gcm.registration.Registration;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsDataSource;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;
import com.test.tabs.tabs.com.tabs.gcm.GcmIntentService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by KCKusumi on 2/1/2016.
 */
public class FireBaseApplication extends Application {
    private static boolean fromAnotherActivity = false;
    private static String name = "";
    private static String userId = "";
    private static FriendRecyclerViewAdapter friendsAdapter;
    private static PostRecyclerViewAdapter publicAdapter;
    private static PostRecyclerViewAdapter privateAdapter;
    private static PostRecyclerViewAdapter myTabsAdapter;
    private static CommentsRecyclerViewAdapter commentsRecyclerViewAdapter;
    private static Post currentPost;

    private Firebase myFirebaseRef;

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        //Firebase apps automatically handle temporary network interruptions for you.
        //Cached data will still be available while offline and your writes will be resent when network connectivity is recovered.
        // Enabling disk persistence allows our app to also keep all of its state even after an app restart.
        // We can enable disk persistence with just one line of code.
        Firebase.getDefaultConfig().setPersistenceEnabled(true);


        myFirebaseRef = new Firebase("https://tabsapp.firebaseio.com/");
        //Batch push notifications
        //Batch.setConfig(new Config("DEV56BC2B1738EFE251617C406E76D"));
        Batch.Push.setGCMSenderId("213033849274");
        Batch.setConfig(new Config("AIzaSyCP0MX6xM67bdd3-2cqCVjHqVFvF4HgcIw"));

        //GCM push notifications
        new GcmRegistrationAsyncTask(this).execute();
        //Starting gcm services
        new GcmIntentService();
        initializeAdapters();
        //Configure Fresco so that image loads quickly
        configFresco();
        //Make sure that adapters don't

    }

    public static String getName() {
        return name;
    }

    public void setName(String userName) {
        name = userName;
    }

    public static String getUserId() {
        return userId;
    }

    public void setUserId(String currentUserId) {
         userId = currentUserId;
    }

    public static PostRecyclerViewAdapter getPublicAdapter() {
        return publicAdapter;
    }

    public void setPublicAdapter(PostRecyclerViewAdapter publicAdapter) {
        this.publicAdapter = publicAdapter;
    }

    public static PostRecyclerViewAdapter getPrivateAdapter() {
        return privateAdapter;
    }

    public void setPrivateAdapter(PostRecyclerViewAdapter privateAdapter) {
        this.privateAdapter = privateAdapter;
    }

    public static PostRecyclerViewAdapter getMyTabsAdapter() {
        return myTabsAdapter;
    }

    public void setMyTabsAdapter(PostRecyclerViewAdapter myTabsAdapter) {
        this.myTabsAdapter = myTabsAdapter;
    }

    public static FriendRecyclerViewAdapter getFriendsRecyclerViewAdapter() {
        return friendsAdapter;
    }

    public void setFriendsAdapter(FriendRecyclerViewAdapter friendsAdapter) {
        this.friendsAdapter = friendsAdapter;
    }

    public static CommentsRecyclerViewAdapter getCommentsRecyclerViewAdapter(){
        return commentsRecyclerViewAdapter;
    }

    public void setCommentsRecyclerViewAdapter(CommentsRecyclerViewAdapter commentsRecyclerViewAdapter) {
        this.commentsRecyclerViewAdapter = commentsRecyclerViewAdapter;
    }

    public void setFromAnotherActivity(boolean fromAnotherActivity) {
        this.fromAnotherActivity = fromAnotherActivity;
    }

    public static boolean getFromAnotherActivity() {
        return fromAnotherActivity;
    }

    public void setCurrentPost(Post post) {
        this.currentPost = post;
    }

    public Post getCurrentPost() {
        return this.getCurrentPost();
    }

    private void initializeAdapters() {
        System.out.println("FireBaseApplication. Initializing Adapters");
        List<Friend> friends = new ArrayList<Friend>();
        List<Post> privatePosts = new ArrayList<Post>();
        List<Post> publicPosts = new ArrayList<Post>();
        List<Post> myTabsPosts = new ArrayList<Post>();
        System.out.println("FriendRecyclerViewAdapter2: Setting freinds adapter header");
        setFriendsAdapter(new FriendRecyclerViewAdapter(friends));
        setPublicAdapter(new PostRecyclerViewAdapter(publicPosts, this, false, "public"));
        setPrivateAdapter(new PostRecyclerViewAdapter(privatePosts, this, false, "private"));
        setMyTabsAdapter(new PostRecyclerViewAdapter(myTabsPosts, this, false, "mytabs"));
    }

    /**
     * Initialize Fresco.
     */
    public void configFresco() {
        Supplier<File> diskSupplier = new Supplier<File>() {
            @Override
            public File get() {
                return getApplicationContext().getCacheDir();
            }
        };

        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder()
                .setBaseDirectoryName("images")
                .setBaseDirectoryPathSupplier(diskSupplier)
                .build();

        ImagePipelineConfig frescoConfig = ImagePipelineConfig.newBuilder(this)
                .setMainDiskCacheConfig(diskCacheConfig)
                .build();

        Fresco.initialize(this, frescoConfig);
    }

}

class GcmRegistrationAsyncTask extends AsyncTask<Void, Void, String> {
    private static Registration regService = null;
    private GoogleCloudMessaging gcm;
    private Context context;

    // TODO: change to your own sender ID to Google Developers Console project number, as per instructions above
    private static final String SENDER_ID = "213033849274";

    public GcmRegistrationAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        if (regService == null) {
//            Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
//                    new AndroidJsonFactory(), null).setRootUrl("https://tabs-1124.appspot.com/_ah/api/");
//            // end of optional local run code
//
//            regService = builder.build();

            Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // Need setRootUrl and setGoogleClientRequestInitializer only for local testing,
                    // otherwise they can be skipped
                    .setRootUrl("http://10.0.2.2:8080/_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                                throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });

            regService = builder.build();
        }

        String msg = "";
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            String regId = gcm.register(SENDER_ID);
            msg = "Device registered, registration ID=" + regId;

            // You should send the registration ID to your server over HTTP,
            // so it can use GCM/HTTP or CCS to send messages to your app.
            // The request to your server should be authenticated if your app
            // is using accounts.
            regService.register(regId).execute();

        } catch (IOException ex) {
            ex.printStackTrace();
            msg = "Error: " + ex.getMessage();
        }
        return msg;
    }

    @Override
    protected void onPostExecute(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        Logger.getLogger("REGISTRATION").log(Level.INFO, msg);
    }
}