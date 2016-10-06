package com.tabs.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.schan.tabs.R;
import com.tabs.database.databaseQuery.DatabaseQuery;
import com.tabs.database.users.User;
import com.tabs.utils.AndroidUtils;
import com.tabs.utils.TabsUtil;

/**
 * Created by schan on 6/28/16.
 */
public class UserProfileActivity extends AppCompatActivity {
    private FireBaseApplication application;
    private DatabaseQuery databaseQuery;
    private String posterUserId;
    private String posterName;
    private String postStatus;
    private String postTimeStamp;
    private String postTitle;
    private String postId;
    private Toolbar toolbar;
    private Button followButton;
    private View progressOverlay;
    private View layoutView;
    private SimpleDraweeView profilePhoto;
    private String userProfileId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
        setupActivity(savedInstanceState);
    }

    private void setupPrivacyToggle() {
        final RadioGroup privacyToggle = (RadioGroup) findViewById(R.id.profile_toggle);
        final RadioButton publicToggle = (RadioButton) findViewById(R.id.public_toggle);
        final RadioButton followersToggle = (RadioButton) findViewById(R.id.followers_toggle);
        if(privacyToggle != null && publicToggle != null && followersToggle != null) {
            publicToggle.setText(R.string.posts);
            followersToggle.setText(R.string.comments);
            publicToggle.setChecked(true);
            publicToggle.setTypeface(Typeface.DEFAULT_BOLD);
            //Set listener for clicking on toggle
            privacyToggle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.public_toggle) {
                        publicToggle.setTypeface(Typeface.DEFAULT_BOLD);
                        if (followersToggle.getTypeface() == Typeface.DEFAULT_BOLD) {
                            followersToggle.setTypeface(Typeface.SANS_SERIF);
                        }
                        databaseQuery.getUserPosts(userProfileId, layoutView, application.getUserAdapter(), getApplicationContext(), "posts", progressOverlay);
                    } else {
                        followersToggle.setTypeface(Typeface.DEFAULT_BOLD);
                        if (publicToggle.getTypeface() == Typeface.DEFAULT_BOLD) {
                            publicToggle.setTypeface(Typeface.SANS_SERIF);
                        }
                        databaseQuery.getUserPosts(userProfileId, layoutView, application.getPostsUserHasCommentedOnAdapter(), getApplicationContext(), "commented_posts", progressOverlay);
                    }
                }
            });
        }
    }

    private void setupFollowButton(final Button button) {
        databaseQuery.getIsFollowing(userProfileId, button, getApplicationContext());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = application.getFollowingRecyclerViewAdapter().containsUserId(userProfileId);
                if(user == null) {
                    User newUser = new User();
                    newUser.setUserId(userProfileId);
                    newUser.setName(posterName);
                    newUser.setId(AndroidUtils.generateId());
                    //No need to add this to the adapter database because it is already done for us in the DatabaseReference getFollowing call
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
        button.setText(R.string.following);
    }

    private void setButtonIsNotFollowing(Button button) {
        button.setBackgroundResource(R.drawable.follow_button_bg);
        button.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        button.setText(R.string.plusFollow);
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
        savedInstanceState.putString("userProfileId", userProfileId);
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
        Intent intent = new Intent(UserProfileActivity.this, CommentsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("postId", postId);
        bundle.putString("userProfileId", userProfileId);
        bundle.putString("posterUserId", posterUserId);
        bundle.putString("posterName", posterName);
        bundle.putString("postStatus", postStatus);
        bundle.putString("postTimeStamp", postTimeStamp);
        bundle.putString("postTitle", postTitle);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                userProfileId = AndroidUtils.getIntentString(data, "userProfileId");
                posterUserId = AndroidUtils.getIntentString(data, "posterUserId");
                posterName = AndroidUtils.getIntentString(data, "posterName");
                postStatus = AndroidUtils.getIntentString(data, "postStatus");
                postTimeStamp = AndroidUtils.getIntentString(data, "postTimeStamp");
                postTitle = AndroidUtils.getIntentString(data, "postTitle");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutView = findViewById(R.id.user_profile_coordinator_layout);
        application = ((FireBaseApplication) getApplication());
        databaseQuery = new DatabaseQuery(this);
        //This part is for sure used but not sure about the bottom
        profilePictureSetup(userProfileId, posterName);
        setupActionBar();

        followButton = (Button) findViewById(R.id.follow_button);
        progressOverlay = findViewById(R.id.progress_overlay);
        //Cannot be our own user
        if (!userProfileId.equals(application.getUserId())) {
            followButton.setVisibility(View.VISIBLE);
        }
        progressOverlay.setVisibility(View.VISIBLE);
        String[] intentStrings = {userProfileId, posterName, postStatus, postTimeStamp, postTitle, posterUserId};
        TabsUtil.setupProfileView(layoutView, "UserProfileActivity", application, databaseQuery, intentStrings);
        setupFollowButton(followButton);
        if(application.getUserAdapter().getUserId() != null && !application.getUserAdapter().getUserId().equals(userProfileId)) {
            //We know here that there is a new user profile id so we need to reset the recycler view
            application.getUserAdapter().getPosts().clear();
            application.getUserAdapter().notifyDataSetChanged();
        }
        if(application.getPostsUserHasCommentedOnAdapter().getUserId() != null && !application.getPostsUserHasCommentedOnAdapter().getUserId().equals(userProfileId)) {
            //We know here that there is a new user profile id so we need to reset the recycler view
            application.getPostsUserHasCommentedOnAdapter().getPosts().clear();
            application.getPostsUserHasCommentedOnAdapter().notifyDataSetChanged();
        }
        if (!application.getUserId().equals(userProfileId)) {
            application.getUserAdapter().setUserId(userProfileId);
            application.getPostsUserHasCommentedOnAdapter().setUserId(userProfileId);
            application.getUserFollowersAdapter().initializeChangedFollowing();
            application.getUserFollowingAdapter().initializeChangedFollowing();
            //Need to clear out the user following array
            databaseQuery.getUserPosts(userProfileId, layoutView, application.getUserAdapter(), getApplicationContext(), "posts", progressOverlay);
            setupPrivacyToggle();

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
