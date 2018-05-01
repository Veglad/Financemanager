package com.example.vlad.financemanager;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomSpinnerAdapter extends ArrayAdapter<SpinnerItem> {

    public CustomSpinnerAdapter(Context context, int resource, ArrayList<SpinnerItem> spinnerItems){
        super(context,resource, spinnerItems);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){
        View view = InitView(position, convertView, parent);
        //view.setBackgroundResource(R.drawable.spinner_item_shape);
        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = InitView(position, convertView, parent);
        view.setBackgroundResource(R.drawable.spinner_shape);
        return view;
    }

    public  View InitView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.spinner_item, parent, false);
        }

        ImageView image = convertView.findViewById(R.id.icon);
        TextView accauntName = convertView.findViewById(R.id.tv_accountType);

        SpinnerItem currentItem = getItem(position);

        if(currentItem != null){
            image.setImageResource(currentItem.getImage());
            accauntName.setText(currentItem.getAccountName());
        }

        return convertView;
    }
}
