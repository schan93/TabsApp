package com.test.tabs.tabs.com.tabs.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.comments.Comment;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsDataSource;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsListAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;
import com.test.tabs.tabs.com.tabs.database.users.User;
import com.test.tabs.tabs.com.tabs.database.users.UsersDataSource;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by schan93 on 10/13/15.
 */
public class login extends Activity implements Serializable{

    //Firebase reference
    private Firebase firebaseRef = new Firebase("https://tabsapp.firebaseio.com/");
    private LoginButton loginButton;

    //Manage callbacks in app
    private CallbackManager callbackManager;

    //Local Database for storing posts
    private PostsDataSource postsDataSource;
    private CommentsDataSource commentsDataSource;
    private FriendsDataSource friendsDataSource;
    private UsersDataSource usersDataSource;

    //Variable for current user facebook user id
    private String userId;

    //Boolean to tell us whether or not we are actually logged in already if we are then we show the loading screen
    Boolean loggedIn = false;

    Handler handler;

    //Initialize location service
    LocationService locationService;

    //Progress overlay
    View progressOverlay;

    //Application
    FireBaseApplication application;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize Facebook SDK & Callback Manager
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        //Configure Fresco so that image loads quickly
        configFresco();
        //Remove DB first this is because we have to change the schema
        deleteDatabase();
        //Initialize DB
        startDatabases();
        //Initialize Handler
        handler = new Handler();
        application =  (FireBaseApplication) getApplication();
        initializeAdapters();
        //Set up location services
        LocationService.getLocationManager(this);

        setContentView(R.layout.activity_login);
        //Set up progress overlay

