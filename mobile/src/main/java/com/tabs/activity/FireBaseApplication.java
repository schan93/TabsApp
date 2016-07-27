package com.tabs.activity;

import android.app.Application;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.firebase.client.Firebase;
import com.tabs.database.comments.Comment;

import com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.tabs.database.followers.FollowerRecyclerViewAdapter;
import com.tabs.database.posts.Post;
import com.tabs.database.posts.PostRecyclerViewAdapter;
import com.tabs.database.users.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by KCKusumi on 2/1/2016.
 */
public class FireBaseApplication extends Application {
    //May be used in case
    private static boolean fromAnotherActivity = false;
    private static String name = "";
    private static String userId = "";

    //For the public tab
    private static PostRecyclerViewAdapter publicAdapter;

    //For the following tab
    private static PostRecyclerViewAdapter followingPostAdapter;

    //For my own tab
    private static PostRecyclerViewAdapter myTabsAdapter;

    //For another user's posts when you click on their name to view their profile
    private static PostRecyclerViewAdapter userAdapter;

    //For the comments that you see when you click on a recycler view item
    private static CommentsRecyclerViewAdapter commentsRecyclerViewAdapter;

    //For the followers you have when you click on view followers button
    private static FollowerRecyclerViewAdapter followerRecyclerViewAdapter;

    //For the following you have when you click on view followers button
    private static FollowerRecyclerViewAdapter followingRecyclerViewAdapter;

    //Not sure actually
    private static List<String> userInfoAdapter;

    //For the user's followers that can be seen when you click on view followers button
    private static FollowerRecyclerViewAdapter userFollowersAdapter;

    //For the user's followers that can be seen when you click on view following button
    private static FollowerRecyclerViewAdapter userFollowingAdapter;

    //For the user's posts that can be seen when you click on their profile picture and you go to the comments tab
    private static PostRecyclerViewAdapter postsUserHasCommentedOnAdapter;

    //For the user's posts that can be seen when on profile tab and you go to the comments tab
    private static PostRecyclerViewAdapter postsThatCurrentUserHasCommentedOnAdapter;

    private static Integer commentsCount;

    private static Integer postCount;


    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        //Firebase apps automatically handle temporary network interruptions for you.
        //Cached data will still be available while offline and your writes will be resent when network connectivity is recovered.
        // Enabling disk persistence allows our app to also keep all of its state even after an app restart.
        // We can enable disk persistence with just one line of code.
        Firebase.getDefaultConfig().setPersistenceEnabled(true);

