package com.test.tabs.tabs.com.tabs.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by schan on 12/30/15.
 */
public class PagerAdapter extends FragmentStatePagerAdapter{
    int numTabs;

    public PagerAdapter(FragmentManager fm, int numTabs) {
        super(fm);
        this.numTabs = numTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                PublicTab tab1 = new PublicTab();
                return tab1;
            case 1:
                FriendsTab tab2 = new FriendsTab();
                return tab2;
            case 2:
                MyTabs tab3 = new MyTabs();
                return tab3;
            case 3:
                FollowersTab tab4 = new FollowersTab();
                return tab4;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
