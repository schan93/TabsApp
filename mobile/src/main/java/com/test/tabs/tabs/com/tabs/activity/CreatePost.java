package com.test.tabs.tabs.com.tabs.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
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

    //Global variable for privacy, 0 = public, 1 = private, initialize to private
    Integer privacy = 1;

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
