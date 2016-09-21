package com.tabs.activity;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * Created by schan on 9/15/16.
 */
public interface LocationTracker {

     interface LocationUpdateListener{

         void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime);

    }

    void start(Context context, LocationManager lm);

    void start(LocationUpdateListener update, Context context, LocationManager lm);

    void stop(Context context, LocationManager lm);

    boolean hasLocation(Context context);

    boolean hasPossiblyStaleLocation(Context context, LocationManager lm);

    Location getLocation();

    Location getPossiblyStaleLocation(Context context, LocationManager lm);
}
