package com.example.vlad.financemanager.ui.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vlad.financemanager.data.database.DatabaseHelper;
import com.example.vlad.financemanager.data.mappers.SpinnerItemMapper;
import com.example.vlad.financemanager.data.models.Account;
import com.example.vlad.financemanager.data.models.Category;
import com.example.vlad.financemanager.ui.IMoneyCalculation;
import com.example.vlad.financemanager.PresenterMoneyCalculator;
import com.example.vlad.financemanager.R;
import com.example.vlad.financemanager.data.models.Operation;
import com.example.vlad.financemanager.data.models.SpinnerItem;
import com.example.vlad.financemanager.ui.adapters.SimpleSpinnerAdapter;
import com.example.vlad.financemanager.ui.fragments.DatePickerFragment;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoneyCalculatorActivity extends AppCompatActivity implements IMoneyCalculation, DatePickerDialog.OnDateSetListener {
    public static final String DATE_KEY = "date_key";
    private static final String DATE_PICKER_TAG = "date picker";

    private final SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMMM");
    private final SimpleDateFormat sdfWithYear = new SimpleDateFormat("E, MMMM dd, yyyy");

    @BindView(R.id.calculationResultTextView) TextView resultText;
    @BindView(R.id.commentMoneyActivityEditText) EditText comment;
    @BindView(R.id.calculatorActivityToolbar) Toolbar toolbar;
    @BindView(R.id.toolbarTitleTextView) TextView toolbarTitle;
    @BindView(R.id.accountSpinner) Spinner accountsSpinner;
    @BindView(R.id.categorySpinner) Spinner categoriesSpinner;
    @BindView(R.id.operationDateButton) Button dateButton;
    @BindView(R.id.calculatorBackButton) Button btnBack;

    private PresenterMoneyCalculator presenter;
    private Date operationDate = new Date();
    private DatabaseHelper databaseHelper;

    List<SpinnerItem> categorySpinnerItemList;
    List<SpinnerItem> accountSpinnerItemList;

    private boolean isOperationIncome;
    private int accountId;
    private int categoryId;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money_calculator);
        ButterKnife.bind(this);

        databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        presenter = new PresenterMoneyCalculator(this);

        btnBack.getBackground().setColorFilter(R.color.darkGrey, PorterDuff.Mode.SRC_ATOP);
        btnBack.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                presenter.clearNumber();
                return true;
            }
        });

        initUiViaExtras(getIntent().getExtras());
    }

    private void initSpinnersItemLists(int userId) {
        List<Account> accountList = databaseHelper.getAllAccounts(userId);
        accountSpinnerItemList = SpinnerItemMapper.mapAccountsToSpinnerItems(accountList);
        List<Category> categoryList = databaseHelper.getAllCategories(userId, isOperationIncome);
        categorySpinnerItemList = SpinnerItemMapper.mapCategoryToSpinnerItems(categoryList);
    }

    private void initToolbar(Boolean isIncome) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String title = isIncome ? getString(R.string.income) : getString(R.string.outcome);
        toolbarTitle.setText(title);
    }

    private void initDateTimePicker(final Date operationDate) {
        dateButton.setText(sdf.format(new Date()));
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment df = new DatePickerFragment();
                df.setCalendar(operationDate);
                df.show(getSupportFragmentManager(), DATE_PICKER_TAG);
            }
        });
    }

    private String getDateButtonTitleByDate(Long dateInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        operationDate = calendar.getTime();

        String dateButtonTitle;
        if (calendar.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)) {
            dateButtonTitle = sdfWithYear.format(calendar.getTime());
        } else {
            dateButtonTitle = sdf.format(calendar.getTime());
        }

        return dateButtonTitle;
    }

    @Override
    public int getCategoryId() {
        return categoryId;
    }

    @Override
    public int getAccountId() {
        return accountId;
    }

    @Override
    public Date getOperationDate() {
        return operationDate;
    }

    @Override
    public String getComment() {
        return comment.getText().toString();
    }

    @Override
    public boolean getIsOperationInput() {
        return isOperationIncome;
    }

    @Override
    public void setCalcResultText(String result) {
        this.resultText.setText(result);
    }

    public void calculatorBtnOnClick(View v) {
        Button btn = (Button) v;
        presenter.calculatorBtnOnClick(v.getId(), btn.getText().toString());
    }

    public void btnSaveOnClick(View v) {
        presenter.onButtonSaveClick();
    }

    public void finishActivity() {
        finish();
    }

    public void calculationErrorSignal() {
        Toast.makeText(this, getString(R.string.incorrect_input), Toast.LENGTH_SHORT).show();
    }

    public void calculationErrorSignal(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void sendNewOperation(Operation operation) {
        Intent intent = new Intent();
        String amount = operation.getAmount().toString();
        Bundle extras = new Bundle();
        extras.putString(MainActivity.AMOUNT_KEY, amount);
        extras.putSerializable(MainActivity.OPERATION_KEY, operation);
        intent.putExtras(extras);

        setResult(0, intent);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        operationDate = calendar.getTime();
        dateButton.setText(getDateButtonTitleByDate(operationDate.getTime()));
    }

    public void initUiViaExtras(Bundle extras) {
        int userId = extras.getInt(MainActivity.USER_ID_KEY);
        isOperationIncome = extras.getBoolean(MainActivity.IS_OPERATION_INCOME);
        boolean isModifyingOperation = extras.getBoolean(MainActivity.IS_MODIFYING_OPERATION);

        initSpinnersItemLists(userId);
        initSpinnersWithItemLists(accountSpinnerItemList, categorySpinnerItemList);
        initToolbar(isOperationIncome);

        if (isModifyingOperation) {//Init UI via operation
            initUiViaOperationValues(extras);
        } else {
            primaryUiInit();
        }
    }

    public void primaryUiInit() {
        initDateTimePicker(new Date());
        String dateButtonTitle = getDateButtonTitleByDate(new Date().getTime());
        dateButton.setText(dateButtonTitle);
    }

    public void initUiViaOperationValues(Bundle extras) {
        String dateButtonTitle;
        Operation operation = initOperationFromExtras(extras);
        operation.setOperationDate(new Date(extras.getLong(DATE_KEY)));

        initDateTimePicker(new Date(operation.getOperationDate().getTime()));
        dateButtonTitle = getDateButtonTitleByDate(operation.getOperationDate().getTime());
        dateButton.setText(dateButtonTitle);
        comment.setText(operation.getComment());
        resultText.setText(operation.getAmount().toString());
        presenter.settingResultText(operation.getAmount());
        selectSpinnerItemMatchesToId(operation.getCategory().getId(), categorySpinnerItemList, categoriesSpinner);
        selectSpinnerItemMatchesToId(operation.getAccountId(), accountSpinnerItemList, accountsSpinner);
    }

    public void selectSpinnerItemMatchesToId(int id, List<SpinnerItem> categorySpinnerItemList, Spinner spinner) {
        for (int i = 0; i < categorySpinnerItemList.size(); i++) {
            if (categorySpinnerItemList.get(i).getId() == id) {
                spinner.setSelection(i);
            }
        }
    }

    public Operation initOperationFromExtras(Bundle extras) {
        Operation operation = (Operation) extras.getSerializable(MainActivity.OPERATION_KEY);
        operation.setAmount(new BigDecimal(getIntent().getExtras().get(MainActivity.AMOUNT_KEY).toString()));
        presenter.setModifyingOperationId(operation.getId());
        return operation;
    }

    public void initSpinnersWithItemLists(List<SpinnerItem> accountSpinnerItemList, List<SpinnerItem> categorySpinnerItemList) {
        accountsSpinner.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        SimpleSpinnerAdapter accountSpinnerAdapter = new SimpleSpinnerAdapter(this, R.layout.spinner_item, accountSpinnerItemList);
        accountsSpinner.setAdapter(accountSpinnerAdapter);
        accountsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {

                accountId = ((SpinnerItem) parent.getItemAtPosition(selectedItemPosition)).getId();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        categoriesSpinner.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        SimpleSpinnerAdapter categoriesSpinnerAdapter = new SimpleSpinnerAdapter(this, R.layout.spinner_item, categorySpinnerItemList);
        categoriesSpinner.setAdapter(categoriesSpinnerAdapter);
        categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {

                categoryId = ((SpinnerItem) parent.getItemAtPosition(selectedItemPosition)).getId();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
