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
import com.test.tabs.tabs.com.tabs.database.friends.Friend;
import com.test.tabs.tabs.com.tabs.database.friends.FriendsDataSource;
import com.test.tabs.tabs.com.tabs.database.posts.PostRecyclerViewAdapter;
import com.test.tabs.tabs.com.tabs.database.posts.PostsDataSource;

import java.util.List;

/**
 * Created by schan on 12/30/15.
 */
public class FriendsTab extends Fragment {

    private View fragmentView;
    PostRecyclerViewAdapter adapter;
    private RecyclerView rv;
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

        System.out.println("Creating freinds view");
        fragmentView = inflater.inflate(R.layout.private_tab, container, false);

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
        System.out.println("On resume");
        LocationService.getLocationManager(getContext());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    private void populateNewsFeedList(View fragmentView, String userId){
        rv = (RecyclerView) fragmentView.findViewById(R.id.rv_private_feed);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        List<Friend> friends = datasource.getAllAddedFriends(userId);
        adapter = new PostRecyclerViewAdapter(postsDataSource.getPostsByFriends(friends), getContext(), false);
        rv.setAdapter(adapter);
    }
}
