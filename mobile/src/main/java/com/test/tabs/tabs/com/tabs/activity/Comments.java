package com.test.tabs.tabs.com.tabs.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Query;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.parse.ParseObject;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.Database.DatabaseQuery;
import com.test.tabs.tabs.com.tabs.database.comments.Comment;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsDataSource;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Created by schan on 11/24/15.
 */
public class Comments extends AppCompatActivity {

    Post post;
    private CommentsDataSource commentsDatasource;
    //Local Database for storing posts
    private PostsDataSource postsDataSource;
    private FireBaseApplication application;
    private List<Comment> commentItems;
    private CommentsRecyclerViewAdapter commentsRecyclerViewAdapter;
    private RecyclerView commentsView;
    private TextView noCommentsView;
    private Toolbar toolbar;
    private LinearLayoutManager llm;
    private NotificationManager notificationManager;
    private boolean isNotificationActive;
    private String postId;
    private String tab;
    private String userId;
    private String posterUserId;
    private String posterName;
    private String postTimeStamp;
    private String postStatus;
    //Progress overlay
    View progressOverlay;
    DatabaseQuery databaseQuery;
    String name;

    private Firebase firebaseRef = new Firebase("https://tabsapp.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments);
        setupActionBar();
        setupActivity(savedInstanceState);

        final EditText comment = (EditText) findViewById(R.id.write_comment);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //Once we send the post, we want to
        comment.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                comment.setCursorVisible(false);
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    comment.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    in.hideSoftInputFromWindow(comment.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
                        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                    }
                }, 250);
                return false;
            }
        };
        comment.setOnTouchListener(onTouchListener);

        //Button for sending post
        final Button button = (Button) findViewById(R.id.send_comment);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (TextUtils.isEmpty(comment.getText())) {
                    Toast.makeText(Comments.this, "Please enter in a comment first.", Toast.LENGTH_SHORT).show();
                } else {
                    String text = comment.getText().toString();
                    Comment createdComment = new Comment("", postId, name, text, userId, getDateTime());
                    Toast.makeText(Comments.this, "Successfully commented.", Toast.LENGTH_SHORT).show();
                    comment.setText("");
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(comment.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    comment.setCursorVisible(false);
                    updatePost();
                    databaseQuery.saveCommentToFirebase(createdComment);
                    saveCommentInCloud(createdComment, tab);
                    if (noCommentsView.getVisibility() == View.VISIBLE) {
                        noCommentsView.setVisibility(View.GONE);
                    }
                    //Notify friends that user has posted a comment on their post. Don't get notification if you posted on your own post.
//                    if(!createdComment.getCommenterUserId().equals(userId)) {
//                        showNotification(v, commenter);
//                    }

                }

            }
        });
        //Now we have to show a loading bar so that we are loading the comments. While we are loading the comments, we update the comments header
        populateCommentView(postId);
    }

    public void populateCommentView(String postId) {
        commentItems = new ArrayList<Comment>();
        getComments(postId);
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

    private void checkAdapterIsEmpty () {
        if(application.getCommentsRecyclerViewAdapter().getItemCount() == 1){
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) noCommentsView.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.view_post);
            noCommentsView.setVisibility(View.VISIBLE);
        }
        else {
            noCommentsView.setVisibility(View.GONE);
        }
    }

    public CommentsHeader getCommentsHeader(String id)
    {
        System.out.println("Going to inflate header");
        CommentsHeader header = new CommentsHeader();
        header.setPosterUserId(posterUserId);
        header.setPosterName(posterName);
        header.setPosterDate(postTimeStamp);
        header.setViewStatus(postStatus);
        return header;
    }


    public void populatePost(String id) {
        TextView statusMsg = (TextView)findViewById(R.id.view_status);
        System.out.println("Post: " + post);
        statusMsg.setText(post.getStatus());

        //Set profile picture
        DraweeController controller = news_feed.getImage(post.getPosterUserId());
        SimpleDraweeView draweeView = (SimpleDraweeView) findViewById(R.id.poster_picture);
        draweeView.setController(controller);

        //Set poster's name
        TextView posterName = (TextView)findViewById(R.id.poster_name);
        posterName.setText(post.getName());

        //Set date of when post was created
        TextView postDate = (TextView) findViewById(R.id.post_date);
        postDate.setText(convertDate(post.getTimeStamp()));
    }

    public void openDatasources(){
        commentsDatasource = new CommentsDataSource(this);
        commentsDatasource.open();
        postsDataSource = new PostsDataSource(this);
        postsDataSource.open();
    }

    public String getIntentString(String value){
        Bundle extras = getIntent().getExtras();
        String result = "";
        if (extras != null) {
            result = extras.getString(value);
        }
        return result;
    }

    private void saveCommentInCloud(Comment comment, String tab){
        updatePostComments(comment.getPostId(), tab);
    }

    private void updatePostComments(String postId, final String tab) {
        Firebase reference = firebaseRef.child("Posts/" + postId + "/numComments");
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    System.out.println("Comments: Setting value to 1");
                    mutableData.setValue(1);
                } else {
                    System.out.println("Comments: Setting value from " + mutableData.getValue());
                    mutableData.setValue((Long) mutableData.getValue() + 1);
                    System.out.println("Comments: Updated value from " + mutableData.getValue());
                }
                System.out.println("Comments: Success!");
                return Transaction.success(mutableData); //we can also abort by calling Transaction.abort()
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                //This method will be called once with the results of the transaction.
                if (firebaseError != null) {
                    System.out.println("Comments: Error: " + firebaseError);
                }
