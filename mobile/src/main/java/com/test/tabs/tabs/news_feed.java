package com.test.tabs.tabs;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import android.view.View;
import com.facebook.appevents.AppEventsLogger;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsListAdapter;

public class news_feed extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ListView newsFeedListView;
    private FeedListAdapter newsFeedListAdapter;
    private List<FeedItem> feedItems;
    private String[] newsFeedNames = {"Silvia", "Kevin", "Stephen", "Chrisdere", "Jwang", "Nathaneil", "TED  "};

    //Friends list values
    private ListView friendsList;
    private FriendsListAdapter friendsAdapter;
    //Local Database for storing friends
    private FriendsDataSource datasource;
    private List<Friend> friendItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Fab!");
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //Listen for navigation evens
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Open DB and get freinds from db.
        datasource = new FriendsDataSource(this);
        datasource.open();

        populateFriendsList();

        populateNewsFeedList();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        System.out.println("Selected item" + item.getItemId());
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        System.out.println("Item: " + item);

//        if (id == R.id.nav_camara) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void populateNewsFeedList(){
        newsFeedListView = (ListView)findViewById(R.id.lv_news_feed);
        feedItems = new ArrayList<FeedItem>();

        newsFeedListAdapter = new FeedListAdapter(this, feedItems);
        newsFeedListView.setAdapter(newsFeedListAdapter);

        //@TODO PUT THIS IN ASYNC TASK!!!! *********************
        for( int i = 0; i < newsFeedNames.length; i++){
            FeedItem item = new FeedItem();
            item.setId(i);
            //item.setName(newsFeedNames[i]);
            item.setName(newsFeedNames[i]);
            item.setStatus("test Status");
            item.setTimeStamp("tes TimeStamp");

            feedItems.add(item);
        }
        //newsFeedListAdapter.notifyDataSetChanged();
        // ************************************************
    }

    private void populateFriendsList() {
        friendsList = (ListView) findViewById(R.id.friends_list);
        friendItems = new ArrayList<Friend>();

        friendsAdapter = new FriendsListAdapter(this, friendItems);
        friendsList.setAdapter(friendsAdapter);

        for(int i = 0; i < datasource.getAllFriends().size(); i++) {
            Friend item = new Friend();
            item.setName("Name");
            item.setEmail("Email");

            friendItems.add(item);
        }
    }

    class UpdateNewsFeedListTask extends AsyncTask<Void,String, Void>
    {
        ArrayAdapter<String> news_feed_adapter;

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //TODO keep array that holds strings up to date
            news_feed_adapter = (ArrayAdapter<String>)newsFeedListView.getAdapter();
        }

        @Override
        protected Void doInBackground(Void... params) {

            //for(String Name : newsFeedString){
            //    //will invoke onProgressUpdate
            //    publishProgress(Name);
            //}
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //add input into adapter
            news_feed_adapter.add(values[0]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }
}
