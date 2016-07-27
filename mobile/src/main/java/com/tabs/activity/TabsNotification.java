package com.tabs.activity;



import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.schan.tabs.R;

/**
 * Created by kevinkusumi on 3/3/16.
 */
public class TabsNotification {

    private String contentTitle;
    private String contentText;
    private String tickerText;

    public TabsNotification(String contentTitle, String contentText, String tickerText){
        this.contentTitle = contentTitle;
        this.contentText = contentText;
        this.tickerText = tickerText;
    }

    public void post(Context context) {
        NotificationCompat.Builder notifiationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setTicker(tickerText)
                .setSmallIcon(R.mipmap.blank_prof_pic);

        Intent moreInfoIntent = new Intent(context, TabsNotification.class);

        //When the user clicks back, it doesn't look sloppy!
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MoreInfoNotification.class);
        taskStackBuilder.addNextIntent(moreInfoIntent);

        //If the intent already exists, just update it and not create a new one
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //When the notification is actually clicked on
        notifiationBuilder.setContentIntent(pendingIntent);

        //Notification manager to notify of background event
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, notifiationBuilder.build());

        //isNotificationActive = true;
    }


}
