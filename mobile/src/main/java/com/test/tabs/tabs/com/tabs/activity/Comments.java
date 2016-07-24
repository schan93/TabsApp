package com.test.tabs.tabs.com.tabs.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Query;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.Database.DatabaseQuery;
import com.test.tabs.tabs.com.tabs.database.comments.Comment;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsDataSource;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.followers.Follower;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;
import com.test.tabs.tabs.com.tabs.database.users.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by schan on 11/24/15.
 */
public class Comments extends AppCompatActivity {

    private static FireBaseApplication application;
    private static RecyclerView commentsView;
    private static String posterUserId;
    private static String posterName;
    private static String postTimeStamp;
    private static String postStatus;
    private static Boolean isFollowingPoster;
    private static LinearLayoutManager llm;
    private static CommentsHeader header;
    private Toolbar toolbar;
    private NotificationManager notificationManager;
    private boolean isNotificationActive;
    private String postId;
    private String userId;
    //Progress overlay
    View progressOverlay;
    DatabaseQuery databaseQuery;
    String name;
    EditText comment;
    Button sendButton;
    private static String postTitle;

    private Firebase firebaseRef = new Firebase("https://tabsapp.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments);

        //Initialize edit text & send button
        comment = (EditText) findViewById(R.id.write_comment);
        sendButton = (Button) findViewById(R.id.send_comment);

        //Initialize Activity UI and variables from clicking on PostRecyclerViewAdapter
        setupActionBar();
        setupActivity(savedInstanceState);

        //Setup Comments page's buttons / functionality
        setupComment(comment);
        setupSendButton(comment, sendButton);
        populateCommentView(postId);


        //TODO: But I worry about the fact that if we go idle on the user profile page we will have no posts. lets test this out.
        //I think we should be calling this when the comments activity starts because that way we will actually be ready for the loading of the next page
        if(!application.getUserId().equals(posterUserId)) {
            databaseQuery.getUserPosts(posterUserId);
            databaseQuery.getPostsUserCommentedOn(posterUserId);
        }

        header = setupCommentsHeader();
    }

    private void createComment() {
        //Set the text of the comment to be empty
        String text = comment.getText().toString();

        Comment createdComment = new Comment("", postId, name, text, userId, getDateTime());
        updatePost();

        databaseQuery.saveCommentToFirebase(createdComment);
        updatePostCommentNumber(createdComment);

        //Push down the keyboard and make the cursor invisible, clear out the text
        resetKeyboardSettings();
        comment.setText("");
        Toast.makeText(Comments.this, "Successfully commented.", Toast.LENGTH_SHORT).show();
    }

