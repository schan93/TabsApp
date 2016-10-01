package com.tabs.activity;

/**
 * Created by schan on 9/15/16.
 */

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import com.google.firebase.crash.FirebaseCrash;
import com.schan.tabs.R;

public class ProviderLocationTracker implements LocationListener, LocationTracker {

    // The minimum distance to change Updates in meters
    private static final long MIN_UPDATE_DISTANCE = 10;

    // The minimum time between updates in milliseconds
    private static final long MIN_UPDATE_TIME = 0;

    private LocationManager lm;

    public enum ProviderType {
        NETWORK,
        GPS
    }

    ;
    private String provider;

    private Location lastLocation;
    private long lastTime;

    private boolean isRunning;
    private boolean gpsEnabled;
    private boolean networkEnabled;

    private LocationUpdateListener listener;

    public ProviderLocationTracker(Context context, ProviderType type) {
        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (type == ProviderType.NETWORK) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            provider = LocationManager.GPS_PROVIDER;
        }
    }

    public void start(Context context, LocationManager lm) {
        if (isRunning) {
            //Already running, do nothing
            return;
        }

        //The provider is on, so start getting updates.  Update current location
        isRunning = true;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
//        lm.requestLocationUpdates(provider, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this);
        lastLocation = null;
        lastTime = 0;
        return;
    }

    public void start(LocationUpdateListener update, Context context, LocationManager lm) {
        start(context, lm);
        listener = update;

    }


    public void stop(Context context, LocationManager lm) {
        if (isRunning) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            lm.removeUpdates(this);
            isRunning = false;
            listener = null;
        }
    }

    public void checkLocationEnabled(Context context, LocationManager lm) {
        //We know that permissions must be enabled at this point for the app to work (I would imagine so at least). Lets assume so
        //for now. Then we need to check if permissions are enabled.
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception e) {
            FirebaseCrash.report(e);
        }
        if(!gpsEnabled && !networkEnabled) {
            showLocationServicesDialog(context);
        }
    }

    public boolean hasLocation(Context context) {
        if (lastLocation == null) {
            return false;
        }
        if (System.currentTimeMillis() - lastTime > 5 * MIN_UPDATE_TIME) {
            return false; //stale
        }
        return true;
    }

    public boolean hasPossiblyStaleLocation(Context context, LocationManager lm) {
        if (lastLocation != null) {
            return true;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        return lm.getLastKnownLocation(provider) != null;
    }

    public Location getLocation() {
        if (lastLocation == null) {
            return null;
        }
        if (System.currentTimeMillis() - lastTime > 5 * MIN_UPDATE_TIME) {
            return null; //stale
        }
        return lastLocation;
    }

    public Location getPossiblyStaleLocation(Context context, LocationManager lm) {
        if (lastLocation != null) {
            return lastLocation;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        return lm.getLastKnownLocation(provider);
    }

    public void onLocationChanged(Location newLoc) {
        long now = System.currentTimeMillis();
        if(listener != null){
            listener.onUpdate(lastLocation, lastTime, newLoc, now);
        }
        //We know that if the last location was null at some point, we have to grab the location
        //And make sure that we query for the public posts because they may not be showing.
        lastLocation = newLoc;
        lastTime = now;
    }

    public void setLocation(Location location) {
        lastLocation = location;
    }

    public void showLocationServicesDialog(final Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage(context.getResources().getString(R.string.gpsNetworkNotEnabled));
        dialog.setPositiveButton(context.getResources().getString(R.string.openLocationSettings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(myIntent);
                //get gps
            }
        });
        dialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

            }
        });
        dialog.show();
    }

    public void onProviderDisabled(String arg0) {

    }

    public void onProviderEnabled(String arg0) {

    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }
}