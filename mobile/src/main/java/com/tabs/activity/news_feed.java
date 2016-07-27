package com.tabs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import android.view.View;

import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;

public class news_feed extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String userId;
    Handler handler;
    FireBaseApplication application;
    List<String> currentFollowerItems = new ArrayList<String>();
    DatabaseQuery databaseQuery;
    String name;
    private static TabLayout tabLayout;
    public news_feed(){

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_news_feed);
        application =  (FireBaseApplication) getApplication();
        databaseQuery = new DatabaseQuery(this);
        handler = new Handler();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(application.getName() != null && !application.getName().equals("")) {
            name = application.getName();
        }
        if(application.getUserId() != null && !application.getUserId().equals("")) {
            userId = application.getUserId();
        }
        setupActivity(savedInstanceState);

        View layout = findViewById(R.id.activity_main);

        tabLayout = (TabLayout) layout.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Public"));
        tabLayout.addTab(tabLayout.newTab().setText("Following"));
        tabLayout.addTab(tabLayout.newTab().setText("Profile"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

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
        //populateNewsFeedList();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public void onResume(){
        super.onResume();
        LocationService.getLocationManager(this);
        AppEventsLogger.activateApp(this);
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
    }

    private void checkFromIntent(ViewPager viewPager) {
        if(getIntent().getExtras() != null) {
            viewPager.setCurrentItem(3);
        }
    }


}
