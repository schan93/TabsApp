package com.tabs.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.schan.tabs.R;
import com.tabs.database.databaseQuery.DatabaseQuery;
import com.tabs.database.posts.Post;
import com.tabs.enums.PrivacyEnum;
import com.tabs.utils.AndroidUtils;

/**
 * Created by schan on 11/14/15.
 */
public class CreatePostActivity extends AppCompatActivity {

    private FireBaseApplication application;
    private DatabaseQuery databaseQuery;
    private String name;

    //Edit for post
    private EditText post;
    private EditText postTitle;

    //Global variable for privacy
    private PrivacyEnum privacy;
    private String userId;

    private Double latitude;
    private Double longitude;

    //Context
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_post);
        application = ((FireBaseApplication) getApplication());
        databaseQuery = new DatabaseQuery(this);

        //Set up action bar
        setupActionBar();
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
                    Toast.makeText(CreatePostActivity.this, "Please enter a title.", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (post.getText().length() == 0) {
                    Toast.makeText(CreatePostActivity.this, "Please enter a status.", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if(latitude == null && latitude != 0.0 && longitude == null && longitude != 0.0) {
                    //We cannot get location for whatever reason so we have to throw some sort of error and exit
                    Toast.makeText(CreatePostActivity.this, "There was an error retriving location information for this post. Please check your location services before trying again.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                Post createdPost = new Post("", postTitle.getText().toString(), name, post.getText().toString(), userId, AndroidUtils.getDateTime(), privacy.toString(), 0);
                Toast.makeText(CreatePostActivity.this, "Successfully posted.", Toast.LENGTH_SHORT).show();
                databaseQuery.savePostToDatabaseReference(createdPost, latitude, longitude);
                Intent intent = new Intent(CreatePostActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupPrivacyToggle() {
        final RadioGroup privacyToggle = (RadioGroup) findViewById(R.id.privacy_toggle);
        final RadioButton publicToggle = (RadioButton) findViewById(R.id.public_toggle);
        final RadioButton followersToggle = (RadioButton) findViewById(R.id.followers_toggle);

        if(privacyToggle != null && publicToggle != null && followersToggle != null) {
            privacyToggle.check(R.id.public_toggle);
            //Set listener for clicking on toggle
            privacyToggle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.public_toggle) {
                        privacy = PrivacyEnum.Public;
                        publicToggle.setTypeface(Typeface.DEFAULT_BOLD);
                        if (followersToggle.getTypeface() == Typeface.DEFAULT_BOLD) {
                            followersToggle.setTypeface(Typeface.SANS_SERIF);
                        }
                    } else {
                        privacy = PrivacyEnum.Following;
                        followersToggle.setTypeface(Typeface.DEFAULT_BOLD);
                        if (publicToggle.getTypeface() == Typeface.DEFAULT_BOLD) {
                            publicToggle.setTypeface(Typeface.SANS_SERIF);
                        }
                    }
                }
            });
        }
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
            latitude = AndroidUtils.getIntentDouble(getIntent(), "latitude");
            longitude = AndroidUtils.getIntentDouble(getIntent(), "longitude");
            name = application.getName();
            userId = application.getUserId();
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