//                updatePostAdapter(tab);
            }
        });
    }

    public String convertDate(String timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        String dateText = "";
        Date date = null;
        try {
            date = dateFormat.parse(timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar postDate = Calendar.getInstance();
        postDate.setTime(date); // your date

        Calendar now = Calendar.getInstance();

        Integer dateOffset = 0;
        System.out.println("Post Date: " + postDate);
        System.out.println("Now: " + now);
        if (now.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == postDate.get(Calendar.MONTH)
                && now.get(Calendar.DAY_OF_YEAR) == postDate.get(Calendar.DAY_OF_YEAR)
                && (now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR) > 1)) {

            dateOffset = now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR);
            dateText = "h";
        } else if (now.get(Calendar.YEAR) == postDate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == postDate.get(Calendar.MONTH)
                && now.get(Calendar.DAY_OF_YEAR) == postDate.get(Calendar.DAY_OF_YEAR)
                && (now.get(Calendar.HOUR) - postDate.get(Calendar.HOUR) == 0)) {
            dateOffset = now.get(Calendar.MINUTE) - postDate.get(Calendar.MINUTE);
            dateText = "m";
        } else if (Math.abs(now.getTime().getTime() - postDate.getTime().getTime()) <= 24 * 60 * 60 * 1000L) {
            dateOffset = (int) getHoursDifference(now, postDate);
            if(dateOffset == 24){
                dateOffset = 1;
                dateText = "d";
            }
            else {
                dateText = "h";
            }
        } else {
            long hours = getHoursDifference(now, postDate);

            dateOffset = (int)hours / 24;
            dateText = "d";
        }
        String newFormat = dateOffset + dateText;
        return newFormat;
    }

    private long getHoursDifference(Calendar now, Calendar postDate) {
        long secs = (now.getTime().getTime() - postDate.getTime().getTime()) / 1000;
        long hours = secs / 3600;
//        secs = secs % 3600;
//        long mins = secs / 60;
//        secs = secs % 60;
        return hours;
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
        savedInstanceState.putString("tab", tab);
        savedInstanceState.putString("userId", userId);
        savedInstanceState.putString("posterUserId", posterUserId);
        savedInstanceState.putString("posterName", posterName);
        savedInstanceState.putString("postTimeStamp", postTimeStamp);
        savedInstanceState.putString("postStatus", postStatus);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupActivity(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            if(savedInstanceState.containsKey("tab")) {
                tab = savedInstanceState.getString("tab");
            }
            if(savedInstanceState.containsKey("postId")) {
                postId = savedInstanceState.getString("postId");
            }
            if(savedInstanceState.containsKey("userId")) {
                userId = savedInstanceState.getString("userId");
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
        } else {
            postId = getIntentString("postId");
            tab = getIntentString("tab");
            userId = getIntentString("userId");
            posterUserId = getIntentString("posterUserId");
            posterName = getIntentString("posterName");
            postTimeStamp = getIntentString("postTimeStamp");
            postStatus = getIntentString("postStatus");
            // Probably initialize members with default values for a new instance
        }

        databaseQuery = new DatabaseQuery(this);
        application = ((FireBaseApplication) getApplication());
        progressOverlay = findViewById(R.id.progress_overlay);

        name = application.getName();
        toolbar = (Toolbar) findViewById(R.id.comments_appbar);
        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        commentsView = (RecyclerView) findViewById(R.id.view_comments);
        noCommentsView = (TextView) findViewById(R.id.no_comments_text);
        llm = new LinearLayoutManager(this);
        commentsView.setLayoutManager(llm);
    }

    //Put the loading screen thing
    public void getComments(final String postId) {
        if(application.getCommentsRecyclerViewAdapter() == null) {
            application.setCommentsRecyclerViewAdapter(new CommentsRecyclerViewAdapter(new CommentsHeader(), commentItems));
        }
        Firebase commentsRef = firebaseRef.child("Comments");
        Query query = commentsRef.orderByChild("postId").equalTo(postId);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot commentSnapShot : dataSnapshot.getChildren()) {
                    Comment comment = commentSnapShot.getValue(Comment.class);
                    application.getCommentsRecyclerViewAdapter().getCommentsList().add(comment);
                }
                application.getCommentsRecyclerViewAdapter().notifyDataSetChanged();
                commentItems = application.getCommentsRecyclerViewAdapter().getCommentsList();
                application.setCommentsRecyclerViewAdapter(new CommentsRecyclerViewAdapter(getCommentsHeader(postId), commentItems));
                application.getCommentsRecyclerViewAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onChanged() {
                        super.onChanged();
                        checkAdapterIsEmpty();
                    }
                });
                commentsView.setLayoutManager(llm);
                commentsView.setAdapter(application.getCommentsRecyclerViewAdapter());
                checkAdapterIsEmpty();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        commentsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Comment newComment = dataSnapshot.getValue(Comment.class);
                List<Comment> comments = application.getCommentsRecyclerViewAdapter().getCommentsList();
                if (application.getCommentsRecyclerViewAdapter().containsId(comments, newComment.getId()) == null && newComment.getPostId().equals(postId)) {
                    application.getCommentsRecyclerViewAdapter().getCommentsList().add(newComment);
                    application.getCommentsRecyclerViewAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Comment changedComment = dataSnapshot.getValue(Comment.class);
                int length = application.getCommentsRecyclerViewAdapter().getCommentsList().size();
                for (int i = 0; i < length; i++) {
                    if (application.getCommentsRecyclerViewAdapter().getCommentsList().get(i).getId().equals(changedComment.getId())) {
                        application.getCommentsRecyclerViewAdapter().getCommentsList().set(i, changedComment);
                    }
                }
                application.getCommentsRecyclerViewAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Comment removedComment = dataSnapshot.getValue(Comment.class);
                int length = application.getCommentsRecyclerViewAdapter().getCommentsList().size();
                for (int i = 0; i < length; i++) {
                    if (application.getCommentsRecyclerViewAdapter().getCommentsList().get(i).getId().equals(removedComment.getId())) {
                        application.getCommentsRecyclerViewAdapter().getCommentsList().remove(i);
                    }
                }
                application.getCommentsRecyclerViewAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //Not sure if used
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

}
