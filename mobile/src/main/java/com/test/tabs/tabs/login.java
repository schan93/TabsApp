package com.test.tabs.tabs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookActivity;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

/**
 * Created by schan93 on 10/13/15.
 */
public class login extends Activity {

    //Widgets
    private TextView info;
    private LoginButton loginButton;

    //Manage callbacks in app
    private CallbackManager callbackManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        //Initialize Callback Manager
        callbackManager = CallbackManager.Factory.create();
        //Set layout of activity
        setContentView(R.layout.activity_login);
        info = (TextView)findViewById(R.id.info);
        loginButton = (LoginButton)findViewById(R.id.login_button);

        //Handle the results of the login
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //Login successful
                info.setText("User ID: " + loginResult.getAccessToken().getUserId() + "\n" + "Auth Token: " + loginResult.getAccessToken().getToken());
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

    //Tapping the login button starts off a new Activity, which returns a result.
    //To receive and handle the result, override the onActivityResult function.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
