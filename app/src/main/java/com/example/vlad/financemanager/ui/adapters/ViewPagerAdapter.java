package com.example.vlad.financemanager.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.vlad.financemanager.ui.fragments.TabFragment;
import com.example.vlad.financemanager.data.models.Operation;
import com.example.vlad.financemanager.data.enums.PeriodsOfTime;
import com.example.vlad.financemanager.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private static final int TABS_COUNT = 2;

    private List<String> tabTitles;
    private Calendar today;
    private PeriodsOfTime period;
    private ArrayList<Operation> operationList;

    public ViewPagerAdapter(FragmentManager fragmentManager, List<String> titles) {
        super(fragmentManager);

        this.tabTitles = titles;
        period = PeriodsOfTime.DAY;
        today = Calendar.getInstance();
    }

    @Override
    public Fragment getItem(int position) {
        String textDate = DateUtils.getStringDateByPeriod(period, today);

        return TabFragment.newInstance(textDate, operationList, position != 0 );
    }

    public void setOperationList(List<Operation> operationList) {
        this.operationList = new ArrayList<>();
        this.operationList.addAll(operationList);
    }

    public void updateTab(Calendar currDay, PeriodsOfTime periods) {
        this.today = currDay;
        this.period = periods;
    }

    //This method is invoked after notifyDataSetChanged method invocation
    @Override
    public int getItemPosition(Object object) {
        TabFragment fragment = (TabFragment) object;
        if (fragment != null) {
            String stringDateByPeriod = DateUtils.getStringDateByPeriod(period, today);
            fragment.updateTabFragment(stringDateByPeriod, operationList);
        }

        return super.getItemPosition(object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles.get(position);
    }

    @Override
    public int getCount() {
        return TABS_COUNT;
    }
}