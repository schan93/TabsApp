package com.tabs.activity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tabs.database.Database.DatabaseQuery;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by schan on 8/15/16.
 */
public class NotificationInstanceService extends FirebaseInstanceIdService {
    private static final String TAG = "NotificationInstance";
    private DatabaseQuery databaseQuery;
    private FireBaseApplication application;

    private final String tokenPreferenceKey = "fcm_token";

    public final static String infoTopicName = "test";

    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String deviceId = FirebaseInstanceId.getInstance().getToken();
        //Send the registration token to DatabaseReference database stored in User
        saveDeviceId(deviceId);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(tokenPreferenceKey, FirebaseInstanceId.getInstance().getToken()).apply();
//        FirebaseMessaging.getInstance().subscribeToTopic(infoTopicName);

    }

    private void saveDeviceId(String deviceId) {
        application =  (FireBaseApplication) getApplication();
        //Only the currently logged in user that is running the code will have this token Id stored in it
        application.setDeviceId(deviceId);
    }
}
