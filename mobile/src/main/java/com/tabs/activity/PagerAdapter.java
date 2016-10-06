package com.tabs.activity;

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
                PublicTabFragment tab1 = new PublicTabFragment();
                return tab1;
            case 1:
                FollowersTabFragment tab2 = new FollowersTabFragment();
                return tab2;
            case 2:
                ProfileTabFragment tab3 = new ProfileTabFragment();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
