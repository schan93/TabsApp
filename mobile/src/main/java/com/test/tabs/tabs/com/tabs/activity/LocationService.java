package com.test.tabs.tabs.com.tabs.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.ActivityCompat.requestPermissions;

/**
 * Created by schan on 1/5/16.
 */
public class LocationService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    //Google Location Services API
    private static LocationService instance = null;
    private static GoogleApiClient googleApiClient;
    private static Location lastLocation;
    LocationManager locationManager;
    Context context;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;


    /**
     * Singleton implementation
     * @return
     */
    public static LocationService getLocationManager(Context context)     {
        if (instance == null) {
            instance = new LocationService(context);
        }
        return instance;
    }

    /**
     * Local constructor
     */
    private LocationService( Context context )     {
        this.context = context;
        initLocationService(context);
    }

    /**
     * Sets up location service after permissions is granted
     */
    private void initLocationService(Context context) {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(com.google.android.gms.location.LocationServices.API)
                    .build();
        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    googleApiClient);
            if (lastLocation != null) {
                //Gets current location
                System.out.println("Last Location: " + lastLocation);
                //Get all posts within a 2 mile radius of the user's current location

            }
            else {
                System.out.println("last location needs to be created.");
            }
        } catch (SecurityException e){
            System.out.println("Security Exception: " + e);
        }

    }

    public static Location getLastLocation(){
        return lastLocation;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected static void onStart() {
        googleApiClient.connect();
    }

    protected void onStop() {
        googleApiClient.disconnect();
    }
}
