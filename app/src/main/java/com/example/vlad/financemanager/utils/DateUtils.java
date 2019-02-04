package com.example.vlad.financemanager.utils;

import com.example.vlad.financemanager.data.enums.PeriodsOfTime;

import java.util.Calendar;

public class DateUtils {

    static public Calendar getEndOfPeriod(Calendar currDate, PeriodsOfTime periods) {
        Calendar endOfPeriod = Calendar.getInstance();
        endOfPeriod.setTime(currDate.getTime());

        switch (periods){
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

    static public Calendar getStartOfPeriod(Calendar currDate, PeriodsOfTime periods){
        Calendar startOfPeriod = Calendar.getInstance();
        startOfPeriod.setTime(currDate.getTime());

        switch (periods){
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
