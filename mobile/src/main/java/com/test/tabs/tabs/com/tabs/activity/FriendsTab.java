package com.test.tabs.tabs.com.tabs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.Database.DatabaseQuery;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

import java.util.List;


/**
 * Created by schan on 12/30/15.
 */
public class FriendsTab extends Fragment {

    private Firebase firebaseRef = new Firebase("https://tabsapp.firebaseio.com/");
    private View fragmentView;
    private FireBaseApplication application;
    private DatabaseQuery databaseQuery;
    private View progressOverlay;
    private String userId;


    /**
     * When activity is created, initialize the Application.
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        application = ((FireBaseApplication) getActivity().getApplication());
    }

    /**
     * Set things such as facebook profile picture, facebook friends photos, etc.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.private_tab, container, false);
        progressOverlay = fragmentView.findViewById(R.id.progress_overlay);
        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
        if(application.getUserId() != null && application.getUserId() != "") {
            userId = application.getUserId();
        }
        setupActivity(savedInstanceState);
        getPrivatePosts(progressOverlay, userId, fragmentView);
        return fragmentView;
    }

    /**
     * Called when the Friends Tab is shown.
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
//                application.getPrivateAdapter().notifyDataSetChanged();
            }
        }
    }

    public void populateNewsFeedList(View fragmentView) {
        RecyclerView rv = (RecyclerView) fragmentView.findViewById(R.id.rv_private_feed);
        RecyclerView.ItemAnimator animator = rv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setAdapter(application.getPrivateAdapter());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("userId", userId);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupActivity(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            if(savedInstanceState.containsKey("userId")) {
                userId = savedInstanceState.getString("userId");
            }
        }
    }

    public void getPrivatePosts(final View progressOverlay, final String userId, final View fragmentView) {
        //Need to do order by / equal to.
        Firebase postsRef = firebaseRef.child("Posts");
        Query query = postsRef.orderByChild("posterUserId").equalTo(userId);
        System.out.println("login3: Getting posts from " + userId);
        query.keepSynced(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    Post post = postSnapShot.getValue(Post.class);
                    //Get all the private posts
                    List<Post> privatePosts = application.getPrivateAdapter().getPosts();
                    //If the posts is equal to private, then we add it into the private posts static adapter
                    if (post.getPosterUserId().equals(userId) && post.getPrivacy().equals("Private") && application.getPrivateAdapter().containsId(privatePosts, post.getId()) == null) {
                        List<Friend> friends = application.getFriendsRecyclerViewAdapter().getFriends();
                        Friend friend = application.getFriendsRecyclerViewAdapter().containsId(friends, post.getPosterUserId());
                        if (friend != null && friend.getIsFriend().equals("true")) {
                            application.getPrivateAdapter().getPosts().add(0, post);
                        }
                    }
                }
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                }
                populateNewsFeedList(fragmentView);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                populateNewsFeedList(fragmentView);
            }
        });

        postsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //checks if a post is added
                Post newPost = dataSnapshot.getValue(Post.class);
                List<Post> privatePosts = application.getPrivateAdapter().getPosts();
                if (newPost.getPrivacy().equals("Private") && application.getPrivateAdapter().containsId(privatePosts, newPost.getId()) == null) {
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
}
