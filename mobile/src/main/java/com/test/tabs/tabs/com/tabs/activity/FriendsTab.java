package com.test.tabs.tabs.com.tabs.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.Database.DatabaseQuery;
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.posts.Post;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;

import java.util.List;


/**
 * Created by schan on 12/30/15.
 */
public class FriendsTab extends Fragment {

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
        databaseQuery = new DatabaseQuery(getActivity());
        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
        if(application.getUserId() != null && application.getUserId() != "") {
            userId = application.getUserId();
        }
        setupActivity(savedInstanceState);
        //For every friend, we need to get every post related to them so we have to use a for loop for this
        databaseQuery.getFriendsPosts(progressOverlay, fragmentView, getContext());
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
                if(application.getFromAnotherActivity() == true) {
                    System.out.println("Friends Tab setUserVisible: VISIBLE");
                    AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
                    fragmentView.findViewById(R.id.rv_private_feed).setVisibility(View.GONE);
                } else {
                    if (progressOverlay.getVisibility() == View.VISIBLE) {
                        System.out.println("Friends Tab setUserVisible: GONE");
                        AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
                        fragmentView.findViewById(R.id.rv_private_feed).setVisibility(View.VISIBLE);
                    }
                }
            }
        }
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
}
