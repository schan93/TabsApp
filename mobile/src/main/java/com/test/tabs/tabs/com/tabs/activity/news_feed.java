package com.test.tabs.tabs.com.tabs.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
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
    private CardView newsFeedCardView;
    private PostRecyclerViewAdapter postListAdapter;
    private List<Post> posts;

    //Firebase reference
    private Firebase firebaseRef = new Firebase("https://tabsapp.firebaseio.com/");

    //View for navigation header
    private NavigationView navigationView;
    //Friends list values
    private ListView friendsList;
    private FriendsListAdapter friendsAdapter;
    //Local Database for storing friends
    private FriendsDataSource datasource;
    private List<Friend> friendItems;
    private List<Friend> friendItemsDifference;
    //Local Database for storing posts
    private PostsDataSource postsDataSource;
    private CommentsDataSource commentsDataSource;
    private List<Post> postItems;
    //Adapter for posts
    PostRecyclerViewAdapter adapter;
    //Progress overlay
    View progressOverlay;

    String userId;

    Activity activityContext;

    Handler handler;

    //To detect if any new freinds are added
    boolean newFriendAdded = false;

    public news_feed(){

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);
        progressOverlay = findViewById(R.id.progress_overlay);
        handler = new Handler();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(!FacebookSdk.isInitialized()){
            FacebookSdk.sdkInitialize(getApplicationContext());
        }
        final Profile profile = Profile.getCurrentProfile();
        userId = AccessToken.getCurrentAccessToken().getUserId();

        //Open DB and get freinds from db & posts.
        datasource = new FriendsDataSource(this);
        datasource.open();
        postsDataSource = new PostsDataSource(this);
        postsDataSource.open();
        commentsDataSource = new CommentsDataSource(this);
        commentsDataSource.open();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("name", profile.getFirstName() + " " + profile.getLastName());
                Intent intent = new Intent(news_feed.this, CreatePost.class);
                if(intent != null) {
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        //Listen for navigation events
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                //Update freinds into firebase reference. Set transparency of loading panel to transparent
                AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200);

                System.out.println("Visible");
                try {
                    System.out.println("Start");
                    for (Friend i : datasource.getAllFriends(userId)) {
                        System.out.println("Friend: " + i.getName());
                        updateFriendToFirebase(i);
                        getPostsFromFriends(i);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            populateMyTabs(userId);
                            populatePrivateFeed(userId);
                            populatePublicFeed();
                            AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                        }

                    });
                }

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
                System.out.println("Tab: " + tab.getPosition());
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

        drawerSetup(userId, profile.getFirstName(), profile.getLastName());

        populateFriendsList(userId);

        //populateNewsFeedList();
    }

    public void populatePublicFeed(){
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_public_feed);
        Location location = LocationService.getLastLocation();
        //Set a 24140.2 meter, or a 15 mile radius.
        adapter = new PostRecyclerViewAdapter(postsDataSource.getAllPublicPosts(location.getLatitude(), location.getLongitude(), 24140.2), getApplicationContext(), true);
        rv.setAdapter(adapter);
    }

    private void populateMyTabs(String userId){
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_my_tabs_feed);
        adapter = new PostRecyclerViewAdapter(postsDataSource.getPostsByUser(userId), getApplicationContext(), false);
        rv.setAdapter(adapter);
    }

    private void populatePrivateFeed(String userId){
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_private_feed);
        List<Friend> friends = datasource.getAllAddedFriends(userId);
        adapter = new PostRecyclerViewAdapter(postsDataSource.getPostsByFriends(friends), getApplicationContext(), false);
        rv.setAdapter(adapter);
    }

    private void getPostsFromFriends(Friend friend){
        System.out.println("Friend Test User Id: " + friend.getUserId());
        firebaseRef.child("Posts/" + friend.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapShot : snapshot.getChildren()) {
                    Post post = postSnapShot.getValue(Post.class);
                    String id = post.getId();
                    String name = post.getName();
                    String status = post.getStatus();
                    String posterUserId = post.getPosterUserId();
                    String timeStamp = post.getTimeStamp();
                    Integer privacy = post.getPrivacy();
                    Double latitude = post.getLatitude();
                    Double longitude = post.getLongitude();
                    Post newPost = postsDataSource.getPost(id);
                    System.out.println("New Post Id Test: " + id);
                    if (newPost == null) {
                        Post createdPost = postsDataSource.createPostFromFireBase(id, posterUserId, status, timeStamp, name, privacy, latitude, longitude);
                        System.out.println("Created Post Id: " + createdPost.getId());
//                        savePostToFirebase(newPost);
                    }
                    getComments(id);
                    System.out.println("Done getting stuff from friend: " + posterUserId);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void getComments(String postId) {
        //TODO: Query longitude and latitude by 15 mile distance
        firebaseRef.child("Comments/" + postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot commentSnapShot : snapshot.getChildren()) {
                    Comment comment = commentSnapShot.getValue(Comment.class);
                    String id = comment.getId();
                    String postId = comment.getPostId();
                    String commenter = comment.getCommenter();
                    String commentText = comment.getComment();
                    String commenterUserId = comment.getCommenterUserId();
                    String timeStamp = comment.getTimeStamp();
                    Comment newComment = commentsDataSource.getComment(id);
                    if (newComment == null) {
                        commentsDataSource.createCommentFromFirebase(id, postId, commenter, commentText, commenterUserId, timeStamp);
                        saveCommentToFirebase(newComment);
                    }
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    /**
     * This method is designed to save a Post to Firebase database.
     * @param post
     */
    private void savePostToFirebase(Post post) {
        firebaseRef.child("Posts/" + post.getPosterUserId() + "/" + post.getId()).setValue(post);
    }

    /**
     * This method is designed to save a Comment to Firebase database.
     * @param comment
     */
    private void saveCommentToFirebase(Comment comment) {
        firebaseRef.child("Comments/" + comment.getPostId()).setValue(comment);
    }

    private void updateFriendToFirebase(Friend friend) {
        Map<String, Object> isFriend = new HashMap<String, Object>();
        isFriend.put("isFriend", friend.getIsFriend());
        firebaseRef.child("Friends/" + friend.getUser() + "/" + friend.getUserId()).updateChildren(isFriend);
    }

    @Override
    public void onResume(){
        super.onResume();
        LocationService.getLocationManager(this);
    }

    private void drawerSetup(String id, String firstName, String lastName) {
        DraweeController controller = getImage(id);
        SimpleDraweeView draweeView = (SimpleDraweeView) findViewById(R.id.avatarImageView);
        draweeView.setController(controller);
        TextView headerName = (TextView) findViewById(R.id.user_name);
        headerName.setText(firstName + " " + lastName);
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

        friendsAdapter = new FriendsListAdapter(this, friendItems);
        friendsList.setAdapter(friendsAdapter);
        if(datasource.isTablePopulated()) {
            System.out.println("Friends data source is populated");
            for (Friend i : datasource.getAllFriends(userId)) {
                System.out.println("Friend name: " + i.getName());
                friendItems.add(i);
            }
        }
    }

//
//    class UpdateNewsFeedListTask extends AsyncTask<Void,String, Void>
//    {
//        //ArrayAdapter<String> news_feed_adapter;
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            //TODO keep array that holds strings up to date
//            //news_feed_adapter = (ArrayAdapter<String>)newsFeedListView.getAdapter();
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            //for(String Name : newsFeedString){
//            //    //will invoke onProgressUpdate
//            //    publishProgress(Name);
//            //}
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(String... values) {
//            super.onProgressUpdate(values);
//            //add input into adapter
//            //news_feed_adapter.add(values[0]);
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        // Logs 'install' and 'app activate' App Events.
//        AppEventsLogger.activateApp(this);
//  //      adapter.notifyDataSetChanged();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        // Logs 'app deactivate' App Event.
//        AppEventsLogger.deactivateApp(this);
//    }
}
