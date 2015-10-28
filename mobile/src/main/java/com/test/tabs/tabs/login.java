package com.test.tabs.tabs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.os.Parcelable;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookActivity;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by schan93 on 10/13/15.
 */
public class login extends Activity {

    private static final String TAG = "Result: ";
    //Widgets
    private TextView info;
    private LoginButton loginButton;

    //Manage callbacks in app
    private CallbackManager callbackManager;

    private LoginManager loginManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        //Initialize Callback Manager
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);
        info = (TextView)findViewById(R.id.info);
        loginButton = (LoginButton)findViewById(R.id.login_button);

        if(isLoggedIn() || (!isLoggedIn() && trackAccessToken())){
            //Check if user is already logged in
            //Go to Activity
            //Not sure if it is efficient to be grabbing data every single time we open the app. However, if we have new friends,
            //we should be able to see them so I will for now, and until we find a more efficient implementation, this will be as is.
            //What we need to do here though is to
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            getFacebookData(accessToken);
            System.out.println("Not logged in yet.");
        }
        else {
            System.out.println("Logging in.");
            //Since this is an asynchronous process, this access token may tell us that we aren't logged in
            //if we aren't logged in, we have to check via the access token tracker
            //Set layout of activity
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    //Login successful. Now instead of setting text, we want to pass data into the next Activity (namely the news feed activity)
                    //So that the user can view their friends and so forth after they login.
                    //info.setText("User ID: " + loginResult.getAccessToken().getUserId() + "\n" + "Auth Token: " + loginResult.getAccessToken().getToken());
                    //We use an asynchronous task so that as we are logging in, we can grab data from the login and put it into news feed.
                    getFacebookData(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    //Login is cancelled
                    info.setText("Login attempt canceled.");
                }

                @Override
                public void onError(FacebookException e) {
                    //Errors out
                    info.setText("Login attempt failed.");
                }
            });
        }

    }

    //Check if user is logged in already. If they are, then return true, else false.
    public boolean isLoggedIn(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        System.out.println("Current access token: " + accessToken.getToken());
        return accessToken != null;
    }

    //Tracking Access Tokens to confirm whether or not we are logged in.
    public boolean trackAccessToken(){
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

    public void getFacebookData(AccessToken accessToken){
        //newMeRequest = My own data
        //myFriendsRequest = my mutual friends who have the app downloaded
        //Basically make 2 requests to one's Facebook info and return the names, links, id, and picture of the individual
        JSONArray friends = new JSONArray();
        GraphRequestBatch batch = new GraphRequestBatch(
                GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject jsonObject,
                                    GraphResponse response) {
                                // Application code for user
                            }
                        }),
                GraphRequest.newMyFriendsRequest(
                        accessToken,
                        new GraphRequest.GraphJSONArrayCallback() {
                            @Override
                            public void onCompleted(
                                    JSONArray jsonArray,
                                    GraphResponse response) {
                                // Application code for users friends
                                System.out.println("Friends: " + jsonArray);
                            }
                        })
        );
        batch.addCallback(new GraphRequestBatch.Callback() {
            @Override
            public void onBatchCompleted(GraphRequestBatch graphRequests) {
                // Application code for when the batch finishes
                Log.d(TAG, graphRequests.toString());

            }
        });
        batch.executeAsync();
        Bundle parameters = new Bundle();
        parameters.putString("id", AccessToken.getCurrentAccessToken().getUserId());

        Intent intent = new Intent(login.this, news_feed.class);
        if(intent != null) {
            ProfilePictureView profilePictureView =  new  ProfilePictureView(getApplicationContext());
            intent.putExtras(parameters);
            startActivity(intent);
        }
    }
    //Tapping the login button starts off a new Activity, which returns a result.
    //To receive and handle the result, override the onActivityResult function.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
