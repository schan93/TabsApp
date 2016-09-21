package com.tabs.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;

import java.util.Calendar;
import java.util.List;


/**
 * Created by schan on 12/30/15.
 */
public class PublicTab extends Fragment {

    public static PublicTab newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(TabsUtil.ARGS_INSTANCE, instance);
        PublicTab fragment = new PublicTab();
        fragment.setArguments(args);
        return fragment;
    }


    public PublicTab() {
    }

    private View fragmentView;
    private LocationManager locationManager;
    private FireBaseApplication application;
    private String provider;
    private boolean isGPSEnabled;
    private static boolean isNetworkEnabled;
    public static double lat = 0.0;
    public static double lng = 0.0;
    private DatabaseQuery databaseQuery;
    private View progressOverlay;
    private String userId;
    private String name;
    private RecyclerView recyclerView;
    final static int REQUEST_LOCATION = 199;
    Location location;
    LocationRequest mLocationRequest;
    ProviderLocationTracker providerLocationTracker;
    PendingResult<LocationSettingsResult> result;
    Context context;
    LocationService locationService;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;


    //GoogleApiClient
    private GoogleApiClient mGoogleApiClient;

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
            databaseQuery = new DatabaseQuery(getActivity());
            progressOverlay = fragmentView.findViewById(R.id.progress_overlay);
            setupActivity(savedInstanceState);
            setupLocation();
        }

        return fragmentView;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
//                application.getPublicAdapter().notifyDataSetChanged();
            }
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
        context = getContext();
        locationService = new LocationService(context, new LocationUpdateListener() {
            @Override
            public void canReceiveLocationUpdates() {
                //Cant receieve location updates so we need to enable them here

            }

            @Override
            public void cannotReceiveLocationUpdates() {
            }

            //update location to our servers for tracking purpose
            @Override
            public void updateLocation(Location location) {
                if (location != null) {
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
