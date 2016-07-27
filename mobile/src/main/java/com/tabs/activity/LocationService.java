package com.tabs.activity;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by schan on 1/5/16.
 */
public class LocationService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    //Google Location Services API
    private static LocationService instance = null;
    public static GoogleApiClient googleApiClient;
    private static Location lastLocation;
    LocationManager locationManager;
    Context context;
    private static double lastKnownLatitude;
    private static double lastKnownLongitude;


    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;


    /**
     * Singleton implementation
     * @return
     */
    public static LocationService getLocationManager(Context context)     {
        if (instance == null) {
            System.out.println("LocationService: Instance is null! creating a new one");
            instance = new LocationService(context);
        }
        System.out.println("Returning instance: " + instance);
        return instance;
    }

    /**
     * Local constructor
     */
    private LocationService( Context context )     {
        System.out.println("LocationService: Context");
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
            System.out.println("LocationService: On Connected");
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    googleApiClient);
            System.out.println("LocationService: Last Location" + lastLocation);
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
        if(lastLocation !=  null) {
            setLastKnownLatitude(lastLocation.getLatitude());
            setLastKnownLongitude(lastLocation.getLongitude());
        }
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

    public static GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public static double getLastKnownLatitude() {
        return lastKnownLatitude;
    }

    public static void setLastKnownLatitude(double latitude) {
         lastKnownLatitude = latitude;
    }

    public static double getLastKnownLongitude() {
        return lastKnownLatitude;
    }

    public static void setLastKnownLongitude(double longitude) {
        lastKnownLongitude = longitude;
    }

}
