package com.tabs.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Icon;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;
import com.tabs.database.comments.Comment;
import com.tabs.database.posts.Post;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by schan on 8/11/16.
 */
public class NotificationService extends FirebaseMessagingService {

    private static final String TAG = "NotificationService";
    private NotificationManager notificationManager;
    public static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    public static final String SERVER_KEY = "AIzaSyBjemu0n27nWyhgLzojS7ICSDOiTLawwEs";   // You FCM AUTH key
    OkHttpClient mClient = new OkHttpClient();

    FireBaseApplication application;

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
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        showNotification(this, remoteMessage);
//        sendNotification(this, R.mipmap.ic_launcher, remoteMessage.getNotification().getBody());
    }

    public void showNotification(Context context, RemoteMessage remoteMessage){
        application = ((FireBaseApplication) getApplication());
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(remoteMessage.getNotification().getBody())
                .setTicker(remoteMessage.getFrom() + " has responded!")
                .setLargeIcon(getBitmapFromURL(remoteMessage.getNotification().getIcon()))
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()))
                .setSmallIcon(R.drawable.ic_tabs_logo_transparent_notification);

        Intent resultIntent = new Intent(context, Comments.class);
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

    public Bitmap getBitmapFromURL(String userId) {
        try {
            URL imgUrl = new URL("https://graph.facebook.com/" + userId + "/picture?type=large");
            InputStream in = (InputStream) imgUrl.getContent();
            Bitmap  bitmap = BitmapFactory.decodeStream(in);
            Bitmap output;
            Rect srcRect;
            if (bitmap.getWidth() > bitmap.getHeight()) {
                output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                srcRect = new Rect((bitmap.getWidth()-bitmap.getHeight())/2, 0, bitmap.getWidth()+(bitmap.getWidth()-bitmap.getHeight())/2, bitmap.getHeight());
            } else {
                output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
                srcRect = new Rect(0, (bitmap.getHeight()-bitmap.getWidth())/2, bitmap.getWidth(), bitmap.getHeight()+(bitmap.getHeight()-bitmap.getWidth())/2);
            }

            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            float r;

            if (bitmap.getWidth() > bitmap.getHeight()) {
                r = bitmap.getHeight() / 2;
            } else {
                r = bitmap.getWidth() / 2;
            }

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(r, r, r, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, srcRect, rect, paint);
            return output;
        } catch (IOException e) {
            FirebaseCrash.report(e);
            return null;
        }
    }
}
