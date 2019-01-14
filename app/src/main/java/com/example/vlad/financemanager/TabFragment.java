package com.example.vlad.financemanager;

import android.graphics.Color;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class TabFragment extends Fragment {
    private View view;
    private TextView textDate;
    boolean isIncome;
    private ArrayList<Operation> operations;
    String text;///////////
    PieChart pieChart;
    private static final String tabDateTextKey = "TabDateText";
    private static final String tabIsIncomeKey = "TabIsIncome";
    private static final String tabOperationsKey = "OperationsKey";

    public static TabFragment newInstance(String str, ArrayList<Operation> operations, boolean isIncome) {
        Bundle args = new Bundle();
        args.putString(tabDateTextKey, str);/////////////////
        args.putBoolean(tabIsIncomeKey, isIncome);
        args.putSerializable(tabOperationsKey, operations);

        TabFragment fragment = new TabFragment();
        fragment.setArguments(args);
        fragment.isIncome = isIncome;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanse){
        view = inflater.inflate(R.layout.fragment_tab, container, false );

        textDate = (TextView)view.findViewById(R.id.datePieChartFragmentTextView);
        text = getArguments().getString(tabDateTextKey);
        textDate.setText(text);

        operations = (ArrayList<Operation>)getArguments().getSerializable(tabOperationsKey);
        boolean isIncome = getArguments().getBoolean(tabIsIncomeKey);

        pieChart = (PieChart) view.findViewById(R.id.pieChart);
        pieChart.setUsePercentValues(true);

        drawPieChart(isIncome);

        return  view;
    }

    //Full tab fragment update
    public void updateTabFragment(String textDate, ArrayList<Operation> operations){
        this.operations = operations;
        this.textDate.setText(textDate);
        drawPieChart(isIncome);
    }

    private void drawPieChart(boolean isIncome){
        List<PieEntry> entries = getPieEntries(isIncome);

        PieDataSet dataSet = new PieDataSet(entries, "Some text");
        if(isIncome)
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        else
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        dataSet.setSliceSpace(3);

        PieData data = new PieData(dataSet);

        if(entries.size() == 0){
            entries.add(setEmptyEntrie());
            dataSet.setColor(Color.rgb(186,195,209));
            dataSet.setDrawValues(false);
        }
        else
            data.setValueFormatter(new PercentFormatter());

        pieChart.setData(data);
        pieChart.setRotationEnabled(false);

        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.getLegend().setEnabled(false);

        pieChart.invalidate();
    }

    //get pie entries for current operation list
    private List<PieEntry> getPieEntries(boolean isIncome) {
        List<PieEntry> entries = new ArrayList<>();

        //getting amount for every category
        HashMap<String, BigDecimal> categoriesMap = new HashMap<>();
        for(Operation operation : operations){
            //Example: If we are looping through the income operations and curr operations is outcome
            if(isIncome != operation.getIsOperationIncome())
                continue;

            BigDecimal lastValue;
            if(categoriesMap.get(operation.getCategory().getName()) == null)
                lastValue = new BigDecimal(0);
            else
                lastValue = categoriesMap.get(operation.getCategory().getName());

            BigDecimal newValue = lastValue.add(operation.getAmount());

            categoriesMap.put(operation.getCategory().getName(), newValue);
        }

        //Loop all hashMap and get entries

        Iterator<Map.Entry<String, BigDecimal>> iterator = categoriesMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, BigDecimal> pair = iterator.next();
            //Entry is category name and the amount for this category
            entries.add(new PieEntry(Float.parseFloat(pair.getValue().toString()), pair.getKey()));
        }

        return entries;
    }

    private PieEntry setEmptyEntrie() {
        PieEntry entry = new PieEntry(1, "");
        return entry;
    }

}
