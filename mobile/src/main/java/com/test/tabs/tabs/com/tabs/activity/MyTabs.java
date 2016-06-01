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
import com.test.tabs.tabs.com.tabs.database.posts.Post;

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
        databaseQuery = new DatabaseQuery(getActivity());
        progressOverlay = fragmentView.findViewById(R.id.progress_overlay);
        System.out.println("MyTabTab: VISIBLE");
        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
        if(application.getUserId() != null && application.getUserId() != "") {
            userId = application.getUserId();
        }
        setupActivity(savedInstanceState);
        databaseQuery.getMyTabsPosts(progressOverlay, fragmentView, getContext());
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
}
