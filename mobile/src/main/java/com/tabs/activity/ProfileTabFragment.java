package com.tabs.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.schan.tabs.R;
import com.tabs.database.databaseQuery.DatabaseQuery;
import com.tabs.utils.TabsUtil;

/**
 * Created by schan on 12/30/15.
 */
public class ProfileTabFragment extends Fragment {

    private View fragmentView;
    private FireBaseApplication application;
    private DatabaseQuery databaseQuery;
    private View progressOverlay;
    private String userId;
    private String name;

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
        application = ((FireBaseApplication) getActivity().getApplication());
        fragmentView = inflater.inflate(R.layout.profile, container, false);
        setupActivity(savedInstanceState);
        return fragmentView;
    }

    private void setupPrivacyToggle(final View fragmentView) {
        final RadioGroup privacyToggle = (RadioGroup) fragmentView.findViewById(R.id.profile_toggle);
        final RadioButton publicToggle = (RadioButton) fragmentView.findViewById(R.id.public_toggle);
        final RadioButton followersToggle = (RadioButton) fragmentView.findViewById(R.id.followers_toggle);

        publicToggle.setText(R.string.posts);
        followersToggle.setText(R.string.comments);
        publicToggle.setChecked(true);
        publicToggle.setTypeface(Typeface.DEFAULT_BOLD);
        //Set listener for clicking on toggle
        privacyToggle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.public_toggle) {
                    publicToggle.setTypeface(Typeface.DEFAULT_BOLD);
                    if(followersToggle.getTypeface() == Typeface.DEFAULT_BOLD) {
                        followersToggle.setTypeface(Typeface.SANS_SERIF);
                    }
                    progressOverlay.setVisibility(View.VISIBLE);
                    databaseQuery.getUserPosts(userId, fragmentView, application.getMyTabsAdapter(), getContext(), "posts", progressOverlay);
                } else {
                    followersToggle.setTypeface(Typeface.DEFAULT_BOLD);
                    if(publicToggle.getTypeface() == Typeface.DEFAULT_BOLD) {
                        publicToggle.setTypeface(Typeface.SANS_SERIF);
                    }
                    progressOverlay.setVisibility(View.VISIBLE);
                    databaseQuery.getUserPosts(userId, fragmentView, application.getPostsThatCurrentUserHasCommentedOnAdapter(), getContext(), "commented_posts", progressOverlay);
                }
            }
        });
    }

    private void profilePictureSetup(String id, String name, View fragmentView) {
        DraweeController controller = TabsUtil.getImage(id);
        SimpleDraweeView profilePhoto = (SimpleDraweeView) fragmentView.findViewById(R.id.profile_picture);
        profilePhoto.setController(controller);
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
//        roundingParams.setBorder(ContextCompat.getColor(getContext(), R.color.white), 10f);
        profilePhoto.getHierarchy().setRoundingParams(roundingParams);
        TextView headerName = (TextView) fragmentView.findViewById(R.id.profile_name);
        headerName.setText(name);
  }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("userId", userId);
        savedInstanceState.putString("name", name);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setupActivity(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            if(savedInstanceState.containsKey("userId")) {
                userId = savedInstanceState.getString("userId");
            }
            if(savedInstanceState.containsKey("name")) {
                name = savedInstanceState.getString("name");
            }
        } else {
            userId = application.getUserId();
            name = application.getName();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        databaseQuery = new DatabaseQuery(getActivity());
        profilePictureSetup(userId, name, fragmentView);
        String [] intentStrings = {userId, name};
        TabsUtil.setupProfileView(fragmentView, "Profile", application, databaseQuery, intentStrings);
        View postsView = fragmentView.findViewById(R.id.posts_tab);
        progressOverlay = postsView.findViewById(R.id.progress_overlay);
        setupPrivacyToggle(fragmentView);
        databaseQuery.getUserPosts(userId, fragmentView, application.getMyTabsAdapter(), getContext(), "posts", progressOverlay);
    }
}
