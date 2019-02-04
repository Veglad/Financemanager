package com.example.vlad.financemanager.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vlad.financemanager.R;
import com.example.vlad.financemanager.data.models.SpinnerItem;

import java.util.ArrayList;

public class IconAndTextSpinnerAdapter extends ArrayAdapter<SpinnerItem> {

    public IconAndTextSpinnerAdapter(Context context, int resource, ArrayList<SpinnerItem> spinnerItems){
        super(context,resource, spinnerItems);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){
        View view = InitView(position, convertView, parent);
        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = InitView(position, convertView, parent);
        view.setBackgroundResource(R.drawable.spinner_shape);
        return view;
    }

    private View InitView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.spinner_item, parent, false);
        }

        ImageView image = convertView.findViewById(R.id.iconSpinnerItem);
        TextView accountName = convertView.findViewById(R.id.accountTypeTextView);

        SpinnerItem currentItem = getItem(position);

        if(currentItem != null){
            image.setImageResource(currentItem.getImage());
            accountName.setText(currentItem.getName());
        }

        return convertView;
    }
}