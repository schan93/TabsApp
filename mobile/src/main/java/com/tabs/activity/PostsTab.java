package com.tabs.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;

/**
 * Created by schan on 7/11/16.
 */
public class PostsTab extends Fragment {

    onProfileSelectedListener mCallback;

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

    // Container Activity must implement this interface
    public interface onProfileSelectedListener {
        public void onProfileSelected(int position);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        application = ((FireBaseApplication) getActivity().getApplication());
        //Remove all instances of the user adapter because we can go to another user's profile and then they will have differnet posts
//        application.getUserAdapter().getPosts().clear();
    }

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
        //Asynchronous call, call this and then hopefully the call will be done by the time we get to the UI so i can just load the ui
        progressOverlay = fragmentView.findViewById(R.id.progress_overlay);
        application = ((FireBaseApplication) getActivity().getApplication());
        setupActivity(savedInstanceState);

        databaseQuery = new DatabaseQuery(getActivity());
        if(!userId.equals(application.getUserId())) {
            TabsUtil.populateNewsFeedList(fragmentView, application.getUserAdapter(), getContext());
            //TODO: Don't really know when this will be done but i guess I really need to fix this because I need
            //To only show the fragment view AFTER the posts are done loading but ill just keep this here for now
//            if(progressOverlay.getVisibility() == View.VISIBLE) {
//                progressOverlay.setVisibility(View.GONE);
//                AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
//            }
        } else {
            TabsUtil.populateNewsFeedList(fragmentView, application.getMyTabsAdapter(), getContext());
        }
        fragmentView.findViewById(R.id.rv_posts_feed).setVisibility(View.VISIBLE);
        AndroidUtils.animateView(progressOverlay, View.GONE, 0, 200);
        //This still needs to be called in case our app hangs in background then is recreated
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        application.getUserAdapter().getPosts().clear();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
