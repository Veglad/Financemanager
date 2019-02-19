package com.example.vlad.financemanager.ui.adapters

import android.content.Context
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.example.vlad.financemanager.R
import com.example.vlad.financemanager.data.models.SpinnerItem

class ImageSpinnerAdapter(context: Context, resource: Int, spinnerItems: List<SpinnerItem>,
                          @ColorRes private val selectedTextColor: Int,
                          @ColorRes private val dropDownTextColor: Int) :
        ArrayAdapter<SpinnerItem>(context, resource, spinnerItems) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent, dropDownTextColor)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent, selectedTextColor)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup, textColor: Int): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
                R.layout.image_spinner_item, parent, false)

        val image = view.findViewById<ImageView>(R.id.iconSpinnerItem)
        val accountName = view.findViewById<TextView>(R.id.accountTypeTextView)
        accountName.setTextColor(ContextCompat.getColor(context, textColor))

        val currentItem = getItem(position)

        if (currentItem != null) {
            image.setImageResource(currentItem.image)
            accountName.text = currentItem.name
        }

        return view
    }
}