        progressOverlay = findViewById(R.id.progress_overlay);

        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("user_friends", "public_profile"));

        System.out.println("Login: App has started");
        if(isLoggedIn() || (!isLoggedIn() && trackAccessToken())){
            //Check if user is already logged in
            loggedIn = true;
            setContentView(R.layout.loading_panel);
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            userId = accessToken.getUserId();
            getUserInfo(userId);
        }
        else {
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

                private ProfileTracker profileTracker;

                @Override
                public void onSuccess(LoginResult loginResult) {
                    loggedIn = true;
                    setContentView(R.layout.loading_panel);

                    if(Profile.getCurrentProfile() == null){
                        profileTracker = new ProfileTracker() {
                            @Override
                            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                                profileTracker.stopTracking();
                            }
                        };
                        profileTracker.startTracking();
                    }
                    AccessToken accessToken = loginResult.getAccessToken();
                    userId = accessToken.getUserId();
                    getUserInfo(userId);
                }

                @Override
                public void onCancel() {
                    //Login is cancelled
                }

                @Override
                public void onError(FacebookException e) {
                    //Errors out
                }
            });
        }

    }

    @Override
    protected void onStart(){
        super.onStart();
        LocationService.onStart();
    }

    /**
     * Check if user is logged in already. If they are, then return true, else false.
     * @return
     */
    public boolean isLoggedIn(){
        if(AccessToken.getCurrentAccessToken() != null) {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            return accessToken != null;
        }
        else {
            return false;
        }
    }

    /**
     * Tracks Access Tokens from Facebook to confirm whether or not we are logged in.
     * @return
     */
    public boolean trackAccessToken(){
        if(AccessToken.getCurrentAccessToken() != null) {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            // If the access token is available already assign it.
            return accessToken != null;
        }
        else {
            return false;
        }
    }

    /**
     * Query from our User database to see if the user has already logged into the app on this phone. If so,
     * we do not need to perform the newMeRequest request to grab Facebook data about them. On the contrary,
     * we will need to grab information about their friends every time they login (better way to do this?)
     * @param userId
     */
    private void getUserInfo(String userId) {
        User user = usersDataSource.getUser(userId);
        if(user == null) {
            getUserFromFacebook(userId);
        }
        else {
            getFriendsFromFirebase(user);
            getFriendsFromFacebook(userId);
        }
    }

    /**
     *  This method is designed to get the user since he/she doesn't exist.
     *  We have to perform a Facebook batch request & insert their data into the local database.
     * @param userId
     * @return
     */
    private void getUserFromFacebook(final String userId) {
        final User[] user = new User[1];
        GraphRequest meRequest = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject jsonObject,
                            GraphResponse response) {
                        try {
                            String id = generateUniqueId();
                            String userId = jsonObject.getString("id");
                            String name = jsonObject.getString("name");
                            user[0] = usersDataSource.createUser(id, userId, name);
                            saveUserToFirebase(user[0]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            System.out.println("Finally user.");
                            //This is after getting the user has completed
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println("Running user");
                                    getFriendsFromFirebase(user[0]);
                                    getFriendsFromFacebook(userId);
                                }
                            });
                        }
                    }
                });
        meRequest.executeAsync();
        return;
    }

    /**
     * This method generates a random unique UUID for each entry in our database.
     * @return
     */
    private String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    /**
     * This method is designed to get all the friends of a user from Firebase, and store into our local database.
     * @param user
     */
    private void getFriendsFromFirebase(User user) {
        firebaseRef.child("Friends/" + user.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot friendSnapShot : snapshot.getChildren()) {
                    Friend friend = friendSnapShot.getValue(Friend.class);
                    String id = friend.getId();
                    String name = friend.getName();
                    String userId = friend.getUserId();
                    String user = friend.getUser();
                    String isFriend = friend.getIsFriend();
                    //System.out.println("Newest Friend id: " + id + " Name: " + name + " User id: " + userId + " User: " + user + " isFriend: " + isFriend);
                    Friend newFriend = friendsDataSource.getFriend(userId);
//                    System.out.println("New Friend Name: " + newFriend.getName());
                    if(newFriend == null) {
                        newFriend = friendsDataSource.createFriend(id, name, userId, user, isFriend);
                        System.out.println("FriendsDataSource: Login123: " + "Friend " + userId + "is a friend of " + user);

                    }
                    List<Friend> friends = application.getFriendsAdapter().getFriends();
                    if(!application.getFriendsAdapter().containsId(friends, newFriend.getUserId())){
                        System.out.println("Login: Adding friend: " + newFriend.getUserId() + " because it is not in friends from Firebase");
                        System.out.println("Login: Firebase count: " + application.getFriendsAdapter().getCount());
                        application.getFriendsAdapter().getFriends().add(newFriend);
                    }
                    getPosts(userId);
                }
                //In case that the data set changes when we are on Resume or something.. I guess we have to check if the dataset changes
//                application.getFriendsAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("Failed getting friends: " + firebaseError.getMessage());
            }
        });
    }

    /**
     * This method is designed to save a freind to Firebase database.
     * User = the user id of the person logged in
     * User_id = the user id of the friend
     * @param friend
     */
    private void saveFriendToFirebase(Friend friend) {
        firebaseRef.child("Friends/" + friend.getUser() + "/" + friend.getUserId()).setValue(friend);
    }

    /**
     * This method is designed to save a Post to Firebase database.
     * @param post
     */
    private void savePostToFirebase(Post post) {
        System.out.println("Posting!");
        firebaseRef.child("Posts/" + post.getPosterUserId()).push().setValue(post);
    }

    /**
     * This method is designed to save a Comment to Firebase database.
     * @param comment
     */
    private void saveCommentToFirebase(Comment comment) {
        firebaseRef.child("Comments").push().setValue(comment);
    }

    private void saveUserToFirebase(User user) {
        firebaseRef.child("Users/" + user.getUserId()).setValue(user);
    }

    /**
     * This method is designed to get all the friends of the current user based on their userId from Facebook.
     * @param userId
     */
    public void getFriendsFromFacebook(final String userId){
        GraphRequest friendsRequest = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(),
            new GraphRequest.GraphJSONArrayCallback() {
                @Override
                public void onCompleted(final JSONArray jsonArray, GraphResponse response) {
                    try {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String id = generateUniqueId();
                            String friendId = jsonObject.getString("id");
                            String name = jsonObject.getString("name");
                            Friend friend = friendsDataSource.getFriend(friendId);
                            if(friend == null) {
                                System.out.println("FriendsDataSource: LoginFB: " + "Friend " + friendId + "is a friend of " + userId);
                                friend = friendsDataSource.createFriend(id, name, friendId, userId, "0");
                                //Insert into Cloud database
                                System.out.println("Login: Setting up friend in Facebook");
                                saveFriendToFirebase(friend);
                            }
                            List<Friend> friends = application.getFriendsAdapter().getFriends();
                            if(!application.getFriendsAdapter().containsId(friends, friend.getUserId())) {
                                System.out.println("Login: Adding friend: " + friend.getUserId() + " because it is not in friends from Facebook");
                                System.out.println("Login: Facebook count: " + application.getFriendsAdapter().getCount());
                                application.getFriendsAdapter().getFriends().add(friend);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        System.out.println("Finally.");
                        //This is after getting all the friends has completed
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("Running.");
                                getPosts(userId);
                            }
                        });
                    }
                }
            });
        friendsRequest.executeAsync();
    }


    /**
     * This method is designed to move the user to the next activity after getting all the details from Firebase and storing information
     * into the local database is performed. This is because doing so will make our application function a lot faster
     * than getting everything on the fly.
     */
    private void setupNextActivity() {
        if (loggedIn) {
//            AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
            System.out.println("Login: Login done.");
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            //findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
        Bundle parameters = new Bundle();
        parameters.putString("id", userId);
        Intent intent = new Intent(login.this, news_feed.class);
        if (intent != null) {
            intent.putExtras(parameters);
            startActivity(intent);
        }
    }

    /**
     * Tapping the login button starts off a new Activity, which returns a result.
     * To receive and handle the result, override the onActivityResult function.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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


    private void deleteDatabase(){
        this.deleteDatabase("databaseManager.db");
    }

    /**
     * This method is designed to get all the Posts from Firebase, store them in local.
     * If a Post does not exist in local database already so we need to also create the post in local db.
     * @param userId
     */
    private void getPosts(final String userId) {
        System.out.println("Getting posts from Firebase");
        //TODO: Query longitude and latitude by 15 mile distance
        firebaseRef.child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println("Snapshot Count: " + snapshot.getChildrenCount());
                if(snapshot.getChildrenCount() == 0) {
                    setupNextActivity();
                }
                for (DataSnapshot postSnapShot : snapshot.getChildren()) {
                    System.out.println("POsts children count: " + postSnapShot.getChildrenCount());
                    String postCreator = postSnapShot.getKey();
                    int i = 0;
                    for(DataSnapshot userSnapShot : postSnapShot.getChildren()){
                        Post post = userSnapShot.getValue(Post.class);
                        String id = post.getId();
                        String name = post.getName();
                        String status = post.getStatus();
                        String posterUserId = post.getPosterUserId();
                        String timeStamp = post.getTimeStamp();
                        String privacy = post.getPrivacy();
                        Double latitude = post.getLatitude();
                        Double longitude = post.getLongitude();
                        Post newPost = postsDataSource.getPost(id);
                        //TODO: This is basically when we update the Post in Firebase we need to be able to handle this change
//                        if(newPost != null && !newPost.getPrivacy().equals(post.getPrivacy())) {
//                            //update the post in the db, then you may need to swap whatever is out from the db to / from the supposed adapters
//                            postsDataSource.updatePost(newPost);
//                            if(newPost.getPrivacy().equals("1")) {
//                                Friend friend = friendsDataSource.getFriend(posterUserId);
//                                if(friend != null && friend.getIsFriend().equals("1")) {
//                                    application.getPrivateAdapter().add(post);
//                                }
//                                application.getPublicAdapter().remove(post, getApplicationContext());
//                            } else {
//                                System.out.println("The new post's privacy = 0. Therefore, it goes into public, out of private");
//                                application.getPublicAdapter().add(post);
//                                application.getPrivateAdapter().remove(post, getApplicationContext());
//                            }
//                            //also need to refresh my tabs
//                            application.getMyTabsAdapter().notifyDataSetChanged();
//                        }
                        //System.out.println("New Post Id: " + id);
                        if (newPost == null) {
                            postsDataSource.createPostFromFireBase(id, posterUserId, status, timeStamp, name, privacy, latitude, longitude);
                        }
                        else {
                            System.out.println("Login: Already have post id: " + newPost.getId());
                        }
                        if(post.getPrivacy().equals("0")) {
                            application.getPublicAdapter().add(post);
                        } else {
                            Friend friend = friendsDataSource.getFriend(posterUserId);
                            if(friend != null && friend.getIsFriend().equals("1") && !friend.getUserId().equals(userId)){
                                application.getPrivateAdapter().add(post);
                            }
                        }
                        if(post.getPosterUserId().equals(userId)) {
                            application.getMyTabsAdapter().add(post);
                        }
                        System.out.println("Login: Done getting Posts, now getting comments");
                        i++;
                        getComments(id);
                        if(i == postSnapShot.getChildrenCount() && application.getFromAnotherActivity() == false) {
                            System.out.println("SETTING UP NEXT ACTIVITY");
                            setupNextActivity();
                        }
                    }
                }
                //In case that the data set changes when we are on Resume or something.. I guess we have to check if the dataset changes
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }


    /**
     * This method is designed to get all the Comments from Firebase, store them in local.
     * If a Comment does not exist in local database already so we need to also create the comment in local db.
     * @param postId
     */
    private void getComments(String postId) {
        //TODO: Query longitude and latitude by 15 mile distance
        firebaseRef.child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot commentSnapShot : snapshot.getChildren()) {
                    System.out.println("Comment snapshot: " + commentSnapShot.getKey());
                    System.out.println("Value of comment: " + commentSnapShot.getValue());
                    System.out.println("String: comment: " + commentSnapShot.getValue().toString());
                    Comment comment = commentSnapShot.getValue(Comment.class);
                    String id = comment.getId();
                    String postId = comment.getPostId();
                    String commenter = comment.getCommenter();
                    String commentText = comment.getComment();
                    String commenterUserId = comment.getCommenterUserId();
                    String timeStamp = comment.getTimeStamp();
                    Comment newComment = commentsDataSource.getComment(id);
                    if (newComment == null) {
                        commentsDataSource.createCommentFromFirebase(id, postId, commenter, commentText, commenterUserId, timeStamp);
                    }
                    System.out.println("Login: Done getting comments.");
//                    application.getCommentsRecyclerViewAdapter().getComments().add(comment);
                }
                System.out.println("Login: Done getting Everything.");
//                application.getCommentsRecyclerViewAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void startDatabases(){
        usersDataSource = new UsersDataSource(this);
        usersDataSource.open();
        postsDataSource = new PostsDataSource(this);
        postsDataSource.open();
        commentsDataSource = new CommentsDataSource(this);
        commentsDataSource.open();
        friendsDataSource = new FriendsDataSource(this);
        friendsDataSource.open();
    }

    private void initializeAdapters() {
        List<Friend> friends = new ArrayList<Friend>();
        List<Post> privatePosts = new ArrayList<Post>();
        List<Post> publicPosts = new ArrayList<Post>();
        List<Post> myTabsPosts = new ArrayList<Post>();

        application.setFriendsAdapter(new FriendsListAdapter(this, friends));
        application.setPublicAdapter(new PostRecyclerViewAdapter(publicPosts, this, false));
        application.setPrivateAdapter(new PostRecyclerViewAdapter(privatePosts, this, false));
        application.setMyTabsAdapter(new PostRecyclerViewAdapter(myTabsPosts, this, false));
    }

}