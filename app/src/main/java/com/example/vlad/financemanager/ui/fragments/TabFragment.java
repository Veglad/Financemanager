package com.example.vlad.financemanager.ui.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.vlad.financemanager.R;
import com.example.vlad.financemanager.data.database.DatabaseHelper;
import com.example.vlad.financemanager.data.enums.PeriodsOfTime;
import com.example.vlad.financemanager.data.models.Operation;
import com.example.vlad.financemanager.ui.adapters.OperationsAdapter;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class TabFragment extends Fragment {
    private static final String END_OF_PERIOD_KEY = "endOfPeriod";
    private static final String CURRENT_PERIOD_KEY = "currentPeriod";
    private static final String IS_INCOME_KEY = "isIncome";
    private static final String ACCOUNT_ID_KEY = "accountIdKey";
    private static final String PIE_CHART_LABEL = "pieChartLabel";
    private static final String DATE_TITLE_KEY = "dateTitleKey";
    private static final int EMPTY_CHART_COLOR = Color.rgb(186, 195, 209);

    @BindView(R.id.pieChart) PieChart pieChart;
    @BindView(R.id.viewPagerDateTextView) TextView dateTitleTextView;
    @BindView(R.id.operationsRecyclerView) RecyclerView recyclerView;
    @BindView(R.id.fragmentTabScrollView) ScrollView scrollView;

    private OperationsAdapter operationsAdapter;

    private DatabaseHelper database;
    private OnChangeOperationClickListener onChangeOperationClickListener;
    private Calendar currentEndOfPeriod;
    private List<Operation> operationList = new ArrayList<>();
    private PeriodsOfTime currentPeriod;
    private Unbinder unbinder;

    private String dateTitle;
    private String balanceString;
    private boolean isIncome;
    private int modifiedOperationIndex;
    private int accountId;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChangeOperationClickListener) {
            onChangeOperationClickListener = ((OnChangeOperationClickListener) context);
        }
    }

    public static TabFragment newInstance(PeriodsOfTime currentPeriod, Calendar endOfPeriod, boolean isIncome, int accountId, String dateTitle) {
        Bundle args = new Bundle();
        args.putSerializable(END_OF_PERIOD_KEY, endOfPeriod);
        args.putSerializable(CURRENT_PERIOD_KEY, currentPeriod);
        args.putBoolean(IS_INCOME_KEY, isIncome);
        args.putInt(ACCOUNT_ID_KEY, accountId);
        args.putString(DATE_TITLE_KEY, dateTitle);

        TabFragment fragment = new TabFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = DatabaseHelper.Companion.getInstance(getContext().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstance) {
        View view = inflater.inflate(R.layout.fragment_tab, container, false);
        unbinder = ButterKnife.bind(this, view);

        initOperationListAdapter();
        Bundle extras = getArguments();
        if (extras != null) {
            //Get extras
            accountId = extras.getInt(ACCOUNT_ID_KEY);
            currentPeriod = (PeriodsOfTime) extras.getSerializable(CURRENT_PERIOD_KEY);
            currentEndOfPeriod = (Calendar) extras.getSerializable(END_OF_PERIOD_KEY);
            dateTitle = extras.getString(DATE_TITLE_KEY);
            isIncome = getArguments().getBoolean(IS_INCOME_KEY);

            fullTabFragmentUpdate(currentPeriod, currentEndOfPeriod, isIncome, accountId, dateTitle);
        }
        pieChart.setUsePercentValues(true);

        return view;
    }

    private BigDecimal getBalance(List<Operation> operationList) {
        BigDecimal balance = new BigDecimal(0);
        for (Operation operation : operationList) {
            balance = balance.add(operation.getAmount());
        }

        return balance;
    }

    private List<Operation> getOperationsByIsIncome(boolean isIncome, List<Operation> operationList) {
        List<Operation> operationListFiltered = new ArrayList<>();
        for (Operation operation : operationList) {
            if (operation.getIsOperationIncome() == isIncome) {
                operationListFiltered.add(operation);
            }
        }

        return operationListFiltered;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    //Full tab fragment update
    public void fullTabFragmentUpdate(PeriodsOfTime currentPeriod, Calendar endOfPeriod, boolean isIncome, int accountId, String dateTitle) {
        operationList = database.getOperations(accountId, currentPeriod, endOfPeriod);
        operationList = getOperationsByIsIncome(isIncome, operationList);
        BigDecimal balance = getBalance(operationList);
        balanceString = getBalanceString(isIncome, balance);

        updateTabFragment(isIncome, dateTitle, balanceString, operationList);
    }

    public String getBalanceString(boolean isIncome, BigDecimal balance) {
        String placeholderBalanceString = getString(isIncome ? (R.string.income_balance_placeholder) : R.string.outcome_balance_placeholder);
        return String.format(placeholderBalanceString, balance);
    }

    public void updateTabFragment(boolean isIncome, String dateTitle, String balanceString, List<Operation> operationList) {
        updateOperationList(operationList);

        pieChart.setCenterText(balanceString);
        drawPieChart(isIncome, operationList);
        dateTitleTextView.setText(dateTitle);
    }

    public void scrollToTop() {
        scrollView.fullScroll(ScrollView.FOCUS_UP);
    }

    private void drawPieChart(boolean isIncome, List<Operation> operations) {
        List<PieEntry> entries = getPieEntries(operations);

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
        pieChart.getPaint(Chart.PAINT_HOLE);
        pieChart.setCenterTextSize(18);
        pieChart.setCenterTextColor(ContextCompat.getColor(getContext(), R.color.white));
        pieChart.setHoleColor(ContextCompat.getColor(getContext(), R.color.transparent));

        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.getLegend().setEnabled(false);

        pieChart.invalidate();
    }

    //get pie entries for current operation list
    private List<PieEntry> getPieEntries(List<Operation> operations) {
        List<PieEntry> entries = new ArrayList<>();
        HashMap<String, BigDecimal> categoriesMap = getCategoryToAmountMap(operations);

        //Loop all hashMap and get entries
        for (Map.Entry<String, BigDecimal> pair : categoriesMap.entrySet()) {
            float amountOfMoney = Float.parseFloat(pair.getValue().toString());
            String categoryName = pair.getKey();
            entries.add(new PieEntry(amountOfMoney, categoryName));
        }

        return entries;
    }

    @NonNull
    private HashMap<String, BigDecimal> getCategoryToAmountMap(List<Operation> operations) {
        //getting amount for every category
        HashMap<String, BigDecimal> categoriesMap = new HashMap<>();
        for (Operation operation : operations) {
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

    private void initOperationListAdapter() {
        recyclerView.setFocusable(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));

        operationsAdapter = new OperationsAdapter(getContext(), operationList);
        operationsAdapter.setOnItemClickListener(new OperationsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                changeOperationClick(position);
            }
        });
        operationsAdapter.setOnItemDeleteClickListener(new OperationsAdapter.onItemDeleteClickListener() {
            @Override
            public void onItemDeleteClick(int position) {
                deleteOperation(position);
            }
        });
        recyclerView.setAdapter(operationsAdapter);
    }

    private void changeOperationClick(int position) {
        modifiedOperationIndex = position;
        Operation operation = operationList.get(position);
        onChangeOperationClickListener.onChangeOperationClick(operation);
    }

    private void deleteOperation(int position) {
        Operation operationToRemove = operationList.get(position);
        database.deleteOperation(operationToRemove);

        fullTabFragmentUpdate(currentPeriod, currentEndOfPeriod, isIncome, accountId, dateTitle);
    }

    private void removeOperationFromTheList(int position) {
        operationList.remove(position);
        operationsAdapter.notifyItemRemoved(position);
        updateTabFragment(isIncome, dateTitle, balanceString, operationList);
    }

    private void updateOperationList(List<Operation> operationList) {
        operationsAdapter.setOperationList(operationList);
        operationsAdapter.notifyDataSetChanged();
    }

    public void updateUiViaModifiedOperation(Operation operation) {
        operationList.set(modifiedOperationIndex, operation);
        operationsAdapter.notifyItemChanged(modifiedOperationIndex);
        updateTabFragment(isIncome, dateTitle, balanceString, operationList);
    }

    public void updateUiViaNewOperation(Operation operation) {
        operationList.add(0, operation);
        operationsAdapter.notifyDataSetChanged();//TODO: If operation date is less than min operation date
    }

    private PieEntry setEmptyEntry() {
        return new PieEntry(1, "");
    }

    public void removeModifiedOperation() {
        removeOperationFromTheList(modifiedOperationIndex);
    }

    public interface OnChangeOperationClickListener {
        void onChangeOperationClick(Operation operation);
    }

    public Calendar getCurrentEndOfPeriod() {
        return currentEndOfPeriod;
    }

    public List<Operation> getOperationList() {
        return operationList;
    }

    public String getDateTitle() {
        return dateTitle;
    }

    public PeriodsOfTime getCurrentPeriod() {
        return currentPeriod;
    }
}
