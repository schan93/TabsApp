package com.tabs.database.databaseQuery;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.schan.tabs.BuildConfig;
import com.schan.tabs.R;
import com.tabs.utils.AndroidUtils;
import com.tabs.activity.FireBaseApplication;
import com.tabs.activity.MainActivity;
import com.tabs.enums.PrivacyEnum;
import com.tabs.utils.TabsUtil;
import com.tabs.database.notifications.Notification;
import com.tabs.database.comments.Comment;
import com.tabs.database.followers.FollowerRecyclerViewAdapter;
import com.tabs.database.posts.Post;
import com.tabs.database.posts.PostRecyclerViewAdapter;
import com.tabs.database.users.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

import static android.R.attr.name;

/**
 * Created by schan on 3/9/16.
 */
public class DatabaseQuery implements Serializable {

    private DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_ROOT_URL);
    private FireBaseApplication application;
    private String userId;
    private DatabaseReference currentUserPath;
    private GeoFire geoFire;

    public DatabaseQuery() {
        currentUserPath = firebaseRef.child("users/" + userId);
        geoFire = new GeoFire(firebaseRef);
    }

    public DatabaseQuery(Activity activity) {
        application = (FireBaseApplication) activity.getApplication();
        userId = application.getUserId();
        currentUserPath = firebaseRef.child("users/" + userId);
        geoFire = new GeoFire(firebaseRef);
    }

    public void getPost(final NotificationCompat.Builder notificationBuilder, final Intent resultIntent, final String postId, final String userId, final String name, final Context context) {
        final DatabaseReference postsRef = firebaseRef.child("posts/" + postId);
        postsRef.keepSynced(true);
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                resultIntent.putExtra("postId", postId);
                resultIntent.putExtra("userId", userId);
                resultIntent.putExtra("name", name);
                resultIntent.putExtra("postTitle", post.getTitle());
                resultIntent.putExtra("userProfileId", post.getPosterUserId());
                resultIntent.putExtra("posterUserId", post.getPosterUserId());
                resultIntent.putExtra("posterName", post.getName());
                resultIntent.putExtra("postTimeStamp", post.getTimeStamp());
                resultIntent.putExtra("postStatus", post.getStatus());

                Intent backIntent = new Intent(context, MainActivity.class);
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
                FirebaseCrash.report(firebaseError.toException());
            }
        });

    }

    public void saveCommentToDatabaseReference(Context context, final Comment comment) {
        //Firstly we need to save a comment to firebase
        final DatabaseReference commentsRef = firebaseRef.child("comments").push();
        String commentId = commentsRef.getKey();
        comment.setId(commentId);
        final Date date = new Date();
        commentsRef.setValue(comment).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report(e);
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FirebaseMessaging.getInstance().subscribeToTopic("post_" + comment.getPostId());
            }
        });

        //Next, we need to set that comment in your own comment's path to be true
        //For now I don't think we'll be using this BUT we can keep this here just in case. I think actually
        //I think we'd want to store the posts that we comment on in the comments section. This is because we can
        //Click on the post and view that comment.
        currentUserPath.child("/comments/" + commentsRef.getKey()).setValue(true).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report(e);
            }
        });

        //Also want to set the commented_posts for that user to be true
        currentUserPath.child("commented_posts/" + comment.getPostId()).setValue(true).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report(e);
            }
        });
    }

    public void saveUserToDatabaseReference(final String id, final String userId, final String name, final String deviceId) {
        final DatabaseReference usersRef = firebaseRef.child("users/" + userId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    User user = new User(id, userId, name);
                    usersRef.setValue(user, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null) {
                                FirebaseCrash.report(databaseError.toException());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                FirebaseCrash.report(firebaseError.toException());
            }
        });
    }

    public void saveUserToPeople(final String id, final String userId, final String name, final String deviceId) {
        final DatabaseReference usersRef = firebaseRef.child("people/" + userId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    User user = new User(id, userId, name);
                    usersRef.setValue(user, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null) {
                                FirebaseCrash.report(databaseError.toException());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                FirebaseCrash.report(firebaseError.toException());
            }
        });
    }

    /**
     * Save a following friend to firebase & update the recycler view. THIS IS NEW!!!
     * @param followerUserId
     */
    public void addFollowing(final String followerUserId) {
        //Firstly, we need to set the fact that the current user is actually following this individual
        firebaseRef.child("users/" + userId + "/following/" + followerUserId).setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FirebaseMessaging.getInstance().subscribeToTopic("user_" + followerUserId);
            }
        });

        //Now, the person that we are following has gained a follower!
        //That means we have to set their follower's of the current user id to be true
        DatabaseReference gainedFollower = firebaseRef.child("users/" + followerUserId);
        gainedFollower.child("/followers/" + userId).setValue(true);

        //Lastly, we need to copy all the posts generated by the user that you just followed into your own news feed
        //Firstly get your feed,
        final DatabaseReference currentUserFeed = currentUserPath.child("feed");

        //Now for every of that new follower's post, set them to be true in your own feed so you see their posts
        gainedFollower.child("/posts").orderByChild("timeStamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    Post post = data.getValue(Post.class);
                    if(post.getPrivacy().equals("Following")) {
                        currentUserFeed.child(post.getId()).setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                FirebaseCrash.report(firebaseError.toException());
            }
        });

    }

    /**
     * Remove a following friend to firebase & update the recycler view. THIS IS NEW!!!
     * @param followerUserId
     */
    public void removeFollowing(final String followerUserId) {
        //Firstly, we need to set the fact that the current user is not following this individual
        firebaseRef.child("users/" + userId + "/following/" + followerUserId).setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //If you're not following this individual anymore, you unsubscribe them from this person's posts
                FirebaseMessaging.getInstance().unsubscribeFromTopic("user_" + followerUserId);
            }
        });

        //Now, the person that we are following has gained a follower!
        //That means we have to set their follower's of the current user id to be true
        DatabaseReference gainedFollower = firebaseRef.child("users/" + followerUserId);
        gainedFollower.child("/followers/" + userId).setValue(false);

        //Lastly, we need to copy all the posts generated by the user that you just followed into your own news feed
        //Firstly get your feed,
        final DatabaseReference currentUserFeed = currentUserPath.child("feed");

        //Now for every of that new follower's post, set them to be true in your own feed so you see their posts
        gainedFollower.child("/posts").orderByChild("timeStamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    currentUserFeed.child(data.getKey()).setValue(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                FirebaseCrash.report(firebaseError.toException());
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
        postsRef.setValue(post).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report(e);
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Subscribe to the post so that you can receive comments when someone comments on your post
                FirebaseMessaging.getInstance().subscribeToTopic("post_" + post.getId());
                //Send notifications now to users who are subscribed to it
                if(post.getPrivacy().equals(PrivacyEnum.Following.toString())) {
                    final String hasPosted = " has posted: ";
                    final String message = post.getName() + hasPosted + post.getTitle();
                    sendUsersNotifications(post, message);
                    FirebaseMessaging.getInstance().subscribeToTopic("user_" + post.getPosterUserId());
                }
            }
        });

        if(post.getPrivacy().equals("Public")) {
            //Set the location for public posts
            geoFire.setLocation("post_locations/" + postsRef.getKey(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, final DatabaseError error) {
                    if (error != null) {
                        FirebaseCrash.report(error.toException());
                    }
                }
            });
        }

        //Next, we need to set that post in your own post's path to be true
        firebaseRef.child("users/" + userId + "/posts/" + postsRef.getKey()).setValue(true).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report(e);
            }
        });

        //Lastly add the post to everyone who follows the current user! ONLY following posts get added in here in this case.
        currentUserPath.child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if(snapshot.getValue() == null) {
                        return;
                    }
                    if(post.getPrivacy().equals("Following")) {
                        firebaseRef.child("users/" + snapshot.getKey() + "/feed/" + postsRef.getKey()).setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                FirebaseCrash.report(firebaseError.toException());
            }
        });


    }

    //THIS IS NEW!!!
    public void getFollowingPosts(final View fragmentView, final Context context, final View progressOverlay) {
        //Create a listener to listen for the content from the master post's feed
        //this is the only feed that contains references in terms of the post id
        DatabaseReference feed = currentUserPath.child("feed");
        feed.keepSynced(true);
        feed.orderByChild("timeStamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Integer[] count = {0};
                final Integer childrenCount = Long.valueOf(dataSnapshot.getChildrenCount()).intValue();
                if(!dataSnapshot.exists()) {
                    TabsUtil.populateNewsFeedList(fragmentView, application.getFollowingPostAdapter(), context);
                    stopProgressOverlay(progressOverlay);
                }
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    DatabaseReference path = firebaseRef.child("posts/" + child.getKey());
                    path.orderByChild("timeStamp").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            Post post = snapshot.getValue(Post.class);
                            application.getFollowingPostAdapter().add(post, application.getFollowingPostAdapter());
                            count[0]++;
                            if (count[0] == childrenCount) {
                                //we know that we have to populate the user posts at this point
                                TabsUtil.populateNewsFeedList(fragmentView, application.getFollowingPostAdapter(), context);
                                stopProgressOverlay(progressOverlay);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                            FirebaseCrash.report(firebaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                FirebaseCrash.report(firebaseError.toException());
            }
        });

        //Now push to the handlers array so that we can "unload" them to call .off on them so that we aren't always
        //listening and making our app have unnecessary amounts of memory


    }

    public void getPublicPosts(Location location, final View progressOverlay, final View fragmentView, final Context context) {
        //Need to do order by / equal to.
        final DatabaseReference postsRef = firebaseRef.child("posts");
        //Query at a location of 15 miles
        DatabaseReference locationsRef = firebaseRef.child("/post_locations");
        geoFire = new GeoFire(locationsRef);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 48.2804);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //Now that we entered into a query that has the public location, we have to add it to our adapter
                application.getPublicPostKeys().add(key);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //I'm not sure if tehre's any better way to do this right now. I will simply get all the posts,
                //Then grab each post by their key. once that's done, then I sort them and show the loading screen. That is the best i feel like I can do.
                postsRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Post post = dataSnapshot.getValue(Post.class);
                        if (post.getPrivacy().equals("Public") && application.getPublicPostKeys().contains(post.getId())) {
                            application.getPublicAdapter().add(post, application.getPublicAdapter());
                        }
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
                        FirebaseCrash.report(firebaseError.toException());
                    }
                });

                postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        stopProgressOverlay(progressOverlay);
                        //Sort the public posts because they are only sorted by their location
                        TabsUtil.populateNewsFeedList(fragmentView, application.getPublicAdapter(), context);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                //Still need to publish all the posts
                FirebaseCrash.report(error.toException());
            }
        });
    }

    public void getIsFollowing(final String userId, final Button button, final Context context) {
        DatabaseReference userFollowingRef = currentUserPath.child("/following");
        userFollowingRef.keepSynced(true);
        userFollowingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean doesNotExist = true;
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    if(child.getKey().equals(userId)) {
                        doesNotExist = false;
                        if(child.getValue().equals(true)) {
                            setButtonIsFollowing(button, context);
                        } else {
                            setButtonIsNotFollowing(button, context);
                        }
                    }
                }
                if(doesNotExist) {
                    setButtonIsNotFollowing(button, context);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setButtonIsFollowing(Button button, Context context) {
        button.setBackgroundResource(R.drawable.following_button_bg);
        button.setTextColor(ContextCompat.getColor(context, R.color.white));
        button.setText(R.string.following);
    }

    private void setButtonIsNotFollowing(Button button, Context context) {
        button.setBackgroundResource(R.drawable.follow_button_bg);
        button.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        button.setText(R.string.plusFollow);
    }

    public void populateFollowList(final String userId, final View layoutView, final FollowerRecyclerViewAdapter adapter, final Context context, String firebaseEndpoint, final View progressOverlay) {
        adapter.getFollowers().clear();
        adapter.notifyDataSetChanged();
        DatabaseReference userFollowingRef = firebaseRef.child("/users/" + userId + "/" + firebaseEndpoint);
        userFollowingRef.keepSynced(true);
        userFollowingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //I think this would mainly be for a profile kind of section or deal so that you don't always have to requery for the
                //user information about an individual
                final Integer[] count = {0};
                final Integer childrenCount = Long.valueOf(dataSnapshot.getChildrenCount()).intValue();
                if(!dataSnapshot.exists()) {
                    TabsUtil.populateFollowList(layoutView, context, adapter);
                    stopProgressOverlay(progressOverlay);
                }
                for(final DataSnapshot child: dataSnapshot.getChildren()) {
                    firebaseRef.child("people/" + child.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (child.getValue() != null && child.getValue() == Boolean.TRUE) {
                                User user = snapshot.getValue(User.class);
                                user.setUserId(child.getKey());
                                adapter.add(user, adapter);
                                count[0]++;
                            }
                            if(child.getValue() == Boolean.FALSE) {
                                count[0]++;
                            }
                            if(Objects.equals(count[0], childrenCount)) {
                                //we know that we have to populate the user posts at this point
                                TabsUtil.populateFollowList(layoutView, context, adapter);
                                stopProgressOverlay(progressOverlay);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                            FirebaseCrash.report(firebaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                FirebaseCrash.report(firebaseError.toException());
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
            getCurrentUserFollow("followers", application.getFollowersRecyclerViewAdapter());
            getCurrentUserFollow("following", application.getFollowingRecyclerViewAdapter());
            activity.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
        Intent intent = new Intent(activity, MainActivity.class);
        if (intent != null) {
            activity.startActivity(intent);
        }
    }

    /**
     * A database call to get the followers/following for the current user this is because when we initialize the application
     * We have to make sure that these are populated beforehand so that the user if they initialize the application and then
     * click on a user's profile and see their followers, we have to make sure the setupFollowButton is set correctly.
     * @param firebaseEndpoint
     * @param adapter
     */
    private void getCurrentUserFollow(String firebaseEndpoint, final FollowerRecyclerViewAdapter adapter) {
        DatabaseReference userFollowingRef = currentUserPath.child("/" + firebaseEndpoint);
        userFollowingRef.keepSynced(true);
        userFollowingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(final DataSnapshot child: dataSnapshot.getChildren()) {
                    firebaseRef.child("people/" + child.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (child.getValue() != null && child.getValue() == Boolean.TRUE) {
                                User user = snapshot.getValue(User.class);
                                user.setUserId(child.getKey());
                                adapter.add(user, adapter);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                            FirebaseCrash.report(firebaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                FirebaseCrash.report(firebaseError.toException());
            }
        });
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
                                //If the application user id and name are not set here, we need to set it in here then
                                application.setUserId(userId);
                                application.setName(name);
                                //We use the full name to store into DB, but we get first name to store into poeple because
                                //This is our "object facing" object. Token Id will be created on a separate API call when
                                //onTokenRefresh() in NotificationInstanceService class
                                String deviceId = FirebaseInstanceId.getInstance().getToken();
                                saveUserToDatabaseReference(id, userId, fullName, deviceId);
                                //I guess we don't really care when we save to the "people"
                                saveUserToPeople(id, userId, name, deviceId);
                            }
                        } catch (JSONException e) {
                            FirebaseCrash.report(e);
                        } finally {
                            //This is after getting the user has completed
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setupNextActivity(loggedIn, activity);
                                }
                            });
                        }
                    }
                });
        meRequest.executeAsync();
        return;
    }

    public void getNumUserComments(String posterUserId, final View view, final String callingActivityName) {
        final DatabaseReference userCommentsRef = firebaseRef.child("users/" + posterUserId + "/comments");
        userCommentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView totalNumComments = (TextView) view.findViewById(R.id.total_num_comments);
                ProgressBar progressOverlay = (ProgressBar) view.findViewById(R.id.profile_progress_num_comments);
                if(callingActivityName.equals("Profile")) {
                    application.setCommentsCount(Long.valueOf(dataSnapshot.getChildrenCount()).intValue());
                    totalNumComments.setText(Html.fromHtml("<b><big>" + application.getCommentsCount() + "</big></b>" + "\nComments"));
                } else {
                    application.setUserCommentNum(Long.valueOf(dataSnapshot.getChildrenCount()).intValue());
                    totalNumComments.setText(Html.fromHtml("<b><big>" + application.getUserCommentNum() + "</big></b>" + "\nComments"));
                }
                progressOverlay.setVisibility(View.GONE);
                totalNumComments.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                FirebaseCrash.report(firebaseError.toException());
            }
        });
    }

    public void getNumUserPosts(String posterUserId, final View view, final String callingActivityName) {
        final DatabaseReference userCommentsRef = firebaseRef.child("users/" + posterUserId + "/posts");
        userCommentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView totalNumPosts = (TextView) view.findViewById(R.id.total_num_posts);
                ProgressBar progressOverlay = (ProgressBar) view.findViewById(R.id.profile_progress_num_posts);
                if(callingActivityName.equals("Profile")) {
                    application.setPostCount(Long.valueOf(dataSnapshot.getChildrenCount()).intValue());
                    totalNumPosts.setText(Html.fromHtml("<b><big>" + application.getPostCount() + "</big></b>" + "\nPosts"));
                } else {
                    application.setUserPostNum(Long.valueOf(dataSnapshot.getChildrenCount()).intValue());
                    totalNumPosts.setText(Html.fromHtml("<b><big>" + application.getUserPostNum() + "</big></b>" + "\nPosts"));
                }
                progressOverlay.setVisibility(View.GONE);
                totalNumPosts.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                FirebaseCrash.report(firebaseError.toException());
            }
        });
    }

    public void getNumUserFollowers(String posterUserId, final View view, final String callingActivityName) {
        final DatabaseReference userCommentsRef = firebaseRef.child("users/" + posterUserId + "/followers");
        userCommentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer count = 0;
                Button followersbutton = (Button) view.findViewById(R.id.followers_button);
                ProgressBar progressOverlay = (ProgressBar) view.findViewById(R.id.profile_progress_num_followers);
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if(snapshot.getValue().equals(true)) {
                        count++;
                    }
                }
                if(callingActivityName.equals("Profile")) {
                    application.setFollowerNum(count);
                    followersbutton.setText(Html.fromHtml("<b><big>" + application.getFollowerNum() + "</big></b>" + "<br/>Followers"));
                } else {
                    application.setUserFollowerNum(count);
                    followersbutton.setText(Html.fromHtml("<b><big>" + application.getUserFollowerNum() + "</big></b>" + "<br/>Followers"));
                }
                progressOverlay.setVisibility(View.GONE);
                followersbutton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                FirebaseCrash.report(firebaseError.toException());
            }
        });
    }

    public void getNumUserFollowing(String posterUserId, final View view, final String callingActivityName) {
        final DatabaseReference userCommentsRef = firebaseRef.child("users/" + posterUserId + "/following");
        userCommentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer count = 0;
                Button followingButton = (Button) view.findViewById(R.id.following_button);
                ProgressBar progressOverlay = (ProgressBar) view.findViewById(R.id.profile_progress_num_following);
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if(snapshot.getValue().equals(true)) {
                        count++;
                    }
                }
                if(callingActivityName.equals("Profile")) {
                    application.setFollowingNum(count);
                    followingButton.setText(Html.fromHtml("<b><big>" + application.getFollowingNum() + "</big></b>" + "<br/>Following"));
                } else {
                    application.setUserFollowingNum(count);
                    followingButton.setText(Html.fromHtml("<b><big>" + application.getUserFollowingNum() + "</big></b>" + "<br/>Following"));
                }
                progressOverlay.setVisibility(View.GONE);
                followingButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                FirebaseCrash.report(firebaseError.toException());
            }
        });
    }

    public void getComments(final Activity activity, final String posterName, final String postTitle, final Long postTimeStamp, final String posterUserId, final String postStatus, final String postId, final View fragmentView, final ListView commentsRecyclerView, final View progressOverlay, final Context context) {
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
                FirebaseCrash.report(firebaseError.toException());
            }
        });

        commentsRef.orderByChild("postId").equalTo(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                stopProgressOverlay(progressOverlay);
                TabsUtil.populateCommentsList(activity, posterName, postTitle, postTimeStamp, posterUserId, postStatus, fragmentView, commentsRecyclerView, context, application.getCommentsRecyclerViewAdapter());
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                FirebaseCrash.report(firebaseError.toException());
            }
        });
    }


    public void getUserPosts(final String userId, final View layoutView, final PostRecyclerViewAdapter adapter, final Context context, String firebaseEndpoint, final View progressOverlay) {
        final DatabaseReference postsRef = firebaseRef.child("/posts");
        DatabaseReference linkRef = firebaseRef.child("/users/" + userId + "/" + firebaseEndpoint);
        linkRef.orderByChild("timeStamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Integer[] count = {0};
                final Integer childrenCount = Long.valueOf(dataSnapshot.getChildrenCount()).intValue();
                if(!dataSnapshot.exists()) {
                    stopProgressOverlay(progressOverlay);
                    RecyclerView recyclerView = TabsUtil.populateNewsFeedList(layoutView, adapter, context);
                    recyclerView.setNestedScrollingEnabled(false);
                }
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    postsRef.child(child.getKey()).orderByChild("timeStamp").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            Post post = snapshot.getValue(Post.class);
                            //We only ADD to the user adapter if the post is public because otherwise we would violate seeing posts designed only for the following posts for others
                            if(!application.getUserId().equals(userId) && post.getPrivacy().equals("Public")) {
                                adapter.add(post, adapter);
                            }
                            if(application.getUserId().equals(userId)) {
                                adapter.add(post, adapter);
                            }
                            count[0]++;
                            if(Objects.equals(count[0], childrenCount)) {
                                //we know that we have to populate the user posts at this point
                                stopProgressOverlay(progressOverlay);
                                RecyclerView recyclerView = TabsUtil.populateNewsFeedList(layoutView, adapter, context);
                                recyclerView.setNestedScrollingEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                            FirebaseCrash.report(firebaseError.toException());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                FirebaseCrash.report(firebaseError.toException());
            }
        });
    }

    /**
     * A user subscribes to the POST type of notification meaning that anyone subscribed to a post will get the notifications about it.
     * @param comment
     * @param message
     */
    public void sendCommentNotifications(final Comment comment, String message) {
        final String notificationTypePost = "post";
        final DatabaseReference notifications = firebaseRef.child("notificationRequests").push();
        Notification notification = new Notification();
        notification.setNotificationType(notificationTypePost);
        notification.setMessage(message);
        notification.setPostId(comment.getPostId());
        //User id = sender of the comment
        notification.setUserId(application.getUserId());
        notification.setName(application.getName());
        notification.setTitle("Tabs");
        notification.setIcon(comment.getCommenterUserId());
        notifications.setValue(notification).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report(e);
            }
        });
    }

    /**
     * A user subscribes to the USER type of notification meaning that anyone subscribed to this user will get a notification about it.
     * @param post
     * @param message
     */
    public void sendUsersNotifications(final Post post, String message) {
        final String notificationTypePost = "user";
        final DatabaseReference notifications = firebaseRef.child("notificationRequests").push();
        Notification notification = new Notification();
        notification.setNotificationType(notificationTypePost);
        notification.setMessage(message);
        notification.setPostId(post.getId());
        //User id = sender of the post
        notification.setUserId(application.getUserId());
        notification.setName(application.getName());
        notification.setTitle("Tabs");
        notification.setIcon(post.getPosterUserId());
        notifications.setValue(notification).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report(e);
            }
        });
    }

    private void stopProgressOverlay(View progressOverlay) {
        if (progressOverlay.getVisibility() == View.VISIBLE) {
            progressOverlay.setVisibility(View.GONE);
            AndroidUtils.animateView(progressOverlay, View.GONE, 0, 0);
        }
    }



}
