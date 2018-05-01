package com.example.vlad.financemanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ViewPagerAdapter extends FragmentStatePagerAdapter{

    CharSequence Titles[];
    int NumbofTubs;

    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbofTabs){

        super(fm);

        Titles = mTitles;
        NumbofTubs = mNumbofTabs;

    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0)
            return TabFragment.newInstance("Tuesday, 17 April");
        else
            return TabFragment.newInstance("Tuesday, 17 April");
    }

    @Override
    public CharSequence getPageTitle(int position){
        return Titles[position];
    }

    @Override
    public int getCount() {
        return NumbofTubs;
    }
}