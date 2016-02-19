package com.test.tabs.tabs.com.tabs.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.test.tabs.tabs.R;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

/**
 * Created by schan on 12/30/15.
 */
public class MyTabs extends Fragment {

    private View fragmentView;
    PostRecyclerViewAdapter adapter;
    //Local Database for storing posts
    private PostsDataSource postsDataSource;
    //Local Database for storing friends
    private FriendsDataSource datasource;
    String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Set things such as facebook profile picture, facebook friends photos, etc.
        userId = AccessToken.getCurrentAccessToken().getUserId();

        //Open DB and get freinds from db & posts.
        datasource = new FriendsDataSource(getContext());
        datasource.open();
        postsDataSource = new PostsDataSource(getContext());
        postsDataSource.open();

        fragmentView = inflater.inflate(R.layout.my_tabs_tab, container, false);

        populateNewsFeedList(fragmentView, userId);

        return fragmentView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
                System.out.println("It's visible!");
                //Simply does a requery but don't we want it so that we just do notifyDataSetChanged?
//                adapter.notifyDataSetChanged();
                populateNewsFeedList(fragmentView, userId);
                // your code goes here
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        LocationService.getLocationManager(getContext());
//        populateNewsFeedList(fragmentView, userId);
        adapter.notifyDataSetChanged();
    }

    private void populateNewsFeedList(View fragmentView, String userId){
        RecyclerView rv = (RecyclerView) fragmentView.findViewById(R.id.rv_my_tabs_feed);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        System.out.println("User Id in Posts: " + userId);
        adapter = new PostRecyclerViewAdapter(postsDataSource.getPostsByUser(userId), getContext(), false);
        rv.setAdapter(adapter);
    }
}
