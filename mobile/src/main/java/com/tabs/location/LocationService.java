package com.tabs.location;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.crash.FirebaseCrash;

/**
 * Created by schan on 9/19/16.
 */
public class LocationService {

    private GoogleServicesCallbacks callbacks = new GoogleServicesCallbacks();
    private LocationUpdateListener locationUpdateListener;
    private Context context;
    private LocationRequest mLocationRequest;
    public GoogleApiClient mGoogleApiClient;

    public static final String ACTION_LOCATION = "com.tabs.LocationService.ACTION_LOCATION";

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 300000;

    private static final float UPDATE_DISPLACEMENT_IN_METERS = 1000;


    public LocationService(Context context, LocationUpdateListener locationUpdateListener) {
        this.locationUpdateListener = locationUpdateListener;
        this.context = context;
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
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
                Toast.makeText(context, "Google play service not updated", Toast.LENGTH_LONG).show();

            }
            locationUpdateListener.cannotReceiveLocationUpdates();
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location.hasAccuracy()) {
                locationUpdateListener.updateLocation(location);
                Intent broadcastIntent = new Intent(LocationService.ACTION_LOCATION);
                broadcastIntent.putExtra("latitude", location.getLatitude());
                broadcastIntent.putExtra("longitude", location.getLongitude());
                context.sendBroadcast(broadcastIntent);
            }
        }
    }

    private static boolean locationEnabled(Context context) {
        boolean gps_enabled = false;
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            FirebaseCrash.report(e);
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
            FirebaseCrash.report(e);
            return false;
        }
    }


    public void startUpdates() {
    /*
     * Connect the client. Don't re-start any requests here; instead, wait
     * for onResume()
     */
        if (servicesConnected(context)) {
            if (locationEnabled(context)) {
                locationUpdateListener.canReceiveLocationUpdates();
                startLocationUpdates();
            } else {
                locationUpdateListener.cannotReceiveLocationUpdates();
            }
        } else {
            locationUpdateListener.cannotReceiveLocationUpdates();
            Toast.makeText(context, "Google play service not available", Toast.LENGTH_LONG).show();
        }
    }

    //start location updates
    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
}
