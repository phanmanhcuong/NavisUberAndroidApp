package com.example.admin.navisuber;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int tabNumber;
    public PagerAdapter(FragmentManager fm, int tabNumber) {
        super(fm);
        this.tabNumber = tabNumber;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                //waiting for handle tab
                TabHandling tabHandling = new TabHandling();
                return tabHandling;
            case 1:
                //waiting for car
                TabWaiting tabWaiting = new TabWaiting();
                return tabWaiting;
            case 2:
                //history tab
                TabHistory tabHistory = new TabHistory();
                return tabHistory;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabNumber;
    }
}
