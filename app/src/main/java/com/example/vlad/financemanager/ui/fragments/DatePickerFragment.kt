package com.example.vlad.financemanager.ui.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment

import java.util.Calendar
import java.util.Date

class DatePickerFragment : DialogFragment() {

    private val calendar = Calendar.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(activity!!,
                activity as DatePickerDialog.OnDateSetListener?, year, month, day)
        datePickerDialog.datePicker.maxDate = Date().time
        return datePickerDialog
    }

    fun setCalendar(date: Date) {
        calendar.time = date
    }
}
