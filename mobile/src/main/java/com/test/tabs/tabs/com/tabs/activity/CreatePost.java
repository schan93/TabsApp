package com.test.tabs.tabs.com.tabs.activity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.Database.DatabaseQuery;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

import java.text.SimpleDateFormat;
import java.util.Date;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_post);
        application = ((FireBaseApplication) getApplication());
        databaseQuery = new DatabaseQuery(this);

        //Set up action bar
        setupActionBar();
        uniquePostId = UUID.randomUUID().toString();
        if(application.getName() != null && application.getName() != "") {
            name = application.getName();
        }
        if(application.getUserId() != null && application.getUserId() != "") {
            userId = application.getUserId();
        }
        setupActivity(savedInstanceState);
        setupKeyBoard();

        setupPrivacyToggle();
        //First posts are always private
        privacy = PrivacyEnum.Friends;
    }

    private void setupActionBar(){
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
        post = (EditText) findViewById(R.id.create_post_status) ;
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
                if(postTitle.getText().length() == 0){
                    Toast.makeText(CreatePost.this, "Please enter a title.", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if(postTitle.getText().length() == 0) {
                    Toast.makeText(CreatePost.this, "Please enter a status.", Toast.LENGTH_SHORT).show();
                    return true;
                }
                Location location = LocationService.getLastLocation();
                Post createdPost = new Post("", postTitle.getText().toString(), name, post.getText().toString(), userId, getDateTime(), privacy, Double.toString(location.getLatitude()), Double.toString(location.getLongitude()), 0);
                Toast.makeText(CreatePost.this, "Successfully posted.", Toast.LENGTH_SHORT).show();
                databaseQuery.savePostToFirebase(createdPost);
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
        RadioGroup privacyToggle = (RadioGroup) findViewById(R.id.privacy_toggle);
        final RadioButton publicToggle = (RadioButton) findViewById(R.id.public_toggle);
        final RadioButton privateToggle = (RadioButton) findViewById(R.id.private_toggle);
        privateToggle.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

        //Set listener for clicking on toggle
        privacyToggle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.public_toggle) {
                    privacy = PrivacyEnum.Public;
                    publicToggle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    privateToggle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    System.out.println("Toggled public");
                } else {
                    privateToggle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    publicToggle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    System.out.println("Toggled private");
                    privacy = PrivacyEnum.Friends;
                }
            }
        });
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "MM/dd/yyyy hh:mm:ss a", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
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
