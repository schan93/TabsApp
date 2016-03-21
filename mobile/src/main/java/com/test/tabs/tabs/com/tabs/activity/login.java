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
import com.firebase.client.ChildEventListener;
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
import com.test.tabs.tabs.com.tabs.database.Database.DatabaseQuery;
import com.test.tabs.tabs.com.tabs.database.comments.Comment;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsDataSource;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
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
    //Variable for current user facebook user id
    private String currentUserId;
    //Boolean to tell us whether or not we are actually logged in already if we are then we show the loading screen
    Boolean loggedIn = false;
    Handler handler;
    LocationService locationService;
    View progressOverlay;
    FireBaseApplication application;
    String name;
    DatabaseQuery databaseQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize Facebook SDK & Callback Manager
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        //Remove DB first this is because we have to change the schema
//        deleteDatabase();
        //Initialize DB
//        startDatabases();
        //Initialize Handler
        databaseQuery = new DatabaseQuery(this);
        handler = new Handler();
        application =  (FireBaseApplication) getApplication();
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
            Profile profile = Profile.getCurrentProfile();
            name = profile.getName();

            loggedIn = true;
            setContentView(R.layout.loading_panel);
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            currentUserId = accessToken.getUserId();
            getUserInfo(currentUserId);
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
                                System.out.println("Current profile get name: " + currentProfile.getName());
                                name = currentProfile.getName();
                                profileTracker.stopTracking();
                            }
                        };
                        profileTracker.startTracking();
                    } else {
                        Profile profile = Profile.getCurrentProfile();
                        System.out.println("Current profile get name2: " + profile.getName());
                        name = profile.getName();
                    }
                    AccessToken accessToken = loginResult.getAccessToken();
                    currentUserId = accessToken.getUserId();
                    getUserInfo(currentUserId);
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
        application.setName(name);
        application.setUserId(userId);
        application.getPublicAdapter().setUserId(userId);
        application.getPrivateAdapter().setUserId(userId);
        application.getMyTabsAdapter().setUserId(userId);
        getUserFromFacebook(userId);
    }

    /**
     *  This method is designed to get the user since he/she doesn't exist.
     *  We have to perform a Facebook batch request & insert their data into the local database.
     * @param userId
     * @return
     */
    private void getUserFromFacebook(final String userId) {
        final User[] user = new User[1];
//        User user = new User(id, userId, name);
        GraphRequest meRequest = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject jsonObject,
                            GraphResponse response) {
                        try {
                            String userId = jsonObject.getString("id");
                            String name = jsonObject.getString("name");
                            databaseQuery.saveUserToFirebase(userId, name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            System.out.println("Finally user.");
                            //This is after getting the user has completed
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println("Running user");
//                                    databaseQuery.getFriends(userId);
                                    getFriends(userId);
                                }
                            });
                        }
                    }
                });
        meRequest.executeAsync();
        return;
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
                                System.out.println("login: Length: " + jsonArray.length());
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String friendId = jsonObject.getString("id");
                                String name = jsonObject.getString("name");
                                System.out.println("login: Name: " + name);
                                Friend friend = new Friend("", friendId, name, currentUserId, "false");
                                List<Friend> friends = application.getFriendsRecyclerViewAdapter().getFriends();
                                if (application.getFriendsRecyclerViewAdapter().containsId(friends, friend.getUserId()) == null) {
                                    application.getFriendsRecyclerViewAdapter().getFriends().add(friend);
                                    System.out.println("login2: Saving " + name + " to firebase.");
                                    databaseQuery.saveFriendToFirebase(friend);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            System.out.println("Finally.");
                            //This is after getting all the friends has completed
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println("Done.");
                                    if (application.getFromAnotherActivity() == false) {
                                        setupNextActivity();
                                    }
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
        parameters.putString("id", currentUserId);
        parameters.putString("name", name);
        Intent intent = new Intent(login.this, news_feed.class);
        if (intent != null) {
            intent.putExtras(parameters);
            System.out.println("news_feed: Starting intent all over again");
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
        if (callbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

    private void deleteDatabase() {
        this.deleteDatabase("databaseManager.db");
    }

    public void getFriends(final String userId) {
        Firebase friendsRef = firebaseRef.child("Friends/" + userId);
        friendsRef.keepSynced(true);
        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot friendSnapShot : dataSnapshot.getChildren()) {
                    Friend friend = friendSnapShot.getValue(Friend.class);
                    String userId = friend.getUserId();
                    List<Friend> friends = application.getFriendsRecyclerViewAdapter().getFriends();
                    if (application.getFriendsRecyclerViewAdapter().containsId(friends, userId) == null) {
                        System.out.println("login2: Adding Friend " + friend.getName() + " to array");
                        application.getFriendsRecyclerViewAdapter().getFriends().add(friend);
                    }
                }
                getFriendsFromFacebook(userId);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        friendsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Friend newFriend = dataSnapshot.getValue(Friend.class);
                List<Friend> friends = application.getFriendsRecyclerViewAdapter().getFriends();
                if (application.getFriendsRecyclerViewAdapter().containsId(friends, newFriend.getUserId()) == null) {
                    application.getFriendsRecyclerViewAdapter().getFriends().add(newFriend);
                    application.getFriendsRecyclerViewAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Friend changedFriend = dataSnapshot.getValue(Friend.class);
                int length = application.getFriendsRecyclerViewAdapter().getItemCount();
                for (int i = 0; i < length; i++) {
                    if (application.getFriendsRecyclerViewAdapter().getFriends().get(i).getId().equals(changedFriend.getId())) {
                        application.getFriendsRecyclerViewAdapter().getFriends().set(i, changedFriend);
                    }
                }
                application.getFriendsRecyclerViewAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Friend removedFriend = dataSnapshot.getValue(Friend.class);
                int length = application.getFriendsRecyclerViewAdapter().getItemCount();
                for (int i = 0; i < length; i++) {
                    if (application.getFriendsRecyclerViewAdapter().getFriends().get(i).getId().equals(removedFriend.getId())) {
                        application.getFriendsRecyclerViewAdapter().getFriends().remove(i);
                    }
                }
                application.getFriendsRecyclerViewAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //Not sure if used
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

}