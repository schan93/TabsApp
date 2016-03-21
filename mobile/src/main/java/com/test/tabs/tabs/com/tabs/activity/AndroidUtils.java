package com.test.tabs.tabs.com.tabs.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by schan on 2/17/16.
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
     * @param timestamp
     * @return
     */
    public static String convertDate(String timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        String dateText = "";
        Date date = null;
        try {
            date = dateFormat.parse(timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar postDate = Calendar.getInstance();
        postDate.setTime(date); // your date

        Calendar now = Calendar.getInstance();

        Integer dateOffset = 0;
        System.out.println("Post Date: " + postDate);
        System.out.println("Now: " + now);
        if (now.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == postDate.get(Calendar.MONTH)
                && now.get(Calendar.DAY_OF_YEAR) == postDate.get(Calendar.DAY_OF_YEAR)
                && (now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR) >= 1)) {
            dateOffset = now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR);
            dateText = "h";
        } else if (now.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == postDate.get(Calendar.MONTH)
                && now.get(Calendar.DAY_OF_YEAR) == postDate.get(Calendar.DAY_OF_YEAR)
                && (now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR) == 0)) {
            dateOffset = now.get(Calendar.MINUTE) - postDate.get(Calendar.MINUTE);
            dateText = "m";
        } else if (Math.abs(now.getTime().getTime() - postDate.getTime().getTime()) <= 24 * 60 * 60 * 1000L) {
            dateOffset = (int) getHoursDifference(now, postDate);
            if(dateOffset == 24){
                dateOffset = 1;
                dateText = "d";
            }
            else {
                dateText = "h";
            }
        } else {
            long hours = getHoursDifference(now, postDate);

            dateOffset = (int)hours / 24;
            dateText = "d";
        }
        String newFormat = dateOffset + dateText;
        return newFormat;
    }

    private static long getHoursDifference(Calendar now, Calendar postDate) {
        long secs = (now.getTime().getTime() - postDate.getTime().getTime()) / 1000;
        long hours = secs / 3600;
        return hours;
    }
}
