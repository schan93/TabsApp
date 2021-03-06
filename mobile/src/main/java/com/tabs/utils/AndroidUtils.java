package com.tabs.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.crash.FirebaseCrash;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by schan on 2/17/16.
 * This class is designed for general code, such as converting date times, changng the view of the progress overlay, and getting intent strings
 */
public class AndroidUtils {
    /**
     * @param view         View to animate
     * @param toVisibility Visibility at the end of animation
     * @param toAlpha      Alpha at the end of animation
     * @param duration     Animation duration in ms
     */
    public static void animateView(final View view, final int toVisibility, float toAlpha, int duration) {
        boolean show = toVisibility == View.VISIBLE;
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate()
                .setDuration(duration)
                .alpha(show ? toAlpha : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(toVisibility);
                    }
                });
    }

    /**
     * Convert the string date to the 1h, 1m, 0s etc. format
     * @param time
     * @return
     */
    public static String convertDate(Long time) {
        return getRelativeTime(-1 * time);
    }

    public static Long getDateTime() {
        Date date = new Date();
        return -1 * date.getTime();
    }

    public static final List<Long> times = Arrays.asList(
            DAYS.toMillis(365),
            DAYS.toMillis(30),
            DAYS.toMillis(7),
            DAYS.toMillis(1),
            HOURS.toMillis(1),
            MINUTES.toMillis(1)
    );

    public static final List<String> timesString = Arrays.asList(
            " years ago", " months ago", " weeks ago", " days ago", " hours ago", " minutes ago"
    );

    public static final List<String> timesStringSingular = Arrays.asList(
            " year ago", " month ago", " week ago", " day ago", " hour ago", " minute ago"
    );

    /**
     * Get relative time ago for date
     *
     * NOTE:
     *  if (duration > WEEK_IN_MILLIS) getRelativeTimeSpanString prints the date.
     *
     * ALT:
     *  return getRelativeTimeSpanString(date, now, SECOND_IN_MILLIS, FORMAT_ABBREV_RELATIVE);
     *
     * @param date String.valueOf(TimeUtils.getRelativeTime(1000L * Date/Time in Millis)
     * @return relative time
     */
    public static String getRelativeTime(final long date) {
        return toDuration( Math.abs(System.currentTimeMillis() - date) );
    }

    private static String toDuration(long duration) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i< times.size(); i++) {
            Long current = times.get(i);
            long temp = duration / current;
            if(temp == 1) {
                sb.append(temp)
                        .append(timesStringSingular.get(i));
                break;
            } else if(temp > 1) {
                sb.append(temp)
                        .append(timesString.get(i));
                break;
            }
        }
        return sb.toString().isEmpty() ? "now" : sb.toString();
    }

    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    public static Double getIntentDouble(Intent intent, String value){
        Bundle extras = intent.getExtras();
        Double result = null;
        if (extras != null) {
            result = extras.getDouble(value);
        }
        return result;
    }

    public static String getIntentString(Intent intent, String value){
        Bundle extras = intent.getExtras();
        String result = "";
        if (extras != null) {
            result = extras.getString(value);
        }
        return result;
    }

    public static Long getIntentLong(Intent intent, String value){
        Bundle extras = intent.getExtras();
        Long result = 123456789L;
        if (extras != null) {
            result = extras.getLong(value);
        }
        return result;
    }


    public static Bitmap getBitmapFromURL(String userId) {
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
