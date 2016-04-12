package com.test.tabs.tabs.com.tabs.database.Database;

import android.app.Activity;
import android.view.View;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.test.tabs.tabs.com.tabs.activity.AndroidUtils;
import com.test.tabs.tabs.com.tabs.activity.Comments;
import com.test.tabs.tabs.com.tabs.activity.CommentsHeader;
import com.test.tabs.tabs.com.tabs.activity.FireBaseApplication;
import com.test.tabs.tabs.com.tabs.database.comments.Comment;
import com.test.tabs.tabs.com.tabs.database.comments.CommentsRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.users.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }

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

    /**
     * This method is designed to save a comment to Firebase database.
     *
     * @param comment
     */

    private void savePostToFirebase(Comment comment) {
        Firebase commentsRef = firebaseRef.child("Comments").push();
        String postId = commentsRef.getKey();
        comment.setId(postId);
        Date date = new Date();
        commentsRef.setValue(comment, 0 - date.getTime());
    }

    public void getFriends(final String userId) {
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
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Friend changedFriend = dataSnapshot.getValue(Friend.class);
                int length = application.getFriendsRecyclerViewAdapter().getItemCount();
                for (int i = 0; i < length; i++) {
                    if (application.getFriendsRecyclerViewAdapter().getFriends().get(i).getId().equals(changedFriend.getId())) {
                        application.getFriendsRecyclerViewAdapter().getFriends().set(i, changedFriend);
                    }
                }
                application.getFriendsRecyclerViewAdapter().notifyDataSetChanged();
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
}
