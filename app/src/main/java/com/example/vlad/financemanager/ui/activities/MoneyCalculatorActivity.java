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

import com.example.vlad.financemanager.ui.IMoneyCalculation;
import com.example.vlad.financemanager.PresenterMoneyCalc;
import com.example.vlad.financemanager.R;
import com.example.vlad.financemanager.data.models.Operation;
import com.example.vlad.financemanager.data.models.SpinnerItem;
import com.example.vlad.financemanager.ui.adapters.IconAndTextSpinnerAdapter;
import com.example.vlad.financemanager.ui.fragments.DatePickerFragment;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoneyCalculatorActivity extends AppCompatActivity implements IMoneyCalculation, DatePickerDialog.OnDateSetListener{

    private PresenterMoneyCalc presenter;

    @BindView(R.id.calculationResultTextView) TextView resultText;
    @BindView(R.id.commentMoneyActivityEditText) EditText comment;
    @BindView(R.id.calculatorActivityToolbar) Toolbar toolbar;
    @BindView(R.id.toolbarTitleTextView) TextView toolbarTitle;
    @BindView(R.id.accountSpinner) Spinner spinnerAccounts;
    @BindView(R.id.categorySpinner) Spinner spinnerCategories;
    @BindView(R.id.operationDateButton) Button setDateButton;
    @BindView(R.id.calculatorBackButton) Button btnBack;
    private Date operDate;
    final SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMMM");
    final SimpleDateFormat sdfWithYear = new SimpleDateFormat("E, MMMM dd, yyyy");
    private boolean isOperationInput;
    private boolean opernForChange;
    private Operation operationForChange;

    private int accountId;
    private int categoryId;

    private ArrayList<SpinnerItem> spinnerCategoriesItems;
    private ArrayList<SpinnerItem> spinnerAccountsItems;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money_calculator);
        ButterKnife.bind(this);

        presenter = new PresenterMoneyCalc(this, this);
        operDate = new Date();

        opernForChange = false;

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
        setDateButton.setText(sdf.format( new Date()));
        setDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment df = new DatePickerFragment();
                if(opernForChange)
                    df.setCalendar(operationForChange.getOperationDate());
                else
                    df.setCalendar(operDate);

                DialogFragment datePicker = df;
                datePicker.show(getSupportFragmentManager(),"date picker");
            }
        });

        //Toolbar settings
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });

        //Getting intent extras
        Bundle extras = getIntent().getExtras();
        String str = (String)extras.get("msg");
        initDateTimePicker(extras);
        if(str.equals("operation")){
            str = OperationChange();
            opernForChange = true;
        }


        toolbarTitle.setText(str);

        isOperationInput = str.equals("Income");


    }

    private void initDateTimePicker(Bundle extras) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(extras.getLong("currDate"));
        if(calendar != null){
            operDate = calendar.getTime();
            if(calendar.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR))
                setDateButton.setText(sdfWithYear.format(calendar.getTime()));
            else
                setDateButton.setText(sdf.format(calendar.getTime()));
        }
    }

    //Methods that implements interface

    /**
     * Method - getting value in the category's name input
     * @return category's name value
     */
    @Override
    public int getCategoryId() {
        return categoryId;
    }
    /**
     * Method - getting value in the account's name input
     * @return account's name value
     */
    @Override
    public int getAccountId() {
        return accountId;
    }
    /**
     * Method - getting date when the operation performed
     * @return date
     */
    @Override
    public Date getOperationDate() {
        return operDate;
    }
    /**
     * Method - getting users's comment for this operation
     * @return comment as string
     */
    @Override
    public String getComment() {
        return comment.getText().toString();
    }

    @Override
    public boolean getIsOperationInput() {
        return isOperationInput;
    }

    /**
     * Method - updating the value in the calculator
     * @param result -  new number
     */
    @Override
    public void setCalcResultText(String result) {
        this.resultText.setText(result);
    }

    /**
     * Handler for input buttons click in the calculator
     * @param v - View representation of the sender
     */
    public void calculatorBtnOnClick(View v){
        Button btn = (Button)v;
        presenter.calculatorBtnOnClick(v.getId(), btn.getText().toString());
    }
    /**
     * Handler for 'save' button click
     * @param v - View representation of the sender
     */
    public void btnSaveOnClick(View v){
        presenter.btnSaveOnClick();
    }

    /**
     * Finish this activity
     */
    public void finishActivity(){
        finish();
    }

    /**
     *  Method - Signals to the user that calculation failed
     */
    public void calculationErrorSignal(){
        Toast.makeText(this,"Incorrect input", Toast.LENGTH_SHORT).show();
    }

    public void calculationErrorSignal(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void sendNewOperation(Operation operation) {
        Intent intent = new Intent();
        String amount = operation.getAmount().toString();
        Bundle extras = new Bundle();
        extras.putString("amount", amount);
        extras.putSerializable("operation",operation);
        intent.putExtras(extras);

        setResult(0, intent);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

        operDate = calendar.getTime();
        String selectedDate;

        if(calendar.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR))
            selectedDate = sdfWithYear.format(calendar.getTime());
        else
            selectedDate = sdf.format(calendar.getTime());

        setDateButton.setText(selectedDate);
    }

    public String OperationChange(){
        Bundle extras = getIntent().getExtras();
        Operation operation = (Operation)extras.getSerializable("operation");
        operation.setAmount(new BigDecimal(getIntent().getExtras().get("amount").toString()));
        operationForChange = operation;

        comment.setText(operation.getComment());
        resultText.setText(operation.getAmount().toString());
        presenter.settingResultText(operation.getAmount());

        //Setting category & account
        for(int i= 0; i < spinnerCategoriesItems.size(); i++){
            if(spinnerCategoriesItems.get(i).getId() == operation.getCategory().getId())
                spinnerCategories.setSelection(i);
        }

        for(int i= 0; i < spinnerAccountsItems.size(); i++){
            if(spinnerAccountsItems.get(i).getId() == operation.getAccountId())
                spinnerAccounts.setSelection(i);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(operation.getOperationDate());
        if(calendar.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR))
            setDateButton.setText(sdfWithYear.format(operation.getOperationDate()));
        else
            setDateButton.setText(sdf.format(operation.getOperationDate()));


        return operation.getIsOperationIncome()?"Income":"Outcome";
    }

    public void initSpinners(){

        Bundle extras = getIntent().getExtras();


        spinnerAccountsItems = (ArrayList<SpinnerItem>) extras.getSerializable("accounts");
        spinnerAccountsItems.remove(0);
        spinnerCategoriesItems = (ArrayList<SpinnerItem>) extras.getSerializable("categories");
        if(spinnerCategoriesItems == null || spinnerAccountsItems == null){
            calculationErrorSignal("Error with getting categories and accounts");
            return;
        }



        spinnerAccounts.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        spinnerCategories.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        IconAndTextSpinnerAdapter adapter_Account = new IconAndTextSpinnerAdapter(this,R.layout.spinner_item, spinnerAccountsItems);
        IconAndTextSpinnerAdapter adapter_Categories = new IconAndTextSpinnerAdapter(this,R.layout.spinner_item, spinnerCategoriesItems);

        spinnerAccounts.setAdapter(adapter_Account);
        spinnerCategories.setAdapter(adapter_Categories);

        //OnItemAccountSelected
        spinnerAccounts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {

                accountId =((SpinnerItem)parent.getItemAtPosition(selectedItemPosition)).getId();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //OnItemCategorySelected
        spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {

                categoryId =((SpinnerItem)parent.getItemAtPosition(selectedItemPosition)).getId();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
