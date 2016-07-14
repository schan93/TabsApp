package com.test.tabs.tabs.com.tabs.activity;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

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
import com.test.tabs.tabs.com.tabs.database.comments.Comment;

import com.test.tabs.tabs.com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.followers.Follower;
import com.test.tabs.tabs.com.tabs.database.followers.FollowerRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.users.User;

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
    private static PostRecyclerViewAdapter publicAdapter;
    private static PostRecyclerViewAdapter followingPostAdapter;
    private static PostRecyclerViewAdapter myTabsAdapter;
    private static PostRecyclerViewAdapter userAdapter;
    private static CommentsRecyclerViewAdapter commentsRecyclerViewAdapter;
    private static FollowerRecyclerViewAdapter followerRecyclerViewAdapter;
    private static FollowerRecyclerViewAdapter followingRecyclerViewAdapter;
    private static List<String> userInfoAdapter;
    private static User viewingUser;

    private static PostRecyclerViewAdapter followerPostAdapter;
    private static Post currentPost;
    public static boolean testIsDone = true;

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

    public static PostRecyclerViewAdapter getFollowerPostAdapter() {
        return followerPostAdapter;
    }

    public static PostRecyclerViewAdapter getMyTabsAdapter() {
        return myTabsAdapter;
    }

    public void setMyTabsAdapter(PostRecyclerViewAdapter myTabsAdapter) {
        this.myTabsAdapter = myTabsAdapter;
    }
    public static CommentsRecyclerViewAdapter getCommentsRecyclerViewAdapter(){
        return commentsRecyclerViewAdapter;
    }

    public void setFollowerRecyclerViewAdapter(FollowerRecyclerViewAdapter followerRecyclerViewAdapter) {
        this.followerRecyclerViewAdapter = followerRecyclerViewAdapter;
    }

    public static FollowerRecyclerViewAdapter getFollowerRecyclerViewAdapter(){
        return followerRecyclerViewAdapter;
    }

    public static FollowerRecyclerViewAdapter getFollowingRecyclerViewAdapter(){
        return followingRecyclerViewAdapter;
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

    public void setFollowingRecyclerViewAdapter(FollowerRecyclerViewAdapter followingRecyclerViewAdapter) {
        this.followingRecyclerViewAdapter = followingRecyclerViewAdapter;
    }

    public void setFollowingPostAdapter(PostRecyclerViewAdapter followingPostAdapter) {
        this.followingPostAdapter = followingPostAdapter;
    }

    public void setUserInfoAdapter(List<String> userInfoAdapter) {
        this.userInfoAdapter = userInfoAdapter;
    }

    public List<String> getUserInfoAdapter() {
        return this.userInfoAdapter;
    }

    public PostRecyclerViewAdapter getFollowingPostAdapter() {
        return this.followingPostAdapter;
    }

    public PostRecyclerViewAdapter getUserAdapter() { return this.userAdapter; }

    public void setUserAdapter(PostRecyclerViewAdapter userAdapter) { this.userAdapter = userAdapter; }

    public User getViewingUser() { return this.viewingUser; }

    public void setViewingUser(User viewingUser) { this.viewingUser = viewingUser; }

    private void initializeAdapters() {
        List<User> followers = new ArrayList<User>();
        List<User> following = new ArrayList<User>();
        List<Post> publicPosts = new ArrayList<Post>();
        List<Post> myTabsPosts = new ArrayList<Post>();
        List<Post> followingPosts = new ArrayList<>();
        List<Post> userPosts = new ArrayList<>();
        List<String> userInfoAdapter = new ArrayList<>();
        User user = new User();
        setCommentsRecyclerViewAdapter(new CommentsRecyclerViewAdapter(new CommentsHeader(), new ArrayList<Comment>()));
        setPublicAdapter(new PostRecyclerViewAdapter(publicPosts, this, false, TabEnum.Public));
        setMyTabsAdapter(new PostRecyclerViewAdapter(myTabsPosts, this, false, TabEnum.MyTab));
        setFollowingPostAdapter(new PostRecyclerViewAdapter(followingPosts, this, false, TabEnum.Following));
        setFollowerRecyclerViewAdapter(new FollowerRecyclerViewAdapter(followers));
        setFollowingRecyclerViewAdapter(new FollowerRecyclerViewAdapter(following));
        setUserAdapter(new PostRecyclerViewAdapter(userPosts, this, false, null));
        setUserInfoAdapter(userInfoAdapter);
        setViewingUser(user);
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