package com.example.vlad.financemanager;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements OnClickListener, IMainActivity{

    private MainActivityPresenter presenter;
    private final String textBalance = "Balance: ";
    private final List<String> TAB_TITLES = Arrays.asList("Outcome", "Income");

    private int userId;
    private int operationAccIdBeforeChange;
    private int positionInList;
    private int operationId;
    private PeriodsOfTime selectedPeriod;
    private DatabaseHelper database;
    private BigDecimal balanceForSelectedPeriod;
    private Calendar lastSelectedDay;
    private BigDecimal lastOperAmount;
    private ViewPagerAdapter viewPagerAdapter;
    private OperationsAdapter mOperationsAdapter;
    private ActionBarDrawerToggle toggleActionBar;

    Spinner dateSpinner;
    @BindView(R.id.accountsSpinner) Spinner accountsSpinner;
    @BindView(R.id.leftButton) Button leftButton;
    @BindView(R.id.rightButton) Button rightButton;
    @BindView(R.id.balanceTextView) TextView balanceTextView;
    @BindView(R.id.mainScreenToolbar) Toolbar toolbar;
    @BindView(R.id.operationsRecyclerView) RecyclerView recyclerView;
    @BindView(R.id.pieChartViewPager) ViewPager viewPager;
    @BindView(R.id.pieChartTabs) TabLayout tabsStrip;
    @BindView(R.id.drawerLayout) DrawerLayout drawerLayout;
    @BindView(R.id.navigationView) NavigationView navigationView;

    ArrayList<SpinnerItem> spinnerAccountItems;
    List<Category> categoryListIncome;
    List<Category> categoryListOutcome;
    List<Account> accountList;
    List<Operation> operationList;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        leftButton.getBackground().setColorFilter(R.color.colorPrimary, PorterDuff.Mode.SRC_ATOP);
        rightButton.getBackground().setColorFilter(R.color.colorPrimary, PorterDuff.Mode.SRC_ATOP);

        setSupportActionBar(toolbar);


        balanceForSelectedPeriod = new BigDecimal(0);
        operationId = 0;
        userId = 0;
        spinnerAccountItems = new ArrayList<>();
        lastSelectedDay = Calendar.getInstance();
        lastSelectedDay.setTime(new Date());
        selectedPeriod = PeriodsOfTime.DAY;
        presenter = new MainActivityPresenter(this,getApplicationContext());

        //DB init
        database= new DatabaseHelper(this);

        /**Spinner setting**/

        spinnerAccountInit();
        spinnerPeriodsInit();

        //Operations Adapter
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        operationList = new ArrayList<>();

        mOperationsAdapter = new OperationsAdapter(this, operationList);
        recyclerView.setAdapter(mOperationsAdapter);

        recyclerView.addOnItemTouchListener(new OperRecyclerTouchListener(this,
                recyclerView, new OperRecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                changeOperation(position);
            }

            @Override
            public void onLingClick(View view, int position) {
                showActionDialog(position);
            }
        }));

        initOperationList();

        /**Sliding tabs**/
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), TAB_TITLES);
        viewPagerAdapter.setOperations(operationList);
        viewPager.setAdapter(viewPagerAdapter);

        // Attach the view pager to the tab strip
        for(int i = 0; i < TAB_TITLES.size(); i++) {
            tabsStrip.addTab(tabsStrip.newTab().setText(TAB_TITLES.get(i)));
        }

        tabsStrip.setupWithViewPager(viewPager);

        /**Toggle action bar**/
        toggleActionBar = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(toggleActionBar);
        toggleActionBar.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initOperationList() {
        operationList.addAll(database.getAllOperations(-1, PeriodsOfTime.DAY, Calendar.getInstance()));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        //////////////////Balance chage!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        ///save in account
        Operation operation;
        if(resultCode != 0){
            Toast.makeText(this, "Operation saving error", Toast.LENGTH_SHORT).show();
            return;
        }

        if(data == null)
            return;
        //Getting operation from extras
        Bundle extras = data.getExtras();
        operation = null;
        if(extras == null){
            Toast.makeText(this, "Operation add error", Toast.LENGTH_SHORT).show();
            return;
        }
        operation = (Operation)extras.getSerializable("operation");
        String amountStr = extras.getString("amount");

        operation.setAmount(new BigDecimal(amountStr));
        Category category = database.getCategory(operation.getCategory().getId());
        operation.setCategory(category);

        if(requestCode == 0){


            long id = database.insertOperation(operation.getAmount().toString(), operation.getIsOperationIncome(),
                    operation.getComment(), operation.getOperationDate(),
                    operation.getCategory().getId(), userId, operation.getAccountId());
            operation = null;

            operation = database.getOperation(id, userId);
            if(operation != null){
                int currentAccId = accountList.get(accountsSpinner.getSelectedItemPosition()).getId();//all acc
                //Add operation to the list if it is not out of the period and performed for current account
                if(!isOutOfPeriod(operation.getOperationDate()) && (currentAccId == operation.getAccountId() || currentAccId == -1)){
                    operationList.add(0,operation);
                    mOperationsAdapter.notifyDataSetChanged();
                    updateBalance(operation, false);
                }
            }
            else
                Toast.makeText(this, "operation insert error", Toast.LENGTH_SHORT).show();
        }
        else {
            operation.setId(operationId);

            database.updateOperation(operation, userId, operation.getAccountId());

            if(operationAccIdBeforeChange != operation.getAccountId() ||
                    isOutOfPeriod(operation.getOperationDate()) )
                removeFromTheList(positionInList);
            else{
                operationList.set(positionInList,operation);
                mOperationsAdapter.notifyItemChanged(positionInList);
                updateBalance(operation, true);
            }
        }

    }

    //Updating balances with a new operation
    private void updateBalance(Operation newOperation, boolean isChangedOperation) {
        if(isChangedOperation){
            if(newOperation.getIsOperationIncome())
                balanceForSelectedPeriod= balanceForSelectedPeriod.subtract(lastOperAmount);
            else
                balanceForSelectedPeriod= balanceForSelectedPeriod.add(lastOperAmount);

        }

        if(newOperation.getIsOperationIncome())
            balanceForSelectedPeriod= balanceForSelectedPeriod.add(newOperation.getAmount());
        else
            balanceForSelectedPeriod= balanceForSelectedPeriod.subtract(newOperation.getAmount());

        viewPagerAdapter.updateTab(lastSelectedDay, selectedPeriod, operationList);
        balanceTextView.setText(textBalance + balanceForSelectedPeriod + " ₴");
    }

    private void updateBalanceForDeletedOper(Operation deletedOperation){
        if(deletedOperation.getIsOperationIncome())
            balanceForSelectedPeriod = balanceForSelectedPeriod.subtract(deletedOperation.getAmount());
        else
            balanceForSelectedPeriod = balanceForSelectedPeriod.add(deletedOperation.getAmount());

        viewPagerAdapter.updateTab(lastSelectedDay, selectedPeriod, operationList);
        balanceTextView.setText(textBalance + balanceForSelectedPeriod + " ₴");
    }

    //Update balance with the new operation's list
    private void fullBalanceAndChartUpdate(){
        balanceForSelectedPeriod = new BigDecimal(0);
        for(Operation operation: operationList){
            if(operation.getIsOperationIncome())
                balanceForSelectedPeriod = balanceForSelectedPeriod.add(operation.getAmount());
            else
                balanceForSelectedPeriod = balanceForSelectedPeriod.subtract(operation.getAmount());
        }

        viewPagerAdapter.updateTab(lastSelectedDay, selectedPeriod, operationList);
        balanceTextView.setText(textBalance + balanceForSelectedPeriod + " ₴");
    }

    //Is new date is out of period
    private boolean isOutOfPeriod(Date operationDate) {
        Calendar operDate = Calendar.getInstance();
        operDate.setTime(operationDate);
        Calendar endOfPeriod = CalendarSettings.getEndOfPeriod(operDate, selectedPeriod);
        Calendar startPeriod = CalendarSettings.getStartOfPeriod(operDate, selectedPeriod);

        boolean result;

        //if out of range and selected period isn't for all time
        if(selectedPeriod == PeriodsOfTime.ALL_TIME)
            result = false;
        else if(selectedPeriod == PeriodsOfTime.DAY &&
                lastSelectedDay.get(Calendar.DAY_OF_YEAR) == endOfPeriod.get(Calendar.DAY_OF_YEAR)
                && lastSelectedDay.get(Calendar.YEAR) == endOfPeriod.get(Calendar.YEAR))
            result = false;
        else if(lastSelectedDay.get(Calendar.YEAR) < startPeriod.get(Calendar.YEAR) ||
                lastSelectedDay.get(Calendar.YEAR) == startPeriod.get(Calendar.YEAR) &&
                        lastSelectedDay.get(Calendar.DAY_OF_YEAR) < startPeriod.get(Calendar.DAY_OF_YEAR)
                || lastSelectedDay.get(Calendar.YEAR) > endOfPeriod.get(Calendar.YEAR) ||
                lastSelectedDay.get(Calendar.YEAR) == endOfPeriod.get(Calendar.YEAR) &&
                        lastSelectedDay.get(Calendar.DAY_OF_YEAR) > endOfPeriod.get(Calendar.DAY_OF_YEAR))
            result = true;
        else
            result = false;

        return result;
    }

    @OnClick({R.id.incomeButton, R.id.outcomeButton})
    @Override
    public void onClick(View v){
        Intent intent = new Intent(this, MoneyCalculatorActivity.class);
        boolean isIncome = false;
        switch (v.getId()){
            case R.id.incomeButton:
                intent.putExtra("msg","Income");
                isIncome = true;
                break;
            case R.id.outcomeButton:
                intent.putExtra("msg", "Outcome");
                isIncome = false;
                break;

        }

        Bundle extras = new Bundle();
        extras.putSerializable("accounts", getAccountsFromDBForSpinner());
        extras.putSerializable("categories", getCategoriesFromDBForSpinner(isIncome));
        extras.putLong("currDate", lastSelectedDay.getTimeInMillis());
        intent.putExtras(extras);

        startActivityForResult(intent, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(toggleActionBar.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    public void spinnerAccountInit(){

        //Gett all accounts for the spinner init
       spinnerAccountItems = getAccountsFromDBForSpinner();

        accountsSpinner.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this,R.layout.spinner_item, spinnerAccountItems);
        accountsSpinner.setAdapter(adapter);

        //OnItemSelected
        accountsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {
                int accountId = spinnerAccountItems.get(selectedItemPosition).getId();
                if(selectedItemPosition == 0)
                    accountId = -1;

                operationList.clear();
                operationList.addAll(database.getAllOperations(accountId, selectedPeriod, lastSelectedDay));
                mOperationsAdapter.notifyDataSetChanged();
                fullBalanceAndChartUpdate();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void spinnerPeriodsInit(){
        dateSpinner = (Spinner) navigationView.getMenu().findItem(R.id.navigation_drawer_item_1).getActionView();


        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(selectedPeriod != getPeriodBySpinnerSelected(position)){
                    selectedPeriod = getPeriodBySpinnerSelected(position);
                    fullUpdate();
                    fullBalanceAndChartUpdate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public PeriodsOfTime getPeriodBySpinnerSelected(int positionInSpinner){
        switch (positionInSpinner){
            case 0:
                return PeriodsOfTime.DAY;
            case 1:
                return PeriodsOfTime.WEEK;
            case 2:
                return PeriodsOfTime.MONTH;
            case 3:
                return PeriodsOfTime.YEAR;
            default:
                return PeriodsOfTime.ALL_TIME;
        }
    }

    private void fullUpdate(){
        operationList.clear();
        int accId = spinnerAccountItems.get(accountsSpinner.getSelectedItemPosition()).getId();
        operationList.addAll(database.getAllOperations(accId, selectedPeriod, lastSelectedDay));
        mOperationsAdapter.notifyDataSetChanged();

        fullBalanceAndChartUpdate();

        viewPagerAdapter.updateTab(lastSelectedDay, selectedPeriod, operationList);
    }

    private void showActionDialog(final int position){
        CharSequence[] buttonsDialog = new CharSequence[]{"Delete", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Do you really want to delete this operation?");
        builder.setItems(buttonsDialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0)
                    deleteOperation(position);

            }
        });
        builder.show();
    }

    private void deleteOperation(int position){
        database.deleteOperation(operationList.get(position));

        removeFromTheList(position);
    }

    private void removeFromTheList(int position){
        Operation deletedOperation = operationList.remove(position);
        mOperationsAdapter.notifyItemRemoved(position);
        updateBalanceForDeletedOper(deletedOperation);
    }

    private void changeOperation(int position) {
        positionInList = position;
        Intent intent = new Intent(this, MoneyCalculatorActivity.class);
        Bundle extras = new Bundle();
        Operation operation  =  operationList.get(position);
        operationId = operation.getId();
        extras.putSerializable("operation",operation);
        extras.putSerializable("accounts", getAccountsFromDBForSpinner());
        extras.putSerializable("categories", getCategoriesFromDBForSpinner(operation.getCategory().getIsInputCategory()));

        intent.putExtras(extras);
        intent.putExtra("msg", "operation");
        intent.putExtra("amount", operation.getAmount().toString());

        operationAccIdBeforeChange = operation.getAccountId();

        lastOperAmount = operation.getAmount();
        startActivityForResult(intent, 1);
    }

    //Getting spinner items collection for categories
    private ArrayList<SpinnerItem> getCategoriesFromDBForSpinner(boolean isIncome){
        ArrayList<SpinnerItem> spinnerItemsCategories = new ArrayList<>();
        if(isIncome ){
            if(categoryListIncome == null)
                categoryListIncome = database.getAllCategories(userId, isIncome);

            for(Category category : categoryListIncome){
                SpinnerItem item= new SpinnerItem(category.getId(), category.getName(), category.getIcon());
                spinnerItemsCategories.add(item);
            }
        }
        else if(!isIncome){
            if(categoryListOutcome == null)
                categoryListOutcome = database.getAllCategories(userId, isIncome);

            for(Category category : categoryListOutcome){
                spinnerItemsCategories.add(new SpinnerItem(category.getId(), category.getName(), category.getIcon()));
            }
        }




        return spinnerItemsCategories
                ;
    }

    //Getting spinner items collection for accounts
    private ArrayList<SpinnerItem> getAccountsFromDBForSpinner(){
        if(accountList == null){
            accountList =  new ArrayList<>(database.getAllAccounts(userId));
            accountList.add(0, new Account(-1,"All",R.drawable.dollar));
        }
        else
            return spinnerAccountItems;

        for(Account account : accountList){
            spinnerAccountItems.add(new SpinnerItem(account.getId(), account.getName(), account.getIcon()));
        }

        return spinnerAccountItems;
    }

    public void buttonLeftClick(View view){
        slideDate(false);
    }

    public void buttonRigthClick(View view){
        slideDate(true);
    }

    private void slideDate(boolean isRightSlide){
        if(selectedPeriod == PeriodsOfTime.ALL_TIME)
            return;

        boolean isChanged = false;
        int slideDays = 0;

        switch (selectedPeriod){
            case DAY:
                slideDays = 1;
                break;
            case WEEK:
                slideDays = 7;
                break;
            case MONTH:
                slideDays = lastSelectedDay.getActualMaximum(Calendar.DAY_OF_MONTH);
                break;
            case YEAR:
                slideDays = lastSelectedDay.getActualMaximum(Calendar.DAY_OF_YEAR);
        }

        if(isRightSlide ){
            lastSelectedDay.set(Calendar.DAY_OF_YEAR, lastSelectedDay.get(Calendar.DAY_OF_YEAR)+slideDays);
            isChanged = true;
        }
        else{
            lastSelectedDay.set(Calendar.DAY_OF_YEAR, lastSelectedDay.get(Calendar.DAY_OF_YEAR)-slideDays);
            isChanged = true;
        }

        //if a new date get out from the today date
        if(lastSelectedDay.compareTo(Calendar.getInstance())>0)
        {
            isChanged = false;
            lastSelectedDay = Calendar.getInstance();
        }

        //if date changed
        if(isChanged){
            fullUpdate();
            fullBalanceAndChartUpdate();
        }
    }
}
