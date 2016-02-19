package com.test.tabs.tabs.com.tabs.activity;

import android.app.Application;

import com.batch.android.Batch;
import com.batch.android.Config;
import com.firebase.client.Firebase;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsDataSource;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

/**
 * Created by KCKusumi on 2/1/2016.
 */
public class FireBaseApplication extends Application {
    //Local Database for storing friends, posts, comments
    private FriendsDataSource friendsDataSource;
    private PostsDataSource postsDataSource;
    private CommentsDataSource commentsDataSource;

    private Firebase myFirebaseRef;

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);

        myFirebaseRef = new Firebase("https://tabsapp.firebaseio.com/");

        //Batch push notifications
        //Batch.setConfig(new Config("DEV56BC2B1738EFE251617C406E76D"));
        Batch.Push.setGCMSenderId("213033849274");
        Batch.setConfig(new Config("AIzaSyCP0MX6xM67bdd3-2cqCVjHqVFvF4HgcIw"));
    }

    private void stateDataBase(){
        postsDataSource = new PostsDataSource(this);
        postsDataSource.open();
        commentsDataSource = new CommentsDataSource(this);
        commentsDataSource.open();
        friendsDataSource = new FriendsDataSource(this);
        friendsDataSource.open();
    }

}
