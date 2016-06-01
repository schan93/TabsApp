package com.test.tabs.tabs.com.tabs.database.Database;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.activity.AndroidUtils;
import com.test.tabs.tabs.com.tabs.activity.Comments;
import com.test.tabs.tabs.com.tabs.activity.CommentsHeader;
import com.test.tabs.tabs.com.tabs.activity.CompanionEnum;
import com.test.tabs.tabs.com.tabs.activity.FireBaseApplication;
import com.test.tabs.tabs.com.tabs.activity.PrivacyEnum;
import com.test.tabs.tabs.com.tabs.activity.TabEnum;
import com.test.tabs.tabs.com.tabs.activity.TabsUtil;
import com.test.tabs.tabs.com.tabs.activity.login;
import com.test.tabs.tabs.com.tabs.activity.news_feed;
import com.test.tabs.tabs.com.tabs.database.comments.Comment;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.followers.Follower;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.users.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by schan on 3/9/16.
 */
public class DatabaseQuery implements Serializable {

    private Firebase firebaseRef = new Firebase("https://tabsapp.firebaseio.com/");
    private Activity activity;
    private FireBaseApplication application;

    public DatabaseQuery(Activity activity) {
        this.activity = activity;
        application = (FireBaseApplication) this.activity.getApplication();
    }

    /**
     * This method is designed to save a freind to Firebase database.
     * User = the user id of the person logged in
     * User_id = the user id of the friend
     *
     * @param friend
     */

