package com.tabs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;
import com.tabs.database.followers.FollowerRecyclerViewAdapter;
import com.tabs.database.users.User;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by schan on 6/22/16.
 */
public class FollowersList extends AppCompatActivity {
    FireBaseApplication application;
    DatabaseQuery databaseQuery;
    View progressOverlay;
    private String posterUserId;
    private String posterName;
    private String postStatus;
    private String postTimeStamp;
    private String postTitle;
    private String userProfileId;
    View layoutView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.follow_list);
        layoutView = findViewById(R.id.follow_list_layout_id);
        application = ((FireBaseApplication) getApplication());
        databaseQuery = new DatabaseQuery(this);

        progressOverlay = findViewById(R.id.progress_overlay);
//        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
        setupActivity(savedInstanceState);
        //Set up action bar
        setupActionBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!userProfileId.equals(application.getUserId())) {
            application.getUserFollowersAdapter().setupFollowersRecyclerView(databaseQuery, getApplicationContext());
            databaseQuery.populateFollowList(userProfileId, layoutView, application.getUserFollowersAdapter(), getApplicationContext(), "followers");
        } else {
            application.getFollowersRecyclerViewAdapter().setupFollowersRecyclerView(databaseQuery, getApplicationContext());
            databaseQuery.populateFollowList(userProfileId, layoutView, application.getFollowersRecyclerViewAdapter(), getApplicationContext(), "followers");
        }
    }

    private void setupActionBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.follow_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("userProfileId", AndroidUtils.getIntentString(getIntent(), "userProfileId"));
        savedInstanceState.putString("posterUserId", AndroidUtils.getIntentString(getIntent(), "posterUserId"));
        savedInstanceState.putString("posterName", AndroidUtils.getIntentString(getIntent(), "posterName"));
        savedInstanceState.putString("postStatus", AndroidUtils.getIntentString(getIntent(), "postStatus"));
        savedInstanceState.putString("postTimeStamp", AndroidUtils.getIntentString(getIntent(), "postTimeStamp"));
        savedInstanceState.putString("postTitle", AndroidUtils.getIntentString(getIntent(), "postTitle"));
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupActivity(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            if(savedInstanceState.containsKey("userProfileId")) {
                userProfileId = savedInstanceState.getString("userProfileId");
            }
            if(savedInstanceState.containsKey("posterUserId")) {
                posterUserId = savedInstanceState.getString("posterUserId");
            }
            if(savedInstanceState.containsKey("posterName")) {
                posterName = savedInstanceState.getString("posternName");
            }
            if(savedInstanceState.containsKey("postStatus")) {
                postStatus = savedInstanceState.getString("postStatus");
            }
            if(savedInstanceState.containsKey("postTimeStamp")) {
                postTimeStamp = savedInstanceState.getString("postTimeStamp");
            }
            if(savedInstanceState.containsKey("postTitle")) {
                postTitle = savedInstanceState.getString("postTitle");
            }
        } else {
            if(getIntent().getExtras() != null) {
                userProfileId = AndroidUtils.getIntentString(getIntent(), "userProfileId");
                posterUserId = AndroidUtils.getIntentString(getIntent(), "posterUserId");
                posterName =  AndroidUtils.getIntentString(getIntent(), "posterName");
                postStatus =  AndroidUtils.getIntentString(getIntent(), "postStatus");
                postTimeStamp = AndroidUtils.getIntentString(getIntent(), "postTimeStamp");
                postTitle = AndroidUtils.getIntentString(getIntent(), "postTitle");
            }
        }
    }

    @Override
    public void onBackPressed() {
        Bundle bundle = new Bundle();
        String parentClass = AndroidUtils.getIntentString(getIntent(), "parentClass");
        Intent intent;
        switch(parentClass) {
            case "Profile":
                intent = new Intent(FollowersList.this, news_feed.class);
                Map<String, Boolean> changedFollowing = application.getFollowersRecyclerViewAdapter().getChangedFollowing();
                updateFollowing(changedFollowing, application.getFollowingRecyclerViewAdapter());
                break;
            case "UserProfile":
                bundle.putString("userProfileId", userProfileId);
                bundle.putString("posterUserId", posterUserId);
                bundle.putString("posterName", posterName);
                bundle.putString("postStatus", postStatus);
                bundle.putString("postTimeStamp", postTimeStamp);
                bundle.putString("postTitle", postTitle);
                Map<String, Boolean> changedUserFollowing = application.getUserFollowersAdapter().getChangedFollowing();
                updateFollowing(changedUserFollowing, application.getUserFollowingAdapter());
                intent = new Intent(FollowersList.this, UserProfile.class);
                break;
            default:
                intent = new Intent(FollowersList.this, news_feed.class);
        }
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void updateFollowing(Map<String, Boolean> changedFollowing, FollowerRecyclerViewAdapter adapter) {
        Iterator it = changedFollowing.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if((Boolean)pair.getValue() == false) {
                User user = adapter.containsUserId((String)pair.getKey());
                if(user != null) {
                    adapter.getFollowers().remove(user);
                }
            } else {
                User user = adapter.containsUserId((String)pair.getKey());
                if(user == null) {
                    user = new User();
                    user.setUserId((String)pair.getKey());
                    user.setId(AndroidUtils.generateId());
                    user.setName(posterName);
                    adapter.getFollowers().add(user);
                }
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        changedFollowing.clear();
    }
}
