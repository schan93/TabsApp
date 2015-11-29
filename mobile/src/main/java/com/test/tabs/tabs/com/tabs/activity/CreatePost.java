package com.test.tabs.tabs.com.tabs.activity;

import android.app.Activity;
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
import android.widget.Toast;

import com.facebook.AccessToken;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by schan on 11/14/15.
 */
public class CreatePost extends AppCompatActivity {


    //Local Database for storing posts
    private PostsDataSource datasource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_post);

        //In bundle, grab userid and name

        Toolbar toolbar = (Toolbar) findViewById(R.id.create_post_toolbar);
        setSupportActionBar(toolbar);

        //Open posts database for storage
        datasource = new PostsDataSource(this);
        datasource.open();

        //Back bar enabled
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Pop up keyboard
        final EditText post = (EditText) findViewById(R.id.type_status);
        post.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(post, InputMethodManager.SHOW_IMPLICIT);




        //Button for sending post
        final Button button = (Button) findViewById(R.id.send_post_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Store into Database the text that the individual writes. For now I will store it my own local DB since I don't have
                //Google cloud running yet.
                //Get Id and user name
                String id = getIntent().getExtras().getString("id");
                String name = getIntent().getExtras().getString("name");

                datasource.createPost(id, post.getText().toString(), name);
                Toast.makeText(CreatePost.this, "Successfully posted.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(CreatePost.this, news_feed.class);
                if(intent != null) {
                    startActivity(intent);
                }

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




}
