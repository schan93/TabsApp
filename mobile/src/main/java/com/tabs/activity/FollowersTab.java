package com.tabs.activity;

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
        application = ((FireBaseApplication) getActivity().getApplication());
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
        application = ((FireBaseApplication) getActivity().getApplication());
        progressOverlay = fragmentView.findViewById(R.id.progress_overlay);
        databaseQuery = new DatabaseQuery(getActivity());
        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
        if(application.getUserId() != null && application.getUserId() != "") {
            userId = application.getUserId();
        }
        setupActivity(savedInstanceState);
        TabsUtil.populateNewsFeedList(fragmentView, application.getFollowingPostAdapter(), getContext(), 0);
        if (progressOverlay.getVisibility() == View.VISIBLE) {
            AndroidUtils.animateView(progressOverlay, View.GONE, 0, 0);
        }
        if(application.getFollowingPostAdapter().getItemCount() == 0) {
            TextView textView = (TextView) fragmentView.findViewById(R.id.no_posts_text);
            textView.setText(R.string.noPostsFollowing);
        }
        return fragmentView;
    }

    /**
     * Called when the Followers Tab is shown.
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
                if(application.getFromAnotherActivity() == true) {
                    System.out.println("Followers Tab setUserVisible: VISIBLE");
                    AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
                    fragmentView.findViewById(R.id.rv_posts_feed).setVisibility(View.GONE);
                } else {
                    if (progressOverlay.getVisibility() == View.VISIBLE) {
                        AndroidUtils.animateView(progressOverlay, View.GONE, 0, 0);
                        fragmentView.findViewById(R.id.rv_posts_feed).setVisibility(View.VISIBLE);
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
