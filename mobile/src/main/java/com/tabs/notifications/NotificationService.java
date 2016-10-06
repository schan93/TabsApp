package com.tabs.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.schan.tabs.R;
import com.tabs.activity.CommentsActivity;
import com.tabs.activity.FireBaseApplication;
import com.tabs.database.databaseQuery.DatabaseQuery;
import com.tabs.utils.AndroidUtils;

/**
 * Created by schan on 8/11/16.
 */
public class NotificationService extends FirebaseMessagingService {

    private static final String TAG = "NotificationService";
    private FireBaseApplication application;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from DatabaseReference Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The DatabaseReference console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getData().get("postId"));

        if(remoteMessage.getData().size() > 0) {
            Log.d(TAG, "From: " + remoteMessage.getData().size());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        //Calling method to generate notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        showNotification(this, remoteMessage);
//        sendNotification(this, R.mipmap.ic_launcher, remoteMessage.getNotification().getBody());
    }

    public void showNotification(Context context, RemoteMessage remoteMessage){
        application = ((FireBaseApplication) getApplication());
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(remoteMessage.getNotification().getBody())
                .setTicker(remoteMessage.getFrom() + " has responded!")
                .setLargeIcon(AndroidUtils.getBitmapFromURL(remoteMessage.getNotification().getIcon()))
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()))
                .setSmallIcon(R.drawable.ic_tabs_logo_transparent_notification);

        Intent resultIntent = new Intent(context, CommentsActivity.class);
        if(remoteMessage.getData().containsKey("postId") && remoteMessage.getData().containsKey("userId")) {
            String postId = remoteMessage.getData().get("postId");
            String userId = remoteMessage.getData().get("userId");
            if(userId.equals(application.getUserId())) {
                //Can't send the comment to yourself, but you can to everyone else that is subscribed to the topic
                return;
            }
            setupPostDetails(notificationBuilder, resultIntent, postId, userId, context);
        }
    }

    private void setupPostDetails(NotificationCompat.Builder notificationBuilder, Intent resultIntent, String postId, String userId, Context context) {
        DatabaseQuery databaseQuery = new DatabaseQuery();
        databaseQuery.getPost(notificationBuilder, resultIntent, postId, userId, context);
    }
}
