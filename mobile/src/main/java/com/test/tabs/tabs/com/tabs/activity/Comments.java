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
import com.firebase.client.Firebase;
import com.parse.ParseObject;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.comments.Comment;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsDataSource;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsListAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by schan on 11/24/15.
 */
public class Comments extends AppCompatActivity {

    Post post;
    private CommentsDataSource commentsDatasource;
    //Local Database for storing posts
    private PostsDataSource postsDataSource;
    private List<Comment> commentItems;
    private CommentsRecyclerViewAdapter commentsRecyclerViewAdapter;
    private RecyclerView commentsView;
    private TextView noCommentsView;
    private Toolbar toolbar;
    private LinearLayoutManager llm;
    private NotificationManager notificationManager;
    private boolean isNotificationActive;
    private String userId;
    String uniqueCommentId;

    private Firebase firebaseRef = new Firebase("https://tabsapp.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userId = AccessToken.getCurrentAccessToken().getUserId();
        setContentView(R.layout.comments);
        final String postId = getPostId();
        toolbar = (Toolbar) findViewById(R.id.comments_appbar);
        setSupportActionBar(toolbar);
        final Profile profile = Profile.getCurrentProfile();

        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        //Back bar enabled
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        final EditText comment = (EditText) findViewById(R.id.write_comment);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        commentsView = (RecyclerView) findViewById(R.id.view_comments);
        noCommentsView = (TextView) findViewById(R.id.no_comments_text);

        llm = new LinearLayoutManager(this);
        commentsView.setLayoutManager(llm);
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

        openDatasources();

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

//        comment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if(hasFocus){
//                    commentsView.scrollToPosition(commentsView.getAdapter().getItemCount()-1);
//                }
//            }
//        });

        comment.setOnTouchListener(onTouchListener);

        final String commenter = profile.getFirstName() + " " + profile.getLastName();

        //Button for sending post
        final Button button = (Button) findViewById(R.id.send_comment);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Store into Database the text that the individual writes. For now I will store it my own local DB since I don't have
                //Google cloud running yet.
                //Get Id and user name
                //Check if the comment is empty. Don't allow an empty post.
                if (TextUtils.isEmpty(comment.getText())) {
                    Toast.makeText(Comments.this, "Please enter in a comment first.", Toast.LENGTH_SHORT).show();
                } else {
                    uniqueCommentId = UUID.randomUUID().toString();
                    Comment createdComment = commentsDatasource.createComment(uniqueCommentId, postId, commenter, comment.getText().toString(), profile.getId());
                    //Toast.makeText(Comments.this, "Successfully commented.", Toast.LENGTH_SHORT).show();
                    //Make comment blank and set cursor to disappear once again. Also hide keyboard again.
                    comment.setText("");
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(comment.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    comment.setCursorVisible(false);
                    //TODO: fix this because we dont want to query for the entire database every time we get the post
                    //Display comment onto layout
                    populateComment(createdComment, commentsView.getAdapter().getItemCount());
                    saveCommentInCloud(createdComment);
                    if(noCommentsView.getVisibility() == View.VISIBLE){
                        noCommentsView.setVisibility(View.GONE);
                    }
                    //Notify friends that user has posted a comment on their post. Don't get notification if you posted on your own post.
//                    if(!createdComment.getCommenterUserId().equals(userId)) {
//                        showNotification(v, commenter);
//                    }

                }

            }
        });

        //populatePost(postId);
        populateComments(postId);
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

    private void populateComment(Comment comment, int position){
        commentsRecyclerViewAdapter.add(comment, position);
        commentsView.postDelayed(new Runnable() {
            @Override
            public void run() {
                commentsView.smoothScrollToPosition(commentsView.getAdapter().getItemCount());
                //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            }
        }, 1000);
    }

    private void checkAdapterIsEmpty () {
        if(commentsRecyclerViewAdapter.getItemCount() == 1){
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) noCommentsView.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.view_post);
            noCommentsView.setVisibility(View.VISIBLE);
        }
        else {
            noCommentsView.setVisibility(View.GONE);
        }
    }


    public void populateComments(String postId) {
        System.out.println("populating comment of post id: " + postId);
        commentsRecyclerViewAdapter = new CommentsRecyclerViewAdapter(getCommentsHeader(postId), commentsDatasource.getCommentsForPost(postId));
        commentsRecyclerViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });
        commentsView.setLayoutManager(llm);
        commentsView.setAdapter(commentsRecyclerViewAdapter);
        checkAdapterIsEmpty();
    }

    public CommentsHeader getCommentsHeader(String id)
    {
        System.out.println("Going to inflate header");
        post = postsDataSource.getPost(id);
        System.out.println("Post Status after inflating header: " + post.getStatus());
        CommentsHeader header = new CommentsHeader();
        header.setPosterUserId(post.getPosterUserId());
        header.setPosterName(post.getName());
        header.setPosterDate(post.getTimeStamp());
        header.setViewStatus(post.getStatus());
        return header;
    }


    public void populatePost(String id) {
        post = postsDataSource.getPost(id);
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

    public String getPostId(){
        Bundle extras = getIntent().getExtras();
        String value;
        if (extras != null) {
            value = extras.getString("id");
            return value;
        }
        return "";
    }

    private void saveCommentInCloud(Comment comment){
        firebaseRef.child("Comments/"+ comment.getPostId()).setValue(comment);
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


}
