package com.tabs.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;
import com.tabs.database.posts.Post;

import java.security.Provider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by schan on 11/14/15.
 */
public class CreatePost extends AppCompatActivity {

    private FireBaseApplication application;
    private DatabaseQuery databaseQuery;
    private String name;

    //Edit for post
    private EditText post;
    private EditText postTitle;

    //Global variable for privacy
    PrivacyEnum privacy;
    String userId;
    String uniquePostId;

    //For location tracking
    ProviderLocationTracker providerLocationTracker;
    LocationManager locationManager;
    Double latitude;
    Double longitude;

    //Context
    Context context;

    private final String networkProvider = "NETWORK_PROVIDER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.create_post_dialog);
        setContentView(R.layout.create_post);
        application = ((FireBaseApplication) getApplication());
        databaseQuery = new DatabaseQuery(this);


        //Set up action bar
        setupActionBar();
        uniquePostId = UUID.randomUUID().toString();
        if (application.getName() != null && application.getName() != "") {
            name = application.getName();
        }
        if (application.getUserId() != null && application.getUserId() != "") {
            userId = application.getUserId();
        }
        setupActivity(savedInstanceState);
        setupKeyBoard();

        setupPrivacyToggle();
        //First posts are always Public
        privacy = PrivacyEnum.Public;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.create_post_toolbar);
        setSupportActionBar(toolbar);
        //Back bar enabled
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        //Toggle bar enabled
    }

    private void setupKeyBoard() {
        //Pop up keyboard
        postTitle = (EditText) findViewById(R.id.create_post_title);
        post = (EditText) findViewById(R.id.create_post_status);
        postTitle.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(postTitle, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_post_menu, menu);
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
                if (postTitle.getText().length() == 0) {
                    Toast.makeText(CreatePost.this, "Please enter a title.", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (post.getText().length() == 0) {
                    Toast.makeText(CreatePost.this, "Please enter a status.", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if(latitude == null && latitude != 0.0 && longitude == null && longitude != 0.0) {
                    //We cannot get location for whatever reason so we have to throw some sort of error and exit
                    Toast.makeText(CreatePost.this, "There was an error retriving location information for this post. Please check your location services before trying again.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                Post createdPost = new Post("", postTitle.getText().toString(), name, post.getText().toString(), userId, getDateTime(), privacy.toString(), 0);
                Toast.makeText(CreatePost.this, "Successfully posted.", Toast.LENGTH_SHORT).show();
                databaseQuery.savePostToDatabaseReference(createdPost, latitude, longitude);
                Intent intent = new Intent(CreatePost.this, news_feed.class);
                if(intent != null) {
                    startActivity(intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupPrivacyToggle() {
        final RadioGroup privacyToggle = (RadioGroup) findViewById(R.id.privacy_toggle);
        final RadioButton publicToggle = (RadioButton) findViewById(R.id.public_toggle);
        final RadioButton followersToggle = (RadioButton) findViewById(R.id.followers_toggle);

        privacyToggle.check(R.id.public_toggle);

        //Set listener for clicking on toggle
        privacyToggle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.public_toggle) {
                    privacy = PrivacyEnum.Public;
                    publicToggle.setTypeface(Typeface.DEFAULT_BOLD);
                    if(followersToggle.getTypeface() == Typeface.DEFAULT_BOLD) {
                        followersToggle.setTypeface(Typeface.SANS_SERIF);
                    }
                } else {
                    privacy = PrivacyEnum.Following;
                    followersToggle.setTypeface(Typeface.DEFAULT_BOLD);
                    if(publicToggle.getTypeface() == Typeface.DEFAULT_BOLD) {
                        publicToggle.setTypeface(Typeface.SANS_SERIF);
                    }
                }
            }
        });
    }

    private Long getDateTime() {
//        SimpleDateFormat dateFormat = new SimpleDateFormat(
//                "MM/dd/yyyy hh:mm:ss a", Locale.getDefault());
        Date date = new Date();
//        return dateFormat.format(date);
        return -1 * date.getTime();
    }

    private void setupActivity(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if(savedInstanceState.containsKey("userId")) {
                userId = savedInstanceState.getString("userId");
            }
            if(savedInstanceState.containsKey("name")) {
                name = savedInstanceState.getString("name");
            }
            if(savedInstanceState.containsKey("latitude")) {
                latitude = savedInstanceState.getDouble("latitude");
            }
            if(savedInstanceState.containsKey("longitude")) {
                longitude = savedInstanceState.getDouble("longitude");
            }
        } else {
            latitude = Double.valueOf(AndroidUtils.getIntentDouble(getIntent(), "latitude"));
            longitude = Double.valueOf(AndroidUtils.getIntentDouble(getIntent(), "longitude"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("userId", userId);
        savedInstanceState.putString("name", name);
        savedInstanceState.putDouble("latitude", latitude);
        savedInstanceState.putDouble("longitude", longitude);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
