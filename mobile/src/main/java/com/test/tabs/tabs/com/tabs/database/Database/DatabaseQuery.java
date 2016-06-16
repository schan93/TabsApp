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

    public void getFollowerPosts(final View progressOverlay, final View fragmentView, final Context context) {
        //Need to do order by / equal to.
        Firebase postsRef = firebaseRef.child("Posts");
        Query query = postsRef.orderByChild("privacy").equalTo(PrivacyEnum.Public.toString());
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TabsUtil.populateNewsFeedList(fragmentView, application.getFollowerPostAdapter(), TabEnum.Following, context);
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                    fragmentView.findViewById(R.id.rv_followers_feed).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                TabsUtil.populateNewsFeedList(fragmentView, application.getFollowerPostAdapter(), TabEnum.Following, context);
            }
        });

        query.addChildEventListener(new ChildEventListener() {
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
                TabsUtil.populateNewsFeedList(fragmentView, application.getPublicAdapter(), TabEnum.Public, context);
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                    fragmentView.findViewById(R.id.rv_public_feed).setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                TabsUtil.populateNewsFeedList(fragmentView, application.getPublicAdapter(), TabEnum.Public, context);
            }
        });

        query.addChildEventListener(new ChildEventListener() {
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
                                    getFollowers(userId, loggedIn, activity);
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
                Comments.setupCommentsAdapter(postId, activity);
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                    fragmentView.findViewById(R.id.rv_view_comments).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        query.addChildEventListener(new ChildEventListener() {
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
                TabsUtil.populateNewsFeedList(fragmentView, application.getMyTabsAdapter(), TabEnum.MyTab, context);
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                    fragmentView.findViewById(R.id.rv_my_tabs_feed).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        query.addChildEventListener(new ChildEventListener() {
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
