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
import android.support.v7.widget.SimpleItemAnimator;
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

import com.facebook.FacebookSdk;
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
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.parse.ParseObject;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.Database.DatabaseQuery;
import com.test.tabs.tabs.com.tabs.database.comments.Comment;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsDataSource;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

public class news_feed extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Firebase reference
    private Firebase firebaseRef = new Firebase("https://tabsapp.firebaseio.com/");
    //Progress overlay
    View progressOverlay;
    String userId;
    Handler handler;
    FireBaseApplication application;
    List<String> currentFriendItems = new ArrayList<String>();
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
        System.out.println("CREATING THE NEWS FEED ACTIVITY BEFORE GOING INTO FRAGMENT");
        setContentView(R.layout.activity_main);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        checkSavedState(savedInstanceState);
        application =  (FireBaseApplication) getApplication();
        databaseQuery = new DatabaseQuery(this);
        progressOverlay = findViewById(R.id.progress_overlay);
        handler = new Handler();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(application.getName() != null && application.getName() != "") {
            name = application.getName();
        }
        if(application.getUserId() != null && application.getUserId() != "") {
            System.out.println("News_feed: seeing if the user id already exists in the application: " + application.getUserId());
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
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                System.out.println("Drawer closed");
                List<Friend> friends = application.getFriendsRecyclerViewAdapter().getFriends();
                updateFriendToFirebase(friends, currentFriendItems);
            }
        };
        System.out.println("news_feed: toolbar: " + toolbar);
        System.out.println("news_feed: Drawer: " + drawer);
        System.out.println("news_feed: toggle: " + toggle);
        drawer.setDrawerListener(toggle);

        View layout = (View) findViewById(R.id.app_bar_main);

        tabLayout = (TabLayout) layout.findViewById(R.id.tab_layout);
        System.out.println("news_feed: Tab Layout: " + tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Public"));
        tabLayout.addTab(tabLayout.newTab().setText("Friends"));
        tabLayout.addTab(tabLayout.newTab().setText("My Tabs"));
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

        drawerSetup(userId, name);
        getFriends(userId);

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
        RecyclerView rv = (RecyclerView) findViewById(R.id.friends_list);
        RecyclerView.ItemAnimator animator = rv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setAdapter(application.getFriendsRecyclerViewAdapter());
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
            if(tabLayout.getSelectedTabPosition() == 1) {
                AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200);
            }
            Firebase reference = new Firebase("https://tabsapp.firebaseio.com/Friends");
            Map<String, Object> updatedFriends = new HashMap<String, Object>();
            for(Friend friend: friends) {
                updatedFriends.put(friend.getUser() + "/" + friend.getId() + "/isFriend", friend.getIsFriend());
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
                    if(progressOverlay.getVisibility() == View.VISIBLE) {
                        AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
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
//                friends.get(i).setIsFriend(currentFriendItems.get(i));
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

    private void checkSavedState(Bundle savedInstanceState) {
        if(savedInstanceState != null) {

        }
        else {

        }
    }

    private void setupActivity(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            System.out.println("News_feed: Setting up activity: " + userId);
            // Restore value of members from saved state
            if(savedInstanceState.containsKey("userId")) {
                userId = savedInstanceState.getString("userId");
                System.out.println("News_feed: Setting userId: " + userId);
            }
            if(savedInstanceState.containsKey("name")) {
                name = savedInstanceState.getString("name");
                System.out.println("News_feed: Setting name: " + name);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("userId", userId);
        savedInstanceState.putString("name", name);
        System.out.println("News_feed: Saving user id: " + userId);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void getFriends(final String userId) {
        progressOverlay = findViewById(R.id.progress_overlay);
        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
        Firebase friendsRef = firebaseRef.child("Friends/" + userId);
        friendsRef.keepSynced(true);
        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot friendSnapShot : dataSnapshot.getChildren()) {
                    Friend friend = friendSnapShot.getValue(Friend.class);
                    String userId = friend.getUserId();
                    List<Friend> friends = application.getFriendsRecyclerViewAdapter().getFriends();
                    if (application.getFriendsRecyclerViewAdapter().containsId(friends, userId) == null) {
                        System.out.println("login2: Adding Friend " + friend.getName() + " to array");
                        application.getFriendsRecyclerViewAdapter().getFriends().add(friend);
                    }
                }
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                }
                populateFriendsList(userId);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        friendsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Friend newFriend = dataSnapshot.getValue(Friend.class);
                List<Friend> friends = application.getFriendsRecyclerViewAdapter().getFriends();
                if (application.getFriendsRecyclerViewAdapter().containsId(friends, newFriend.getUserId()) == null) {
                    application.getFriendsRecyclerViewAdapter().getFriends().add(newFriend);
                    application.getFriendsRecyclerViewAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Friend changedFriend = dataSnapshot.getValue(Friend.class);
                int length = application.getFriendsRecyclerViewAdapter().getItemCount();
                for (int i = 0; i < length; i++) {
                    if (application.getFriendsRecyclerViewAdapter().getFriends().get(i).getId().equals(changedFriend.getId())) {
                        application.getFriendsRecyclerViewAdapter().getFriends().set(i, changedFriend);
                    }
                }
                application.getFriendsRecyclerViewAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Friend removedFriend = dataSnapshot.getValue(Friend.class);
                int length = application.getFriendsRecyclerViewAdapter().getItemCount();
                for (int i = 0; i < length; i++) {
                    if (application.getFriendsRecyclerViewAdapter().getFriends().get(i).getId().equals(removedFriend.getId())) {
                        application.getFriendsRecyclerViewAdapter().getFriends().remove(i);
                    }
                }
                application.getFriendsRecyclerViewAdapter().notifyDataSetChanged();
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
