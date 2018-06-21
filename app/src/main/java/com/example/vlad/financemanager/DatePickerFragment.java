package com.example.vlad.financemanager;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment {

    private Calendar calendar;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c;
        if(calendar == null)
            c = Calendar.getInstance();
        else
            c = calendar;

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        //The max date is today
        DatePickerDialog dpd = new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener)getActivity(), year, month, day);
        dpd.getDatePicker().setMaxDate(new Date().getTime());
        return dpd;
    }

    public void setCalendar(Date date){
        calendar = Calendar.getInstance();
        calendar.setTime(date);
    }
}
