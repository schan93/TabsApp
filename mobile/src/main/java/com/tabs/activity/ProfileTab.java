package com.tabs.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.schan.tabs.R;
import com.tabs.database.Database.DatabaseQuery;
import com.tabs.database.posts.PostRecyclerViewAdapter;

/**
 * Created by schan on 12/30/15.
 */
public class ProfileTab extends Fragment {

    public static ProfileTab  newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt(TabsUtil.ARGS_INSTANCE, instance);
        ProfileTab fragment = new ProfileTab();
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileTab(){
    }

    private DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://tabsapp.firebaseio.com/");
    private View fragmentView;
    private FireBaseApplication application;
    private DatabaseQuery databaseQuery;
    private View progressOverlay;
    private String userId;
    private String name;
    private Button followersButton;
    private Button followingButton;
    private TabLayout tabLayout;
    SimpleDraweeView profilePhoto;
    private View postsView;
    private View privacyToggleView;
    private RecyclerView postsRecyclerView;

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
        //Set listener for clicking on toggle
        privacyToggle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.public_toggle) {
                    publicToggle.setTypeface(Typeface.DEFAULT_BOLD);
                    if(followersToggle.getTypeface() == Typeface.DEFAULT_BOLD) {
                        followersToggle.setTypeface(Typeface.SANS_SERIF);
                    }
                    databaseQuery.getUserPosts(userId, fragmentView, application.getMyTabsAdapter(), getContext(), "posts");
                } else {
                    followersToggle.setTypeface(Typeface.DEFAULT_BOLD);
                    if(publicToggle.getTypeface() == Typeface.DEFAULT_BOLD) {
                        publicToggle.setTypeface(Typeface.SANS_SERIF);
                    }
                    databaseQuery.getUserPosts(userId, fragmentView, application.getPostsThatCurrentUserHasCommentedOnAdapter(), getContext(), "commented_posts");
                }
            }
        });
    }

    private void populatePostsView(PostRecyclerViewAdapter adapter) {
        postsRecyclerView = TabsUtil.populateNewsFeedList(fragmentView, adapter, getContext(), adapter.getItemCount());
        postsRecyclerView.setNestedScrollingEnabled(false);
    }


    private void setupButtonOnClick() {
        followersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void setNameAndId() {
        if(application.getUserId() != null && !application.getUserId().equals("")) {
            userId = application.getUserId();
        }
        if(application.getName() != null && !application.getName().equals("")) {
            name = application.getName();
        }
    }

    private void profilePictureSetup(String id, String name, View fragmentView) {
        DraweeController controller = TabsUtil.getImage(id);
        profilePhoto = (SimpleDraweeView) fragmentView.findViewById(R.id.profile_picture);
        profilePhoto.setController(controller);
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
//        roundingParams.setBorder(ContextCompat.getColor(getContext(), R.color.white), 10f);
        profilePhoto.getHierarchy().setRoundingParams(roundingParams);
        TextView headerName = (TextView) fragmentView.findViewById(R.id.profile_name);
        headerName.setText(name);
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
        }
    }

//    private void setupPostsAndCommentsTabLayout(View fragmentView) {
////        tabLayout = (TabLayout) fragmentView.findViewById(R.id.comments_post_tab_layout);
//        tabLayout = (TabLayout) fragmentView.findViewById(R.id.profile_tab_layout);
//        tabLayout.addTab(tabLayout.newTab().setText("Posts"));
//        tabLayout.addTab(tabLayout.newTab().setText("Comments"));
//
////        final ViewPager viewPager = (ViewPager) fragmentView.findViewById(R.id.comments_posts_pager);
//        final ViewPager viewPager = (ViewPager) fragmentView.findViewById(R.id.profile_view_pager);
//        viewPager.setOffscreenPageLimit(2);
//        final PostsCommentsAdapter adapter = new PostsCommentsAdapter
//                (getChildFragmentManager(), tabLayout.getTabCount());
//        viewPager.setAdapter(adapter);
//        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
//        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                viewPager.setCurrentItem(tab.getPosition());
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });
//    }

    @Override
    public void onResume() {
        super.onResume();
        databaseQuery = new DatabaseQuery(getActivity());
        setNameAndId();
//        setupPostsAndCommentsTabLayout(fragmentView);
        profilePictureSetup(userId, name, fragmentView);
//        progressOverlay = fragmentView.findViewById(R.id.progress_overlay);
//        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.9f, 200);
//        databaseQuery.getMyTabsPosts(progressOverlay, fragmentView, getContext());

        String [] intentStrings = {userId, name};

        //TODO: Only call this after u get all the posts!
        TabsUtil.setupProfileView(fragmentView, "Profile", application, databaseQuery, intentStrings);
        postsView = fragmentView.findViewById(R.id.posts_tab);
        progressOverlay = postsView.findViewById(R.id.progress_overlay);
        setupPrivacyToggle(fragmentView);
        databaseQuery.getUserPosts(userId, fragmentView, application.getMyTabsAdapter(), getContext(), "posts");
    }
}
