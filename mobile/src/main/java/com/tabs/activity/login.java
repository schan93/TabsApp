package com.tabs.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by schan93 on 10/13/15.
 */
public class login extends Activity implements Serializable, LocationListener {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private String currentUserId;
    private Boolean loggedIn = false;
    private FireBaseApplication application;
    private String name;
    private String userId;
    private DatabaseQuery databaseQuery;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    private LocationManager locationManager;
    ProviderLocationTracker providerLocationTracker;
    Context context;
    LocationService locationService;


    private final String TAG = "Login.java";

    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivity();
        checkIsLoggedIn();
        //Setup database

    }

    @Override
    protected void onStart(){
        super.onStart();
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
    private void getUserInfo(String userId, String name) {
        application.setName(name);
        application.setUserId(userId);
        application.getPublicAdapter().setUserId(userId);
        application.getFollowingPostAdapter().setUserId(userId);
        application.getMyTabsAdapter().setUserId(userId);
        databaseQuery = new DatabaseQuery(this);
        databaseQuery.getUserFromFacebook(name, currentUserId, loggedIn, login.this);

        //Subscribe to DatabaseReference notification test
        FirebaseMessaging.getInstance().subscribeToTopic("test");
        FirebaseInstanceId.getInstance().getToken();
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
        application =  (FireBaseApplication) getApplication();
        //Set up location services
        setContentView(R.layout.activity_login);
        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("user_friends", "public_profile"));
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //Set the application status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        }
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
            getUserInfo(currentUserId, name);
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
                                currentUserId = currentProfile.getId();
                                profileTracker.stopTracking();
                                getUserInfo(currentUserId, name);
                            }
                        };
                        profileTracker.startTracking();
                    } else {
                        Profile profile = Profile.getCurrentProfile();
                        name = profile.getFirstName();
                    }
                    AccessToken accessToken = loginResult.getAccessToken();
                    currentUserId = accessToken.getUserId();
                    getUserInfo(currentUserId, name);
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
    public void onLocationChanged(Location location) {
        if(providerLocationTracker.getLocation() == null) {
            providerLocationTracker.setLocation(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}