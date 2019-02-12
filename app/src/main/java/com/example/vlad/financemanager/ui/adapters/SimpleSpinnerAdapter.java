package com.example.vlad.financemanager.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vlad.financemanager.R;
import com.example.vlad.financemanager.data.models.SpinnerItem;

import java.util.List;

public class SimpleSpinnerAdapter extends ArrayAdapter<String> {

    private int dropDownTextColor;
    private int selectedTextColor;

    public SimpleSpinnerAdapter(Context context, int resource, String[] spinnerItems, int selectedTextColor, int dropDownTextColor) {
        super(context, resource, spinnerItems);
        this.selectedTextColor = selectedTextColor;
        this.dropDownTextColor = dropDownTextColor;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent, dropDownTextColor);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = initView(position, convertView, parent, selectedTextColor);
        return view;
    }

    private View initView(int position, View convertView, ViewGroup parent, int textColor) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.simple_spinner_item, parent, false);
        }

        TextView accountName = convertView.findViewById(R.id.accountTypeTextView);
        accountName.setText(getItem(position));
        accountName.setTextColor(textColor);

        return convertView;
    }
}
