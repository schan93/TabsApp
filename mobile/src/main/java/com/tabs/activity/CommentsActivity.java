package com.tabs.activity;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.schan.tabs.R;
import com.tabs.database.databaseQuery.DatabaseQuery;
import com.tabs.database.comments.Comment;
import com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.tabs.database.posts.Post;
import com.tabs.database.posts.PostRecyclerViewAdapter;
import com.tabs.database.users.User;
import com.tabs.utils.AndroidUtils;
import com.tabs.utils.TabsUtil;

import java.util.List;

/**
 * Created by schan on 11/24/15.
 */
public class CommentsActivity extends AppCompatActivity {

    private FireBaseApplication application;
    private ListView commentsView;
    private String posterUserId;
    private String posterName;
    private Long postTimeStamp;
    private String postStatus;
    private Boolean isFollowingPoster;
    private Toolbar toolbar;
    private String postId;
    private String userId;
    private String postTitle;
    private String userProfileId;
    private View progressOverlay;
    private DatabaseQuery databaseQuery;
    private String name;
    private EditText comment;

    private DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://tabsapp.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments);

        //Initialize edit text & send button
        comment = (EditText) findViewById(R.id.write_comment);
        Button sendButton = (Button) findViewById(R.id.send_comment);
        //Initialize Activity UI and variables from clicking on PostRecyclerViewAdapter
        setupActionBar();
        setupActivity(savedInstanceState);
        //Setup CommentsActivity page's buttons / functionality
        setupComment(comment);
        setupSendButton(comment, sendButton);
        //Setup comments header
        checkIfNewUser();
        setupHeader();
    }

    private void checkIfNewUser() {
        if(application.getUserAdapter().getAdapterOwnerId() != null && !application.getUserAdapter().getAdapterOwnerId().equals(posterUserId)) {
            //If the user of the user adapter is not the same as the current user, then we need to get their comments. otherwise we don't need to get their number of comments
            //Since we have a listener for htem.
            application.getUserAdapter().setAdapterOwnerId(posterUserId);
            application.getUserAdapter().getPosts().clear();
            application.getUserAdapter().notifyDataSetChanged();

            application.getPostsUserHasCommentedOnAdapter().setAdapterOwnerId(posterUserId);
            application.getPostsUserHasCommentedOnAdapter().getPosts().clear();
            application.getPostsUserHasCommentedOnAdapter().notifyDataSetChanged();
        }
    }

    private void createComment() {
        //Set the text of the comment to be empty
        String text = comment.getText().toString();
        if(userId.equals("") || name.equals("")) {
            //Cannot create the comment because the name or user id are empty
            Toast.makeText(CommentsActivity.this, "There was an error creating this comment. Please re-open the application and try again.", Toast.LENGTH_SHORT).show();
        } else {
            Comment createdComment = new Comment("", postId, name, text, userId, AndroidUtils.getDateTime());
            //Scroll down to last comment
            commentsView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    commentsView.smoothScrollToPosition(commentsView.getAdapter().getCount());
                    //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                }
            }, 1000);
            databaseQuery.saveCommentToDatabaseReference(getApplicationContext(), createdComment);
            updatePostDetails(createdComment);
            //Push down the keyboard and make the cursor invisible, clear out the text
            resetKeyboardSettings();
            comment.setText("");
            resetCommentListView();
            Toast.makeText(CommentsActivity.this, "Successfully commented.", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetCommentListView() {
        TextView emptyView = (TextView) findViewById(R.id.empty_list_item);
        if(emptyView != null && emptyView.getVisibility() == View.VISIBLE) {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void setupSendButton(final EditText comment, Button sendButton) {
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (TextUtils.isEmpty(comment.getText())) {
                    Toast.makeText(CommentsActivity.this, "Please enter in a comment first.", Toast.LENGTH_SHORT).show();
                } else {
                    createComment();
                }

            }
        });
    }

    private void resetKeyboardSettings() {
        //Push down the keyboard and make the cursor invisible, clear out the text
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(comment.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        comment.setCursorVisible(false);
    }

    public void setupComment(final EditText comment) {
        //Once we send the post, we want to
        comment.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    comment.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    resetKeyboardSettings();
                }
                return false;
            }
        });

        //Hide the cursor until view is clicked on
        View.OnTouchListener onTouchListener = new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                System.out.println("Touched");
                if (v.getId() == comment.getId()) {
                    comment.setCursorVisible(true);
                }
                commentsView.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        commentsView.smoothScrollToPosition(commentsView.getAdapter().getCount());
                    }
                }, 750);
                return false;
            }
        };
        comment.setOnTouchListener(onTouchListener);
    }

    public void populateCommentView(String postId) {
        //Set up loading page first && make sure comments recycler view has been cleared out incase someone clicks back or soemthing
        application.getCommentsRecyclerViewAdapter().getCommentsList().clear();
        application.getCommentsRecyclerViewAdapter().notifyDataSetChanged();
        databaseQuery.getComments(this, posterName, postTitle, postTimeStamp, posterUserId, postStatus, postId, findViewById(R.id.comments_layout), commentsView, progressOverlay, getApplicationContext());
        setupCommentsAdapter();
    }

    private void setupActionBar() {
        toolbar = (Toolbar) findViewById(R.id.comments_appbar);
        setSupportActionBar(toolbar);
        //Back bar enabled
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //What happens if you click back
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressOverlay.setVisibility(View.VISIBLE);
        populateCommentView(postId);
    }

    public void setupCommentsAdapter() {
        List<Comment> commentItems = application.getCommentsRecyclerViewAdapter().getCommentsList();
        application.setCommentsRecyclerViewAdapter(new CommentsRecyclerViewAdapter(getApplicationContext(), commentItems, postStatus, postTimeStamp, posterUserId, postTitle));
        application.getCommentsRecyclerViewAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }
        });
        commentsView.setAdapter(new ArrayAdapter<>(this, R.layout.comment_item, android.R.id.text2, commentItems));
        commentsView.setAdapter(application.getCommentsRecyclerViewAdapter());
    }

    private void setupHeader() {
        View header = getLayoutInflater().inflate(R.layout.comments_header, null);
        SimpleDraweeView photo = (SimpleDraweeView) header.findViewById(R.id.poster_picture);
        TextView title = (TextView) header.findViewById(R.id.comment_post_title);
        TextView name = (TextView) header.findViewById(R.id.poster_name);
        TextView date = (TextView) header.findViewById(R.id.post_date);
        TextView status = (TextView) header.findViewById(R.id.view_status);
        name.setText(posterName);
        title.setText(postTitle);
        date.setText(AndroidUtils.convertDate(postTimeStamp));
        DraweeController controller = TabsUtil.getImage(posterUserId);
        status.setText(postStatus);
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        photo.getHierarchy().setRoundingParams(roundingParams);
        photo.setController(controller);
        if(!userProfileId.equals(application.getUserId())) {
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupOnClickListener();
                }
            });
            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupOnClickListener();
                }
            });
        }
        commentsView.addHeaderView(header, null, false);
    }

    private void setupOnClickListener() {
        Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("postId", postId);
        bundle.putString("posterUserId", posterUserId);
        bundle.putString("userProfileId", userProfileId);
        bundle.putString("posterName", posterName);
        bundle.putString("postStatus", postStatus);
        bundle.putLong("postTimeStamp", postTimeStamp);
        bundle.putString("postTitle", postTitle);
        bundle.putString("userId", userId);
        bundle.putString("name", name);

        intent.putExtras(bundle);
        startActivity(intent, bundle);
    }

    private void updatePostDetails(Comment comment){
        updatePostComments(comment.getPostId());
        final String hasCommented = " has commented: ";
        String message = comment.getCommenter() + hasCommented + comment.getComment();
        databaseQuery.sendCommentNotifications(comment, message);
    }

    private void updatePostComments(final String postId) {
        DatabaseReference reference = firebaseRef.child("posts/" + postId + "/numComments");
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue((Long) mutableData.getValue() + 1);
                }
                return Transaction.success(mutableData); //we can also abort by calling Transaction.abort()
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    FirebaseCrash.report(databaseError.toException());
                } else {
                    //Update adapters so that they would know how many comments for that post there are
                    updatePostAdapters(application.getUserAdapter(), dataSnapshot);
                    updatePostAdapters(application.getMyTabsAdapter(), dataSnapshot);
                    updatePostAdapters(application.getPostsThatCurrentUserHasCommentedOnAdapter(), dataSnapshot);
                    updatePostAdapters(application.getPostsUserHasCommentedOnAdapter(), dataSnapshot);
                    updatePostAdapters(application.getPublicAdapter(), dataSnapshot);
                    updatePostAdapters(application.getFollowingPostAdapter(), dataSnapshot);
                }
            }
        });
    }

    private void updatePostAdapters(PostRecyclerViewAdapter adapter, DataSnapshot dataSnapshot) {
        Post post = adapter.containsId(postId);
        if(post != null) {
            post.setNumComments(Integer.valueOf(dataSnapshot.getValue().toString()));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putString("postId", postId);
        savedInstanceState.putString("userId", userId);
        savedInstanceState.putString("name", name);
        savedInstanceState.putString("posterUserId", posterUserId);
        savedInstanceState.putString("userProfileId", userProfileId);
        savedInstanceState.putString("posterName", posterName);
        savedInstanceState.putLong("postTimeStamp", postTimeStamp);
        savedInstanceState.putString("postStatus", postStatus);
        savedInstanceState.putString("postTitle", postTitle);
        savedInstanceState.putBoolean("isFollowingPoster", isFollowingPoster);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupActivity(Bundle savedInstanceState) {
        //Regardless of activity is created by going in background or not, we need to initialize these variables first
        databaseQuery = new DatabaseQuery(this);
        application = ((com.tabs.activity.FireBaseApplication) getApplication());
        //Need to clear the commentsRecyclerView after we click on another post
        application.getCommentsRecyclerViewAdapter().clear();
        progressOverlay = findViewById(R.id.progress_overlay);
        toolbar = (Toolbar) findViewById(R.id.comments_appbar);
        commentsView = (ListView) findViewById(R.id.rv_view_comments);
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            if(savedInstanceState.containsKey("postId")) {
                postId = savedInstanceState.getString("postId");
            }
            if(savedInstanceState.containsKey("userId")) {
                userId = savedInstanceState.getString("userId");
            }
            if(savedInstanceState.containsKey("name")) {
                name = savedInstanceState.getString("name");
            }
            if(savedInstanceState.containsKey("postTitle")) {
                postTitle = savedInstanceState.getString("postTitle");
            }
            if(savedInstanceState.containsKey("posterUserId")) {
                posterUserId = savedInstanceState.getString("posterUserId");
            }
            if(savedInstanceState.containsKey("posterName")) {
                posterName = savedInstanceState.getString("posterName");
            }
            if(savedInstanceState.containsKey("postTimeStamp")) {
                postTimeStamp = savedInstanceState.getLong("postTimeStamp");
            }
            if(savedInstanceState.containsKey("postStatus")) {
                postStatus = savedInstanceState.getString("postStatus");
            }
            if(savedInstanceState.containsKey("isFollowingPoster")) {
                isFollowingPoster = savedInstanceState.getBoolean("isFollowingPoster");
            }
            if(savedInstanceState.containsKey("userProfileId")) {
                userProfileId = savedInstanceState.getString("userProfileId");
            }
            if(savedInstanceState.containsKey("name")) {
                name = savedInstanceState.getString("name");
            }
            if(savedInstanceState.containsKey("userId")) {
                userId = savedInstanceState.getString("userId");
            }
        } else {
            //You get all these from the post view adapter
            postId = AndroidUtils.getIntentString(getIntent(), "postId");
            postTitle = AndroidUtils.getIntentString(getIntent(), "postTitle");
            posterUserId = AndroidUtils.getIntentString(getIntent(), "posterUserId");
            userProfileId = AndroidUtils.getIntentString(getIntent(), "userProfileId");
            posterName = AndroidUtils.getIntentString(getIntent(), "posterName");
            postTimeStamp = AndroidUtils.getIntentLong(getIntent(), "postTimeStamp");
            postStatus = AndroidUtils.getIntentString(getIntent(), "postStatus");
            userId = AndroidUtils.getIntentString(getIntent(), "userId");
            name = AndroidUtils.getIntentString(getIntent(), "name");
            User user = application.getFollowersRecyclerViewAdapter().containsUserId(posterUserId);
            if (user != null) {
                isFollowingPoster = true;
            } else {
                isFollowingPoster = false;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                postId = AndroidUtils.getIntentString(data, "postId");
                posterUserId = AndroidUtils.getIntentString(data, "posterUserId");
                userProfileId = AndroidUtils.getIntentString(data, "userProfileId");
                posterName = AndroidUtils.getIntentString(data, "posterName");
                postStatus = AndroidUtils.getIntentString(data, "postStatus");
                postTimeStamp = AndroidUtils.getIntentLong(data, "postTimeStamp");
                postTitle = AndroidUtils.getIntentString(data, "postTitle");
                name = AndroidUtils.getIntentString(data, "name");
                userId = AndroidUtils.getIntentString(data, "userId");
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
