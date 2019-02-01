package com.example.vlad.financemanager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private static final int TABS_NUMBER = 2;
    private static final int DAYS_IN_WEEK = 7;
    private static final String DATE_MEDIUM_PATTERN = "E, dd MMMM";
    private static final String DATE_MONTH_AND_YEAR_PATTERN = "MMMM, yyyy";
    private static final String DATE_YEAR_PATTERN = "yyyy";

    private static final SimpleDateFormat mediumDateFormat = new SimpleDateFormat(DATE_MEDIUM_PATTERN, Locale.getDefault());
    private static final SimpleDateFormat monthAndYearDateFormat = new SimpleDateFormat(DATE_MONTH_AND_YEAR_PATTERN, Locale.getDefault());
    private static final SimpleDateFormat yearDateFormat = new SimpleDateFormat(DATE_YEAR_PATTERN, Locale.getDefault());

    private List<String> titles;
    private Calendar currDay;
    private PeriodsOfTime periods;
    private ArrayList<Operation> operations;

    public ViewPagerAdapter(FragmentManager fm, List<String> titles) {
        super(fm);

        this.titles = titles;
        periods = PeriodsOfTime.DAY;
        currDay = Calendar.getInstance();
    }

    @Override
    public Fragment getItem(int position) {
        String textDate = getStringDate();

        return position == 0 ? TabFragment.newInstance(textDate, operations, false) :
                TabFragment.newInstance(textDate, operations, true);
    }

    public void setOperations(List<Operation> operations) {
        this.operations = new ArrayList<>();
        this.operations.addAll(operations);
    }

    //Get text date for chosen period
    private String getStringDate() {
        String textDate = "";
        switch (periods) {
            case DAY:
                textDate = mediumDateFormat.format(currDay.getTime());
                break;
            case WEEK:
                Calendar endOfPeriod = CalendarSettings.getEndOfPeriod(currDay, periods);
                textDate = getTextDateForWeek(endOfPeriod);
                break;
            case MONTH:
                textDate = monthAndYearDateFormat.format(currDay.getTime());
                break;
            case YEAR:
                textDate = yearDateFormat.format(currDay.getTime());
                break;
            case ALL_TIME:
                textDate = "All";
        }

        return textDate;
    }

    //Get text date if chosen period is week
    private String getTextDateForWeek(Calendar endOfPeriod) {
        StringBuilder weekPeriodTextDateBuilder = new StringBuilder();

        if (endOfPeriod.get(Calendar.DAY_OF_MONTH) < DAYS_IN_WEEK) {
            Calendar fromDate = Calendar.getInstance();
            fromDate.setTime(endOfPeriod.getTime());
            fromDate.set(Calendar.DAY_OF_WEEK, 1);

            weekPeriodTextDateBuilder.append(fromDate.get(Calendar.DAY_OF_MONTH))
                    .append(" ")
                    .append(monthAndYearDateFormat.format(fromDate.getTime()))
                    .append(" - ")
                    .append(endOfPeriod.get(Calendar.DAY_OF_MONTH))
                    .append(" ")
                    .append(monthAndYearDateFormat.format(endOfPeriod.getTime()));

        } else {
            weekPeriodTextDateBuilder.append(endOfPeriod.get(Calendar.DAY_OF_MONTH) - (DAYS_IN_WEEK - 1))
                    .append(" - ")
                    .append(endOfPeriod.get(Calendar.DAY_OF_MONTH))
                    .append(" ")
                    .append(monthAndYearDateFormat.format(endOfPeriod.getTime()));
        }


        return weekPeriodTextDateBuilder.toString();
    }


    public void updateTab(Calendar currDay, PeriodsOfTime periods, List<Operation> operations) {
        this.currDay = currDay;
        this.periods = periods;
        setOperations(operations);

        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        TabFragment fragment = (TabFragment) object;
        if (fragment != null)
            fragment.updateTabFragment(getStringDate(), operations);

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