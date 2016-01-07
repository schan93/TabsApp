package com.test.tabs.tabs.com.tabs.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;

/**
 * Created by schan93 on 10/13/15.
 */
public class login extends Activity {

    private static final String TAG = "Result: ";
    //Widgets
    private LoginButton loginButton;

    //Manage callbacks in app
    private CallbackManager callbackManager;

    //Local Database for storing friends
    private FriendsDataSource datasource;

    //Local Database for storing posts
    private PostsDataSource postsDataSource;

    //Google Location Services API
    GoogleApiClient googleApiClient;
    LocationServices lastLocation;
    Location location;
    LocationManager locationManager;

    //Boolean to tell us whether or not we are actually logged in already if we are then we show the loading screen
    Boolean loggedIn = false;

    //Initialize location service
    LocationService locationService;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        //Initialize Callback Manager
        callbackManager = CallbackManager.Factory.create();
        //Configure Fresco so that image loads quickly
        configFresco();
        //Remove DB first this is because we have to change the schema
        //deletePostsDatabase();
        //Initialize DB
        startFriendsDatabase();

        //Set up location services
        LocationService.getLocationManager(this);

        setContentView(R.layout.activity_login);
        loginButton = (LoginButton)findViewById(R.id.login_button);

        loginButton.setReadPermissions(Arrays.asList("user_friends", "public_profile"));


        if(isLoggedIn() || (!isLoggedIn() && trackAccessToken())){
            //Check if user is already logged in
            //Go to Activity
            //Not sure if it is efficient to be grabbing data every single time we open the app. However, if we have new friends,
            //we should be able to see them so I will for now, and until we find a more efficient implementation, this will be as is.
            //What we need to do here though is to
            loggedIn = true;
            setContentView(R.layout.loading_panel);
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            System.out.println("Now");
            getFacebookData(accessToken);
        }
        else {
            System.out.println("Later");
            //I think we want to remove the friends db
            //This is just because ok lets think about this way: the user somehow logs out of our app
            //And then signs into another user
            //If this happens, we have to make sure that the friends db doesn't match up with the previous one. dont think we
            //want to remove the posts db because the posts db is supposed to just keep getting appended to,
            //Maybe after some time we will delete the posts db (certain posts after  X time)
            //TODO: Set up some cache so we can lazily load the friends from cache. Right now we simply purge out the db and recreate it.
            //Since this is an asynchronous process, this access token may tell us that we aren't logged in
            //if we aren't logged in, we have to check via the access token tracker
            //Set layout of activity
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
                    //Login successful. Now instead of setting text, we want to pass data into the next Activity (namely the news feed activity)
                    //So that the user can view their friends and so forth after they login.
                    //We use an asynchronous task so that as we are logging in, we can grab data from the login and put it into news feed.
                    //We have to set the loading view here also because we have the batch request that is in progress
                    //even if we are or arent in the db. I probably should make this more efficient but can't really think for right now.
                    getFacebookData(loginResult.getAccessToken());
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

    //Check if user is logged in already. If they are, then return true, else false.
    public boolean isLoggedIn(){
        if(AccessToken.getCurrentAccessToken() != null) {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            return accessToken != null;
        }
        else {
            return false;
        }
    }

    //Tracking Access Tokens to confirm whether or not we are logged in.
    public boolean trackAccessToken(){
        if(AccessToken.getCurrentAccessToken() != null) {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(
                        AccessToken oldAccessToken,
                        AccessToken currentAccessToken) {
                    // Set the access token using
                    // currentAccessToken when it's loaded or set.
                    //accessToken = currentAccessToken;
                }
            };
            accessToken = AccessToken.getCurrentAccessToken();
            // If the access token is available already assign it.
            return accessToken != null;
        }
        else {
            return false;
        }
    }

    public void getFacebookData(final AccessToken accessToken){
        //newMeRequest = My own data
        //myFriendsRequest = my mutual friends who have the app downloaded
        //Basically make 2 requests to one's Facebook info and return the names, links, id, and picture of the individual
        GraphRequestBatch batch = new GraphRequestBatch(
                GraphRequest.newMeRequest(
                        AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject jsonObject,
                                    GraphResponse response) {
                                System.out.println("Response in me: " + response.toString());
                                // Application code for user
                                // TODO: Set up Google Cloud SQL so that we can store the user information into the global DB
                                // as long as that id doesn't exist already.
                            }
                        }),
                GraphRequest.newMyFriendsRequest(
                        AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONArrayCallback() {
                            @Override
                            public void onCompleted(JSONArray jsonArray,
                                                    GraphResponse response) {
                                // Application code for users friends
                                // Insert into our local DB
                                System.out.println("Response: " + response);
                                System.out.println("JSON ARRAY: " + jsonArray);
                                String token = AccessToken.getCurrentAccessToken().getUserId();

                                try {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject row = jsonArray.getJSONObject(i);
                                        System.out.println("Friend: " + row);
                                        datasource.createFriend(row.getString("name"), row.getString("id"), token);
                                        System.out.println("Friend Id: " + datasource.getFriend(row.getString("id")).getId());
                                        System.out.println("Friend Name: " + datasource.getFriend(row.getString("id")).getName());
                                    }
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                System.out.println("JSON: " + jsonArray);
                            }
                        }
                )
        );
        batch.addCallback(new GraphRequestBatch.Callback() {
            @Override
            public void onBatchCompleted(GraphRequestBatch graphRequests) {
                // Application code for when the batch finishes
                Log.d(TAG, graphRequests.toString());

            }
        });
        batch.executeAsync();

        //After all batches execute, then we go into the main activity.
        batch.addCallback(new GraphRequestBatch.Callback() {
            @Override
            public void onBatchCompleted(GraphRequestBatch graphRequests) {
                if (loggedIn) {
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                }
                Bundle parameters = new Bundle();
                //parameters.putString("url", );
                parameters.putString("id", AccessToken.getCurrentAccessToken().getUserId());

                Intent intent = new Intent(login.this, news_feed.class);
                if (intent != null) {
                    //ProfilePictureView profilePictureView =  new  ProfilePictureView(getApplicationContext());
                    intent.putExtras(parameters);
                    System.out.println("Passing Id: " + AccessToken.getCurrentAccessToken().getUserId());
                    startActivity(intent);
                }
            }
        });
    }
    //Tapping the login button starts off a new Activity, which returns a result.
    //To receive and handle the result, override the onActivityResult function.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

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

    private void startFriendsDatabase(){
        datasource = new FriendsDataSource(this);
        datasource.open();
    }

    private void deletePostsDatabase(){
        this.deleteDatabase("databaseManager.db");
    }

}
