package com.tabs.activity;

import android.app.Application;
import android.content.Context;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.tabs.database.comments.Comment;

import com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.tabs.database.followers.FollowerRecyclerViewAdapter;
import com.tabs.database.posts.Post;
import com.tabs.database.posts.PostRecyclerViewAdapter;
import com.tabs.database.users.User;
import com.tabs.enums.AdapterEnum;
import com.tabs.enums.FollowerEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by KCKusumi on 2/1/2016.
 */
public class FireBaseApplication extends Application {
    private String name;
    private String userId;

    //For the public tab
    private PostRecyclerViewAdapter publicAdapter;

    //For the following tab
    private PostRecyclerViewAdapter followingPostAdapter;

    //For my own tab
    private PostRecyclerViewAdapter myTabsAdapter;

    //For another user's posts when you click on their name to view their profile
    private PostRecyclerViewAdapter userAdapter;

    //For the comments that you see when you click on a recycler view item
    private CommentsRecyclerViewAdapter commentsRecyclerViewAdapter;

    //For the followers you have when you click on view followers button
    private FollowerRecyclerViewAdapter followerRecyclerViewAdapter;

    //For the following you have when you click on view followers button
    private FollowerRecyclerViewAdapter followingRecyclerViewAdapter;

    //For the user's followers that can be seen when you click on view followers button
    private FollowerRecyclerViewAdapter userFollowersAdapter;

    //For the user's followers that can be seen when you click on view following button
    private FollowerRecyclerViewAdapter userFollowingAdapter;

    //For the user's posts that can be seen when you click on their profile picture and you go to the comments tab
    private PostRecyclerViewAdapter postsUserHasCommentedOnAdapter;

    //For the user's posts that can be seen when on profile tab and you go to the comments tab
    private PostRecyclerViewAdapter postsThatCurrentUserHasCommentedOnAdapter;

    private Integer commentsCount;

    private Integer postCount;

    private Integer userPostNum;

    private Integer userCommentNum;

    private Integer userFollowerNum;

    private Integer userFollowingNum;

    private Integer followerNum;

    private Integer followingNum;

    @Override
    public void onCreate() {
        super.onCreate();
        //DatabaseReference apps automatically handle temporary network interruptions for you.
        //Cached data will still be available while offline and your writes will be resent when network connectivity is recovered.
        // Enabling disk persistence allows our app to also keep all of its state even after an app restart.
        // We can enable disk persistence with just one line of code.
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        initializeAdapters();
        //Configure Fresco so that image loads quickly
        configFresco();
        //Make sure that adapters don't

    }

    public Integer getCommentsCount() { return commentsCount; };

    public void setCommentsCount(Integer commentsCount) { this.commentsCount = commentsCount; }

    public Integer getUserPostNum() { return userPostNum; };

    public void setUserPostNum(Integer userPostNum) { this.userPostNum = userPostNum; }

    public Integer getUserFollowerNum() { return userFollowerNum; };

    public void setUserFollowerNum(Integer userFollowerNum) { this.userFollowerNum = userFollowerNum; }

    public Integer getUserFollowingNum() { return userFollowingNum; };

    public void setUserFollowingNum(Integer userFollowingNum) { this.userFollowingNum = userFollowingNum; }

    public Integer getFollowerNum() { return followerNum; };

    public void setFollowerNum(Integer followerNum) { this.followerNum = followerNum; }

    public Integer getFollowingNum() { return followingNum; };

    public void setFollowingNum(Integer followingNum) { this.followingNum = followingNum; }

    public Integer getUserCommentNum() { return userCommentNum; };

    public void setUserCommentNum(Integer userCommentNum) {
        this.userCommentNum = userCommentNum;
    }

    public Integer getPostCount() { return postCount; };

    public void setPostCount(Integer postCount) { this.postCount = postCount; }

    public String getName() {
        return name;
    }

    public void setName(String userName) {
        name = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String currentUserId) {
         userId = currentUserId;
    }

    public PostRecyclerViewAdapter getPublicAdapter() {
        return publicAdapter;
    }

    public void setPublicAdapter(PostRecyclerViewAdapter publicAdapter) {
        this.publicAdapter = publicAdapter;
    }

    public PostRecyclerViewAdapter getMyTabsAdapter() {
        return myTabsAdapter;
    }

    public void setMyTabsAdapter(PostRecyclerViewAdapter myTabsAdapter) {
        this.myTabsAdapter = myTabsAdapter;
    }
    public CommentsRecyclerViewAdapter getCommentsRecyclerViewAdapter(){
        return commentsRecyclerViewAdapter;
    }

    public void setFollowerRecyclerViewAdapter(FollowerRecyclerViewAdapter followerRecyclerViewAdapter) {
        this.followerRecyclerViewAdapter = followerRecyclerViewAdapter;
    }

    public FollowerRecyclerViewAdapter getFollowersRecyclerViewAdapter(){
        return followerRecyclerViewAdapter;
    }

    public FollowerRecyclerViewAdapter getFollowingRecyclerViewAdapter(){
        return followingRecyclerViewAdapter;
    }

