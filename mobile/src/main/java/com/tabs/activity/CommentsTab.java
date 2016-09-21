package com.tabs.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;
import com.tabs.database.posts.PostRecyclerViewAdapter;

/**
 * Created by schan on 7/11/16.
 */
public class CommentsTab extends Fragment{
    private View fragmentView;
    private FireBaseApplication application;
    private static boolean isNetworkEnabled;
    private DatabaseQuery databaseQuery;
    private View progressOverlay;
    private String userId;
    private String name;
    private Boolean profileViewSet = false;

    //GoogleApiClient
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        UserProfile userProfile;

        if(context instanceof UserProfile) {
            userProfile = (UserProfile) context;
            Intent intent = userProfile.getIntent();
            profileViewSet = true;
            setupActivity(intent.getExtras());
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        application = ((FireBaseApplication) getActivity().getApplication());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ProgressBar progressBar = (TextView)view.findViewById(R.id.prog);

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
        fragmentView = inflater.inflate(R.layout.posts_tab, container, false);
        progressOverlay = fragmentView.findViewById(R.id.progress_overlay);
//        databaseQuery = new DatabaseQuery(getActivity());
        application = ((FireBaseApplication) getActivity().getApplication());
//        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
        setupActivity(savedInstanceState);

        databaseQuery = new DatabaseQuery(getActivity());
        if(!userId.equals(application.getUserId()) && TabsUtil.checkIfAdapterEmpty(application.getPostsUserHasCommentedOnAdapter())) {
             TabsUtil.populateNewsFeedList(fragmentView, application.getPostsUserHasCommentedOnAdapter(), getContext(), 0);
            //TODO: Don't really know when this will be done but i guess I really need to fix this because I need
            //To only show the fragment view AFTER the posts are done loading but ill just keep this here for now
//            if(progressOverlay.getVisibility() == View.VISIBLE) {
//                progressOverlay.setVisibility(View.GONE);
//                AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
//            }
        } else if(userId.equals(application.getUserId()) && TabsUtil.checkIfAdapterEmpty(application.getPostsThatCurrentUserHasCommentedOnAdapter())) {
            TabsUtil.populateNewsFeedList(fragmentView, application.getPostsThatCurrentUserHasCommentedOnAdapter(), getContext(), 0);
        } else {
            TabsUtil.setupPostsCommentsView(fragmentView, R.string.noCommentsPosted);
        }
        AndroidUtils.animateView(progressOverlay, View.GONE, 0, 0);
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
//                application.getPublicAdapter().notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("posterUserId", userId);
        savedInstanceState.putString("posterName", name);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupActivity(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            if(savedInstanceState.containsKey("posterUserId")) {
                userId = savedInstanceState.getString("posterUserId");
            } else {
                userId = application.getUserId();
            }
            if(savedInstanceState.containsKey("posterName")) {
                name = savedInstanceState.getString("posterName");
            } else {
                name = application.getName();
            }
        } else if(savedInstanceState == null && profileViewSet == false){
            userId = application.getUserId();
            name = application.getName();
        }
    }
}
