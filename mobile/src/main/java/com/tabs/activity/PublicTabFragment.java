package com.tabs.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.crash.FirebaseCrash;
import com.schan.tabs.R;
import com.tabs.database.databaseQuery.DatabaseQuery;
import com.tabs.location.LocationService;
import com.tabs.location.LocationUpdateListener;


/**
 * Created by schan on 12/30/15.
 */
public class PublicTabFragment extends Fragment {

    private View fragmentView;
    private FireBaseApplication application;
    private DatabaseQuery databaseQuery;
    private View progressOverlay;
    private View noPostsView;
    private String userId;
    private String name;
    private LocationService locationService;
    final static int REQUEST_LOCATION = 199;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        application = ((FireBaseApplication) getActivity().getApplication());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Set things such as facebook profile picture, facebook friends photos, etc.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.posts_tab, container, false);
            setupActivity(savedInstanceState);
        }
        return fragmentView;
    }

    private void setupNoPostsView() {
        progressOverlay.setVisibility(View.GONE);
        noPostsView.setVisibility(View.VISIBLE);
        RecyclerView recyclerView = (RecyclerView) fragmentView.findViewById(R.id.rv_posts_feed);
        recyclerView.setVisibility(View.GONE);
        TextView noPosts = (TextView) noPostsView.findViewById(R.id.no_posts_text);
        noPosts.setText(R.string.permission_location_rationale);
    }




    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        databaseQuery = new DatabaseQuery(getActivity());
        progressOverlay = fragmentView.findViewById(R.id.progress_overlay);
        progressOverlay.setVisibility(View.VISIBLE);
        noPostsView = fragmentView.findViewById(R.id.no_posts_layout);
        setupLocation();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
        }
    }

    private void setNameAndId() {
        if (application.getUserId() != null && !application.getUserId().equals("")) {
            userId = application.getUserId();
        }
        if (application.getName() != null && !application.getName().equals("")) {
            name = application.getName();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("userId", userId);
        savedInstanceState.putString("name", name);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupActivity(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            if (savedInstanceState.containsKey("userId")) {
                userId = savedInstanceState.getString("userId");
            }
            if (savedInstanceState.containsKey("name")) {
                name = savedInstanceState.getString("name");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private void setupLocation() {
        Context context = getContext();
        if(locationService == null) {
            locationService = new LocationService(context, new LocationUpdateListener() {
                @Override
                public void canReceiveLocationUpdates() {

                }

                @Override
                public void cannotReceiveLocationUpdates() {
                    createSettingsRequest();
                    //well we know we cant receive updates so we have to create a settings request
                }

                //update location to our servers for tracking purpose
                @Override
                public void updateLocation(Location location) {
                    if (location != null) {
//                    noLocation = true;
                        databaseQuery.getPublicPosts(location, progressOverlay, fragmentView, getContext());
                    }
                }

                @Override
                public void updateLocationName(String localityName, Location location) {
                    locationService.stopLocationUpdates();
                }
            });
            locationService.startUpdates();
        }
    }


    private void createSettingsRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(locationService.mGoogleApiClient
                        , builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            FirebaseCrash.report(e);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
//                        setupLocation();
//                        noLocation = true;
                        break;
                    case Activity.RESULT_CANCELED:
//                        setupNoPostsView();
                        break;
                }
                break;
        }
    }
}
