package com.tabs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.client.Firebase;
import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;
import com.tabs.database.users.User;

import java.util.List;

/**
 * Created by schan on 6/28/16.
 */
public class UserProfile extends AppCompatActivity implements PostsTab.onProfileSelectedListener{
    private Firebase firebaseRef = new Firebase("https://tabsapp.firebaseio.com/");
    private FireBaseApplication application;
    private DatabaseQuery databaseQuery;
//    private View progressOverlay;
    private String posterUserId;
    private String posterName;
    private String postStatus;
    private String postTimeStamp;
    private String postTitle;
    private String postId;
    private Toolbar toolbar;
    private Button followersButton;
    private Button followingButton;
    private Button followButton;
    private List<User> following;
    private TabLayout tabLayout;
    SimpleDraweeView profilePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
        application = ((FireBaseApplication) getApplication());
        databaseQuery = new DatabaseQuery(this);
//        progressOverlay = findViewById(R.id.progress_overlay);
        following = application.getFollowingRecyclerViewAdapter().getFollowers();
        setupPostsAndCommentsTabLayout();
//        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
        setupActivity(savedInstanceState);
        profilePictureSetup(posterUserId, posterName);
        setupActionBar();

        followButton = (Button) findViewById(R.id.follow_button);
        //Cannot be our own user
        if(!posterUserId.equals(application.getUserId())) {
            followButton.setVisibility(View.VISIBLE);
        }
        //This part is for sure used but not sure about the bottom
        String [] intentStrings = {posterUserId, posterName, postStatus, postTimeStamp, postTitle};
        TabsUtil.setupProfileView(findViewById(R.id.user_profile_coordinator_layout), "UserProfile", application, intentStrings);
        setupFollowButton(followButton, following);
        if(!application.getUserId().equals(posterUserId)) {
            application.getUserAdapter().setUserId(posterUserId);
            application.getPostsUserHasCommentedOnAdapter().setUserId(posterUserId);
            application.getUserFollowersAdapter().initializeChangedFollowing();
            application.getUserFollowingAdapter().initializeChangedFollowing();
            databaseQuery.getUserPosts(posterUserId);
            databaseQuery.getPostsUserCommentedOn(posterUserId);
            databaseQuery.getUserFollowers(posterUserId);
            databaseQuery.getUserFollowing(posterUserId);
        }
    }

    private void setupFollowButton(final Button button, final List<User> following) {
        if(application.getFollowingRecyclerViewAdapter().containsUserId(posterUserId) != null) {
            setButtonIsFollowing(button);
        } else {
            setButtonIsNotFollowing(button);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = application.getFollowingRecyclerViewAdapter().containsUserId(posterUserId);
                if(user == null) {
                    User newUser = new User();
                    newUser.setUserId(posterUserId);
                    newUser.setName(posterName);
                    newUser.setId(AndroidUtils.generateId());
                    //No need to add this to the adapter database because it is already done for us in the Firebase getFollowing call
                    application.getFollowingRecyclerViewAdapter().getFollowers().add(newUser);
                    databaseQuery.addFollowing(posterUserId);
                    setButtonIsFollowing(button);
                } else {
                    application.getFollowingRecyclerViewAdapter().getFollowers().remove(user);
                    databaseQuery.removeFollowing(posterUserId);
                    setButtonIsNotFollowing(button);
                }
            }
        });
    }

    private void setButtonIsFollowing(Button button) {
        button.setBackgroundResource(R.drawable.following_button_bg);
        button.setTextColor(ContextCompat.getColor(this, R.color.white));
        button.setText("Following");
    }

    private void setButtonIsNotFollowing(Button button) {
        button.setBackgroundResource(R.drawable.follow_button_bg);
        button.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        button.setText("+ Follow");
    }

    private void setupActionBar() {
        toolbar = (Toolbar) findViewById(R.id.comments_appbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(posterName);
        //Back bar enabled
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void profilePictureSetup(String id, String name) {
        DraweeController controller = TabsUtil.getImage(id);
        profilePhoto = (SimpleDraweeView) findViewById(R.id.profile_picture);
        profilePhoto.setController(controller);
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        roundingParams.setBorder(ContextCompat.getColor(this, R.color.white), 10f);
        profilePhoto.getHierarchy().setRoundingParams(roundingParams);
        TextView headerName = (TextView) findViewById(R.id.profile_name);
        headerName.setText(name);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("posterUserId", posterUserId);
        savedInstanceState.putString("posterName", posterName);
        savedInstanceState.putString("postStatus", postStatus);
        savedInstanceState.putString("postTimeStamp", postTimeStamp);
        savedInstanceState.putString("postTitle", postTitle);
        savedInstanceState.putString("postId", postId);
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
                posterName = savedInstanceState.getString("posterName");
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
            if(savedInstanceState.containsKey("postId")) {
                postId = savedInstanceState.getString("postId");
            }
        } else {
            if(getIntent().getExtras() != null) {
                postId = AndroidUtils.getIntentString(getIntent(), "postId");
                posterUserId = AndroidUtils.getIntentString(getIntent(), "posterUserId");
                posterName =  AndroidUtils.getIntentString(getIntent(), "posterName");
                postStatus =  AndroidUtils.getIntentString(getIntent(), "postStatus");
                postTimeStamp = AndroidUtils.getIntentString(getIntent(), "postTimeStamp");
                postTitle = AndroidUtils.getIntentString(getIntent(), "postTitle");
            }
        }
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
    public void onBackPressed() {
        super.onBackPressed();
        application.getUserAdapter().getPosts().clear();
        application.getUserAdapter().notifyDataSetChanged();
        Intent intent = new Intent(UserProfile.this, Comments.class);
        Bundle bundle = new Bundle();
        bundle.putString("postId", postId);
        bundle.putString("posterUserId", posterUserId);
        bundle.putString("posterName", posterName);
        bundle.putString("postStatus", postStatus);
        bundle.putString("postTimeStamp", postTimeStamp);
        bundle.putString("postTitle", postTitle);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void setupPostsAndCommentsTabLayout() {
        tabLayout = (TabLayout) findViewById(R.id.comments_post_tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Posts"));
        tabLayout.addTab(tabLayout.newTab().setText("Comments"));
        final ViewPager viewPager = (ViewPager) findViewById(R.id.comments_posts_pager);
        viewPager.setOffscreenPageLimit(2);
        final PostsCommentsAdapter adapter = new PostsCommentsAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                posterUserId = AndroidUtils.getIntentString(data, "posterUserId");
                posterName = AndroidUtils.getIntentString(data, "posterName");
                postStatus = AndroidUtils.getIntentString(data, "postStatus");
                postTimeStamp = AndroidUtils.getIntentString(data, "postTimeStamp");
                postTitle = AndroidUtils.getIntentString(data, "postTitle");
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    //TODO: check if i even need this...
    @Override
    public void onProfileSelected(int position) {
        // The user selected the headline of an article from the HeadlinesFragment
        // Do something here to display that article

        PostsTab postsTab = (PostsTab)
                getSupportFragmentManager().findFragmentById(R.id.posts_tab);
        if (postsTab != null) {
            String [] intentStrings = {posterUserId, posterName, postStatus, postTimeStamp, postTitle};
            TabsUtil.setupProfileView(postsTab.getView(), "UserProfile", application, intentStrings);
        }
    }
}
