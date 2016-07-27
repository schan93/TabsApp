package com.tabs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tabs.MyPostNotificationReceiver;
import com.schan.tabs.R;

/**
 * Example shell activity which simply broadcasts to our receiver and exits.
 */
public class MyStubBroadcastActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent();
        i.setAction("com.test.tabs.tabs.SHOW_NOTIFICATION");
        i.putExtra(MyPostNotificationReceiver.CONTENT_KEY, getString(R.string.title));
        sendBroadcast(i);
        finish();
    }
}