    public void setCommentsRecyclerViewAdapter(CommentsRecyclerViewAdapter commentsRecyclerViewAdapter) {
        this.commentsRecyclerViewAdapter = commentsRecyclerViewAdapter;
    }

    public void setFollowingRecyclerViewAdapter(FollowerRecyclerViewAdapter followingRecyclerViewAdapter) {
        this.followingRecyclerViewAdapter = followingRecyclerViewAdapter;
    }

    public void setFollowingPostAdapter(PostRecyclerViewAdapter followingPostAdapter) {
        this.followingPostAdapter = followingPostAdapter;
    }

    public PostRecyclerViewAdapter getFollowingPostAdapter() {
        return followingPostAdapter;
    }

    public PostRecyclerViewAdapter getUserAdapter() { return userAdapter; }

    public void setUserAdapter(PostRecyclerViewAdapter userAdapter) { this.userAdapter = userAdapter; }

    public PostRecyclerViewAdapter getPostsThatCurrentUserHasCommentedOnAdapter() { return postsThatCurrentUserHasCommentedOnAdapter; }

    public void setPostsThatCurrentUserHasCommentedOnAdapter(PostRecyclerViewAdapter postsThatCurrentUserHasCommentedOnAdapter) { this.postsThatCurrentUserHasCommentedOnAdapter = postsThatCurrentUserHasCommentedOnAdapter; }

    public PostRecyclerViewAdapter getPostsUserHasCommentedOnAdapter() { return postsUserHasCommentedOnAdapter; }

    public void setPostsUserHasCommentedOnAdapter(PostRecyclerViewAdapter postsUserHasCommentedOnAdapter) { this.postsUserHasCommentedOnAdapter = postsUserHasCommentedOnAdapter; }

    public void setUserFollowersAdapter(FollowerRecyclerViewAdapter userFollowersAdapter) {
        this.userFollowersAdapter = userFollowersAdapter;
    }

    public FollowerRecyclerViewAdapter getUserFollowingAdapter() {
        return userFollowingAdapter;
    }

    public void setUserFollowingAdapter(FollowerRecyclerViewAdapter userFollowingAdapter) {
        this.userFollowingAdapter = userFollowingAdapter;
    }

    public FollowerRecyclerViewAdapter getUserFollowersAdapter() {
        return userFollowersAdapter;
    }

    public void initializeAdapters() {
        List<User> followers = new ArrayList<>();
        List<User> following = new ArrayList<>();
        List<User> userFollowers = new ArrayList<>();
        List<User> userFollowing = new ArrayList<>();
        List<Post> publicPosts = new ArrayList<>();
        List<Post> myTabsPosts = new ArrayList<>();
        List<Post> followingPosts = new ArrayList<>();
        List<Post> userPosts = new ArrayList<>();
        List<Post> commentPosts = new ArrayList<>();
        List<Post> userCommentPosts = new ArrayList<>();
        setCommentsRecyclerViewAdapter(new CommentsRecyclerViewAdapter(this, new ArrayList<Comment>()));
        setPublicAdapter(new PostRecyclerViewAdapter(publicPosts, AdapterEnum.Public, this));
        setMyTabsAdapter(new PostRecyclerViewAdapter(myTabsPosts, AdapterEnum.ProfilePosts, this));
        setFollowingPostAdapter(new PostRecyclerViewAdapter(followingPosts, AdapterEnum.Following, this));
        setFollowerRecyclerViewAdapter(new FollowerRecyclerViewAdapter(followers, this, FollowerEnum.ProfileFollower, this));
        setFollowingRecyclerViewAdapter(new FollowerRecyclerViewAdapter(following, this, FollowerEnum.ProfileFollowing, this));
        setUserFollowersAdapter(new FollowerRecyclerViewAdapter(userFollowers, this, FollowerEnum.UserFollower, this));
        setUserFollowingAdapter(new FollowerRecyclerViewAdapter(userFollowing, this, FollowerEnum.UserFollowing, this));
        setUserAdapter(new PostRecyclerViewAdapter(userPosts, AdapterEnum.UserPosts, this));
        setPostsThatCurrentUserHasCommentedOnAdapter(new PostRecyclerViewAdapter(commentPosts, AdapterEnum.ProfileComments, this));
        setPostsUserHasCommentedOnAdapter(new PostRecyclerViewAdapter(userCommentPosts, AdapterEnum.UserComments, this));
        setCommentsCount(0);
        setPostCount(0);
        setName("");
        setUserId("");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    /**
     * Initialize Fresco.
     */
    public void configFresco() {
        Supplier<File> diskSupplier = new Supplier<File>() {
            @Override
            public File get() {
                return getApplicationContext().getCacheDir();
            }
        };

        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(getApplicationContext())
                .setBaseDirectoryName("images")
                .setBaseDirectoryPathSupplier(diskSupplier)
                .build();

        ImagePipelineConfig frescoConfig = ImagePipelineConfig.newBuilder(this)
                .setMainDiskCacheConfig(diskCacheConfig)
                .build();

        Fresco.initialize(this, frescoConfig);
    }

}