    private void setupSendButton(final EditText comment, Button sendButton) {
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (TextUtils.isEmpty(comment.getText())) {
                    Toast.makeText(Comments.this, "Please enter in a comment first.", Toast.LENGTH_SHORT).show();
                } else {
                    createComment();
                    //Show notifications
//                    if (noCommentsView.getVisibility() == View.VISIBLE) {
//                        noCommentsView.setVisibility(View.GONE);
//                    }
                    //Notify friends that user has posted a comment on their post. Don't get notification if you posted on your own post.
//                    if(!createdComment.getCommenterUserId().equals(userId)) {
//                        showNotification(v, commenter);
//                    }

                    //Indicate success


                }

            }
        });
    }

    private void resetKeyboardSettings() {
        //Push down the keyboard and make the cursor invisible, clear out the text
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(comment.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        comment.setCursorVisible(false);
//        comment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
//                    createComment();
//                }
//                return false;
//            }
//        });
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
                        commentsView.smoothScrollToPosition(commentsView.getAdapter().getItemCount() - 1);
                    }
                }, 250);
                return false;
            }
        };
        comment.setOnTouchListener(onTouchListener);
    }

    public void populateCommentView(String postId) {
        //Set up loading page first
        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
        databaseQuery.getComments(postId, Comments.this, commentsView, progressOverlay);
    }

    private void setupActionBar() {
        toolbar = (Toolbar) findViewById(R.id.comments_appbar);
        setSupportActionBar(toolbar);
        //Back bar enabled
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //What happens if you click back
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showNotification(View view, String commenter){
        NotificationCompat.Builder notifiationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(commenter + " has commented on your post!")
                .setContentText("Click to view or respond back!")
                .setTicker("New comment from " + commenter)
                .setSmallIcon(R.mipmap.blank_prof_pic);

        Intent moreInfoIntent = new Intent(this, Comments.class);

        //When the user clicks back, it doesn't look sloppy!
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(MoreInfoNotification.class);
        taskStackBuilder.addNextIntent(moreInfoIntent);

        //If the intent already exists, just update it and not create a new one
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //When the notification is actually clicked on
        notifiationBuilder.setContentIntent(pendingIntent);

        //Notification manager to notify of background event
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, notifiationBuilder.build());

        isNotificationActive = true;
    }

    private void stopNotification(View view){
        if(isNotificationActive){
            notificationManager.cancel(1);
        }
    }

    public void updatePost(){
        commentsView.postDelayed(new Runnable() {
            @Override
            public void run() {
                commentsView.smoothScrollToPosition(commentsView.getAdapter().getItemCount());
                //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            }
        }, 1000);
    }

    public static void setupCommentsAdapter(String postId, Activity activity) {
        List<Comment> commentItems = application.getCommentsRecyclerViewAdapter().getCommentsList();
        application.setCommentsRecyclerViewAdapter(new CommentsRecyclerViewAdapter(application, activity, header, commentItems));
        application.getCommentsRecyclerViewAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }
        });
        commentsView.setLayoutManager(llm);
        commentsView.setAdapter(application.getCommentsRecyclerViewAdapter());
    }

    public static CommentsHeader setupCommentsHeader() {
        CommentsHeader header = new CommentsHeader();
        header.setPosterUserId(posterUserId);
        header.setPosterName(posterName);
        header.setPosterDate(postTimeStamp);
        header.setViewStatus(postStatus);
        header.setIsFollowing(isFollowingPoster);
        header.setPostTitle(postTitle);
        return header;
    }

    private void updatePostCommentNumber(Comment comment){
        updatePostComments(comment.getPostId());
    }

    private void updatePostComments(final String postId) {
        Firebase reference = firebaseRef.child("posts/" + postId + "/numComments");
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
            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                if (firebaseError != null) {
                    System.out.println("Comments: Error: " + firebaseError);
                } else {
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

    void updatePostAdapters(PostRecyclerViewAdapter adapter, DataSnapshot dataSnapshot) {
        Post post = adapter.containsId(postId);
        if(post != null) {
            post.setNumComments(Integer.valueOf(dataSnapshot.getValue().toString()));
            adapter.notifyDataSetChanged();
        }
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "MM/dd/yyyy hh:mm:ss a", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putString("postId", postId);
        savedInstanceState.putString("userId", userId);
        savedInstanceState.putString("name", name);
        savedInstanceState.putString("posterUserId", posterUserId);
        savedInstanceState.putString("posterName", posterName);
        savedInstanceState.putString("postTimeStamp", postTimeStamp);
        savedInstanceState.putString("postStatus", postStatus);
        savedInstanceState.putBoolean("isFollowingPoster", isFollowingPoster);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupActivity(Bundle savedInstanceState) {
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
                postTimeStamp = savedInstanceState.getString("postTimeStamp");
            }
            if(savedInstanceState.containsKey("postStatus")) {
                postStatus = savedInstanceState.getString("postStatus");
            }
            if(savedInstanceState.containsKey("isFollowingPoster")) {
                isFollowingPoster = savedInstanceState.getBoolean("isFollowingPoster");
            }
        } else {
            //You get all these from the post view adapter
            postId = AndroidUtils.getIntentString(getIntent(), "postId");
            userId = AndroidUtils.getIntentString(getIntent(), "userId");
            postTitle = AndroidUtils.getIntentString(getIntent(), "postTitle");
            posterUserId = AndroidUtils.getIntentString(getIntent(), "posterUserId");
            posterName = AndroidUtils.getIntentString(getIntent(), "posterName");
            postTimeStamp = AndroidUtils.getIntentString(getIntent(), "postTimeStamp");
            postStatus = AndroidUtils.getIntentString(getIntent(), "postStatus");
            User user = application.getFollowersRecyclerViewAdapter().containsUserId(posterUserId);
            if (user != null) {
                isFollowingPoster = true;
            } else {
                isFollowingPoster = false;
            }
        }

        databaseQuery = new DatabaseQuery(this);
        application = ((FireBaseApplication) getApplication());
        progressOverlay = findViewById(R.id.progress_overlay);
        if(application.getName() != null && application.getName() != "") {
            name = application.getName();
        } else {
            if(savedInstanceState != null) {
                if(savedInstanceState.containsKey("name")) {
                    name = savedInstanceState.getString("name");
                }
            }
        }
        toolbar = (Toolbar) findViewById(R.id.comments_appbar);
        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        commentsView = (RecyclerView) findViewById(R.id.rv_view_comments);
//        noCommentsView = (TextView) findViewById(R.id.no_comments_text);
        llm = new LinearLayoutManager(this);
        commentsView.setLayoutManager(llm);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Comments.this, news_feed.class);
        startActivity(intent);
        finish();
    }

}