    public void saveFriendToFirebase(final Friend friend) {
        final Firebase friendsRef = firebaseRef.child("Friends/" + friend.getUser());
        Query query = friendsRef.orderByChild("userId").equalTo(friend.getUser());
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Friend newFreind = friend;
                    Firebase friendsReference = friendsRef.push();
                    String friendId = friendsReference.getKey();
                    newFreind.setId(friendId);
                    friendsReference.setValue(newFreind);
                } else {
                    System.out.println("DatabaseQuery: User " + friend.getName() + " Already exists");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void saveCommentToFirebase(Comment comment) {
        Firebase commentsRef = firebaseRef.child("Comments").push();
        String commentId = commentsRef.getKey();
        comment.setId(commentId);
        Date date = new Date();
        commentsRef.setValue(comment, 0 - date.getTime());
    }

    public void saveUserToFirebase(final String userId, final String name) {
        final Firebase usersRef = firebaseRef.child("Users");
        Query query = usersRef.orderByChild("userId").equalTo(userId);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Firebase usersReference = usersRef.push();
                    String id = usersReference.getKey();
                    User user = new User(id, userId, name);
                    usersReference.setValue(user);
                } else {
                    System.out.println("DatabaseQuery: User " + name + " Already exists");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    /**
     * Save a follower friend to firebase & update the recycler view.
     * @param follower
     */
    public void saveFollowerToFirebase(final Follower follower) {
        Firebase followersRef = firebaseRef.child("Followers/" + follower.getUser()).push();
        String followerId = followersRef.getKey();
        follower.setId(followerId);
        followersRef.setValue(follower);
    }


    /**
     * Update a Follower to Firebase
     *
     * @param follower
     */

    public void updateFollowerToFirebase(Follower follower) {
        //Need to check if the we should even update the freinds list
        Firebase reference = new Firebase("https://tabsapp.firebaseio.com/Followers");
        Map<String, Object> updatedFollower = new HashMap<String, Object>();
        String isFollowing = follower.getIsFollowing();
        updatedFollower.put(follower.getUser() + "/" + follower.getId() + "/isFollowing", follower.getIsFollowing());
        application.setFromAnotherActivity(true);
        //Need to clear posts and friends because we have updated the friends and posts at this point
        application.getFollowerPostAdapter().getPosts().clear();
        reference.updateChildren(updatedFollower, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    System.out.println("There was an error saving data. ");
                } else {
                    if (application.getFromAnotherActivity() == true) {
                        application.setFromAnotherActivity(false);
                    }
                }
            }
        });
    }

    /**
     * This method is designed to save a post to Firebase database.
     *
     * @param post
     */

    public void savePostToFirebase(Post post) {
        Firebase postsRef = firebaseRef.child("Posts").push();
        String postId = postsRef.getKey();
        post.setId(postId);
        Date date = new Date();
        postsRef.setValue(post, date.getTime() - 0);
    }

    public void removeFriendPosts(Friend friend) {
        //Get all the posts related to a friend that you're removing.
        List<Post> changedFriendPosts = new ArrayList<>();
        if(friend.getIsFriend().equals("false")) {
            int length = application.getPrivateAdapter().getItemCount();
            List<Post> privatePosts = application.getPrivateAdapter().getPosts();
            for(int i = 0; i < length; i++) {
                if(privatePosts.get(i).getPosterUserId().equals(friend.getUserId())) {
                    changedFriendPosts.add(privatePosts.get(i));
                }
            }
        }
        //Now that we have all the posts by an individual that we want to remove, simply remove them from the query
        application.getPrivateAdapter().getPosts().removeAll(changedFriendPosts);
        application.getPrivateAdapter().notifyDataSetChanged();
    }

    public void addFriendPosts(final Friend friend) {
        //Need to do order by / equal to.
        Firebase postsRef = firebaseRef.child("Posts");
        String userId = friend.getUserId();
        Query query = postsRef.orderByChild("posterUserId").equalTo(userId);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Post post = postSnapShot.getValue(Post.class);
                    if (post.getPrivacy() == PrivacyEnum.Friends && friend.getIsFriend().equals("true")) {
                        application.getPrivateAdapter().getPosts().add(0, post);
                    }
                }
                application.getPrivateAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    public void removeFollowerPosts(Follower follower) {
        //Get all the posts related to a friend that you're removing.
        List<Post> changedFollowerPosts = new ArrayList<>();
        if(follower.getIsFollowing().equals("false")) {
            int length = application.getPublicAdapter().getItemCount();
            List<Post> followerPosts = application.getPublicAdapter().getPosts();
            for(int i = 0; i < length; i++) {
                if(followerPosts.get(i).getPosterUserId().equals(follower.getUserId())) {
                    changedFollowerPosts.add(followerPosts.get(i));
                }
            }
        }
        //Now that we have all the posts by an individual that we want to remove, simply remove them from the query
        application.getFollowerPostAdapter().getPosts().removeAll(changedFollowerPosts);
    }

    public void addFollowerPosts(final Follower follower) {
        //Need to do order by / equal to.
        Firebase postsRef = firebaseRef.child("Posts");
        String userId = follower.getUserId();
        Query query = postsRef.orderByChild("posterUserId").equalTo(userId);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Post post = postSnapShot.getValue(Post.class);
                    if (post.getPrivacy() == PrivacyEnum.Public && follower.getIsFollowing().equals("true")) {
                        application.getFollowerPostAdapter().getPosts().add(0, post);
                    }
                }
                application.getFollowerPostAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    public void getFriendsPosts(final View progressOverlay, final View fragmentView, final Context context) {
        //Need to do order by / equal to. Need to get every post EQUAL TO the
        Firebase postsRef = firebaseRef.child("Posts");
        Query query = postsRef.orderByChild("privacy").equalTo(PrivacyEnum.Friends.toString());
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Post post = postSnapShot.getValue(Post.class);
                    //Get all the private posts
                    List<Post> privatePosts = application.getPrivateAdapter().getPosts();
                    //If the posts is equal to private, then we add it into the private posts static adapter
                    if (application.getPrivateAdapter().containsId(privatePosts, post.getId()) == null) {
                        List<Friend> friends = application.getFriendsRecyclerViewAdapter().getFriends();
                        Friend friend = application.getFriendsRecyclerViewAdapter().containsId(friends, post.getPosterUserId());
                        if (friend != null && friend.getIsFriend().equals("true")) {
                            application.getPrivateAdapter().getPosts().add(0, post);
                        }
                    }
                }
                TabsUtil.populateNewsFeedList(fragmentView, application.getPrivateAdapter(), TabEnum.Friends, context);
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    System.out.println("getFriendsPosts: GONE");
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                    fragmentView.findViewById(R.id.rv_private_feed).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                TabsUtil.populateNewsFeedList(fragmentView, application.getPrivateAdapter(), TabEnum.Friends, context);
            }
        });

        postsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //checks if a post is added
                Post newPost = dataSnapshot.getValue(Post.class);
                List<Post> privatePosts = application.getPrivateAdapter().getPosts();
                if (newPost.getPrivacy() == PrivacyEnum.Friends && application.getPrivateAdapter().containsId(privatePosts, newPost.getId()) == null) {
                    List<Friend> friends = application.getFriendsRecyclerViewAdapter().getFriends();
                    Friend friend = application.getFriendsRecyclerViewAdapter().containsId(friends, newPost.getPosterUserId());
                    if (friend != null && friend.getIsFriend().equals("true")) {
                        application.getPrivateAdapter().getPosts().add(0, newPost);
                        application.getFriendsRecyclerViewAdapter().notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Post changedPost = dataSnapshot.getValue(Post.class);
                int length = application.getPrivateAdapter().getPosts().size();
                for (int i = 0; i < length; i++) {
                    if (application.getPrivateAdapter().getPosts().get(i).getId().equals(changedPost.getId())) {
                        application.getPrivateAdapter().getPosts().set(i, changedPost);
                    }
                }
                application.getPrivateAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Post removedPost = dataSnapshot.getValue(Post.class);
                int length = application.getPrivateAdapter().getPosts().size();
                for (int i = 0; i < length; i++) {
                    if (application.getPrivateAdapter().getPosts().get(i).getId().equals(removedPost.getId())) {
                        application.getPrivateAdapter().getPosts().remove(i);
                    }
                }
                application.getPrivateAdapter().notifyDataSetChanged();
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

    public void getFollowerPosts(final View progressOverlay, final View fragmentView, final Context context) {
        //Need to do order by / equal to.
        Firebase postsRef = firebaseRef.child("Posts");
        Query query = postsRef.orderByChild("privacy").equalTo(PrivacyEnum.Public.toString());
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    new AsyncTask<URL, Integer, Long>() {
                        @Override
                        protected Long doInBackground(URL... params) {
                            Post post = postSnapShot.getValue(Post.class);
                            //Get all the private posts
                            List<Post> publicPosts = application.getPublicAdapter().getPosts();
                            //If the posts is equal to private, then we add it into the private posts static adapter
                            if (application.getPublicAdapter().containsId(publicPosts, post.getId()) == null) {
                                List<Follower> followers = application.getFollowerRecyclerViewAdapter().getFollowers();
                                Follower follower = application.getFollowerRecyclerViewAdapter().containsId(followers, post.getPosterUserId());
                                if (follower != null && follower.getIsFollowing().equals("true")) {
                                    application.getFollowerPostAdapter().getPosts().add(0, post);
                                }
                            }
                            return null;
                        }

                        @Override
                        protected void onProgressUpdate(Integer... progress) {
                        }

                        @Override
                        protected void onPostExecute(Long result) {
                            TabsUtil.populateNewsFeedList(fragmentView, application.getFollowerPostAdapter(), TabEnum.Following, context);
                            if (progressOverlay.getVisibility() == View.VISIBLE) {
                                System.out.println("getFollowerPosts: GONE");
                                AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                                fragmentView.findViewById(R.id.rv_followers_feed).setVisibility(View.VISIBLE);
                            }
                        }
                    };
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                TabsUtil.populateNewsFeedList(fragmentView, application.getFollowerPostAdapter(), TabEnum.Following, context);
            }
        });

        postsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //checks if a post is added
                Post newPost = dataSnapshot.getValue(Post.class);
                List<Post> publicPosts = application.getPublicAdapter().getPosts();
                if (newPost.getPrivacy().equals("PublicTab") && application.getPublicAdapter().containsId(publicPosts, newPost.getId()) == null) {
                    List<Follower> followers = application.getFollowerRecyclerViewAdapter().getFollowers();
                    Follower follower = application.getFollowerRecyclerViewAdapter().containsId(followers, newPost.getPosterUserId());
                    if (follower != null && follower.getIsFollowing().equals("true")) {
                        application.getPublicAdapter().getPosts().add(0, newPost);
                        application.getPublicAdapter().notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Post changedPost = dataSnapshot.getValue(Post.class);
                int length = application.getPublicAdapter().getPosts().size();
                for (int i = 0; i < length; i++) {
                    if (application.getPublicAdapter().getPosts().get(i).getId().equals(changedPost.getId())) {
                        application.getPublicAdapter().getPosts().set(i, changedPost);
                    }
                }
                application.getPublicAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Post removedPost = dataSnapshot.getValue(Post.class);
                int length = application.getPublicAdapter().getPosts().size();
                for (int i = 0; i < length; i++) {
                    if (application.getPublicAdapter().getPosts().get(i).getId().equals(removedPost.getId())) {
                        application.getPublicAdapter().getPosts().remove(i);
                    }
                }
                application.getPublicAdapter().notifyDataSetChanged();
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

    public void getPublicPosts(final View progressOverlay, final View fragmentView, final Context context) {
        //Need to do order by / equal to.
        Firebase postsRef = firebaseRef.child("Posts");
        Query query = postsRef.orderByChild("privacy").equalTo(PrivacyEnum.Public.toString());
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    AsyncTask task = new AsyncTask<URL, Integer, Long>() {
                        @Override
                        protected Long doInBackground(URL... params) {
                            Post post = postSnapShot.getValue(Post.class);
                            List<Post> publicPosts = application.getPublicAdapter().getPosts();
                            if (post.getPrivacy() == PrivacyEnum.Public && application.getPublicAdapter().containsId(publicPosts, post.getId()) == null) {
                                application.getPublicAdapter().getPosts().add(0, post);
                            }
                            return null;
                        }
                        @Override
                        protected void onProgressUpdate(Integer... progress) {
                        }

                        @Override
                        protected void onPostExecute(Long result) {
                            System.out.println("Finished executing public");
                            TabsUtil.populateNewsFeedList(fragmentView, application.getPublicAdapter(), TabEnum.Public, context);
                            if (progressOverlay.getVisibility() == View.VISIBLE) {
                                System.out.println("getPublicPosts: GONE");
                                AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                                fragmentView.findViewById(R.id.rv_public_feed).setVisibility(View.VISIBLE);
                            }
                        }
                    };
//                    task.execute();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                TabsUtil.populateNewsFeedList(fragmentView, application.getPublicAdapter(), TabEnum.Public, context);
            }
        });

        postsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post newPost = dataSnapshot.getValue(Post.class);
                List<Post> publicPosts = application.getPublicAdapter().getPosts();
                if (application.getPublicAdapter().containsId(publicPosts, newPost.getId()) == null && newPost.getPrivacy() == PrivacyEnum.Public) {
                    application.getPublicAdapter().getPosts().add(0, newPost);
                    application.getPublicAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Post changedPost = dataSnapshot.getValue(Post.class);
                int length = application.getPublicAdapter().getPosts().size();
                for (int i = 0; i < length; i++) {
                    if (application.getPublicAdapter().getPosts().get(i).getId().equals(changedPost.getId())) {
                        application.getPublicAdapter().getPosts().set(i, changedPost);
                    }
                }
                application.getPublicAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Post removedPost = dataSnapshot.getValue(Post.class);
                int length = application.getPublicAdapter().getPosts().size();
                for (int i = 0; i < length; i++) {
                    if (application.getPublicAdapter().getPosts().get(i).getId().equals(removedPost.getId())) {
                        application.getPublicAdapter().getPosts().remove(i);
                    }
                }
                application.getPublicAdapter().notifyDataSetChanged();
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

    public void getFollowers(final String userId, final Boolean loggedIn, final Activity activity) {
        Firebase followersRef = firebaseRef.child("Followers/" + userId);
        followersRef.keepSynced(true);
        followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot friendSnapShot : dataSnapshot.getChildren()) {
                    Follower follower = friendSnapShot.getValue(Follower.class);
                    String userId = follower.getUserId();
                    List<Follower> followers = application.getFollowerRecyclerViewAdapter().getFollowers();
                    if (application.getFollowerRecyclerViewAdapter().containsId(followers, userId) == null) {
                        application.getFollowerRecyclerViewAdapter().getFollowers().add(follower);
                    }
                }
                if (application.getFromAnotherActivity() == false) {
                    setupNextActivity(loggedIn, activity);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        followersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Follower newFollower = dataSnapshot.getValue(Follower.class);
                List<Follower> followers = application.getFollowerRecyclerViewAdapter().getFollowers();
                if (application.getFollowerRecyclerViewAdapter().containsId(followers, newFollower.getUserId()) == null) {
                    application.getFollowerRecyclerViewAdapter().getFollowers().add(newFollower);
                    application.getFollowerRecyclerViewAdapter().notifyDataSetChanged();
                }
                addFollowerPosts(newFollower);
            }



            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //We need to get the number of posts that that changed friend has and update the private adapters post
                Follower changedFollower = dataSnapshot.getValue(Follower.class);
                if(changedFollower.getIsFollowing().equals("false")) {
                    removeFollowerPosts(changedFollower);
                } else {
                    addFollowerPosts(changedFollower);
                }
                application.getFollowerRecyclerViewAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Follower removedFollower = dataSnapshot.getValue(Follower.class);
                int length = application.getFollowerRecyclerViewAdapter().getItemCount();
                for (int i = 0; i < length; i++) {
                    if (application.getFollowerRecyclerViewAdapter().getFollowers().get(i).getId().equals(removedFollower.getId())) {
                        application.getFollowerRecyclerViewAdapter().getFollowers().remove(i);
                    }
                }
                application.getFollowerRecyclerViewAdapter().notifyDataSetChanged();
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

    public void getFriends(final String userId, final Boolean loggedIn, final Activity activity) {
        Firebase friendsRef = firebaseRef.child("Friends/" + userId);
        friendsRef.keepSynced(true);
        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot friendSnapShot : dataSnapshot.getChildren()) {
                    Friend friend = friendSnapShot.getValue(Friend.class);
                    String userId = friend.getUserId();
                    List<Friend> friends = application.getFriendsRecyclerViewAdapter().getFriends();
                    if (application.getFriendsRecyclerViewAdapter().containsId(friends, userId) == null) {
                        System.out.println("login2: Adding Friend " + friend.getName() + " to array");
                        application.getFriendsRecyclerViewAdapter().getFriends().add(friend);
                    }
                }
                getFriendsFromFacebook(userId, loggedIn, activity);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        friendsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Friend newFriend = dataSnapshot.getValue(Friend.class);
                List<Friend> friends = application.getFriendsRecyclerViewAdapter().getFriends();
                if (application.getFriendsRecyclerViewAdapter().containsId(friends, newFriend.getUserId()) == null) {
                    application.getFriendsRecyclerViewAdapter().getFriends().add(newFriend);
                    application.getFriendsRecyclerViewAdapter().notifyDataSetChanged();
                }
                addFriendPosts(newFriend);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //We need to get the number of posts that that changed friend has and update the private adapters post
                Friend changedFriend = dataSnapshot.getValue(Friend.class);
                List<Post> changedFriendPosts = new ArrayList<>();
                if(changedFriend.getIsFriend().equals("false")) {
                    removeFriendPosts(changedFriend);
                } else {
                    addFriendPosts(changedFriend);
                }
                application.getFriendsRecyclerViewAdapter().notifyDataSetChanged();
                //If the friend is changed, we also need to add all the posts that comes with a newly added friend
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Friend removedFriend = dataSnapshot.getValue(Friend.class);
                int length = application.getFriendsRecyclerViewAdapter().getItemCount();
                for (int i = 0; i < length; i++) {
                    if (application.getFriendsRecyclerViewAdapter().getFriends().get(i).getId().equals(removedFriend.getId())) {
                        application.getFriendsRecyclerViewAdapter().getFriends().remove(i);
                    }
                }
                removeFriendPosts(removedFriend);
                application.getFriendsRecyclerViewAdapter().notifyDataSetChanged();
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

    /**
     * This method is designed to get all the friends of the current user based on their userId from Facebook.
     */
    public void getFriendsFromFacebook(final String userId, final Boolean loggedIn, final Activity activity){
        final Handler handler = new Handler();
        GraphRequest friendsRequest = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(final JSONArray jsonArray, GraphResponse response) {
                        try {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String friendId = jsonObject.getString("id");
                                String name = jsonObject.getString("name");
                                Friend friend = new Friend("", friendId, name, application.getUserId(), "false");
                                List<Friend> friends = application.getFriendsRecyclerViewAdapter().getFriends();
                                if (application.getFriendsRecyclerViewAdapter().containsId(friends, friend.getUserId()) == null) {
                                    application.getFriendsRecyclerViewAdapter().getFriends().add(friend);
                                    saveFriendToFirebase(friend);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            //This is after getting all the friends has completed
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    getFollowers(userId, loggedIn, activity);
                                }
                            });
                        }
                    }
                });
        friendsRequest.executeAsync();
    }

    /**
     * This method is designed to move the user to the next activity after getting all the details from Firebase and storing information
     * into the local database is performed. This is because doing so will make our application function a lot faster
     * than getting everything on the fly.
     */
    private void setupNextActivity(Boolean loggedIn, Activity activity) {
        if (loggedIn) {
            activity.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
        Intent intent = new Intent(activity, news_feed.class);
        if (intent != null) {
            activity.startActivity(intent);
        }
    }


    /**
     *  This method is designed to get the user since he/she doesn't exist.
     *  We have to perform a Facebook batch request & insert their data into the local database.
     * @param userId
     * @return
     */
    public void getUserFromFacebook(final String userId, final Boolean loggedIn, final Activity activity) {
        final Handler handler = new Handler();
        final User[] user = new User[1];
//        User user = new User(id, userId, name);
        GraphRequest meRequest = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject jsonObject,
                            GraphResponse response) {
                        try {
                            String userId = jsonObject.getString("id");
                            String name = jsonObject.getString("name");
                            saveUserToFirebase(userId, name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            //This is after getting the user has completed
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    getFriends(userId, loggedIn, activity);
                                }
                            });
                        }
                    }
                });
        meRequest.executeAsync();
        return;
    }

    //Put the loading screen thing
    public void getComments(final String postId, final Activity activity, final View fragmentView, final View progressOverlay) {
        final List<Comment> commentItems = new ArrayList<>();
        application.setCommentsRecyclerViewAdapter(new CommentsRecyclerViewAdapter(application, activity, new CommentsHeader(), commentItems));
        Firebase commentsRef = firebaseRef.child("Comments");
        Query query = commentsRef.orderByChild("postId").equalTo(postId);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot commentSnapShot : dataSnapshot.getChildren()) {
                    Comment newComment = commentSnapShot.getValue(Comment.class);
                    if (application.getCommentsRecyclerViewAdapter().containsId(commentItems, newComment.getId()) == null && newComment.getPostId().equals(postId) ) {
                        application.getCommentsRecyclerViewAdapter().getCommentsList().add(newComment);
                    }
                }
                application.getCommentsRecyclerViewAdapter().notifyDataSetChanged();
                Comments.setupCommentsAdapter(postId, activity);
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    System.out.println("getComments DB: GONE");
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                    fragmentView.findViewById(R.id.view_comments).setVisibility(View.VISIBLE);
                }
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

    public void getMyTabsPosts(final View progressOverlay, final View fragmentView, final Context context) {
        //Need to do order by / equal to.
        final String userId = application.getUserId();
        Firebase postsRef = firebaseRef.child("Posts");
        Query query = postsRef.orderByChild("posterUserId").equalTo(userId);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Post post = postSnapShot.getValue(Post.class);
                    List<Post> myTabsPosts = application.getMyTabsAdapter().getPosts();
                    if (application.getMyTabsAdapter().containsId(myTabsPosts, post.getId()) == null && post.getPosterUserId().equals(userId)) {
                        application.getMyTabsAdapter().getPosts().add(0, post);
                    }
                }
                TabsUtil.populateNewsFeedList(fragmentView, application.getMyTabsAdapter(), TabEnum.MyTab, context);
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    System.out.println("getMyTabsPosts : GONE");
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                    fragmentView.findViewById(R.id.rv_my_tabs_feed).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        postsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post newPost = dataSnapshot.getValue(Post.class);
                List<Post> myTabsPosts = application.getMyTabsAdapter().getPosts();
                if (application.getMyTabsAdapter().containsId(myTabsPosts, newPost.getId()) == null && newPost.getPosterUserId().equals(userId)) {
                    application.getMyTabsAdapter().getPosts().add(0, newPost);
                    application.getMyTabsAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Post changedPost = dataSnapshot.getValue(Post.class);
                int length = application.getMyTabsAdapter().getPosts().size();
                for (int i = 0; i < length; i++) {
                    if (application.getMyTabsAdapter().getPosts().get(i).getId().equals(changedPost.getId())) {
                        application.getMyTabsAdapter().getPosts().set(i, changedPost);
                    }
                }
                application.getMyTabsAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Post removedPost = dataSnapshot.getValue(Post.class);
                int length = application.getMyTabsAdapter().getPosts().size();
                for (int i = 0; i < length; i++) {
                    if (application.getMyTabsAdapter().getPosts().get(i).getId().equals(removedPost.getId())) {
                        application.getMyTabsAdapter().getPosts().remove(i);
                    }
                }
                application.getMyTabsAdapter().notifyDataSetChanged();
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

    public void updateFollowersToFirebase(List<Follower> followers) {
        //Need to check if the we should even update the freinds list
        List<String> currentFollowerItems = new ArrayList<String>();
        if(currentFollowerItems.size() > 0) {
            currentFollowerItems.clear();
        }
        for(Follower follower: application.getFollowerRecyclerViewAdapter().getFollowers()) {
            currentFollowerItems.add(follower.getIsFollowing());
        }

        boolean areUpdated = checkUpdatedFollowers(followers, currentFollowerItems);
        if(areUpdated) {
            Firebase reference = new Firebase("https://tabsapp.firebaseio.com/Followers");
            Map<String, Object> updatedFriends = new HashMap<String, Object>();
            for(Follower follower: followers) {
                updatedFriends.put(follower.getUser() + "/" + follower.getId() + "/isFollowing", follower.getIsFollowing());
            }
            application.setFromAnotherActivity(true);
            reference.updateChildren(updatedFriends, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        System.out.println("There was an error saving data. ");
                    } else {
                        if(application.getFromAnotherActivity() == true) {
                            application.setFromAnotherActivity(false);
                        }
                    }
                    application.getPrivateAdapter().notifyDataSetChanged();
                }
            });
        }
    }

    public boolean checkUpdatedFollowers(List<Follower> followers, List<String> currentFollowerItems) {
        boolean result = false;
        if(followers.size() != currentFollowerItems.size()){
            return false;
        }
        for(int i = 0; i < followers.size(); i++) {
            if(followers.get(i).getIsFollowing() == currentFollowerItems.get(i)) {
                continue;
            } else {
                result = true;
            }
        }
        return result;
    }
}
