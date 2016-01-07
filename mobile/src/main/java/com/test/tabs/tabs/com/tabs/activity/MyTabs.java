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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Set things such as facebook profile picture, facebook friends photos, etc.
        String userId = AccessToken.getCurrentAccessToken().getUserId();

        //Open DB and get freinds from db & posts.
        datasource = new FriendsDataSource(getContext());
        datasource.open();
        postsDataSource = new PostsDataSource(getContext());
        postsDataSource.open();

        fragmentView = inflater.inflate(R.layout.content_news_feed, container, false);

        populateNewsFeedList(fragmentView, userId);

        return fragmentView;
    }

    @Override
    public void onResume(){
        super.onResume();
        LocationService.getLocationManager(getContext());
    }

    private void populateNewsFeedList(View fragmentView, String userId){
        RecyclerView rv = (RecyclerView) fragmentView.findViewById(R.id.rv_news_feed);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        adapter = new PostRecyclerViewAdapter(postsDataSource.getPostsByUser(userId), getContext(), false);
        rv.setAdapter(adapter);
    }
}
