package com.example.vlad.financemanager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private static final int TABS_NUMBER = 2;

    private List<String> _titles;
    private Calendar _currDay;
    private PeriodsOfTime _periods;
    private ArrayList<Operation> _operations;

    public ViewPagerAdapter(FragmentManager fm, List<String> titles){
        super(fm);

        _titles = titles;
        _periods = PeriodsOfTime.DAY;
        _currDay = Calendar.getInstance();
    }

    @Override
    public Fragment getItem(int position) {
        String textDate = getStringDate();


        if(position == 0)
            return TabFragment.newInstance(textDate, _operations, false);
        else
            return TabFragment.newInstance(textDate, _operations, true);
    }

    public void setOperations(List<Operation> operations){
        _operations = new ArrayList<>();
        _operations.addAll(operations);
    }

    //Get text date for chosen period
    private String getStringDate() {
        String textDate = "";
        switch (_periods){
            case DAY:
                textDate = new SimpleDateFormat("E, dd MMMM").format(_currDay.getTime());
                break;
            case WEEK:
                Calendar endOfPeriod = CalendarSettings.getEndOfPeriod(_currDay, _periods);
                textDate = getTextDateForWeek(endOfPeriod);
                break;
            case MONTH:
                textDate = new SimpleDateFormat("MMMM, yyyy").format(_currDay.getTime());
                break;
            case YEAR:
                textDate = new SimpleDateFormat("yyyy").format(_currDay.getTime());
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
        _currDay = currDay;
        _periods = periods;
        setOperations(operations);

        this.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object){
        TabFragment fragment = (TabFragment)object;
        if(fragment != null)
            fragment.updateTabFragment(getStringDate(), _operations);

        return super.getItemPosition(object);
    }

    @Override
    public CharSequence getPageTitle(int position){
        return _titles.get(position);
    }

    @Override
    public int getCount() {
        return TABS_NUMBER;
    }
}