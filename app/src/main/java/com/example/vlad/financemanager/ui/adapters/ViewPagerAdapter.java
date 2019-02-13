package com.example.vlad.financemanager.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.example.vlad.financemanager.ui.fragments.TabFragment;
import com.example.vlad.financemanager.data.enums.PeriodsOfTime;

import java.util.Calendar;
import java.util.List;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private PeriodsOfTime currentPeriod;
    private List<Calendar> endOfPeriodList;
    private List<String> tabTitles;
    SparseArray<TabFragment> registeredFragments = new SparseArray<TabFragment>();

    private int accountId;
    private boolean isIncome;

    public ViewPagerAdapter(FragmentManager fragmentManager, List<String> titles, List<Calendar> endOfPeriodList,
                            PeriodsOfTime periodsOfTime, int accountId, boolean isIncome) {
        super(fragmentManager);

        this.accountId = accountId;
        this.isIncome = isIncome;
        this.currentPeriod = periodsOfTime;
        this.endOfPeriodList = endOfPeriodList;
        this.tabTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return TabFragment.newInstance(currentPeriod, endOfPeriodList.get(position), isIncome, accountId, tabTitles.get(position));
    }

    public void setendOfPeriodList(List<Calendar> endOfPeriodList) {
        this.endOfPeriodList = endOfPeriodList;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setCurrentPeriod(PeriodsOfTime currentPeriod) {
        this.currentPeriod = currentPeriod;
    }

    //This method is invoked after notifyDataSetChanged method invocation
    @Override
    public int getItemPosition(Object object) {
        TabFragment fragment = (TabFragment) object;
        if (fragment != null) {
            fragment.fullTabFragmentUpdate(currentPeriod, fragment.getCurrentEndOfPeriod(), isIncome, accountId, fragment.getDateTitle());
        }

        return super.getItemPosition(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TabFragment fragment = (TabFragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public TabFragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles.get(position);
    }

    @Override
    public int getCount() {
        return endOfPeriodList.size();
    }

    public List<Calendar> getEndOfPeriodList() {
        return endOfPeriodList;
    }

    public void setEndOfPeriodList(List<Calendar> endOfPeriodList) {
        this.endOfPeriodList = endOfPeriodList;
    }

    public List<String> getTabTitles() {
        return tabTitles;
    }

    public void setTabTitles(List<String> tabTitles) {
        this.tabTitles = tabTitles;
    }

    public void setIsIncome(boolean isIncome) {
        this.isIncome = isIncome;
    }
}