        initializeAdapters();
        //Configure Fresco so that image loads quickly
        configFresco();
        //Make sure that adapters don't

    }

    public static Integer getCommentsCount() { return commentsCount; };

    public void setCommentsCount(Integer commentsCount) { this.commentsCount = commentsCount; }

    public static Integer getPostCount() { return postCount; };

    public void setPostCount(Integer postCount) { this.postCount = postCount; }

    public static String getName() {
        return name;
    }

    public void setName(String userName) {
        name = userName;
    }

    public static String getUserId() {
        return userId;
    }

    public void setUserId(String currentUserId) {
         userId = currentUserId;
    }

    public static PostRecyclerViewAdapter getPublicAdapter() {
        return publicAdapter;
    }

    public void setPublicAdapter(PostRecyclerViewAdapter publicAdapter) {
        this.publicAdapter = publicAdapter;
    }

    public static PostRecyclerViewAdapter getMyTabsAdapter() {
        return myTabsAdapter;
    }

    public void setMyTabsAdapter(PostRecyclerViewAdapter myTabsAdapter) {
        this.myTabsAdapter = myTabsAdapter;
    }
    public static CommentsRecyclerViewAdapter getCommentsRecyclerViewAdapter(){
        return commentsRecyclerViewAdapter;
    }

    public void setFollowerRecyclerViewAdapter(FollowerRecyclerViewAdapter followerRecyclerViewAdapter) {
        this.followerRecyclerViewAdapter = followerRecyclerViewAdapter;
    }

    public static FollowerRecyclerViewAdapter getFollowersRecyclerViewAdapter(){
        return followerRecyclerViewAdapter;
    }

    public static FollowerRecyclerViewAdapter getFollowingRecyclerViewAdapter(){
        return followingRecyclerViewAdapter;
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

    public void setFollowingRecyclerViewAdapter(FollowerRecyclerViewAdapter followingRecyclerViewAdapter) {
        this.followingRecyclerViewAdapter = followingRecyclerViewAdapter;
    }

    public void setFollowingPostAdapter(PostRecyclerViewAdapter followingPostAdapter) {
        this.followingPostAdapter = followingPostAdapter;
    }

    public void setUserInfoAdapter(List<String> userInfoAdapter) {
        this.userInfoAdapter = userInfoAdapter;
    }

    public static List<String> getUserInfoAdapter() {
        return userInfoAdapter;
    }

    public static PostRecyclerViewAdapter getFollowingPostAdapter() {
        return followingPostAdapter;
    }

    public static PostRecyclerViewAdapter getUserAdapter() { return userAdapter; }

    public void setUserAdapter(PostRecyclerViewAdapter userAdapter) { this.userAdapter = userAdapter; }

    public static PostRecyclerViewAdapter getPostsThatCurrentUserHasCommentedOnAdapter() { return postsThatCurrentUserHasCommentedOnAdapter; }

    public void setPostsThatCurrentUserHasCommentedOnAdapter(PostRecyclerViewAdapter postsThatCurrentUserHasCommentedOnAdapter) { this.postsThatCurrentUserHasCommentedOnAdapter = postsThatCurrentUserHasCommentedOnAdapter; }

    public static PostRecyclerViewAdapter getPostsUserHasCommentedOnAdapter() { return postsUserHasCommentedOnAdapter; }

    public void setPostsUserHasCommentedOnAdapter(PostRecyclerViewAdapter postsUserHasCommentedOnAdapter) { this.postsUserHasCommentedOnAdapter = postsUserHasCommentedOnAdapter; }

    public void setUserFollowersAdapter(FollowerRecyclerViewAdapter userFollowersAdapter) {
        this.userFollowersAdapter = userFollowersAdapter;
    }

    public FollowerRecyclerViewAdapter getUserFollowingAdapter() {
        return userFollowersAdapter;
    }

    public void setUserFollowingAdapter(FollowerRecyclerViewAdapter userFollowingAdapter) {
        this.userFollowingAdapter = userFollowingAdapter;
    }

    public FollowerRecyclerViewAdapter getUserFollowersAdapter() {
        return userFollowingAdapter;
    }

    public void initializeAdapters() {
        List<User> followers = new ArrayList<User>();
        List<User> following = new ArrayList<User>();
        List<User> userFollowers = new ArrayList<User>();
        List<User> userFollowing = new ArrayList<User>();
        List<Post> publicPosts = new ArrayList<Post>();
        List<Post> myTabsPosts = new ArrayList<Post>();
        List<Post> followingPosts = new ArrayList<>();
        List<Post> userPosts = new ArrayList<>();
        List<Post> commentPosts = new ArrayList<>();
        List<Post> userCommentPosts = new ArrayList<>();
        List<String> userInfoAdapter = new ArrayList<>();
        setCommentsRecyclerViewAdapter(new CommentsRecyclerViewAdapter(new CommentsHeader(), new ArrayList<Comment>()));
        setPublicAdapter(new PostRecyclerViewAdapter(publicPosts, this, TabEnum.Public));
        setMyTabsAdapter(new PostRecyclerViewAdapter(myTabsPosts, this, TabEnum.MyTab));
        setFollowingPostAdapter(new PostRecyclerViewAdapter(followingPosts, this, TabEnum.Following));
        setFollowerRecyclerViewAdapter(new FollowerRecyclerViewAdapter(followers, this));
        setFollowingRecyclerViewAdapter(new FollowerRecyclerViewAdapter(following, this));
        setUserFollowersAdapter(new FollowerRecyclerViewAdapter(userFollowers));
        setUserFollowingAdapter(new FollowerRecyclerViewAdapter(userFollowing));
        setUserAdapter(new PostRecyclerViewAdapter(userPosts, this, null));
        setPostsThatCurrentUserHasCommentedOnAdapter(new PostRecyclerViewAdapter(commentPosts, this, null));
        setPostsUserHasCommentedOnAdapter(new PostRecyclerViewAdapter(userCommentPosts, this, null));
        setUserInfoAdapter(userInfoAdapter);
        setCommentsCount(0);
        setPostCount(0);
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

        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder()
                .setBaseDirectoryName("images")
                .setBaseDirectoryPathSupplier(diskSupplier)
                .build();

        ImagePipelineConfig frescoConfig = ImagePipelineConfig.newBuilder(this)
                .setMainDiskCacheConfig(diskCacheConfig)
                .build();

        Fresco.initialize(this, frescoConfig);
    }

}