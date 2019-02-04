package com.example.vlad.financemanager.utils;

import com.example.vlad.financemanager.data.enums.PeriodsOfTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateUtils {

    private static final int DAYS_IN_WEEK = 7;
    private static final String DATE_MEDIUM_PATTERN = "E, dd MMMM";
    private static final String DATE_MONTH_AND_YEAR_PATTERN = "MMMM, yyyy";
    private static final String DATE_YEAR_PATTERN = "yyyy";

    private static final SimpleDateFormat mediumDateFormat = new SimpleDateFormat(DATE_MEDIUM_PATTERN, Locale.getDefault());
    private static final SimpleDateFormat monthAndYearDateFormat = new SimpleDateFormat(DATE_MONTH_AND_YEAR_PATTERN, Locale.getDefault());
    private static final SimpleDateFormat yearDateFormat = new SimpleDateFormat(DATE_YEAR_PATTERN, Locale.getDefault());

    //Get text date for chosen period
    public static String getStringDateByPeriod(PeriodsOfTime periodOfTIme, Calendar date) {
        String textDate = "";
        switch (periodOfTIme) {
            case DAY:
                textDate = mediumDateFormat.format(date.getTime());
                break;
            case WEEK:
                Calendar endOfPeriod = getEndOfPeriod(date, periodOfTIme);
                textDate = getStringDateForWeek(endOfPeriod);
                break;
            case MONTH:
                textDate = monthAndYearDateFormat.format(date.getTime());
                break;
            case YEAR:
                textDate = yearDateFormat.format(date.getTime());
                break;
            case ALL_TIME:
                textDate = "All";
        }

        return textDate;
    }

    //Get text date if chosen period is week
    private static String getStringDateForWeek(Calendar endOfPeriod) {
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


    public static Calendar getEndOfPeriod(Calendar currDate, PeriodsOfTime period) {
        Calendar endOfPeriod = Calendar.getInstance();
        endOfPeriod.setTime(currDate.getTime());

        switch (period) {
            case WEEK:
                endOfPeriod.set(Calendar.DAY_OF_WEEK, 7);
                break;
            case MONTH:
                endOfPeriod.set(Calendar.DAY_OF_MONTH, endOfPeriod.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            case YEAR:
                endOfPeriod.set(Calendar.DAY_OF_YEAR, endOfPeriod.getActualMaximum(Calendar.DAY_OF_YEAR));
                break;
        }

        return endOfPeriod;
    }

    public static Calendar getStartOfPeriod(Calendar currDate, PeriodsOfTime period) {
        Calendar startOfPeriod = Calendar.getInstance();
        startOfPeriod.setTime(currDate.getTime());

        switch (period) {
            case WEEK:
                startOfPeriod.set(Calendar.DAY_OF_WEEK, 1);
                break;
            case MONTH:
                startOfPeriod.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case YEAR:
                startOfPeriod.set(Calendar.DAY_OF_YEAR, 1);

        }

        return startOfPeriod;
    }
}
