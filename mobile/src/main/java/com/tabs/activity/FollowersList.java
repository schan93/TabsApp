package com.tabs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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

        progressOverlay = findViewById(R.id.progress_overlay);
//        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
        setupActivity(savedInstanceState);
        //Set up action bar
        setupActionBar();

        //All we need to do is render the page now
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.follow_list);


        Button followButton = (Button) findViewById(R.id.follower_button);
        if(!posterUserId.equals(application.getUserId())) {
            TabsUtil.populateFollowList(getApplicationContext(), recyclerView, application.getUserFollowersAdapter());
            application.getUserFollowersAdapter().setupFollowersRecyclerView(databaseQuery, getApplicationContext());
        } else {
            TabsUtil.populateFollowList(getApplicationContext(), recyclerView, application.getFollowersRecyclerViewAdapter());
            application.getFollowersRecyclerViewAdapter().setupFollowersRecyclerView(databaseQuery, getApplicationContext());

        }

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
                intent = new Intent(FollowersList.this, news_feed.class);
                Map<String, Boolean> changedFollowing = application.getFollowersRecyclerViewAdapter().getChangedFollowing();
                updateFollowing(changedFollowing, application.getFollowingRecyclerViewAdapter());
                break;
            case "UserProfile":
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
        startActivity(intent);
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
