package com.test.tabs.tabs.com.tabs.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.Database.DatabaseQuery;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schan on 12/30/15.
 */
public class Public extends Fragment implements LocationListener {

    private Firebase firebaseRef = new Firebase("https://tabsapp.firebaseio.com/");
    private View fragmentView;
    PostRecyclerViewAdapter adapter;
    //Local Database for storing posts
    private PostsDataSource postsDataSource;
    //Local Database for storing friends
    private FriendsDataSource datasource;
    private LocationManager locationManager;
    private FireBaseApplication application;
    private String provider;
    private Location location;
    private boolean isGPSEnabled;
    private static boolean isNetworkEnabled;
    public static double lat = 0.0;
    public static double lng = 0.0;
    private DatabaseQuery databaseQuery;
    private View progressOverlay;

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
//        ProgressBar progressBar = (TextView)view.findViewById(R.id.prog);

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
        fragmentView = inflater.inflate(R.layout.public_tab, container, false);
        progressOverlay = fragmentView.findViewById(R.id.progress_overlay);
        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
        getPublicPosts(progressOverlay, fragmentView);
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
//        getPublicPosts();
    }

//    public void getPublicPosts() {
//        //Need to do order by / equal to.
//        Firebase postsRef = firebaseRef.child("Posts");
//        Query query = postsRef.orderByChild("privacy").equalTo("Public");
//        query.keepSynced(true);
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
//                    Post post = postSnapShot.getValue(Post.class);
//                    List<Post> publicPosts = application.getPublicAdapter().getPosts();
//                    if (post.getPrivacy().equals("Public") && application.getPublicAdapter().containsId(publicPosts, post.getId()) == null) {
//                        application.getPublicAdapter().add(post);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });
//
//        postsRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Post newPost = dataSnapshot.getValue(Post.class);
//                List<Post> publicPosts = application.getPublicAdapter().getPosts();
//                if (application.getPublicAdapter().containsId(publicPosts, newPost.getId()) == null && newPost.getPrivacy().equals("Public")) {
//                    application.getPublicAdapter().getPosts().add(newPost);
//                    application.getPublicAdapter().notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                Post changedPost = dataSnapshot.getValue(Post.class);
//                int length = application.getPublicAdapter().getPosts().size();
//                for (int i = 0; i < length; i++) {
//                    if (application.getPublicAdapter().getPosts().get(i).getId().equals(changedPost.getId())) {
//                        application.getPublicAdapter().getPosts().set(i, changedPost);
//                    }
//                }
//                application.getPublicAdapter().notifyDataSetChanged();
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                Post removedPost = dataSnapshot.getValue(Post.class);
//                int length = application.getPublicAdapter().getPosts().size();
//                for (int i = 0; i < length; i++) {
//                    if (application.getPublicAdapter().getPosts().get(i).getId().equals(removedPost.getId())) {
//                        application.getPublicAdapter().getPosts().remove(i);
//                    }
//                }
//                application.getPublicAdapter().notifyDataSetChanged();
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//                //Not sure if used
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });
//    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
//                application.getPublicAdapter().notifyDataSetChanged();
            }
        }
    }

    public void populateNewsFeedList(View fragmentView) {
        RecyclerView rv = (RecyclerView) fragmentView.findViewById(R.id.rv_public_feed);
        RecyclerView.ItemAnimator animator = rv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setAdapter(application.getPublicAdapter());
    }

    public void checkLocation(Activity activity) {
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        Criteria c = new Criteria();
        provider = locationManager.getBestProvider(c, false);
        location = getLocation(activity);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
        locationManager.removeUpdates(this);

        if (location != null) {
            // get latitude and longitude of the location
            onLocationChanged(location);

        } else {
            Log.d("TAG", "Unable to find Location");
        }
    }

    public Location getLocation(Activity activity) {
        try {
            locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 0, this);
                    Log.d("Network", "Network Enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 0, this);
                        Log.d("GPS", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                lat = location.getLatitude();
                                lng = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (SecurityException e) {
            Log.e("PERMISSION_EXCEPTION","PERMISSION_NOT_GRANTED");
        }catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void getPublicPosts(final View progressOverlay, final View fragmentView) {
        //Need to do order by / equal to.
        Firebase postsRef = firebaseRef.child("Posts");
        Query query = postsRef.orderByChild("privacy").equalTo("Public");
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Post post = postSnapShot.getValue(Post.class);
                    List<Post> publicPosts = application.getPublicAdapter().getPosts();
                    if (post.getPrivacy().equals("Public") && application.getPublicAdapter().containsId(publicPosts, post.getId()) == null) {
                        application.getPublicAdapter().add(post);
                    }
                }
                System.out.println("Public: Visiblity: " + progressOverlay);
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                }
                populateNewsFeedList(fragmentView);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
//                populateNewsFeedList(fragmentView);
            }
        });

        postsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post newPost = dataSnapshot.getValue(Post.class);
                List<Post> publicPosts = application.getPublicAdapter().getPosts();
                if (application.getPublicAdapter().containsId(publicPosts, newPost.getId()) == null && newPost.getPrivacy().equals("Public")) {
                    application.getPublicAdapter().getPosts().add(newPost);
                    application.getPublicAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Post changedPost = dataSnapshot.getValue(Post.class);
                int length = application.getPublicAdapter().getPosts().size();
                for (int i = 0; i < length; i++) {
                    if (application.getPublicAdapter().getPosts().get(i).getId().equals(changedPost.getId())) {
                        application.getPublicAdapter().getPosts().set(i, changedPost);
                    }
                }
                application.getPublicAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Post removedPost = dataSnapshot.getValue(Post.class);
                int length = application.getPublicAdapter().getPosts().size();
                for (int i = 0; i < length; i++) {
                    if (application.getPublicAdapter().getPosts().get(i).getId().equals(removedPost.getId())) {
                        application.getPublicAdapter().getPosts().remove(i);
                    }
                }
                application.getPublicAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //Not sure if used
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
