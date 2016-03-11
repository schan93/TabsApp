package com.test.tabs.tabs.com.tabs.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.params.Face;
import android.location.Location;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.parse.ParseObject;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.comments.Comment;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsDataSource;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsListAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

public class news_feed extends BatchAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Firebase reference
    private Firebase firebaseRef = new Firebase("https://tabsapp.firebaseio.com/");

    //Friends list values
    private ListView friendsList;
    private FriendsListAdapter friendsAdapter;
    //Local Database for storing friends
    private FriendsDataSource datasource;
    private List<Friend> friendItems;
    //Local Database for storing posts
    private PostsDataSource postsDataSource;
    private CommentsDataSource commentsDataSource;
    //Progress overlay
    View progressOverlay;

    String userId;

    Activity activityContext;

    Handler handler;

    AccessToken accessToken;

    FireBaseApplication application;

    List<String> currentFriendItems = new ArrayList<String>();

    public news_feed(){

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("CREATING THE NEWS FEED ACTIVITY BEFORE GOING INTO FRAGMENT");
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);
        System.out.println("News_Feed: 0");
        application =  (FireBaseApplication) getApplication();
        System.out.println("News_Feed: 1");
        startDatabases();
        System.out.println("News_Feed: 2");
        progressOverlay = findViewById(R.id.progress_overlay);
        System.out.println("News_Feed: 3");
        handler = new Handler();
        System.out.println("News_Feed: 4");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        System.out.println("News_Feed: 5");
        if(!FacebookSdk.isInitialized()) {
            System.out.println("News_Feed: 6");
            FacebookSdk.sdkInitialize(getApplicationContext());
            accessToken = checkAccessToken();
        }

        //Listen for navigation events
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
                if(currentFriendItems.size() > 0) {
                    currentFriendItems.clear();
                }
                for(Friend friend: application.getFriendsAdapter().getFriends()) {
                    currentFriendItems.add(friend.getIsFriend());
                }
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                System.out.println("Drawer closed");
                List<Friend> friends = application.getFriendsAdapter().getFriends();
                updateFriendToFirebase(friends, currentFriendItems);
            }
        };

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Public"));
        tabLayout.addTab(tabLayout.newTab().setText("Friends"));
        tabLayout.addTab(tabLayout.newTab().setText("My Tabs"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
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

        //Set things such as facebook profile picture, facebook friends photos, etc.
        //Set up recycler view
        ListView listView = (ListView) findViewById(R.id.friends_list);

        //Inflate ListView header
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.add_friends_header, listView,
                false);
        listView.addHeaderView(header, null, false);
        System.out.println("News_Feed: 8");




        final String finalName = application.getName();
        accessToken = checkAccessToken();
        userId = accessToken.getUserId();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("name", finalName);
                Intent intent = new Intent(news_feed.this, CreatePost.class);
                if (intent != null) {
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        drawerSetup(userId, finalName);
        populateFriendsList();

        //populateNewsFeedList();
    }

    @Override
    public void onResume(){
        super.onResume();
        LocationService.getLocationManager(this);
        AppEventsLogger.activateApp(this);
    }

    private void drawerSetup(String id, String name) {
        DraweeController controller = getImage(id);
        SimpleDraweeView draweeView = (SimpleDraweeView) findViewById(R.id.avatarImageView);
        draweeView.setController(controller);
        TextView headerName = (TextView) findViewById(R.id.user_name);
        headerName.setText(name);
    }

    public static DraweeController getImage(String userId){
        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>(){
            @Override
            public void onFinalImageSet(
                    String id,
                    @Nullable ImageInfo imageInfo,
                    @Nullable Animatable anim) {
                if(imageInfo == null)
                    return;
                QualityInfo qualityInfo = imageInfo.getQualityInfo();
                FLog.d("Final image received ! " + "Size %d x %d", "Quality level %d, good enough : %s, full quality: %s",
                        imageInfo.getWidth(),
                        imageInfo.getHeight(),
                        qualityInfo.getQuality(),
                        qualityInfo.isOfGoodEnoughQuality(),
                        qualityInfo.isOfFullQuality());
                }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo){
                FLog.d(getClass(), "Intermediate image received");
            }

            @Override
        public void onFailure(String id, Throwable throwable){
                FLog.e(getClass(), throwable, "Error loading %s ", id);
            }
        };
        Uri uri = Uri.parse("http://graph.facebook.com/" + userId + "/picture?type=large");
        System.out.println("Uri: " + uri);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(controllerListener)
                .setUri(uri)
                .build();
        System.out.println("Controller: " + controller);
        return controller;
        //draweeView.setImageURI(uri);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

    private void populateFriendsList(String userId) {
        friendsList = (ListView) findViewById(R.id.friends_list);
        friendItems = new ArrayList<>();

        activityContext = this;

        friendsAdapter = application.getFriendsAdapter();
        friendsList.setAdapter(friendsAdapter);
        System.out.println("Freinds Adapter: " + friendsAdapter);
        for (Friend i : datasource.getAllFriends(userId)) {
            System.out.println("Friend name: " + i.getName());
            friendItems.add(i);
        }
    }

    private void populateFriendsList() {
        friendsList = (ListView) findViewById(R.id.friends_list);
        System.out.println("News_feed: Friends list adapter size: " + application.getFriendsAdapter().getCount());
        friendsList.setAdapter(application.getFriendsAdapter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    private void startDatabases() {
        //Open DB and get freinds from db & posts.
        datasource = new FriendsDataSource(this);
        datasource.open();
        postsDataSource = new PostsDataSource(this);
        postsDataSource.open();
        commentsDataSource = new CommentsDataSource(this);
        commentsDataSource.open();
    }

    private AccessToken checkAccessToken() {
        try {
            accessToken = AccessToken.getCurrentAccessToken();
            if(accessToken == null) {
                setContentView(R.layout.loading_panel);
                Thread.sleep(1000);
                System.out.println("News_Feed: 7");
            }
            return AccessToken.getCurrentAccessToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateFriendToFirebase(List<Friend> friends, List<String> currentFriendItems) {
        //Need to check if the we should even update the freinds list

        boolean areUpdated = checkUpdatedFriends(friends, currentFriendItems);
        if(areUpdated) {
            AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200);
            Firebase reference = new Firebase("https://tabsapp.firebaseio.com/Friends");
            Map<String, Object> updatedFriends = new HashMap<String, Object>();
            for(Friend friend: friends) {
                updatedFriends.put(friend.getUser() + "/" + friend.getUserId() + "/isFriend", friend.getIsFriend());
            }
            application.setFromAnotherActivity(true);
            //Need to clear posts and friends because we have updated the friends and posts at this point
            System.out.println("Login: Adding to Clearing posts: " +  application.getPrivateAdapter().getPosts().size());
            application.getPrivateAdapter().getPosts().clear();
//            application.getPrivateAdapter().notifyDataSetChanged();
            System.out.println("Login: Adding to After clearing posts: " + application.getPrivateAdapter().getPosts().size());
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
                    application.getPrivateAdapter().notifyDataSetChanged();
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
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
//                friends.get(i).setIsFriend(currentFriendItems.get(i));
                System.out.println("news_feed: The friend get is friend " + friends.get(i).getName() + " is now: " + friends.get(i).getIsFriend());
                result = true;
            }
        }
        if(result == true) {
            application.getFriendsAdapter().notifyDataSetChanged();
        }
        System.out.println("news_feed: Final Returning Result: " + result);
        return result;
    }


}
