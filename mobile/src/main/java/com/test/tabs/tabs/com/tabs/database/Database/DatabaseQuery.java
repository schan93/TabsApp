package com.test.tabs.tabs.com.tabs.database.Database;

import android.app.Activity;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
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
        commentsRef.setValue(comment);
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
        postsRef.setValue(post);
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
        commentsRef.setValue(comment);
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
                    List<Friend> friends = application.getFriendsAdapter().getFriends();
                    if (application.getFriendsAdapter().containsId(friends, userId) == null) {
                        System.out.println("login2: Adding Friend " + friend.getName() + " to array");
                        application.getFriendsAdapter().getFriends().add(friend);
                    }
                    getPrivatePosts(userId);
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
                List<Friend> friends = application.getFriendsAdapter().getFriends();
                if (application.getFriendsAdapter().containsId(friends, newFriend.getUserId()) == null) {
                    application.getFriendsAdapter().getFriends().add(newFriend);
                    application.getFriendsAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Friend changedFriend = dataSnapshot.getValue(Friend.class);
                int length = application.getFriendsAdapter().getCount();
                for (int i = 0; i < length; i++) {
                    if (application.getFriendsAdapter().getFriends().get(i).getId().equals(changedFriend.getId())) {
                        application.getFriendsAdapter().getFriends().set(i, changedFriend);
                    }
                }
                application.getFriendsAdapter().notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Friend removedFriend = dataSnapshot.getValue(Friend.class);
                int length = application.getFriendsAdapter().getCount();
                for (int i = 0; i < length; i++) {
                    if (application.getFriendsAdapter().getFriends().get(i).getId().equals(removedFriend.getId())) {
                        application.getFriendsAdapter().getFriends().remove(i);
                    }
                }
                application.getFriendsAdapter().notifyDataSetChanged();
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

    public void getPrivatePosts(final String userId) {
        //Need to do order by / equal to.
        Firebase postsRef = firebaseRef.child("Posts");
        Query query = postsRef.orderByChild("posterUserId").equalTo(userId);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Post post = postSnapShot.getValue(Post.class);
                    List<Post> privatePosts = application.getPrivateAdapter().getPosts();
                    if (post.getPrivacy().equals("Private") && application.getPrivateAdapter().containsId(privatePosts, post.getId()) == null) {
                        List<Friend> friends = application.getFriendsAdapter().getFriends();
                        Friend friend = application.getFriendsAdapter().containsId(friends, userId);
                        if (friend != null && friend.getIsFriend().equals("true")) {
                            application.getPrivateAdapter().add(post);
                        }
                    }
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
                List<Post> privatePosts = application.getPrivateAdapter().getPosts();
                if (newPost.getPrivacy().equals("Private") && application.getPrivateAdapter().containsId(privatePosts, newPost.getId()) == null) {
                    List<Friend> friends = application.getFriendsAdapter().getFriends();
                    Friend friend = application.getFriendsAdapter().containsId(friends, userId);
                    if (friend != null && friend.getIsFriend().equals("true")) {
                        application.getPrivateAdapter().add(newPost);
                        application.getFriendsAdapter().notifyDataSetChanged();
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

    public void getPublicPosts() {
        //Need to do order by / equal to.
        Firebase postsRef = firebaseRef.child("Posts");
        Query query = postsRef.orderByChild("privacy").equalTo("Public");
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Post post = postSnapShot.getValue(Post.class);
                    List<Post> publicPosts = application.getPublicAdapter().getPosts();
                    if (post.getPrivacy().equals("Public") && application.getPublicAdapter().containsId(publicPosts, post.getId()) == null) {
                        application.getPublicAdapter().add(post);
                    }
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
                List<Post> publicPosts = application.getPublicAdapter().getPosts();
                if (application.getPublicAdapter().containsId(publicPosts, newPost.getId()) == null && newPost.getPrivacy().equals("Public")) {
                    application.getPublicAdapter().getPosts().add(newPost);
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

    public void getMyTabsPosts(final String userId) {
        //Need to do order by / equal to.
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
                        application.getMyTabsAdapter().add(post);
                    }
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
                    application.getMyTabsAdapter().getPosts().add(newPost);
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

    public void getComments(final String postId) {
        Firebase commentsRef = firebaseRef.child("Comments");
        Query query = commentsRef.orderByChild("postId").equalTo(postId);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot commentSnapShot : dataSnapshot.getChildren()) {
                    Comment comment = commentSnapShot.getValue(Comment.class);
                    application.getCommentsRecyclerViewAdapter().getCommentsList().add(comment);
                }
                application.getCommentsRecyclerViewAdapter().notifyDataSetChanged();
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
                if(application.getCommentsRecyclerViewAdapter().containsId(comments, newComment.getId()) == null && newComment.getPostId().equals(postId)) {
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
}
