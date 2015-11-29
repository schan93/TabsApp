package com.test.tabs.tabs.com.tabs.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.comments.Comment;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsDataSource;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsListAdapter;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsListAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schan on 11/24/15.
 */
public class Comments extends AppCompatActivity {

    Post post;
    private ListView commentsList;
    private CommentsDataSource commentsDatasource;
    private List<Comment> commentItems;
    private CommentsListAdapter commentsListAdapter;
    //Local Database for storing posts
    private PostsDataSource postsDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments);

        final long postId = getPostId();
        final EditText comment = (EditText) findViewById(R.id.write_comment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.comments_toolbar);
        setSupportActionBar(toolbar);
        final Profile profile = Profile.getCurrentProfile();

        //Back bar enabled
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        openDatasources();
        populatePost(postId);
        populateComments(postId);

        final String commenter = profile.getFirstName() + " " + profile.getLastName();

        //Button for sending post
        final Button button = (Button) findViewById(R.id.send_comment);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Store into Database the text that the individual writes. For now I will store it my own local DB since I don't have
                //Google cloud running yet.
                //Get Id and user name

                commentsDatasource.createComment(postId, commenter, comment.getText().toString(), profile.getId());
                Toast.makeText(Comments.this, "Successfully commented.", Toast.LENGTH_SHORT).show();
                //Make comment blank.
                comment.setText("");
                //TODO: fix this because we dont want to query for the entire database every time we get the post
                populateComments(postId);

//                Intent intent = new Intent(Comments.this, news_feed.class);
//                if (intent != null) {
//                    startActivity(intent);
//                }

            }
        });

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

    public void populateComments(long postId) {
        commentsList = (ListView) findViewById(R.id.lv_comments_feed);
        commentItems = new ArrayList<>();

        commentsListAdapter = new CommentsListAdapter(this, commentItems);
        commentsList.setAdapter(commentsListAdapter);

        if(commentsDatasource.isTablePopulated()) {
            for (Comment i : commentsDatasource.getCommentsForPost(postId)) {
                commentItems.add(i);
            }
        }
    }

    public void populatePost(long id) {
        post = postsDataSource.getPost(id);
        System.out.println("Grabbed post: " + post.getName());
        TextView statusMsg = (TextView)findViewById(R.id.view_status);
        statusMsg.setText(post.getStatus());
        //Set profile picture
        DraweeController controller = news_feed.getImage(post.getPosterUserId());
        SimpleDraweeView draweeView = (SimpleDraweeView) findViewById(R.id.friend_profile_picture);
        draweeView.setController(controller);
    }

    public void openDatasources(){
        commentsDatasource = new CommentsDataSource(this);
        commentsDatasource.open();
        postsDataSource = new PostsDataSource(this);
        postsDataSource.open();
    }

    public long getPostId(){
        Bundle extras = getIntent().getExtras();
        Long value;
        if (extras != null) {
            value = extras.getLong("id");
            return value;
        }
        return 0;
    }

}
