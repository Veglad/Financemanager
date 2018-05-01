package com.example.vlad.financemanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;


public class TabFragment extends Fragment {
    private View view;
    private TextView textDate;
    String text;///////////
    PieChart pieChart;

    public static TabFragment newInstance(String str) {
        Bundle args = new Bundle();
        args.putString("TabText", str);/////////////////
        TabFragment fragment = new TabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanse){
        view = inflater.inflate(R.layout.fragment_tab, container, false );

        textDate = (TextView)view.findViewById(R.id.tvDateText);
        text = getArguments().getString("TabText");
        textDate.setText(text);

        pieChart = (PieChart) view.findViewById(R.id.pieChart);
        pieChart.setUsePercentValues(true);

        initPieChart();

        return  view;
    }

    private void initPieChart(){
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(25f,"Car"));
        entries.add(new PieEntry(12f,"Food"));
        entries.add(new PieEntry(24f,"Clothes"));
        entries.add(new PieEntry(17f,"House"));
        entries.add(new PieEntry(15f,"Transport"));
        entries.add(new PieEntry(8f,"Cafes"));

        PieDataSet dataSet = new PieDataSet(entries, "Some text");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setSliceSpace(3);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());

        pieChart.setData(data);

        pieChart.setRotationEnabled(false);

        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.getLegend().setEnabled(false);

        pieChart.invalidate();
    }

}
