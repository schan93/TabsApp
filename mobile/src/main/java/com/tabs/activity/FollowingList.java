package com.tabs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;
import com.tabs.database.followers.FollowerRecyclerViewAdapter;
import com.tabs.database.users.User;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by schan on 6/22/16.
 */
public class FollowingList extends AppCompatActivity {

    FireBaseApplication application;
    DatabaseQuery databaseQuery;
    View progressOverlay;
    String posterUserId;
    String posterName;
    String postStatus;
    String postTimeStamp;
    String postTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.follow_list);
        application = ((FireBaseApplication) getApplication());
        databaseQuery = new DatabaseQuery(this);

        //Set up action bar
        setupActionBar();

        progressOverlay = findViewById(R.id.progress_overlay);
//        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
        setupActivity(savedInstanceState);

        //All we need to do is render the page now
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.follow_list);

        if(!posterUserId.equals(application.getUserId())) {
            TabsUtil.populateFollowList(getApplicationContext(), recyclerView, application.getUserFollowingAdapter());
            application.getUserFollowersAdapter().setupFollowersRecyclerView(databaseQuery, getApplicationContext());
        } else {
            TabsUtil.populateFollowList(getApplicationContext(), recyclerView, application.getFollowingRecyclerViewAdapter());
            application.getFollowingRecyclerViewAdapter().setupFollowersRecyclerView(databaseQuery, getApplicationContext());
        }

//        if (progressOverlay.getVisibility() == View.VISIBLE) {
//            AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
//            findViewById(R.id.follow_list).setVisibility(View.VISIBLE);
//        }
    }

    private void setupActionBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.follow_toolbar);
        setSupportActionBar(toolbar);
        //Back bar enabled
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
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
                intent = new Intent(FollowingList.this, news_feed.class);
                Map<String, Boolean> changedFollowing = application.getFollowingRecyclerViewAdapter().getChangedFollowing();
                updateFollowing(changedFollowing, application.getFollowingRecyclerViewAdapter());
                break;
            case "UserProfile":
                bundle.putString("posterUserId", posterUserId);
                bundle.putString("posterName", posterName);
                bundle.putString("postStatus", postStatus);
                bundle.putString("postTimeStamp", postTimeStamp);
                bundle.putString("postTitle", postTitle);
                Map<String, Boolean> changedUserFollowing = application.getUserFollowingAdapter().getChangedFollowing();
                updateFollowing(changedUserFollowing, application.getFollowingRecyclerViewAdapter());
                intent = new Intent(FollowingList.this, UserProfile.class);
                break;
            default:
                intent = new Intent(FollowingList.this, news_feed.class);
        }
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void updateFollowing(Map<String, Boolean> changedFollowing, FollowerRecyclerViewAdapter adapter) {
        Iterator it = changedFollowing.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if((Boolean)pair.getValue() == false) {
                User user = adapter.containsUserId((String)pair.getKey());
                adapter.getFollowers().remove(user);
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        changedFollowing.clear();
    }
}
