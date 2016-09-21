package com.tabs.activity;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * Created by schan on 9/19/16.
 */
public class LocationService {

    private GoogleServicesCallbacks callbacks = new GoogleServicesCallbacks();
    LocationUpdateListener locationUpdateListener;
    Context activity;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    public static final String ACTION_LOCATION = "com.tabs.LocationService.ACTION_LOCATION";


    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 300000;

    //About 1 mile, we will call locationOnChanged to re-update the location.
    public static final float UPDATE_DISPLACEMENT_IN_METERS = 1000;

    //Settings request check
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;


    public LocationService(Context activity, LocationUpdateListener locationUpdateListener) {
        this.locationUpdateListener = locationUpdateListener;
        this.activity = activity;
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        //Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(callbacks)
                .addOnConnectionFailedListener(callbacks)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
        mGoogleApiClient.connect();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(UPDATE_DISPLACEMENT_IN_METERS);
    }

    private class GoogleServicesCallbacks implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

        @Override
        public void onConnected(Bundle bundle) {
            startLocationUpdates();
        }

        @Override
        public void onConnectionSuspended(int i) {
            mGoogleApiClient.connect();
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

            if (connectionResult.getErrorCode() == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
                Toast.makeText(activity, "Google play service not updated", Toast.LENGTH_LONG).show();

            }
            locationUpdateListener.cannotReceiveLocationUpdates();
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location.hasAccuracy()) {
                locationUpdateListener.updateLocation(location);
            }
        }
    }

    private static boolean locationEnabled(Context context) {
        boolean gps_enabled = false;
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return gps_enabled;
    }

    private boolean servicesConnected(Context context) {
        return isPackageInstalled(GooglePlayServicesUtil.GOOGLE_PLAY_STORE_PACKAGE, context);
    }

    private boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void startUpdates() {
    /*
     * Connect the client. Don't re-start any requests here; instead, wait
     * for onResume()
     */
        if (servicesConnected(activity)) {
            if (locationEnabled(activity)) {
                locationUpdateListener.canReceiveLocationUpdates();
                startLocationUpdates();
            } else {
                locationUpdateListener.cannotReceiveLocationUpdates();
//                Toast.makeText(activity, "Unable to get your location.Please turn on your device GPS", Toast.LENGTH_LONG).show();
            }
        } else {
            locationUpdateListener.cannotReceiveLocationUpdates();
            Toast.makeText(activity, "Google play service not available", Toast.LENGTH_LONG).show();
        }
    }

    //stop location updates
    public void stopUpdates() {
        stopLocationUpdates();
    }

    //start location updates
    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, callbacks);
        }
    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, callbacks);
        }
    }

    public void startGoogleApi() {
        mGoogleApiClient.connect();
    }

    public void closeGoogleApi() {
        mGoogleApiClient.disconnect();
    }

//    public static setLocation(Location location) {
//        this.location = location;
//    }
//
//    public static getLocation() {
//        return
//    }

}
