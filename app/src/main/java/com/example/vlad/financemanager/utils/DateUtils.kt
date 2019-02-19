package com.example.vlad.financemanager.utils

import com.example.vlad.financemanager.data.enums.PeriodsOfTime

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val MILLISECONDS_IN_DAY = (1000 * 60 * 60 * 24).toLong()
    private const val DAYS_IN_WEEK = 7
    private const val DATE_MEDIUM_PATTERN = "dd MMMM"
    private const val DATE_MONTH_AND_YEAR_PATTERN = "MMMM, yyyy"
    private const val DATE_YEAR_PATTERN = "yyyy"
    const val DATE_FULL_PATTERN = "MM.dd.yyyy"

    private val DATE_MONTH_YEAR_FORMAT = SimpleDateFormat(DATE_MONTH_AND_YEAR_PATTERN, Locale.getDefault())

    //Get text date for chosen period
    fun getStringDateByPeriod(periodOfTIme: PeriodsOfTime, date: Calendar): String {
        var textDate = ""
        textDate = when (periodOfTIme) {
            PeriodsOfTime.DAY -> getStringDate(date.time, DATE_MEDIUM_PATTERN)
            PeriodsOfTime.WEEK -> {
                val endOfPeriod = getEndOfPeriod(date, periodOfTIme)
                getStringDateForWeek(endOfPeriod)
            }
            PeriodsOfTime.MONTH -> getStringDate(date.time, DATE_MONTH_AND_YEAR_PATTERN)
            PeriodsOfTime.YEAR -> getStringDate(date.time, DATE_YEAR_PATTERN)
            PeriodsOfTime.ALL_TIME -> "All" //TODO: extract to string resources
        }

        return textDate
    }

    fun slideDateIfAble(currDate: Calendar, isRightSlide: Boolean, currentPeriod: PeriodsOfTime, minDate: Date, maxDate: Date, includeLast: Boolean): Boolean { //TODO: Refactor this
        var maxDate = maxDate
        if (currentPeriod === PeriodsOfTime.ALL_TIME)
            return false

        if (includeLast) {
            val calendar = Calendar.getInstance()
            calendar.time = maxDate
            maxDate = getEndOfPeriod(calendar, currentPeriod).time
        }

        val slideDays = when (currentPeriod) {
            PeriodsOfTime.DAY -> 1
            PeriodsOfTime.WEEK -> 7
            PeriodsOfTime.MONTH -> getActualMaxForNextMonth(currDate)
            else -> getActualMaxForNextYear(currDate)
        }

        if (isRightSlide) {
            currDate.time = Date(currDate.time.time + slideDays * MILLISECONDS_IN_DAY)
        } else {
            currDate.time = Date(currDate.time.time - slideDays * MILLISECONDS_IN_DAY)
        }

        setMinTimeOfADay(currDate)

        //if a new date get out from the today date
        if (currDate.time > maxDate) {
            currDate.time = maxDate
            return false
        } else if (currDate.time < minDate) {
            currDate.time = minDate
            return false
        }

        return true
    }

    private fun getActualMaxForNextMonth(currDate: Calendar): Int {
        val nextMonth = Calendar.getInstance()
        nextMonth.time = currDate.time
        nextMonth.add(Calendar.MONTH, 1)
        return nextMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun getActualMaxForNextYear(currDate: Calendar): Int {
        val nextYear = Calendar.getInstance()
        nextYear.time = currDate.time
        nextYear.add(Calendar.YEAR, 1)
        return nextYear.getActualMaximum(Calendar.DAY_OF_YEAR)
    }

    fun getStringDate(date: Date, pattern: String): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }

    fun getDateFromString(dateString: String, pattern: String): Date {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
            Date()
        }
    }

    //Get text date if chosen period is week
    private fun getStringDateForWeek(endOfPeriod: Calendar): String {
        val weekPeriodTextDateBuilder = StringBuilder()

        if (endOfPeriod.get(Calendar.DAY_OF_MONTH) < DAYS_IN_WEEK) {
            val fromDate = Calendar.getInstance()
            fromDate.time = endOfPeriod.time
            fromDate.set(Calendar.DAY_OF_WEEK, fromDate.firstDayOfWeek)

            weekPeriodTextDateBuilder.append(fromDate.get(Calendar.DAY_OF_MONTH))
                    .append(" ")
                    .append(DATE_MONTH_YEAR_FORMAT.format(fromDate.time))
                    .append(" - ")
                    .append(endOfPeriod.get(Calendar.DAY_OF_MONTH))
                    .append(" ")
                    .append(DATE_MONTH_YEAR_FORMAT.format(endOfPeriod.time))

        } else {
            weekPeriodTextDateBuilder.append(endOfPeriod.get(Calendar.DAY_OF_MONTH) - (DAYS_IN_WEEK - 1))
                    .append(" - ")
                    .append(endOfPeriod.get(Calendar.DAY_OF_MONTH))
                    .append(" ")
                    .append(DATE_MONTH_YEAR_FORMAT.format(endOfPeriod.time))
        }


        return weekPeriodTextDateBuilder.toString()
    }


    fun getEndOfPeriod(currDate: Calendar, period: PeriodsOfTime): Calendar {
        val endOfPeriod = Calendar.getInstance()
        endOfPeriod.time = currDate.time

        when (period) {
            PeriodsOfTime.WEEK -> endOfPeriod.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            PeriodsOfTime.MONTH -> endOfPeriod.set(Calendar.DAY_OF_MONTH, endOfPeriod.getActualMaximum(Calendar.DAY_OF_MONTH))
            PeriodsOfTime.YEAR -> endOfPeriod.set(Calendar.DAY_OF_YEAR, endOfPeriod.getActualMaximum(Calendar.DAY_OF_YEAR))
            else -> {
            }
        }
        setMaxTimeOfADay(endOfPeriod)

        return endOfPeriod
    }

    fun substractOneDay(date: Date): Date {
        return Date(date.time - MILLISECONDS_IN_DAY)
    }

    fun getSutedDateIndexByDateFromList(date: Calendar, endOfPeriodList: List<Calendar>): Int {
        if (endOfPeriodList.size <= 1) return 0

        var position = endOfPeriodList.size - 1
        var i = position
        while (i != 0 && date <= endOfPeriodList[--i]) {
            position = i
        }

        return position
    }

    private fun setMaxTimeOfADay(endOfPeriod: Calendar) {
        with(endOfPeriod) {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
    }

    fun getStartOfPeriod(currDate: Calendar, period: PeriodsOfTime): Calendar {
        val startOfPeriod = Calendar.getInstance()
        startOfPeriod.time = currDate.time

        when (period) {
            PeriodsOfTime.WEEK -> startOfPeriod.set(Calendar.DAY_OF_WEEK, startOfPeriod.firstDayOfWeek)
            PeriodsOfTime.MONTH -> startOfPeriod.set(Calendar.DAY_OF_MONTH, 1)
            PeriodsOfTime.YEAR -> startOfPeriod.set(Calendar.DAY_OF_YEAR, 1)
            else -> {
            }
        }
        setMinTimeOfADay(startOfPeriod)

        return startOfPeriod
    }

    private fun setMinTimeOfADay(startOfPeriod: Calendar) {
        startOfPeriod.set(Calendar.HOUR_OF_DAY, 0)
        startOfPeriod.set(Calendar.MINUTE, 0)
        startOfPeriod.set(Calendar.SECOND, 0)
    }

    fun isOutOfPeriod(checkedDate: Date, period: PeriodsOfTime, endOfPeriodCalendar: Calendar): Boolean {
        if (PeriodsOfTime.ALL_TIME === period) return false
        val startOfPeriod = getStartOfPeriod(endOfPeriodCalendar, period).time
        setMaxTimeOfADay(endOfPeriodCalendar)
        val endOfPeriod = endOfPeriodCalendar.time

        return checkedDate < startOfPeriod || checkedDate > endOfPeriod
    }
}
