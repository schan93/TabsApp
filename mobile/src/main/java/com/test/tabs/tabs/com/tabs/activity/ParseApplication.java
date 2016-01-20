package com.test.tabs.tabs.com.tabs.activity;

import android.app.Application;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsDataSource;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

import java.util.List;

/**
 * Created by schan on 1/14/16.
 */


public class ParseApplication extends Application{

    //Local Database for storing friends, posts, comments
    private FriendsDataSource friendsDataSource;
    private PostsDataSource postsDataSource;
    private CommentsDataSource commentsDataSource;

    String userId;

    @Override
    public void onCreate(){
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this);

        startDatabases();
    }

    private void storePost(ParseObject post){
    }

    private void storeComment(ParseObject comment){

    }

    private void storeFriend(ParseObject friend){
        //User = the user id of the person logged in
        //User_id = the user id of the friend
//        friendsDataSource.createFriend(friend.get("friendName").toString(), friend.get("friendUserId").toString(), friend.getString("friendUserId").toString(), Integer.parseInt(friend.get("isFriend").toString()));
    }

    private void startDatabases(){
        postsDataSource = new PostsDataSource(this);
        postsDataSource.open();
        commentsDataSource = new CommentsDataSource(this);
        commentsDataSource.open();
        friendsDataSource = new FriendsDataSource(this);
        friendsDataSource.open();
    }

}
