package com.test.tabs.tabs.com.tabs.database.Database;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

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
import java.util.Objects;

/**
 * Created by schan on 3/9/16.
 */
public class DatabaseQuery implements Serializable {

    private Firebase firebaseRef = new Firebase("https://tabsapp.firebaseio.com/");
    private Activity activity;
    private FireBaseApplication application;
    private String userId;
    private String name;
    private final Firebase currentUserPath;

    public DatabaseQuery(Activity activity) {
        this.activity = activity;
        application = (FireBaseApplication) this.activity.getApplication();
        userId = application.getUserId();
        name = application.getName();
        currentUserPath = firebaseRef.child("users/" + userId);
    }


    public void saveCommentToFirebase(Comment comment) {
        Firebase commentsRef = firebaseRef.child("comments").push();
        String commentId = commentsRef.getKey();
        comment.setId(commentId);
        Date date = new Date();
        commentsRef.setValue(comment, 0 - date.getTime());
    }

    public void saveUserToFirebase(final String id, final String userId, final String name) {
        final Firebase usersRef = firebaseRef.child("users");
        Query query = usersRef.orderByChild("userId").equalTo(userId);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Firebase usersReference = usersRef.child(userId);
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

    public void saveUserToPeople(final String id, final String userId, final String name) {
        final Firebase usersRef = firebaseRef.child("people");
        Query query = usersRef.orderByChild("userId").equalTo(userId);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Firebase peopleReference = usersRef.child(userId);
                    User user = new User(id, userId, name);
                    peopleReference.setValue(user);
                } else {
                    System.out.println("DatabaseQuery: People User " + name + " Already exists");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    /**
     * Save a following friend to firebase & update the recycler view. THIS IS NEW!!!
     * @param followerUserId
     */
    public void addFollowing(final String followerUserId) {
        //Firstly, we need to set the fact that the current user is actually following this individual
        firebaseRef.child("users/" + userId + "/following/" + followerUserId).setValue(true);

        //Now, the person that we are following has gained a follower!
        //That means we have to set their follower's of the current user id to be true
        Firebase gainedFollower = firebaseRef.child("users/" + followerUserId);
        gainedFollower.child("/followers/" + userId).setValue(true);

        //Lastly, we need to copy all the posts generated by the user that you just followed into your own news feed
        //Firstly get your feed,
        final Firebase currentUserFeed = currentUserPath.child("feed");

        //Now for every of that new follower's post, set them to be true in your own feed so you see their posts
        gainedFollower.child("/posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    currentUserFeed.child(data.getKey()).setValue(true);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    /**
     * Remove a following friend to firebase & update the recycler view. THIS IS NEW!!!
     * @param followerUserId
     */
    public void removeFollowing(final String followerUserId) {
        //Firstly, we need to set the fact that the current user is actually following this individual
        firebaseRef.child("users/" + userId + "/following/" + followerUserId).setValue(false);

        //Now, the person that we are following has gained a follower!
        //That means we have to set their follower's of the current user id to be true
        Firebase gainedFollower = firebaseRef.child("users/" + followerUserId);
        gainedFollower.child("/followers/" + userId).setValue(false);

        //Lastly, we need to copy all the posts generated by the user that you just followed into your own news feed
        //Firstly get your feed,
        final Firebase currentUserFeed = currentUserPath.child("feed");

        //Now for every of that new follower's post, set them to be true in your own feed so you see their posts
        gainedFollower.child("/posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    currentUserFeed.child(data.getKey()).setValue(false);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    /**
     * This method is designed to save a post to Firebase database. THIS IS NEW!!!
     *
     * @param post
     */

    public void savePostToFirebase(final Post post) {
        //Firstly, we need to keep track of all posts so we put them into a posts path.
        final Firebase postsRef = firebaseRef.child("posts").push();
        String postId = AndroidUtils.generateId();
        post.setId(postId);
        Date date = new Date();
        postsRef.setValue(post, date.getTime() - 0);

        //Next, we need to set that post in your own post's path to be true
        firebaseRef.child("users/" + userId + "/posts/" + postsRef.getKey()).setValue(true);

        //Also need to set this post in your displayed feed to be true
//        firebaseRef.child("users/" + userId + "/feed/" + postsRef.getKey()).setValue(true);

        //(Not sure if this is needed but for more "recent" users, we add a priority to their post under the recent-users path
        long time = new Date().getTime();
        firebaseRef.child("recent-users").child(userId).setPriority(time, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    System.out.println("Data could not be saved. " + firebaseError.getMessage());
                } else {
                    System.out.println("Data saved successfully.");
                }
            }
        });

        //Display the post in a list that shows the most recent posts (what we would want by design)
        firebaseRef.child("recent-posts").child(postsRef.getKey()).setPriority(time, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    System.out.println("Data could not be saved. " + firebaseError.getMessage());
                } else {
                    System.out.println("Data saved successfully.");
                }
            }
        });

        //Lastly add the post to everyone who follows the current user!
        currentUserPath.child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if(snapshot.getValue() == null) {
                        return;
                    }
                    firebaseRef.child("users/" + snapshot.getKey() + "/feed/" + postsRef.getKey()).setValue(true);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    //THIS IS NEW!!!
    public void getLatestPosts() {
        Firebase feed = currentUserPath.child("feed");
        onNewPostForFeed(feed);
    }

    //THIS IS NEW!!!
    public void onNewPostForFeed(Firebase feed) {
        //Create a listener to listen for the content from the master post's feed
        //this is the only feed that contains references in terms of the post id
        feed.keepSynced(true);
        feed.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String postId = dataSnapshot.getKey();
                if(dataSnapshot.getValue().equals(true)) {
                    Firebase path = firebaseRef.child("posts/" + postId);
                    path.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Post post = dataSnapshot.getValue(Post.class);
                            if(application.getFollowingRecyclerViewAdapter().containsUserId(application.getFollowingRecyclerViewAdapter().getFollowers(), post.getPosterUserId()) != null) {
                                application.getFollowingPostAdapter().add(post);
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(final DataSnapshot initalSnapshot, String s) {
                    Firebase path = firebaseRef.child("posts/" + initalSnapshot.getKey());
                    path.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Post post = dataSnapshot.getValue(Post.class);
                            if(initalSnapshot.getValue().equals(true)) {
                                application.getFollowingPostAdapter().getPosts().add(post);
                            } else {
                                application.getFollowingPostAdapter().remove(post);
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        //Now push to the handlers array so that we can "unload" them to call .off on them so that we aren't always
        //listening and making our app have unnecessary amounts of memory


    }

    public void getPublicPosts(final View progressOverlay, final View fragmentView, final Context context) {
        //Need to do order by / equal to.
        Firebase postsRef = firebaseRef.child("posts");
        Query query = postsRef.orderByChild("privacy").equalTo(PrivacyEnum.Public.toString());
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TabsUtil.populateNewsFeedList(fragmentView, application.getPublicAdapter(), TabEnum.Public, context);
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    progressOverlay.setVisibility(View.GONE);
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
//                    application.getPublicAdapter().notifyDataSetChanged();
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

    //THIS IS NEW!!!
    public void getFollowers(final String userId, final Boolean loggedIn, final Activity activity) {
        Firebase followersRef = currentUserPath.child("/followers");
//        Firebase followersRef = firebaseRef.child("Users/" + userId + "/Followers");
        followersRef.keepSynced(true);
        followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (application.getFromAnotherActivity() == false) {
                    getLatestPosts();
                    getProfilePosts(userId);
                    setupNextActivity(loggedIn, activity);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        followersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
//                I think this would mainly be for a profile kind of section or deal so that you don't always have to requery for the
                //user information about an individual
                firebaseRef.child("people/" + dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if(dataSnapshot.getValue().equals(true)) {
                            User user = snapshot.getValue(User.class);
                            user.setUserId(dataSnapshot.getKey());
                            application.getFollowerRecyclerViewAdapter().add(user);
                        }
                        //Add the follower to the followers array and update the ui. for now i will updateNotifydatasetchange here
//                        application.getFollowerRecyclerViewAdapter().notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }



            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //We need to get the number of posts that that changed friend has and update the private adapters post
                //The data snap shot here will just be they user id of the follower and if they are actaully a follower or not
//                if(dataSnapshot.getValue() == false) {
//                } else {
//
//                }
                //Add the follower to the followers array and update the ui. for now i will updateNotifydatasetchange here
                //TODO: Update hte UI because the follower has now changed to also being a follower. Need to update their button color
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

    //THIS IS NEW!!!
    public void getFollowing(final String userId, final boolean loggedIn, final Activity activity) {
        Firebase followingRef = currentUserPath.child("/following");
//        Firebase followingRef = firebaseRef.child("Users/" + userId + "/Following");
        followingRef.keepSynced(true);
        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getFollowers(userId, loggedIn, activity);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        followingRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                firebaseRef.child("people/" + dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if(snapshot.getValue() != null) {
                            if(dataSnapshot.getValue().equals(true)) {
                                User user = snapshot.getValue(User.class);
                                user.setUserId(dataSnapshot.getKey());
                                application.getFollowingRecyclerViewAdapter().add(user);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
//                addFollowerPosts(newFollower);
            }



            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //We need to get the number of posts that that changed friend has and update the private adapters post
//                Follower changedFollower = dataSnapshot.getValue(Follower.class);
                //TODO: Update hte UI because the follower has now changed to also being a follower. Need to update their button color
                application.getFollowingRecyclerViewAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Follower removedFollower = dataSnapshot.getValue(Follower.class);
                int length = application.getFollowingRecyclerViewAdapter().getItemCount();
                for (int i = 0; i < length; i++) {
                    if (application.getFollowingRecyclerViewAdapter().getFollowers().get(i).getId().equals(removedFollower.getId())) {
                        application.getFollowingRecyclerViewAdapter().getFollowers().remove(i);
                    }
                }
                application.getFollowingRecyclerViewAdapter().notifyDataSetChanged();
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
                            if(jsonObject != null) {
                                String userId = jsonObject.getString("id");
                                String name = jsonObject.getString("name");
                                String id = AndroidUtils.generateId();
                                saveUserToFirebase(id, userId, name);
                                //I guess we don't really care when we save to the "people"
                                saveUserToPeople(id, userId, name);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            //This is after getting the user has completed
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    getFollowing(userId, loggedIn, activity);
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
        Firebase commentsRef = firebaseRef.child("comments");
        Query query = commentsRef.orderByChild("postId").equalTo(postId);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Comments.setupCommentsAdapter(postId, activity);
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    progressOverlay.setVisibility(View.GONE);
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
        final Firebase postsRef = firebaseRef.child("/posts");
        Firebase linkRef = currentUserPath.child("/posts");
        linkRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                postsRef.child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Post post = dataSnapshot.getValue(Post.class);
                        application.getMyTabsAdapter().add(post);
                        TabsUtil.populateNewsFeedList(fragmentView, application.getMyTabsAdapter(), TabEnum.MyTab, context);
                        if (progressOverlay.getVisibility() == View.VISIBLE) {
                            progressOverlay.setVisibility(View.GONE);
                            AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                            fragmentView.findViewById(R.id.rv_my_tabs_feed).setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void getProfilePosts(String userId) {
        final Firebase postsRef = firebaseRef.child("/posts");
        Firebase linkRef = firebaseRef.child("/users/ " + userId + "/posts");
        linkRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                postsRef.child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Post post = dataSnapshot.getValue(Post.class);
                        application.getMyTabsAdapter().add(post);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void getUserPosts(String userId, final View progressOverlay, final View fragmentView, final Context context) {
        final Firebase postsRef = firebaseRef.child("/posts");
        Firebase linkRef = firebaseRef.child("/users/" + userId + "/posts");
        linkRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                postsRef.child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Post post = dataSnapshot.getValue(Post.class);
                        application.getUserAdapter().add(post);
                        TabsUtil.populateNewsFeedList(fragmentView, application.getUserAdapter(), TabEnum.User, context);
                        //TODO: Fix this because this stopping of fragment view will be called so many times
                        if(progressOverlay.getVisibility() == View.VISIBLE) {
                            progressOverlay.setVisibility(View.GONE);
                            AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                            fragmentView.findViewById(R.id.rv_posts_feed).setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


//    public void updateFollowingToFirebase(String userId, List<Follower> followers) {
//        Firebase reference = new Firebase("https://tabsapp.firebaseio.com/Users/" + userId + "/Following");
//        Map<String, Object> updatedFriends = new HashMap<String, Object>();
//        for(Follower follower: followers) {
//            updatedFriends.put(follower.getUser() + "/" + follower.getId() + "/isFollowing", follower.getIsAlsoFollowing());
//        }
//        application.setFromAnotherActivity(true);
//        reference.updateChildren(updatedFriends, new Firebase.CompletionListener() {
//            @Override
//            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
//                if (firebaseError != null) {
//                    System.out.println("There was an error saving data. ");
//                } else {
//                    if(application.getFromAnotherActivity() == true) {
//                        application.setFromAnotherActivity(false);
//                    }
//                }
//                application.getPrivateAdapter().notifyDataSetChanged();
//            }
//        }
//    }
//
//    public void updateFollowersToFirebase(List<Follower> followers) {
//        Firebase reference = new Firebase("https://tabsapp.firebaseio.com/Users");
//        Map<String, Object> updatedFriends = new HashMap<String, Object>();
//        for(Follower follower: followers) {
//            updatedFriends.put(follower.getUser() + "/" + follower.getId() + "/isFollowing", follower.getIsAlsoFollowing());
//        }
//        application.setFromAnotherActivity(true);
//        reference.updateChildren(updatedFriends, new Firebase.CompletionListener() {
//            @Override
//            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
//                if (firebaseError != null) {
//                    System.out.println("There was an error saving data. ");
//                } else {
//                    if(application.getFromAnotherActivity() == true) {
//                        application.setFromAnotherActivity(false);
//                    }
//                }
//                application.getPrivateAdapter().notifyDataSetChanged();
//            }
//        });
//    }
}
