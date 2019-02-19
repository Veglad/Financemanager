package com.example.vlad.financemanager.ui.adapters

import android.content.Context
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import com.example.vlad.financemanager.R

class SimpleSpinnerAdapter(context: Context, resource: Int, spinnerItems: Array<String>,
                           @param:ColorRes @field:ColorRes private val selectedTextColor: Int,
                           @param:ColorRes @field:ColorRes private val dropDownTextColor: Int) :
        ArrayAdapter<String>(context, resource, spinnerItems) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent, dropDownTextColor)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent, selectedTextColor)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup, textColor: Int): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
                R.layout.simple_spinner_item, parent, false)

        val accountName = view.findViewById<TextView>(R.id.accountTypeTextView)
        accountName.text = getItem(position)
        accountName.setTextColor(ContextCompat.getColor(context, textColor))

        return view
    }
}
