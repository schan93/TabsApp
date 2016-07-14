package com.test.tabs.tabs.com.tabs.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by schan on 7/11/16.
 */
public class PostsCommentsAdapter extends FragmentStatePagerAdapter{
    int numTabs;

    public PostsCommentsAdapter(FragmentManager fm, int numTabs) {
        super(fm);
        this.numTabs = numTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                PostsTab tab1 = new PostsTab();
                return tab1;
            case 1:
                CommentsTab tab2 = new CommentsTab();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
