package com.tabs.database.Database;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.schan.tabs.R;
import com.tabs.activity.AndroidUtils;
import com.tabs.activity.Comments;
import com.tabs.activity.CommentsHeader;
import com.tabs.activity.FireBaseApplication;
import com.tabs.activity.NotificationService;
import com.tabs.activity.TabsUtil;
import com.tabs.activity.news_feed;
import com.tabs.database.comments.Comment;
import com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.tabs.database.followers.Follower;
import com.tabs.database.posts.Post;
import com.tabs.database.users.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by schan on 3/9/16.
 */
public class DatabaseQuery implements Serializable {

    private DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://tabsapp.firebaseio.com/");
    private Activity activity;
    private FireBaseApplication application;
    private String userId;
    private String name;
    private DatabaseReference currentUserPath;
    private GeoFire geoFire;

    public DatabaseQuery() {
        userId = application.getUserId();
        name = application.getName();
        currentUserPath = firebaseRef.child("users/" + userId);
        geoFire = new GeoFire(firebaseRef);
    }

    public DatabaseQuery(Activity activity) {
        this.activity = activity;
        application = (FireBaseApplication) this.activity.getApplication();
        userId = application.getUserId();
        name = application.getName();
        currentUserPath = firebaseRef.child("users/" + userId);
        geoFire = new GeoFire(firebaseRef);
    }

    public void getPost(final NotificationCompat.Builder notificationBuilder, final Intent resultIntent, final String postId, final String userId, final Context context) {
        final DatabaseReference postsRef = firebaseRef.child("posts/" + postId);
        postsRef.keepSynced(true);
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                resultIntent.putExtra("postId", postId);
                resultIntent.putExtra("userId", userId);
                resultIntent.putExtra("postTitle", post.getTitle());
                resultIntent.putExtra("posterUserId", post.getPosterUserId());
                resultIntent.putExtra("posterName", post.getName());
                resultIntent.putExtra("postTimeStamp", post.getTimeStamp());
                resultIntent.putExtra("postStatus", post.getStatus());

                Intent backIntent = new Intent(context, news_feed.class);
                backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


                //Set a random notification Id for each notification so that if you get multiple, the first does not get replaced.
                //I guess the pending intent request code will also be unique.
                Random random = new Random();
                int notificationId = random.nextInt(9999 - 1000) + 1000;
                PendingIntent pendingIntent = PendingIntent.getActivities(context, notificationId, new Intent[] { backIntent, resultIntent}, PendingIntent.FLAG_ONE_SHOT);
                //When the notification is actually clicked on
                notificationBuilder.setContentIntent(pendingIntent);
                //Notification manager to notify of background event
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
                notificationManager.notify(notificationId, notificationBuilder.build());
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

    }

