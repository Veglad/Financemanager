package com.example.vlad.financemanager.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vlad.financemanager.R;
import com.example.vlad.financemanager.data.models.Operation;
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
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class TabFragment extends Fragment {
    private static final String DATE_TEXT_KEY = "TabDateText";
    private static final String IS_INCOME_KEY = "TabIsIncome";
    private static final String OPERATIONS_KEY = "OperationsKey";
    private static final String PIE_CHART_LABEL = "pieChartLabel";
    private static final int EMPTY_CHART_COLOR = Color.rgb(186, 195, 209);

    @BindView(R.id.datePieChartFragmentTextView) TextView textDate;
    @BindView(R.id.pieChart) PieChart pieChart;

    private Unbinder unbinder;

    public static TabFragment newInstance(String str, ArrayList<Operation> operations, boolean isIncome) {
        Bundle args = new Bundle();
        args.putString(DATE_TEXT_KEY, str);
        args.putBoolean(IS_INCOME_KEY, isIncome);
        args.putSerializable(OPERATIONS_KEY, operations);

        TabFragment fragment = new TabFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanse) {
        View view = inflater.inflate(R.layout.fragment_tab, container, false);
        unbinder = ButterKnife.bind(this, view);

        Bundle extras = getArguments();
        if (extras != null) {
            textDate.setText(extras.getString(DATE_TEXT_KEY));
            ArrayList<Operation> operations = (ArrayList<Operation>) extras.getSerializable(OPERATIONS_KEY);
            drawPieChart(getArguments().getBoolean(IS_INCOME_KEY), operations);
        }
        pieChart.setUsePercentValues(true);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    //Full tab fragment update
    public void updateTabFragment(String textDate, ArrayList<Operation> operations) {//TODO: create method for updating one operation
        this.textDate.setText(textDate);
        Bundle extras = getArguments();
        if (extras != null) {
            drawPieChart(extras.getBoolean(IS_INCOME_KEY), operations);
        }
    }

    private void drawPieChart(boolean isIncome, ArrayList<Operation> operations) {
        List<PieEntry> entries = getPieEntries(isIncome, operations);

        PieDataSet dataSet = new PieDataSet(entries, PIE_CHART_LABEL);
        dataSet.setColors(isIncome ? ColorTemplate.MATERIAL_COLORS : ColorTemplate.COLORFUL_COLORS);

        dataSet.setSliceSpace(3);

        PieData data = new PieData(dataSet);

        if (entries.size() == 0) {
            entries.add(setEmptyEntry());
            dataSet.setColor(EMPTY_CHART_COLOR);
            dataSet.setDrawValues(false);
        } else
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
    private List<PieEntry> getPieEntries(boolean isIncome, ArrayList<Operation> operations) {
        List<PieEntry> entries = new ArrayList<>();
        HashMap<String, BigDecimal> categoriesMap = getCategoryToAmountMap(isIncome, operations);

        //Loop all hashMap and get entries
        for (Map.Entry<String, BigDecimal> pair : categoriesMap.entrySet()) {
            float amountOfMoney = Float.parseFloat(pair.getValue().toString());
            String categoryName = pair.getKey();
            entries.add(new PieEntry(amountOfMoney, categoryName));
        }

        return entries;
    }

    @NonNull
    private HashMap<String, BigDecimal> getCategoryToAmountMap(boolean isIncome, ArrayList<Operation> operations) {
        //getting amount for every category
        HashMap<String, BigDecimal> categoriesMap = new HashMap<>();
        for (Operation operation : operations) {
            //Example: If we are looping through the income operations and curr operations is outcome
            if (isIncome != operation.getIsOperationIncome())
                continue;

            BigDecimal lastValue;
            if (categoriesMap.get(operation.getCategory().getName()) == null)
                lastValue = new BigDecimal(0);
            else
                lastValue = categoriesMap.get(operation.getCategory().getName());

            BigDecimal newValue = lastValue.add(operation.getAmount());

            categoriesMap.put(operation.getCategory().getName(), newValue);
        }
        return categoriesMap;
    }

    private PieEntry setEmptyEntry() {
        return new PieEntry(1, "");
    }
}
