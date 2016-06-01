package com.test.tabs.tabs.com.tabs.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.Database.DatabaseQuery;
import com.test.tabs.tabs.com.tabs.database.followers.Follower;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.users.User;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by schan93 on 10/13/15.
 */
public class login extends Activity implements Serializable{

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private String currentUserId;
    private Boolean loggedIn = false;
    private LocationService locationService;
    private FireBaseApplication application;
    private String name;
    private DatabaseQuery databaseQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivity();
        checkIsLoggedIn();
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
        databaseQuery.getUserFromFacebook(userId, loggedIn, login.this);
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


    private void setupActivity() {
        //Initialize Facebook SDK & Callback Manager
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        databaseQuery = new DatabaseQuery(this);
        application =  (FireBaseApplication) getApplication();
        //Set up location services
        LocationService.getLocationManager(this);
        setContentView(R.layout.activity_login);
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("user_friends", "public_profile"));
    }

    private void checkIsLoggedIn() {
        if(isLoggedIn() || (!isLoggedIn() && trackAccessToken())){
            //Check if user is already logged in
            Profile profile = Profile.getCurrentProfile();
            name = profile.getFirstName();
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
                                name = currentProfile.getFirstName();
                                profileTracker.stopTracking();
                            }
                        };
                        profileTracker.startTracking();
                    } else {
                        Profile profile = Profile.getCurrentProfile();
                        name = profile.getFirstName();
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
}