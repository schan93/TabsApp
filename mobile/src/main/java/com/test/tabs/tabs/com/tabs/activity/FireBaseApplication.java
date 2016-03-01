package com.test.tabs.tabs.com.tabs.activity;

import android.app.Application;

import com.batch.android.Batch;
import com.batch.android.Config;
import com.firebase.client.Firebase;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsDataSource;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsListAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

/**
 * Created by KCKusumi on 2/1/2016.
 */
public class FireBaseApplication extends Application {
    //Local Database for storing friends, posts, comments
    private FriendsDataSource friendsDataSource;
    private PostsDataSource postsDataSource;
    private CommentsDataSource commentsDataSource;
    private static boolean fromAnotherActivity = false;

    private static FriendsListAdapter friendsAdapter;
    private static PostRecyclerViewAdapter publicAdapter;
    private static PostRecyclerViewAdapter privateAdapter;
    private static PostRecyclerViewAdapter myTabsAdapter;
    private static CommentsRecyclerViewAdapter commentsRecyclerViewAdapter;

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

        //Make sure that adapters don't

    }

    public static PostRecyclerViewAdapter getPublicAdapter() {
        return publicAdapter;
    }

    public void setPublicAdapter(PostRecyclerViewAdapter publicAdapter) {
        this.publicAdapter = publicAdapter;
    }

    public static PostRecyclerViewAdapter getPrivateAdapter() {
        return privateAdapter;
    }

    public void setPrivateAdapter(PostRecyclerViewAdapter privateAdapter) {
        this.privateAdapter = privateAdapter;
    }

    public static PostRecyclerViewAdapter getMyTabsAdapter() {
        return myTabsAdapter;
    }

    public void setMyTabsAdapter(PostRecyclerViewAdapter myTabsAdapter) {
        this.myTabsAdapter = myTabsAdapter;
    }

    public static FriendsListAdapter getFriendsAdapter() {
        return friendsAdapter;
    }

    public void setFriendsAdapter(FriendsListAdapter friendsAdapter) {
        this.friendsAdapter = friendsAdapter;
    }

    public static CommentsRecyclerViewAdapter getCommentsRecyclerViewAdapter(){
        return commentsRecyclerViewAdapter;
    }

    public void setCommentsRecyclerViewAdapter(CommentsRecyclerViewAdapter commentsRecyclerViewAdapter) {
        this.commentsRecyclerViewAdapter = commentsRecyclerViewAdapter;
    }

    public void setFromAnotherActivity(boolean fromAnotherActivity) {
        this.fromAnotherActivity = fromAnotherActivity;
    }

    public static boolean getFromAnotherActivity() {
        return fromAnotherActivity;
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
