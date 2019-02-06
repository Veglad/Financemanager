package com.example.vlad.financemanager.ui.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
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
import com.example.vlad.financemanager.PresenterMoneyCalc;
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
    @BindView(R.id.accountSpinner) Spinner spinnerAccounts;
    @BindView(R.id.categorySpinner) Spinner spinnerCategories;
    @BindView(R.id.operationDateButton) Button setDateButton;
    @BindView(R.id.calculatorBackButton) Button btnBack;

    private PresenterMoneyCalc presenter;
    private Date operationDate;
    private Operation operationForChange;
    private DatabaseHelper databaseHelper;

    List<SpinnerItem> categorySpinnerItemList;
    List<SpinnerItem> accountSpinnerItemList;

    private boolean isOperationInput;
    private boolean openForChange;
    private int accountId;
    private int categoryId;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money_calculator);
        ButterKnife.bind(this);

        databaseHelper = DatabaseHelper.getInstance(getApplicationContext());

        presenter = new PresenterMoneyCalc(this, this);
        operationDate = new Date();

        openForChange = false;

        //Setting long clear button press;
        btnBack.getBackground().setColorFilter(R.color.darkGrey, PorterDuff.Mode.SRC_ATOP);
        btnBack.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                presenter.clearNumber();
                return true;
            }
        });

        //main views
        initSpinners();

        //datePicker Button
        setDateButton.setText(sdf.format(new Date()));
        setDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment df = new DatePickerFragment();
                if (openForChange)
                    df.setCalendar(operationForChange.getOperationDate());
                else
                    df.setCalendar(operationDate);

                DialogFragment datePicker = df;
                datePicker.show(getSupportFragmentManager(), DATE_PICKER_TAG);
            }
        });

        //Toolbar settings
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

        //Getting intent extras
        Bundle extras = getIntent().getExtras();
        String str = (String) extras.get(MainActivity.MESSAGE_KEY);
        initDateTimePicker(extras);
        if (str.equals(MainActivity.OPERATION_VALUE)) {
            str = OperationChange();
            openForChange = true;
        }


        toolbarTitle.setText(str);

        isOperationInput = str.equals(MainActivity.INCOME_VALUE);
    }

    private void initDateTimePicker(Bundle extras) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(extras.getLong(DATE_KEY));

        operationDate = calendar.getTime();
        if (calendar.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)) {
            setDateButton.setText(sdfWithYear.format(calendar.getTime()));
        } else {
            setDateButton.setText(sdf.format(calendar.getTime()));
        }
    }

    //Methods that implements interface
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
        return isOperationInput;
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
        presenter.btnSaveOnClick();
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
        String selectedDate;

        if (calendar.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR))
            selectedDate = sdfWithYear.format(calendar.getTime());
        else
            selectedDate = sdf.format(calendar.getTime());

        setDateButton.setText(selectedDate);
    }

    public String OperationChange() {
        Bundle extras = getIntent().getExtras();
        Operation operation = (Operation) extras.getSerializable(MainActivity.OPERATION_KEY);
        operation.setAmount(new BigDecimal(getIntent().getExtras().get(MainActivity.AMOUNT_KEY).toString()));
        operationForChange = operation;

        comment.setText(operation.getComment());
        resultText.setText(operation.getAmount().toString());
        presenter.settingResultText(operation.getAmount());

        //Setting category & account
        for (int i = 0; i < categorySpinnerItemList.size(); i++) {
            if (categorySpinnerItemList.get(i).getId() == operation.getCategory().getId())
                spinnerCategories.setSelection(i);
        }

        for (int i = 0; i < accountSpinnerItemList.size(); i++) {
            if (accountSpinnerItemList.get(i).getId() == operation.getAccountId())
                spinnerAccounts.setSelection(i);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(operation.getOperationDate());
        if (calendar.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR))
            setDateButton.setText(sdfWithYear.format(operation.getOperationDate()));
        else
            setDateButton.setText(sdf.format(operation.getOperationDate()));


        return operation.getIsOperationIncome() ? getString(R.string.income) : getString(R.string.outcome);
    }

    public void initSpinners() {

        Bundle extras = getIntent().getExtras();
        if(extras == null) return;

        List<Account> accountList = databaseHelper.getAllAccounts(extras.getInt(MainActivity.USER_ID_KEY));
        accountSpinnerItemList = SpinnerItemMapper.mapAccountsToSpinnerItems(accountList);

        int userId = extras.getInt(MainActivity.USER_ID_KEY);
        boolean areInputCategories = extras.getBoolean(MainActivity.ARE_CATEGORIES_INPUT_KEY);
        List<Category> categoryList = databaseHelper.getAllCategories(userId, areInputCategories);
        categorySpinnerItemList = SpinnerItemMapper.mapCategoryToSpinnerItems(categoryList);

        spinnerAccounts.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        spinnerCategories.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        SimpleSpinnerAdapter adapter_Account = new SimpleSpinnerAdapter(this, R.layout.spinner_item, accountSpinnerItemList);
        SimpleSpinnerAdapter adapter_Categories = new SimpleSpinnerAdapter(this, R.layout.spinner_item, categorySpinnerItemList);

        spinnerAccounts.setAdapter(adapter_Account);
        spinnerCategories.setAdapter(adapter_Categories);

        //OnItemAccountSelected
        spinnerAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {

                accountId = ((SpinnerItem) parent.getItemAtPosition(selectedItemPosition)).getId();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //OnItemCategorySelected
        spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {

                categoryId = ((SpinnerItem) parent.getItemAtPosition(selectedItemPosition)).getId();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
