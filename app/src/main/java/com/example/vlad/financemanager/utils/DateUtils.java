package com.example.vlad.financemanager.utils;

import com.example.vlad.financemanager.data.enums.PeriodsOfTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

    private static final int DAYS_IN_WEEK = 7;
    private static final String DATE_MEDIUM_PATTERN = "E, dd MMMM";
    private static final String DATE_MONTH_AND_YEAR_PATTERN = "MMMM, yyyy";
    private static final String DATE_YEAR_PATTERN = "yyyy";
    public static final String DATE_FULL_PATTERN = "MM.dd.yyyy";

    private static final SimpleDateFormat DATE_MONTH_YEAR_FORMAT = new SimpleDateFormat(DATE_MONTH_AND_YEAR_PATTERN, Locale.getDefault());

    private DateUtils() {
    }

    //Get text date for chosen period
    public static String getStringDateByPeriod(PeriodsOfTime periodOfTIme, Calendar date) {
        String textDate = "";
        switch (periodOfTIme) {
            case DAY:
                textDate = getStringDate(date.getTime(), DATE_MEDIUM_PATTERN);
                break;
            case WEEK:
                Calendar endOfPeriod = getEndOfPeriod(date, periodOfTIme);
                textDate = getStringDateForWeek(endOfPeriod);
                break;
            case MONTH:
                textDate = getStringDate(date.getTime(), DATE_MONTH_AND_YEAR_PATTERN);
                break;
            case YEAR:
                textDate = getStringDate(date.getTime(), DATE_YEAR_PATTERN);
                break;
            case ALL_TIME:
                textDate = "All"; //TODO: extract to string resources
            default:
                break;
        }

        return textDate;
    }

    public static String getStringDate(Date date, String pattern) {
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }

    public static Date getDateFromString(String dateString, String pattern) {
        try {
            return new SimpleDateFormat(pattern, Locale.getDefault()).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
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
                    .append(DATE_MONTH_YEAR_FORMAT.format(fromDate))
                    .append(" - ")
                    .append(endOfPeriod.get(Calendar.DAY_OF_MONTH))
                    .append(" ")
                    .append(DATE_MONTH_YEAR_FORMAT.format(endOfPeriod));

        } else {
            weekPeriodTextDateBuilder.append(endOfPeriod.get(Calendar.DAY_OF_MONTH) - (DAYS_IN_WEEK - 1))
                    .append(" - ")
                    .append(endOfPeriod.get(Calendar.DAY_OF_MONTH))
                    .append(" ")
                    .append(DATE_MONTH_YEAR_FORMAT.format(endOfPeriod));
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
            default:
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
                break;
            default:
                break;
        }

        return startOfPeriod;
    }
}
