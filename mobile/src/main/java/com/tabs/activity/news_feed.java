package com.tabs.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.RelativeLayout;

import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.firebase.crash.FirebaseCrash;
import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;
import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;
import com.tabs.database.followers.Follower;

public class news_feed extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String userId;
    Handler handler;
    FireBaseApplication application;
    DatabaseQuery databaseQuery;
    String name;
    private BottomBar mBottomBar;
    private FragNavController fragNavController;
    private PublicTab publicTab;
    private static TabLayout tabLayout;
    private Toolbar toolbar;
    Location location;

    public news_feed(){

    }

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if(location == null) {
                    location = new Location("Provider");
                }
                location.setLatitude(intent.getDoubleExtra("latitude", 0.0));
                location.setLongitude(intent.getDoubleExtra("longitude", 0.0));
            } catch (Exception e) {
                FirebaseCrash.report(e);
            }

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_news_feed);
        application =  (FireBaseApplication) getApplication();
        databaseQuery = new DatabaseQuery(this);
        handler = new Handler();
        if(application.getName() != null && !application.getName().equals("")) {
            name = application.getName();
        }
        if(application.getUserId() != null && !application.getUserId().equals("")) {
            userId = application.getUserId();
        }
        setupActivity(savedInstanceState);
        setupToolbar();
        View layout = findViewById(R.id.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //TODO: Maybe make fragment for create post
//                    FragmentManager fm = getFragmentManager();
//                    CreatePostDialog dialogFragment = new CreatePostDialog();
//                    dialogFragment.show(fm, "Sample Fragment");
                    Bundle bundle = new Bundle();
                    Intent intent = new Intent(news_feed.this, CreatePost.class);
                    if (intent != null) {
                        intent.putExtras(bundle);
                        if(location != null) {
                            intent.putExtra("latitude", location.getLatitude());
                            intent.putExtra("longitude", location.getLongitude());
                        }
                        startActivity(intent);
                    }
                }
            });
        }

//        List<Fragment> fragments = new ArrayList<>(3);
//
//        fragments.add(PublicTab.newInstance(0));
//        fragments.add(FollowersTab.newInstance(1));
//        fragments.add(ProfileTab.newInstance(2));
//
//        fragNavController = new FragNavController(getSupportFragmentManager(), R.id.container, fragments);
//
//        // Instead of attach(), use attachShy():
//
//        mBottomBar = BottomBar.attach(findViewById(R.id.activity_main), savedInstanceState);
//        mBottomBar.setItems(R.menu.bottombar_menu);
//        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
//            @Override
//            public void onMenuTabSelected(@IdRes int menuItemId) {
//                if (menuItemId == R.id.bb_menu_nearby) {
//                    publicTab = PublicTab.newInstance(0);
//                    fragNavController.switchTab(FragNavController.TAB1);
////                    startActivity(new Intent(getApplicationContext(), PublicTab.class));
//                    // The user selected item number one.
//                }
//                if (menuItemId == R.id.bb_menu_followers) {
//                    fragNavController.switchTab(FragNavController.TAB2);
//
////                    startActivity(new Intent(getApplicationContext(), FollowersTab.class));
//                    // The user selected item number two.
//                }
//                if (menuItemId == R.id.bb_menu_profile) {
//                    fragNavController.switchTab(FragNavController.TAB3);
//                    //Try to set the navigation toolbar to gone?
////                    toolbar.setVisibility(View.GONE);
////                    startActivity(new Intent(getApplicationContext(), ProfileTab.class));
//                    // The user selected item number three.
//                }
//            }
//
//            @Override
//            public void onMenuTabReSelected(@IdRes int menuItemId) {
////                if (menuItemId == R.id.bb_menu_nearby) {
////                    // The user reselected item number one, scroll your content to top.
////                }
////                if (menuItemId == R.id.bb_menu_followers) {
////                    // The user reselected item number two, scroll your content to top.
////                }
////                if (menuItemId == R.id.bb_menu_profile) {
////                    // The user reselected item number three, scroll your content to top.
////          tack();
//            }
//        });

        // Set the color for the active tab. Ignored on mobile when there are more than three tabs.
//        mBottomBar.setActiveTabColor("#009688");

        // Disable the left bar on tablets and behave exactly the same on mobile and tablets instead.
//        mBottomBar.noTabletGoodness();

        tabLayout = (TabLayout) layout.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Public"));
        tabLayout.addTab(tabLayout.newTab().setText("Following"));
        tabLayout.addTab(tabLayout.newTab().setText("Profile"));

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(3);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        checkFromIntent(viewPager);


        //populateNewsFeedList();
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PublicTab.REQUEST_LOCATION){
            publicTab.onActivityResult(requestCode, resultCode, data);
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public void onResume(){
        super.onResume();
        AppEventsLogger.activateApp(this);
        registerReceiver(locationReceiver, new IntentFilter(LocationService.ACTION_LOCATION));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.news_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(news_feed.this, login.class);
            if(intent != null) {
                application.initializeAdapters();
                startActivity(intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }


//    public boolean checkUpdatedFriends(List<Friend> friends, List<String> currentFriendItems) {
//        boolean result = false;
//        if(friends.size() != currentFriendItems.size()){
//            System.out.println("Error: There was an unexpected error.");
//            System.out.println("news_feed: Returning false");
//            return false;
//        }
//        for(int i = 0; i < friends.size(); i++) {
//            System.out.println("news_feed: Friend 1: " + friends.get(i).getName() + "'s isFriend is: "
//                    + friends.get(i).getIsFriend() + ". Friend 2's isFriend is: " +
//                    currentFriendItems.get(i));
//            if(friends.get(i).getIsFriend() == currentFriendItems.get(i)) {
//                continue;
//            } else {
//                System.out.println("news_feed: There was a difference because: " + friends.get(i).getName() + "'s isFriend is: "
//                        + friends.get(i).getIsFriend() + " but Friend 2's isFriend is: " +
//                        currentFriendItems.get(i));
//                System.out.println("news_feed: The friend get is friend " + friends.get(i).getName() + " is now: " + friends.get(i).getIsFriend());
//                result = true;
//            }
//        }
//        if(result == true) {
//            application.getFriendsRecyclerViewAdapter().notifyDataSetChanged();
//        }
//        System.out.println("news_feed: Final Returning Result: " + result);
//        return result;
//    }

    private void setupActivity(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            if(savedInstanceState.containsKey("userId")) {
                userId = savedInstanceState.getString("userId");
            }
            if(savedInstanceState.containsKey("name")) {
                name = savedInstanceState.getString("name");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("userId", userId);
        savedInstanceState.putString("name", name);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
//        mBottomBar.onSaveInstanceState(savedInstanceState);

    }

    private void checkFromIntent(ViewPager viewPager) {
        if(getIntent().getExtras() != null) {
            viewPager.setCurrentItem(3);
        }
    }


}