    public void saveCommentToDatabaseReference(Context context, Comment comment) {
        //Firstly we need to save a comment to firebase
        final DatabaseReference commentsRef = firebaseRef.child("comments").push();
        String commentId = commentsRef.getKey();
        comment.setId(commentId);
        Date date = new Date();
        commentsRef.setPriority(0 - date.getTime());
        commentsRef.setValue(comment);

        //Next, we need to set that comment in your own comment's path to be true
        //For now I don't think we'll be using this BUT we can keep this here just in case. I think actually
        //I think we'd want to store the posts that we comment on in the comments section. This is because we can
        //Click on the post and view that comment.
        currentUserPath.child("/comments/" + commentsRef.getKey()).setValue(true);

        //Also need to set this post in your displayed feed to be true
//        firebaseRef.child("users/" + userId + "/feed/" + postsRef.getKey()).setValue(true);

        //(Not sure if this is needed but for more "recent" users, we add a priority to their post under the recent-users path
        long time = new Date().getTime();
        firebaseRef.child("recent-users").child(userId).setPriority(time, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.out.println("Data could not be saved. " + databaseError.getMessage());
                } else {
                    System.out.println("Data saved successfully.");
                }
            }
        });

        //Display the comment in a list that shows the most recent comments (what we would want by design)
        firebaseRef.child("recent-comments").child(commentsRef.getKey()).setPriority(time, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.out.println("Data could not be saved. " + databaseError.getMessage());
                } else {
                    System.out.println("Data saved successfully.");
                }
            }

        });

        //Lastly add the comment as a child to the Post! I need the KEY of the post, not its id
        //The query gets the key, then based on htis query we add taht to the user location and also
        //The post_comments list
        firebaseRef.child("post_comments/" + comment.getPostId() + "/" + comment.getId()).setValue(true);
        //Also want to set the commented_posts for that user to be true
        currentUserPath.child("commented_posts/" + comment.getPostId()).setValue(true);
    }

    public void getUsersToSendCommentNotificationTo(final String userId, final Comment comment, final String hasAction, final HashMap<String, String> users) {
        final List<String> deviceIds = new ArrayList<String>();
        final DatabaseReference usersRef = firebaseRef.child("people");
        usersRef.keepSynced(true);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    //If the user is in the userIds array, then we have to send the notification to this user
                    if (users.get(user.getUserId()) != null && !deviceIds.contains(user.getDeviceId())) {
                        //Get their device Ids
                        deviceIds.add(user.getDeviceId());
                    }
                }
                application.setDeviceIdsToNotifyUsers(deviceIds);

                NotificationService notificationService = new NotificationService();
                try {
                    notificationService.sendCommentNotificationToServer(userId, comment, hasAction, deviceIds);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    public void getUsersToSendPostNotificationTo(final String userId, final Post post, final String hasAction, final List<User> users) {
        final List<String> deviceIds = new ArrayList<String>();
        final DatabaseReference usersRef = firebaseRef.child("people");
        usersRef.keepSynced(true);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    //If the user is in the userIds array, then we have to send the notification to this user
                    if (users.contains(user) && !deviceIds.contains(user.getDeviceId())) {
                        //Get their device Ids
                        deviceIds.add(user.getDeviceId());
                    }
                }
                application.setDeviceIdsToNotifyUsers(deviceIds);

                NotificationService notificationService = new NotificationService();
                try {
                    notificationService.sendPostNotificationToServer(userId, post, hasAction, deviceIds);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    public void saveUserToDatabaseReference(final String id, final String userId, final String name, final String deviceId) {
        final DatabaseReference usersRef = firebaseRef.child("users/" + userId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    User user = new User(id, userId, name, deviceId);
                    usersRef.setValue(user);
                } else {
                    User user = dataSnapshot.getValue(User.class);
                    //Device Id can change
                    if(user.getDeviceId().equals("") || !user.getDeviceId().equals(deviceId)) {
                        user.setDeviceId(deviceId);
                        Map<String,Object> updatedDevice = new HashMap<String,Object>();
                        updatedDevice.put("deviceId", deviceId);
                        usersRef.updateChildren(updatedDevice);
                    }
                    System.out.println("DatabaseQuery: User " + name + " Already exists");
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    public void saveUserToPeople(final String id, final String userId, final String name, final String deviceId) {
        final DatabaseReference usersRef = firebaseRef.child("people/" + userId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    User user = new User(id, userId, name, deviceId);
                    usersRef.setValue(user);
                } else {
                    User user = dataSnapshot.getValue(User.class);
                    //Device id can change
                    if(user.getDeviceId().equals("") || !user.getDeviceId().equals(deviceId)) {
                        user.setDeviceId(deviceId);
                        Map<String,Object> updatedDevice = new HashMap<String,Object>();
                        updatedDevice.put("deviceId", deviceId);
                        usersRef.updateChildren(updatedDevice);
                    }
                    System.out.println("DatabaseQuery: People User " + name + " Already exists");
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

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
        DatabaseReference gainedFollower = firebaseRef.child("users/" + followerUserId);
        gainedFollower.child("/followers/" + userId).setValue(true);

        //Lastly, we need to copy all the posts generated by the user that you just followed into your own news feed
        //Firstly get your feed,
        final DatabaseReference currentUserFeed = currentUserPath.child("feed");

        //Now for every of that new follower's post, set them to be true in your own feed so you see their posts
        gainedFollower.child("/posts").orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    currentUserFeed.child(data.getKey()).setValue(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

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
        DatabaseReference gainedFollower = firebaseRef.child("users/" + followerUserId);
        gainedFollower.child("/followers/" + userId).setValue(false);

        //Lastly, we need to copy all the posts generated by the user that you just followed into your own news feed
        //Firstly get your feed,
        final DatabaseReference currentUserFeed = currentUserPath.child("feed");

        //Now for every of that new follower's post, set them to be true in your own feed so you see their posts
        gainedFollower.child("/posts").orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    currentUserFeed.child(data.getKey()).setValue(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

    }

    /**
     * This method is designed to save a post to DatabaseReference database. THIS IS NEW!!!
     *
     * @param post
     */

    public void savePostToDatabaseReference(final Post post, Double latitude, Double longitude) {
        //Firstly, we need to keep track of all posts so we put them into a posts path.
        final DatabaseReference postsRef = firebaseRef.child("posts").push();
        String postId = postsRef.getKey();
        post.setId(postId);
        Date date = new Date();
        postsRef.setValue(post, 0 - date.getTime());

        //Notify users that the post is completed and can be sent
       getUsersToSendPostNotificationTo(userId, post, " has posted: ", application.getFollowersRecyclerViewAdapter().getFollowers());


//        postsRef.setPriority(date.getTime());
//        postsRef.setValue(post);

        //Set the location for public posts
        geoFire.setLocation("post_locations/" + postsRef.getKey(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    //Successfully saved location data.
                    //Next, we need to set that post in your own post's path to be true
                    firebaseRef.child("users/" + userId + "/posts/" + postsRef.getKey()).setValue(true);

                    //Also need to set this post in your displayed feed to be true
                    //firebaseRef.child("users/" + userId + "/feed/" + postsRef.getKey()).setValue(true);

                    //(Not sure if this is needed but for more "recent" users, we add a priority to their post under the recent-users path
                    long time = new Date().getTime();
                    firebaseRef.child("recent-users").child(userId).setPriority(time, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                            if (firebaseError != null) {
                                System.out.println("Data could not be saved. " + firebaseError.getMessage());
                            } else {
                                System.out.println("Data saved successfully.");
                            }
                        }
                    });

                    //Display the post in a list that shows the most recent posts (what we would want by design)
                    firebaseRef.child("recent-posts").child(postsRef.getKey()).setPriority(time, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
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
                        public void onCancelled(DatabaseError firebaseError) {

                        }
                    });
                }
            }
        });


    }

    //THIS IS NEW!!!
    public void getLatestPosts(Boolean loggedIn, Activity activity) {
        DatabaseReference feed = currentUserPath.child("feed");
        onNewPostForFeed(loggedIn, activity, feed);
    }

    //THIS IS NEW!!!
    public void onNewPostForFeed(final Boolean loggedIn, final Activity activity, DatabaseReference feed) {
        //Create a listener to listen for the content from the master post's feed
        //this is the only feed that contains references in terms of the post id
        feed.keepSynced(true);
        feed.orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getMyTabsPosts(loggedIn, activity);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
        feed.orderByPriority().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String postId = dataSnapshot.getKey();
                if(dataSnapshot.getValue().equals(true)) {
                    DatabaseReference path = firebaseRef.child("posts/" + postId);
                    path.orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Post post = dataSnapshot.getValue(Post.class);
                            if(application.getFollowingRecyclerViewAdapter().containsUserId(post.getPosterUserId()) != null) {
                                application.getFollowingPostAdapter().add(post, application.getFollowingPostAdapter());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(final DataSnapshot initalSnapshot, String s) {
                    DatabaseReference path = firebaseRef.child("posts/" + initalSnapshot.getKey());
                    path.orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Post post = dataSnapshot.getValue(Post.class);
                            if(initalSnapshot.getValue().equals(true)) {
                                application.getFollowingPostAdapter().add(post, application.getFollowingPostAdapter());
                            } else {
                                application.getFollowingPostAdapter().remove(post);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {

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
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

        //Now push to the handlers array so that we can "unload" them to call .off on them so that we aren't always
        //listening and making our app have unnecessary amounts of memory


    }

    public void getPublicPosts(Location location, final View progressOverlay, final View fragmentView, final Context context) {
        //Need to do order by / equal to.
        final DatabaseReference postsRef = firebaseRef.child("posts");
        final int [] publicPostsCount = new int[1];
        //Query at a location of 15 miles
        DatabaseReference locationsRef = firebaseRef.child("/post_locations");
        geoFire = new GeoFire(locationsRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 48.2804);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //Now that we entered into a query that has the public location, we have to add it to our adapter
                System.out.println("Keyu: " + key);
                publicPostsCount[0]++;
                postsRef.child(key).orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getPriority() != null) {
                            System.out.println("Post123: " + dataSnapshot.getValue(Post.class).getTitle() +  " Priority: " + dataSnapshot.getPriority());
                        } else {
                            System.out.println("Post456: " + dataSnapshot.getValue(Post.class).getTitle() +  " Priority: " + dataSnapshot.getPriority());

                        }
                        //If we have walked into a location with the key of that post, then we add it to the public adapter
                        Post post = dataSnapshot.getValue(Post.class);
                        Post existingPost = application.getPublicAdapter().containsId(post.getId());
                        if(existingPost == null) {
                            application.getPublicAdapter().add(post, application.getPublicAdapter());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

                    }
                });
            }

            @Override
            public void onKeyExited(String key) {
                //Now that we entered into a query that has the public location, we have to add it to our adapter
//                postsRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        //If we have walked into a location with the key of that post, then we add it to the public adapter
//                        Post post = dataSnapshot.getValue(Post.class);
//                        Post containedPost = application.getPublicAdapter().containsId(application.getPublicAdapter().getPosts(), post.getId());
//                        if(containedPost != null) {
//                            //Remove the post from our public adapter because we don't care about it anymore unfortunately (?) maybe bad for user exp but we'll see, can comment it out.
//                            application.getPublicAdapter().remove(containedPost);
//                        }
//                        application.getPublicAdapter().notifyDataSetChanged();
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError firebaseError) {
//
//                    }
//                });
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //Now we can load all the posts and listen for posts that are incoming
                System.out.println("Test size: " + publicPostsCount[0]);
                TabsUtil.populateNewsFeedList(fragmentView, application.getPublicAdapter(), context, publicPostsCount[0]);
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 0);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
                //Still need to publish all the posts
                TabsUtil.populateNewsFeedList(fragmentView, application.getPublicAdapter(), context, publicPostsCount[0]);
            }
        });
    }

    public void getPostsUserCommentedOn(final String userId) {
        DatabaseReference commentedPostsRef = firebaseRef.child("/users/" + userId + "/commented_posts");
        final DatabaseReference postsRef = firebaseRef.child("/posts");
        commentedPostsRef.orderByPriority().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                postsRef.child(dataSnapshot.getKey()).orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        application.getPostsUserHasCommentedOnAdapter().add(post, application.getPostsUserHasCommentedOnAdapter());
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                System.out.println("Here");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

    }

    public void getUserFollowing(final String userId) {
        DatabaseReference userFollowingRef = firebaseRef.child("/users/" + userId + "/following");
//        DatabaseReference followersRef = firebaseRef.child("Users/" + userId + "/Followers");
        userFollowingRef.keepSynced(true);
        userFollowingRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
//                I think this would mainly be for a profile kind of section or deal so that you don't always have to requery for the
                //user information about an individual
                firebaseRef.child("people/" + dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if(dataSnapshot.getValue() != null) {
                            User user = snapshot.getValue(User.class);
                            user.setUserId(dataSnapshot.getKey());
                            application.getUserFollowingAdapter().add(user, application.getUserFollowingAdapter());
                        }
                        //Add the follower to the followers array and update the ui. for now i will updateNotifydatasetchange here
//                        application.getFollowerRecyclerViewAdapter().notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

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
                application.getUserFollowingAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Follower removedFollower = dataSnapshot.getValue(Follower.class);
                int length = application.getUserFollowingAdapter().getItemCount();
                for (int i = 0; i < length; i++) {
                    if (application.getUserFollowingAdapter().getFollowers().get(i).getId().equals(removedFollower.getId())) {
                        application.getUserFollowingAdapter().getFollowers().remove(i);
                    }
                }
                application.getUserFollowingAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //Not sure if used
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    public void getUserFollowers(final String userId) {
        DatabaseReference userFollowingRef = firebaseRef.child("/users/" + userId + "/followers");
//        DatabaseReference followersRef = firebaseRef.child("Users/" + userId + "/Followers");
        userFollowingRef.keepSynced(true);
        userFollowingRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
//                I think this would mainly be for a profile kind of section or deal so that you don't always have to requery for the
                //user information about an individual
                firebaseRef.child("people/" + dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if(dataSnapshot.getValue() != null) {
                            User user = snapshot.getValue(User.class);
                            user.setUserId(dataSnapshot.getKey());
                            application.getUserFollowersAdapter().add(user, application.getUserFollowersAdapter());
                        }
                        //Add the follower to the followers array and update the ui. for now i will updateNotifydatasetchange here
//                        application.getFollowerRecyclerViewAdapter().notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

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
                application.getUserFollowersAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Follower removedFollower = dataSnapshot.getValue(Follower.class);
                int length = application.getUserFollowersAdapter().getItemCount();
                for (int i = 0; i < length; i++) {
                    if (application.getUserFollowersAdapter().getFollowers().get(i).getId().equals(removedFollower.getId())) {
                        application.getUserFollowersAdapter().getFollowers().remove(i);
                    }
                }
                application.getUserFollowersAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //Not sure if used
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    public void getPostsCurrentUserCommentedOn(final Boolean loggedIn, final Activity activity) {
        DatabaseReference commentedPostsRef = currentUserPath.child("/commented_posts");
        final DatabaseReference postsRef = firebaseRef.child("/posts");
        commentedPostsRef.orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (application.getFromAnotherActivity() == false) {
                    setupNextActivity(loggedIn, activity);
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
        commentedPostsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                postsRef.child(dataSnapshot.getKey()).orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapShot) {
                        application.getPostsThatCurrentUserHasCommentedOnAdapter().add(snapShot.getValue(Post.class), application.getPostsThatCurrentUserHasCommentedOnAdapter());
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

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
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

    }

    //THIS IS NEW!!!
    public void getFollowers(final String userId, final Boolean loggedIn, final Activity activity) {
        DatabaseReference followersRef = currentUserPath.child("/followers");
//        DatabaseReference followersRef = firebaseRef.child("Users/" + userId + "/Followers");
        followersRef.keepSynced(true);
        followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getLatestPosts(loggedIn, activity);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

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
                        if(dataSnapshot.getValue() != null) {
                            User user = snapshot.getValue(User.class);
                            user.setUserId(dataSnapshot.getKey());
                            application.getFollowersRecyclerViewAdapter().add(user, application.getFollowersRecyclerViewAdapter());
                        }
                        //Add the follower to the followers array and update the ui. for now i will updateNotifydatasetchange here
//                        application.getFollowerRecyclerViewAdapter().notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

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
                application.getFollowersRecyclerViewAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Follower removedFollower = dataSnapshot.getValue(Follower.class);
                int length = application.getFollowersRecyclerViewAdapter().getItemCount();
                for (int i = 0; i < length; i++) {
                    if (application.getFollowersRecyclerViewAdapter().getFollowers().get(i).getId().equals(removedFollower.getId())) {
                        application.getFollowersRecyclerViewAdapter().getFollowers().remove(i);
                    }
                }
                application.getFollowersRecyclerViewAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //Not sure if used
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    //THIS IS NEW!!!
    public void getFollowing(final String userId, final boolean loggedIn, final Activity activity) {
        DatabaseReference followingRef = currentUserPath.child("/following");

        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(final DataSnapshot child: dataSnapshot.getChildren()) {
                    firebaseRef.child("people/" + child.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            Boolean isFriend = (Boolean) child.getValue();
                            Log.d("IsFriend:",isFriend.toString());
                            if(isFriend) {
                                User user = snapshot.getValue(User.class);
                                user.setUserId(child.getKey());
                                application.getFollowingRecyclerViewAdapter().add(user, application.getFollowingRecyclerViewAdapter());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {

                        }
                    });
                }
                Log.d("IsFriend:", userId);
                getFollowers(userId, loggedIn, activity);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    /**
     * This method is designed to move the user to the next activity after getting all the details from DatabaseReference and storing information
     * into the local database is performed. This is because doing so will make our application function a lot faster
     * than getting everything on the fly.
     */
    public void setupNextActivity(Boolean loggedIn, Activity activity) {
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
    public void getUserFromFacebook(final String name, final String userId, final Boolean loggedIn, final Activity activity) {
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
                                String fullName = jsonObject.getString("name");
                                String id = AndroidUtils.generateId();
                                //We use the full name to store into DB, but we get first name to store into poeple because
                                //This is our "object facing" object. Token Id will be created on a separate API call when
                                //onTokenRefresh() in NotificationInstanceService class
                                String deviceId = FirebaseInstanceId.getInstance().getToken();
                                saveUserToDatabaseReference(id, userId, fullName, deviceId);
                                //I guess we don't really care when we save to the "people"
                                saveUserToPeople(id, userId, name, deviceId);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            //This is after getting the user has completed
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    getFollowing(userId, loggedIn, activity);
                                    getNumComments();
                                    getNumFollowers();
                                    getNumFollowing();
                                }
                            });
                        }
                    }
                });
        meRequest.executeAsync();
        return;
    }

    public void removeUserCommentsListener(String posterUserId, ValueEventListener listener){
        final DatabaseReference userCommentsRef = firebaseRef.child("users/" + posterUserId + "comments");
        userCommentsRef.removeEventListener(listener);
    }
    public void removeUserPostsListener(String posterUserId, ValueEventListener listener) {
        final DatabaseReference userPostsRef = firebaseRef.child("users/" + posterUserId + "posts");
        userPostsRef.removeEventListener(listener);
    }

    public ValueEventListener getNumUserComments(String posterUserId) {
        final DatabaseReference userCommentsRef = firebaseRef.child("users/" + posterUserId + "/comments");
        ValueEventListener listener = userCommentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                application.setUserCommentNum(Long.valueOf(dataSnapshot.getChildrenCount()).intValue());
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
        return listener;
    }

    public ValueEventListener getNumUserPosts(String posterUserId) {
        final DatabaseReference userCommentsRef = firebaseRef.child("users/" + posterUserId + "/posts");
        ValueEventListener listener = userCommentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                application.setUserPostNum(Long.valueOf(dataSnapshot.getChildrenCount()).intValue());
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
        return listener;
    }

    public ValueEventListener getNumUserFollowers(String posterUserId) {
        final DatabaseReference userCommentsRef = firebaseRef.child("users/" + posterUserId + "/followers");
        ValueEventListener listener = userCommentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer count = 0;
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if(snapshot.getValue().equals(true)) {
                        count++;
                    }
                }
                application.setUserFollowerNum(count);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
        return listener;
    }

    public ValueEventListener getNumUserFollowing(String posterUserId) {
        final DatabaseReference userCommentsRef = firebaseRef.child("users/" + posterUserId + "/following");
        ValueEventListener listener = userCommentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer count = 0;
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if(snapshot.getValue().equals(true)) {
                        count++;
                    }
                    application.setUserFollowingNum(count);
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
        return listener;
    }

    public ValueEventListener getNumFollowers() {
        final DatabaseReference userCommentsRef = currentUserPath.child("/followers");
        ValueEventListener listener = userCommentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer count = 0;
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if(snapshot.getValue().equals(true)) {
                        count++;
                    }
                }
                application.setFollowerNum(count);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
        return listener;
    }

    public ValueEventListener getNumFollowing() {
        final DatabaseReference userCommentsRef = currentUserPath.child("/following");
        ValueEventListener listener = userCommentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer count = 0;
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if(snapshot.getValue().equals(true)) {
                        count++;
                    }
                }
                application.setFollowingNum(count);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
        return listener;
    }

    //Put the loading screen thing
    public void getComments(final Activity activity, final String posterName, final String postTitle, final String postTimeStamp, final String posterUserId, final String postStatus, final String postId, final View fragmentView, final ListView commentsRecyclerView, final View progressOverlay, final Context context) {
        final DatabaseReference commentsRef = firebaseRef.child("/comments");
        commentsRef.orderByChild("postId").equalTo(postId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                application.getCommentsRecyclerViewAdapter().add(dataSnapshot.getValue(Comment.class), application.getCommentsRecyclerViewAdapter());
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
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

        commentsRef.orderByChild("postId").equalTo(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Comment comment = dataSnapshot.getValue(Comment.class);
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 0);
                }
                TabsUtil.populateCommentsList(activity, posterName, postTitle, postTimeStamp, posterUserId, postStatus, fragmentView, commentsRecyclerView, context, application.getCommentsRecyclerViewAdapter());
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    public void getNumComments() {
        final DatabaseReference commentsRef = currentUserPath.child("/comments");
        commentsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                application.setCommentsCount(application.getCommentsCount() + 1);
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
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

    }

    public void getMyTabsPosts(final Boolean loggedIn, final Activity activity) {
        final DatabaseReference postsRef = firebaseRef.child("/posts");
        DatabaseReference linkRef = currentUserPath.child("/posts");
        linkRef.orderByPriority().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                postsRef.child(dataSnapshot.getKey()).orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Post post = dataSnapshot.getValue(Post.class);
                        System.out.println("Snapshot With Priority: " + dataSnapshot.getValue(true));
                        application.getMyTabsAdapter().add(post, application.getMyTabsAdapter());
                        application.setPostCount(application.getPostCount() + 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

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
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

        linkRef.orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getPostsCurrentUserCommentedOn(loggedIn, activity);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    public void getUserPosts(String userId) {
        final DatabaseReference postsRef = firebaseRef.child("/posts");
        DatabaseReference linkRef = firebaseRef.child("/users/" + userId + "/posts");
        linkRef.orderByPriority().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                postsRef.child(dataSnapshot.getKey()).orderByPriority().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        application.getUserAdapter().add(post, application.getUserAdapter());
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

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
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }


//    public void updateFollowingToDatabaseReference(String userId, List<Follower> followers) {
//        DatabaseReference reference = new DatabaseReference("https://tabsapp.firebaseio.com/Users/" + userId + "/Following");
//        Map<String, Object> updatedFriends = new HashMap<String, Object>();
//        for(Follower follower: followers) {
//            updatedFriends.put(follower.getUser() + "/" + follower.getId() + "/isFollowing", follower.getIsAlsoFollowing());
//        }
//        application.setFromAnotherActivity(true);
//        reference.updateChildren(updatedFriends, new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
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
//    public void updateFollowersToDatabaseReference(List<Follower> followers) {
//        DatabaseReference reference = new DatabaseReference("https://tabsapp.firebaseio.com/Users");
//        Map<String, Object> updatedFriends = new HashMap<String, Object>();
//        for(Follower follower: followers) {
//            updatedFriends.put(follower.getUser() + "/" + follower.getId() + "/isFollowing", follower.getIsAlsoFollowing());
//        }
//        application.setFromAnotherActivity(true);
//        reference.updateChildren(updatedFriends, new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
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
