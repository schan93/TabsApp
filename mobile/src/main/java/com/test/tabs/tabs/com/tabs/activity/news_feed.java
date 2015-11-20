package com.test.tabs.tabs.com.tabs.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
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
import com.facebook.login.widget.ProfilePictureView;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsListAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostListAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

import de.hdodenhof.circleimageview.CircleImageView;

public class news_feed extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ListView newsFeedListView;
    private PostListAdapter postListAdapter;
    private List<Post> posts;
    private String[] newsFeedNames = {"Silvia", "Kevin", "Stephen", "Chrisdere", "Jwang", "Nathaneil", "TED  "};

    //View for navigation header
    private NavigationView navigationView;
    //Friends list values
    private ListView friendsList;
    private FriendsListAdapter friendsAdapter;
    //Local Database for storing friends
    private FriendsDataSource datasource;
    private List<Friend> friendItems;
    //Local Database for storing posts
    private PostsDataSource postsDataSource;
    private List<Post> postItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_news_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Profile profile = Profile.getCurrentProfile();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                //parameters.putString("url", );
                bundle.putString("id", AccessToken.getCurrentAccessToken().getUserId());
                bundle.putString("name", profile.getFirstName() + " " + profile.getLastName());
                Intent intent = new Intent(news_feed.this, CreatePost.class);
                if(intent != null) {
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        //Listen for navigation events

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Open DB and get freinds from db.
        datasource = new FriendsDataSource(this);
        datasource.open();
        postsDataSource = new PostsDataSource(this);
        postsDataSource.open();

        //Set things such as facebook profile picture, facebook friends photos, etc.
        drawerSetup(profile.getId(), profile.getFirstName(), profile.getLastName());

        populateFriendsList();

        populateNewsFeedList();
    }

    private void drawerSetup(String id, String firstName, String lastName) {
        System.out.println("profiel id: " + id);
        getImage(id);

        TextView headerName = (TextView) findViewById(R.id.user_name);
        headerName.setText(firstName  + " " + lastName);
    }

    public void getImage(String userId){
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
        Uri uri = Uri.parse("http://graph.facebook.com/" + userId + "/picture?type=normal");
        int width = 100, height = 100;
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(controllerListener)
                .setUri(uri)
                .build();
        SimpleDraweeView draweeView = (SimpleDraweeView) findViewById(R.id.avatarImageView);
        draweeView.setController(controller);
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
        posts = new ArrayList<Post>();


        postListAdapter = new PostListAdapter(this, posts);
        newsFeedListView.setAdapter(postListAdapter);
        if(postsDataSource.isTablePopulated()) {
            System.out.println("Size: " + postsDataSource.getAllPosts().size());
            for (Post i : postsDataSource.getAllPosts()) {
                posts.add(i);
            }
        }
        else{
            System.out.println("Is not populated");
        }
        //postListAdapter.notifyDataSetChanged();
        // ************************************************
    }

    private void populateFriendsList() {
        friendsList = (ListView) findViewById(R.id.friends_list);
        friendItems = new ArrayList<>();

        friendsAdapter = new FriendsListAdapter(this, friendItems);
        friendsList.setAdapter(friendsAdapter);

        System.out.println("Number of friends: " + datasource.getAllFriends().size());
        if(datasource.isTablePopulated()) {
            for (Friend i : datasource.getAllFriends()) {
                friendItems.add(i);
            }
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
