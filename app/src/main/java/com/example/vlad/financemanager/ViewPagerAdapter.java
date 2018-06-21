package com.example.vlad.financemanager;

import android.graphics.Path;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ViewPagerAdapter extends FragmentStatePagerAdapter{

    CharSequence Titles[];
    int NumbofTubs;
    Calendar  currDay;
    PeriodsOfTime periods;
    ArrayList<Operation> operations;

    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbofTabs){

        super(fm);

        Titles = mTitles;
        NumbofTubs = mNumbofTabs;
        periods = PeriodsOfTime.DAY;

        currDay = Calendar.getInstance();
    }

    @Override
    public Fragment getItem(int position) {
        String textDate = getStringDate();


        if(position == 0)
            return TabFragment.newInstance(textDate, operations, false);/////////
        else
            return TabFragment.newInstance(textDate, operations, true);///////////
    }

    public void setOperations(List<Operation> operations){
        this.operations = new ArrayList<>();
        this.operations.addAll(operations);
    }

    //Get text date for chosen period
    private String getStringDate() {
        String textDate = "";
        switch (periods){
            case DAY:
                textDate = new SimpleDateFormat("E, dd MMMM").format(currDay.getTime());
                break;
            case WEEK:
                Calendar endOfPeriod = CalendarSettings.getEndOfPeriod(currDay ,periods);
                textDate = getTextDateForWeek(endOfPeriod);
                break;
            case MONTH:
                textDate = new SimpleDateFormat("MMMM, yyyy").format(currDay.getTime());
                break;
            case YEAR:
                textDate = new SimpleDateFormat("yyyy").format(currDay.getTime());
                break;
            case ALL_TIME:
                textDate = "All";
        }

        return textDate;
    }

    //Get text date if chosen period is week
    private String getTextDateForWeek(Calendar endOfPeriod) {

        String weekPeriodTextDate;

        if(endOfPeriod.get(Calendar.DAY_OF_MONTH ) <= 6){
            Calendar fromDate = Calendar.getInstance();
            fromDate.setTime(endOfPeriod.getTime());
            fromDate.set(Calendar.DAY_OF_WEEK, 1);

            weekPeriodTextDate =fromDate.get(Calendar.DAY_OF_MONTH) + " " + new SimpleDateFormat("MMMM, yyyy").format(fromDate.getTime())+
                    " - " + endOfPeriod.get(Calendar.DAY_OF_MONTH) + " " + new SimpleDateFormat("MMMM, yyyy").format(endOfPeriod.getTime());
        }
        else {
            weekPeriodTextDate = (endOfPeriod.get(Calendar.DAY_OF_MONTH) - 6) + " - " + endOfPeriod.get(Calendar.DAY_OF_MONTH) + " " + new SimpleDateFormat("MMMM, yyyy").format(endOfPeriod.getTime());
        }


        return weekPeriodTextDate;
    }


    public void updateTab(Calendar currDay, PeriodsOfTime periods, List<Operation> operations){
        this.currDay = currDay;
        this.periods = periods;
        setOperations(operations);

        this.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object){
        TabFragment fragment = (TabFragment)object;
        if(fragment != null)
            fragment.updateTabFragment(getStringDate(), operations);

        return super.getItemPosition(object);
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