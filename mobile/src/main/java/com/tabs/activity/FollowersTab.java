package com.tabs.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;

/**
 * Created by schan on 5/15/16.
 */
public class FollowersTab extends Fragment {

    public static FollowersTab  newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(TabsUtil.ARGS_INSTANCE, instance);
        FollowersTab fragment = new FollowersTab();
        fragment.setArguments(args);
        return fragment;
    }


    public FollowersTab(){
    }

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
        setupActivity(savedInstanceState);
    }

    /**
     * Set things such as facebook profile picture, followers photos, etc.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.posts_tab, container, false);
        return fragmentView;
    }

    /**
     * Called when the Followers Tab is shown.
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
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

    @Override
    public void onResume() {
        super.onResume();
        application = ((FireBaseApplication) getActivity().getApplication());
        progressOverlay = fragmentView.findViewById(R.id.progress_overlay);
        databaseQuery = new DatabaseQuery(getActivity());
        if(application.getUserId() != null && application.getUserId() != "") {
            userId = application.getUserId();
        }
        databaseQuery.getFollowingPosts(fragmentView, getContext(), progressOverlay);
    }
}
