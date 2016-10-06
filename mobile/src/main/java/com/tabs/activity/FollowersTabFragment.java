package com.tabs.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.schan.tabs.R;
import com.tabs.database.databaseQuery.DatabaseQuery;

/**
 * Created by schan on 5/15/16.
 */
public class FollowersTabFragment extends Fragment {

    private View fragmentView;
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
        FireBaseApplication application = ((FireBaseApplication) getActivity().getApplication());
        View progressOverlay = fragmentView.findViewById(R.id.progress_overlay);
        DatabaseQuery databaseQuery = new DatabaseQuery(getActivity());
        userId = application.getUserId();
        databaseQuery.getFollowingPosts(fragmentView, getContext(), progressOverlay);
    }
}
