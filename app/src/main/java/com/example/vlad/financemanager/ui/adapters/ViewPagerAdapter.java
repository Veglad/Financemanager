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

    private static final int TABS_NUMBER = 2;

    private List<String> titles;
    private Calendar currDay;
    private PeriodsOfTime period;
    private ArrayList<Operation> operationList;

    public ViewPagerAdapter(FragmentManager fm, List<String> titles) {
        super(fm);

        this.titles = titles;
        period = PeriodsOfTime.DAY;
        currDay = Calendar.getInstance();
    }

    @Override
    public Fragment getItem(int position) {
        String textDate = DateUtils.getStringDateByPeriod(period, currDay);

        return position == 0 ? TabFragment.newInstance(textDate, operationList, false) :
                TabFragment.newInstance(textDate, operationList, true);
    }

    public void setOperationList(List<Operation> operationList) {
        this.operationList = new ArrayList<>();
        this.operationList.addAll(operationList);
    }


    public void updateTab(Calendar currDay, PeriodsOfTime periods, List<Operation> operations) {
        this.currDay = currDay;
        this.period = periods;
        setOperationList(operations);

        notifyDataSetChanged();
    }

    //This method is invoked after notifyDataSetChanged method invocation
    @Override
    public int getItemPosition(Object object) {
        TabFragment fragment = (TabFragment) object;
        if (fragment != null) {
            String stringDateByPeriod = DateUtils.getStringDateByPeriod(period, currDay);
            fragment.updateTabFragment(stringDateByPeriod, operationList);
        }

        return super.getItemPosition(object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    @Override
    public int getCount() {
        return TABS_NUMBER;
    }
}