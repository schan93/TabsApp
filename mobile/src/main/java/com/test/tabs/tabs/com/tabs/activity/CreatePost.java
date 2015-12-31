package com.test.tabs.tabs.com.tabs.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by schan on 11/14/15.
 */
public class CreatePost extends AppCompatActivity {


    //Local Database for storing posts
    private PostsDataSource datasource;

    //Edit for post
    private EditText post;

    //Global variable for privacy
    Integer privacy = 0;

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
    }

    private void setupActionBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.create_post_toolbar);
        setSupportActionBar(toolbar);
        //Back bar enabled
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //Toggle bar enabled
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.privacy_toggle_layout);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
        ToggleButton privacyToggle = (ToggleButton) findViewById(R.id.actionbar_service_toggle);

        //Set listener for clicking on toggle
        privacyToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled, store into DB that it is public, meaning displayed to everyone
                    privacy = 0;
                } else {
                    // The toggle is disabled, store into DB that it is private, meaning only displayed to friends
                    privacy = 1;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_post_menu, menu);
        return true;
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

                datasource.createPost(postId, post.getText().toString(), name, privacy);
                Toast.makeText(CreatePost.this, "Successfully posted.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(CreatePost.this, news_feed.class);
                if(intent != null) {
                    startActivity(intent);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }




}
