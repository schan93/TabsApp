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
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

import java.util.List;

/**
 * Created by schan on 12/30/15.
 */
public class MyTabs extends Fragment {

    private Firebase firebaseRef = new Firebase("https://tabsapp.firebaseio.com/");
    private View fragmentView;
    private FireBaseApplication application;
    private DatabaseQuery databaseQuery;
    private View progressOverlay;
    private String userId;

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
        fragmentView = inflater.inflate(R.layout.my_tabs_tab, container, false);
        System.out.println("MyTabs: Create view");
        progressOverlay = fragmentView.findViewById(R.id.progress_overlay);
        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
        if(application.getUserId() != null) {
            userId = application.getUserId();
        } else {
            setupActivity(savedInstanceState);
        }
        getMyTabsPosts(progressOverlay, userId, fragmentView);
        return fragmentView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
//                application.getMyTabsAdapter().notifyDataSetChanged();
            }
        }
    }

    public void populateNewsFeedList(View fragmentView) {
        RecyclerView rv = (RecyclerView) fragmentView.findViewById(R.id.rv_my_tabs_feed);
        RecyclerView.ItemAnimator animator = rv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setAdapter(application.getMyTabsAdapter());
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

    public void getMyTabsPosts(final View progressOverlay, final String userId, final View fragmentView) {
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
                if (progressOverlay.getVisibility() == View.VISIBLE) {
                    AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                }
                populateNewsFeedList(fragmentView);
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
}
