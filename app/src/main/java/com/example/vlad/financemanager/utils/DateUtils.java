package com.example.vlad.financemanager.utils;

import com.example.vlad.financemanager.data.enums.PeriodsOfTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class DateUtils {

    private static final long MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;
    private static final int DAYS_IN_WEEK = 7;
    private static final String DATE_MEDIUM_PATTERN = "dd MMMM";
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

    public static boolean slideDateIfAble(Calendar currDate, boolean isRightSlide, PeriodsOfTime currentPeriod, Date minDate, Date maxDate, boolean includeLast) { //TODO: Refactor this
        if (currentPeriod == PeriodsOfTime.ALL_TIME)
            return false;

        if (includeLast) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(maxDate);
            maxDate = getEndOfPeriod(calendar, currentPeriod).getTime();
        }

        int slideDays;
        switch (currentPeriod) {
            case DAY:
                slideDays = 1;
                break;
            case WEEK:
                slideDays = 7;
                break;
            case MONTH:
                slideDays = getActualMaxForNextMonth(currDate);
                break;
            default:
                slideDays = getActualMaxForNextYear(currDate);
                break;
        }

        if (isRightSlide) {
            currDate.setTime(new Date(currDate.getTime().getTime() + slideDays * MILLISECONDS_IN_DAY));
        } else {
            currDate.setTime(new Date(currDate.getTime().getTime() - slideDays * MILLISECONDS_IN_DAY));
        }

        setMinTimeOfADay(currDate);

        //if a new date get out from the today date
        if (currDate.getTime().compareTo(maxDate) > 0) {
            currDate.setTime(maxDate);
            return false;
        } else if (currDate.getTime().compareTo(minDate) < 0) {
            currDate.setTime(minDate);
            return false;
        }

        return true;
    }

    private static int getActualMaxForNextMonth(Calendar currDate) {
        Calendar nextMonth = Calendar.getInstance();
        nextMonth.setTime(currDate.getTime());
        nextMonth.add(Calendar.MONTH, 1);
        return nextMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private static int getActualMaxForNextYear(Calendar currDate) {
        Calendar nextYear = Calendar.getInstance();
        nextYear.setTime(currDate.getTime());
        nextYear.add(Calendar.YEAR, 1);
        return nextYear.getActualMaximum(Calendar.DAY_OF_YEAR);
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
            fromDate.set(Calendar.DAY_OF_WEEK, fromDate.getFirstDayOfWeek());

            weekPeriodTextDateBuilder.append(fromDate.get(Calendar.DAY_OF_MONTH))
                    .append(" ")
                    .append(DATE_MONTH_YEAR_FORMAT.format(fromDate.getTime()))
                    .append(" - ")
                    .append(endOfPeriod.get(Calendar.DAY_OF_MONTH))
                    .append(" ")
                    .append(DATE_MONTH_YEAR_FORMAT.format(endOfPeriod.getTime()));

        } else {
            weekPeriodTextDateBuilder.append(endOfPeriod.get(Calendar.DAY_OF_MONTH) - (DAYS_IN_WEEK - 1))
                    .append(" - ")
                    .append(endOfPeriod.get(Calendar.DAY_OF_MONTH))
                    .append(" ")
                    .append(DATE_MONTH_YEAR_FORMAT.format(endOfPeriod.getTime()));
        }


        return weekPeriodTextDateBuilder.toString();
    }


    public static Calendar getEndOfPeriod(Calendar currDate, PeriodsOfTime period) {
        Calendar endOfPeriod = Calendar.getInstance();
        endOfPeriod.setTime(currDate.getTime());

        switch (period) {
            case WEEK:
                endOfPeriod.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
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
        setMaxTimeOfADay(endOfPeriod);

        return endOfPeriod;
    }

    public static Date substractOneDay(Date date) {
        return new Date(date.getTime() - MILLISECONDS_IN_DAY);
    }

    public static int getSutedDateIndexByDateFromList(Calendar date, List<Calendar> endOfPeriodList) {
        if (endOfPeriodList.size() == 1) return 0;

        int position = endOfPeriodList.size() - 1;
        int i = position;
        while (date.compareTo(endOfPeriodList.get(--i)) <= 0) {
            position = i;
        }

        return position;
    }

    public static void setMaxTimeOfADay(Calendar endOfPeriod) {
        endOfPeriod.set(Calendar.HOUR_OF_DAY, 23);
        endOfPeriod.set(Calendar.MINUTE, 59);
        endOfPeriod.set(Calendar.SECOND, 59);
    }

    public static Calendar getStartOfPeriod(Calendar currDate, PeriodsOfTime period) {
        Calendar startOfPeriod = Calendar.getInstance();
        startOfPeriod.setTime(currDate.getTime());

        switch (period) {
            case WEEK:
                startOfPeriod.set(Calendar.DAY_OF_WEEK, startOfPeriod.getFirstDayOfWeek());
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
        setMinTimeOfADay(startOfPeriod);

        return startOfPeriod;
    }

    public static void setMinTimeOfADay(Calendar startOfPeriod) {
        startOfPeriod.set(Calendar.HOUR_OF_DAY, 0);
        startOfPeriod.set(Calendar.MINUTE, 0);
        startOfPeriod.set(Calendar.SECOND, 0);
    }

    public static boolean isOutOfPeriod(Date checkedDate, PeriodsOfTime period, Calendar endOfPeriodCalendar) {
        if (PeriodsOfTime.ALL_TIME == period) return false;
        Date startOfPeriod = getStartOfPeriod(endOfPeriodCalendar, period).getTime();
        setMaxTimeOfADay(endOfPeriodCalendar);
        Date endOfPeriod = endOfPeriodCalendar.getTime();

        return checkedDate.compareTo(startOfPeriod) < 0 || checkedDate.compareTo(endOfPeriod) > 0;
    }
}
