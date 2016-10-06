package com.tabs.notifications;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by schan on 8/15/16.
 */
public class NotificationInstanceService extends FirebaseInstanceIdService {

    private final String tokenPreferenceKey = "fcm_token";

    @Override
    public void onTokenRefresh() {

        //Getting registration token
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(tokenPreferenceKey, FirebaseInstanceId.getInstance().getToken()).apply();

    }
}
