package com.tabs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.schan.tabs.R;
import com.tabs.database.databaseQuery.DatabaseQuery;
import com.tabs.database.followers.FollowerRecyclerViewAdapter;
import com.tabs.database.users.User;
import com.tabs.utils.AndroidUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by schan on 6/22/16.
 */
public class FollowingListActivity extends AppCompatActivity {

    FireBaseApplication application;
    DatabaseQuery databaseQuery;
    View progressOverlay;
    private String posterUserId;
    private String posterName;
    private String postStatus;
    private Long postTimeStamp;
    private String postTitle;
    private String userProfileId;
    private View layoutView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.follow_list);
        application = ((FireBaseApplication) getApplication());
        databaseQuery = new DatabaseQuery(this);
        layoutView = findViewById(R.id.follow_list_layout_id);

        //Set up action bar
        setupActionBar();

        progressOverlay = findViewById(R.id.progress_overlay);
//        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
        setupActivity(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        progressOverlay.setVisibility(View.VISIBLE);
        if(!userProfileId.equals(application.getUserId())) {
            application.getUserFollowersAdapter().setupFollowersRecyclerView(databaseQuery, getApplicationContext(), posterUserId, posterName, postStatus, postTimeStamp, postTitle, userProfileId);
            databaseQuery.populateFollowList(userProfileId,layoutView, application.getUserFollowingAdapter(), getApplicationContext(), "following", progressOverlay);
        } else {
            application.getFollowingRecyclerViewAdapter().setupFollowersRecyclerView(databaseQuery, getApplicationContext(), posterUserId, posterName, postStatus, postTimeStamp, postTitle, userProfileId);
            databaseQuery.populateFollowList(userProfileId,layoutView, application.getFollowingRecyclerViewAdapter(), getApplicationContext(), "following", progressOverlay);
        }
    }

    private void setupActionBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.follow_toolbar);
        setSupportActionBar(toolbar);
        //Back bar enabled
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
        }
        //Toggle bar enabled
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
        savedInstanceState.putLong("postTimeStamp", AndroidUtils.getIntentLong(getIntent(), "postTimeStamp"));
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
                posterName = savedInstanceState.getString("posterName");
            }
            if(savedInstanceState.containsKey("postStatus")) {
                postStatus = savedInstanceState.getString("postStatus");
            }
            if(savedInstanceState.containsKey("postTimeStamp")) {
                postTimeStamp = savedInstanceState.getLong("postTimeStamp");
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
                postTimeStamp = AndroidUtils.getIntentLong(getIntent(), "postTimeStamp");
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
                intent = new Intent(FollowingListActivity.this, MainActivity.class);
                Map<String, Boolean> changedFollowing = application.getFollowingRecyclerViewAdapter().getChangedFollowing();
                updateFollowing(changedFollowing, application.getFollowingRecyclerViewAdapter());
                break;
            case "UserProfileActivity":
                bundle.putString("userProfileId", userProfileId);
                bundle.putString("posterUserId", posterUserId);
                bundle.putString("posterName", posterName);
                bundle.putString("postStatus", postStatus);
                bundle.putLong("postTimeStamp", postTimeStamp);
                bundle.putString("postTitle", postTitle);
                Map<String, Boolean> changedUserFollowing = application.getUserFollowingAdapter().getChangedFollowing();
                updateFollowing(changedUserFollowing, application.getUserFollowingAdapter());
                intent = new Intent(FollowingListActivity.this, UserProfileActivity.class);
                break;
            default:
                intent = new Intent(FollowingListActivity.this, MainActivity.class);
        }
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void updateFollowing(Map<String, Boolean> changedFollowing, FollowerRecyclerViewAdapter adapter) {
        Iterator it = changedFollowing.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if(!((Boolean) pair.getValue())) {
                User user = adapter.containsUserId((String)pair.getKey());
                adapter.getFollowers().remove(user);
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        changedFollowing.clear();
    }
}
