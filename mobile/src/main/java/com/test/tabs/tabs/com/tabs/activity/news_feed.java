package com.test.tabs.tabs.com.tabs.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.View;
import android.widget.TextView;

import com.facebook.appevents.AppEventsLogger;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.login.LoginManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.Database.DatabaseQuery;
import com.test.tabs.tabs.com.tabs.database.followers.Follower;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;

public class news_feed extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String userId;
    Handler handler;
    FireBaseApplication application;
    List<String> currentFriendItems = new ArrayList<String>();
    List<String> currentFollowerItems = new ArrayList<String>();
    DatabaseQuery databaseQuery;
    String name;
    private static DrawerLayout drawer;
    private static TabLayout tabLayout;
    private static ActionBarDrawerToggle toggle;
    public news_feed(){

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        application =  (FireBaseApplication) getApplication();
        databaseQuery = new DatabaseQuery(this);
        handler = new Handler();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(application.getName() != null && application.getName() != "") {
            name = application.getName();
        }
        if(application.getUserId() != null && application.getUserId() != "") {
            userId = application.getUserId();
        }
        setupActivity(savedInstanceState);

        //Listen for navigation events
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
                if(currentFriendItems.size() > 0) {
                    currentFriendItems.clear();
                }
                for(Friend friend: application.getFriendsRecyclerViewAdapter().getFriends()) {
                    currentFriendItems.add(friend.getIsFriend());
                }
                if(currentFollowerItems.size() > 0) {
                    currentFollowerItems.clear();
                }
                for(Follower follower: application.getFollowerRecyclerViewAdapter().getFollowers()) {
                    currentFollowerItems.add(follower.getIsFollowing());
                }
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                System.out.println("Drawer closed");
                List<Friend> friends = application.getFriendsRecyclerViewAdapter().getFriends();
//                List<Follower> followers = application.getFollowerRecyclerViewAdapter().getFollowers();
                updateFriendToFirebase(friends, currentFriendItems);
//                updateFollowerToFirebase(followers, currentFollowerItems);
            }
        };

        drawer.setDrawerListener(toggle);

        View layout = findViewById(R.id.app_bar_main);

        tabLayout = (TabLayout) layout.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Public"));
        tabLayout.addTab(tabLayout.newTab().setText("Friends"));
        tabLayout.addTab(tabLayout.newTab().setText("My Tabs"));
        tabLayout.addTab(tabLayout.newTab().setText("Following"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(4);
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                Intent intent = new Intent(news_feed.this, CreatePost.class);
                if (intent != null) {
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        drawerHeaderSetup(userId, name);
        TabsUtil.populateCompanionList(CompanionEnum.Friend, news_feed.this);
        TabsUtil.populateCompanionList(CompanionEnum.Follower, news_feed.this);

        //populateNewsFeedList();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();

    }

    @Override
    public void onResume(){
        super.onResume();
        LocationService.getLocationManager(this);
        AppEventsLogger.activateApp(this);
    }

    private void drawerHeaderSetup(String id, String name) {
        DraweeController controller = TabsUtil.getImage(id);
        SimpleDraweeView draweeView = (SimpleDraweeView) findViewById(R.id.avatarImageView);
        draweeView.setController(controller);
        TextView headerName = (TextView) findViewById(R.id.user_name);
        headerName.setText(name);
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    public void updateFriendToFirebase(List<Friend> friends, List<String> currentFriendItems) {
        //Need to check if the we should even update the freinds list
        boolean areUpdated = checkUpdatedFriends(friends, currentFriendItems);
        if(areUpdated) {
            Firebase reference = new Firebase("https://tabsapp.firebaseio.com/Friends");
            Map<String, Object> updatedFriends = new HashMap<String, Object>();
            for(Friend friend: friends) {
                updatedFriends.put(friend.getUser() + "/" + friend.getId() + "/isFriend", friend.getIsFriend());
            }
            application.setFromAnotherActivity(true);
            reference.updateChildren(updatedFriends, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        System.out.println("There was an error saving data. ");
                    } else {
                        if(application.getFromAnotherActivity() == true) {
                            application.setFromAnotherActivity(false);
                        }
                    }
                }
            });
        }
    }


    public boolean checkUpdatedFriends(List<Friend> friends, List<String> currentFriendItems) {
        boolean result = false;
        if(friends.size() != currentFriendItems.size()){
            System.out.println("Error: There was an unexpected error.");
            System.out.println("news_feed: Returning false");
            return false;
        }
        for(int i = 0; i < friends.size(); i++) {
            System.out.println("news_feed: Friend 1: " + friends.get(i).getName() + "'s isFriend is: "
                    + friends.get(i).getIsFriend() + ". Friend 2's isFriend is: " +
                    currentFriendItems.get(i));
            if(friends.get(i).getIsFriend() == currentFriendItems.get(i)) {
                continue;
            } else {
                System.out.println("news_feed: There was a difference because: " + friends.get(i).getName() + "'s isFriend is: "
                        + friends.get(i).getIsFriend() + " but Friend 2's isFriend is: " +
                        currentFriendItems.get(i));
                System.out.println("news_feed: The friend get is friend " + friends.get(i).getName() + " is now: " + friends.get(i).getIsFriend());
                result = true;
            }
        }
        if(result == true) {
            application.getFriendsRecyclerViewAdapter().notifyDataSetChanged();
        }
        System.out.println("news_feed: Final Returning Result: " + result);
        return result;
    }

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
    }


}
