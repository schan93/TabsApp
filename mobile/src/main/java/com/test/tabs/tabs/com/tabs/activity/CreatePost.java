package com.test.tabs.tabs.com.tabs.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.parse.ParseObject;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by schan on 11/14/15.
 */
public class CreatePost extends AppCompatActivity {


    //Local Database for storing posts
    private PostsDataSource datasource;

    //Edit for post
    private EditText post;

    //Global variable for privacy, 0 = public, 1 = private, initialize to private
    Integer privacy;

    // Resgistration Id from GCM
    private static final String PREF_GCM_REG_ID = "PREF_GCM_REG_ID";
    private SharedPreferences prefs;
    // Your project number and web server url. Please change below.
    private static final String GCM_SENDER_ID = "213033849274";
    private static final String WEB_SERVER_URL = "jdbc:google:mysql://tabs-1124:tabs-backend/tabs_backend";

    Button registerBtn;
    TextView regIdView;

    private static final int ACTION_PLAY_SERVICES_DIALOG = 100;
    protected static final int MSG_REGISTER_WITH_GCM = 101;
    protected static final int MSG_REGISTER_WEB_SERVER = 102;
    protected static final int MSG_REGISTER_WEB_SERVER_SUCCESS = 103;
    protected static final int MSG_REGISTER_WEB_SERVER_FAILURE = 104;
    private String gcmRegId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_post);

        //Set up action bar
        setupActionBar();

        //Open posts database for storage
        datasource = new PostsDataSource(this);
        datasource.open();

        //Pop up keyboard
        post = (EditText) findViewById(R.id.type_status);
        post.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(post, InputMethodManager.SHOW_IMPLICIT);

        //First posts are always private
        privacy = 1;
    }

    private void setupActionBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.create_post_toolbar);
        setSupportActionBar(toolbar);
        //Back bar enabled
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        //Toggle bar enabled
        ActionBar actionBar = getSupportActionBar();
//        actionBar.setCustomView(R.layout.privacy_toggle_layout);
//        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
//        final RadioGroup privacyToggle = (RadioGroup) findViewById(R.id.privacy_toggle);
//        final RadioButton publicToggle = (RadioButton) findViewById(R.id.public_toggle);
//        final RadioButton privateToggle = (RadioButton) findViewById(R.id.private_toggle);
//
//        privateToggle.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
//
//        //Set listener for clicking on toggle
//        privacyToggle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                if(checkedId == R.id.public_toggle){
//                    privacy = 0;
//                    publicToggle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
//                    privateToggle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
//                    System.out.println("Toggled public");
//                }
//                else {
//                    privateToggle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
//                    publicToggle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
//                    System.out.println("Toggled private");
//                    privacy = 1;
//                }
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.create_post_menu, menu);
//        return true;
        // Get the action view used in your toggleservice item
        getMenuInflater().inflate(R.menu.create_post_menu, menu);
        MenuItem toggleservice = menu.findItem(R.id.toggle_test);
        RadioGroup privacyToggle = (RadioGroup) getLayoutInflater().inflate(R.layout.privacy_toggle_layout, null);
        toggleservice.setActionView(privacyToggle);

        final RadioButton publicToggle = (RadioButton) toggleservice.getActionView().findViewById(R.id.public_toggle);
        final RadioButton privateToggle = (RadioButton) toggleservice.getActionView().findViewById(R.id.private_toggle);

        privateToggle.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

        //Set listener for clicking on toggle
        privacyToggle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.public_toggle){
                    privacy = 0;
                    publicToggle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    privateToggle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    System.out.println("Toggled public");
                }
                else {
                    privateToggle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    publicToggle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    System.out.println("Toggled private");
                    privacy = 1;
                }
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //What happens if you click back
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.send_post:
                String postId = getIntent().getExtras().getString("id");
                String name = getIntent().getExtras().getString("name");
                if(post.getText().length() == 0){
                    Toast.makeText(CreatePost.this, "Please enter something in the post.", Toast.LENGTH_SHORT).show();
                }

                System.out.println("Privacy: " + privacy);
                Location location = LocationService.getLastLocation();
                Post createdPost = datasource.createPost(postId, post.getText().toString(), name, privacy, location.getLatitude(), location.getLongitude());
                Toast.makeText(CreatePost.this, "Successfully posted.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(CreatePost.this, news_feed.class);

                savePostInCloud(createdPost, location);


                if(intent != null) {
                    startActivity(intent);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void savePostInCloud(Post post, Location location){
        //Save post to Parse Cloud DB as a 'Post' object
        ParseObject postObj = new ParseObject("Post");
        postObj.put("postId", post.getId());
        postObj.put("postStatus", post.getStatus());
        postObj.put("posterName", post.getName());
        postObj.put("privacy", privacy);
        postObj.put("latitude", location.getLatitude());
        postObj.put("longitude", location.getLongitude());
        postObj.saveInBackground();

        //** Send push notifications to friends **
        //Send targeted push notifications to friends
            //get friends from current user
            //get installation id from current user
        //Push notification
        //Subscribe current user to channel

    }


}
