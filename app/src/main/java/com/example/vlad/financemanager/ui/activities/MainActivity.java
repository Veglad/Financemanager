package com.example.vlad.financemanager.ui.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
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

import com.example.vlad.financemanager.R;
import com.example.vlad.financemanager.data.database.DatabaseHelper;
import com.example.vlad.financemanager.data.mappers.SpinnerItemMapper;
import com.example.vlad.financemanager.data.models.Account;
import com.example.vlad.financemanager.data.models.Category;
import com.example.vlad.financemanager.data.models.Operation;
import com.example.vlad.financemanager.data.enums.PeriodsOfTime;
import com.example.vlad.financemanager.data.models.SpinnerItem;
import com.example.vlad.financemanager.ui.adapters.SimpleSpinnerAdapter;
import com.example.vlad.financemanager.ui.adapters.OperationsAdapter;
import com.example.vlad.financemanager.ui.adapters.ViewPagerAdapter;
import com.example.vlad.financemanager.utils.DateUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.vlad.financemanager.ui.activities.MoneyCalculatorActivity.DATE_KEY;


public class MainActivity extends AppCompatActivity implements OnClickListener {

    public static final String OPERATION_KEY = "operation";
    public static final String ARE_CATEGORIES_INPUT_KEY = "are_categories_input";
    public static final String MESSAGE_KEY = "message";
    public static final String AMOUNT_KEY = "amount_key";
    public static final String USER_ID_KEY = "user_id_key";

    public static final String INCOME_VALUE = "Income";
    public static final String OUTCOME_VALUE = "Outcome";
    public static final String OPERATION_VALUE = "operation";
    public static final int ACCOUNT_ALL_ID = -1;

    private final List<String> TAB_TITLES = Arrays.asList("Outcome", "Income");

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

    private BigDecimal balanceForSelectedPeriod = new BigDecimal(0);
    private Calendar lastSelectedDay;
    private BigDecimal lastOperationAmount;
    private ActionBarDrawerToggle toggleActionBar;

    {
        lastSelectedDay = Calendar.getInstance();
        lastSelectedDay.setTime(new Date());
    }

    private ViewPagerAdapter viewPagerAdapter;
    private OperationsAdapter operationsAdapter;
    private PeriodsOfTime currentPeriod = PeriodsOfTime.DAY;
    private DatabaseHelper database;

    private List<Category> categoryListIncome;
    private List<Category> categoryListOutcome;
    private List<Account> accountList;
    private List<Operation> operationList;

    private int userId = 0;
    private int accountId = ACCOUNT_ALL_ID;
    private int operationAccIdBeforeChange;
    private int positionInList;
    private int operationId = 0;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        database = DatabaseHelper.getInstance(getApplicationContext());

        initSupportActionBar();
        initAccountsSpinner();
        initPeriodsSpinner();
        initOperationListAdapter();
        initViewPagerWithTabs();
        initToggleActionBar();
    }

    private void initSupportActionBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void initAccountsSpinner() {
        List<SpinnerItem> spinnerAccountItems = getAccountSpinnerItemListFromDb();

        accountsSpinner.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        SimpleSpinnerAdapter adapter = new SimpleSpinnerAdapter(this, R.layout.spinner_item, spinnerAccountItems);
        accountsSpinner.setAdapter(adapter);

        accountsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected,
                                       int selectedItemPosition, long selectedId) {
                int selectedAccountId = ((SpinnerItem)parent.getSelectedItem()).getId();
                if(accountId != selectedAccountId) {
                    accountId = selectedAccountId;
                }
                fullUpdate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initPeriodsSpinner() {
        dateSpinner = (Spinner) navigationView.getMenu().findItem(R.id.navigation_drawer_item_1).getActionView();
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PeriodsOfTime selectedPeriod = getPeriodBySpinnerSelected(position);
                if (currentPeriod != selectedPeriod) {
                    currentPeriod = selectedPeriod;
                    fullUpdate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initViewPagerWithTabs() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), TAB_TITLES);
        viewPagerAdapter.setOperationList(operationList);//TODO: delete (empty list)
        viewPager.setAdapter(viewPagerAdapter);

        // Attach the view pager to the tab strip
        for (int i = 0; i < TAB_TITLES.size(); i++) {
            tabsStrip.addTab(tabsStrip.newTab().setText(TAB_TITLES.get(i)));
        }
        tabsStrip.setupWithViewPager(viewPager);
    }

    private void initOperationListAdapter() {
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        operationList = new ArrayList<>();//TODO: delete (empty list)

        operationsAdapter = new OperationsAdapter(this, operationList);
        operationsAdapter.setOnItemClickListener(new OperationsAdapter.ItemClick() {
            @Override
            public void onItemClick(int position) {
                changeOperation(position);
            }
        });
        operationsAdapter.setOnItemLongClickListener(new OperationsAdapter.ItemLongClick() {
            @Override
            public void onItemLongClick(int position) {
                showDeleteDialog(position);
            }
        });
        recyclerView.setAdapter(operationsAdapter);
    }

    private void initToggleActionBar() {
        toggleActionBar = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggleActionBar);
        toggleActionBar.syncState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //////////////////Balance chage!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        ///save in account
        Operation operation;
        if (resultCode != 0) {
            Toast.makeText(this, getString(R.string.operation_save_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if (data == null)
            return;
        //Getting operation from extras
        Bundle extras = data.getExtras();
        if (extras == null) {
            Toast.makeText(this, getString(R.string.operation_add_error), Toast.LENGTH_SHORT).show();
            return;
        }
        operation = (Operation) extras.getSerializable(OPERATION_KEY);
        String amountStr = extras.getString(AMOUNT_KEY);

        operation.setAmount(new BigDecimal(amountStr));
        Category category = database.getCategory(operation.getCategory().getId());
        operation.setCategory(category);

        if (requestCode == 0) {


            long id = database.insertOperation(operation.getAmount().toString(), operation.getIsOperationIncome(),
                    operation.getComment(), operation.getOperationDate(),
                    operation.getCategory().getId(), userId, operation.getAccountId());
            operation = null;

            operation = database.getOperation(id, userId);
            if (operation != null) {
                int currentAccId = ((SpinnerItem)accountsSpinner.getSelectedItem()).getId();
                //ADD operation to the list if it is not out of the period and performed for current account
                if (!isOutOfPeriod(operation.getOperationDate()) && (currentAccId == operation.getAccountId() || currentAccId == ACCOUNT_ALL_ID)) {
                    operationList.add(0, operation);
                    operationsAdapter.notifyDataSetChanged();
                    updateBalance(operation, false);
                }
            } else
                Toast.makeText(this, getString(R.string.operation_insert_error), Toast.LENGTH_SHORT).show();
        } else {
            operation.setId(operationId);

            database.updateOperation(operation, userId, operation.getAccountId());

            if (operationAccIdBeforeChange != operation.getAccountId() ||
                    isOutOfPeriod(operation.getOperationDate()))
                removeOperationFromTheList(positionInList);
            else {
                operationList.set(positionInList, operation);
                operationsAdapter.notifyItemChanged(positionInList);
                updateBalance(operation, true);
            }
        }

    }

    //Updating balances with a new operation
    private void updateBalance(Operation newOperation, boolean isChangedOperation) {
        if (isChangedOperation) {
            if (newOperation.getIsOperationIncome())
                balanceForSelectedPeriod = balanceForSelectedPeriod.subtract(lastOperationAmount);
            else
                balanceForSelectedPeriod = balanceForSelectedPeriod.add(lastOperationAmount);

        }

        if (newOperation.getIsOperationIncome())
            balanceForSelectedPeriod = balanceForSelectedPeriod.add(newOperation.getAmount());
        else
            balanceForSelectedPeriod = balanceForSelectedPeriod.subtract(newOperation.getAmount());

        updateViewPagerAdapter();
        balanceTextView.setText(String.format(getString(R.string.balance_placeholder), balanceForSelectedPeriod));
    }

    private void updateBalanceForDeletedOper(Operation deletedOperation) {
        if (deletedOperation.getIsOperationIncome())
            balanceForSelectedPeriod = balanceForSelectedPeriod.subtract(deletedOperation.getAmount());
        else
            balanceForSelectedPeriod = balanceForSelectedPeriod.add(deletedOperation.getAmount());

        updateViewPagerAdapter();
        balanceTextView.setText(String.format(getString(R.string.balance_placeholder), balanceForSelectedPeriod));
    }

    //Update balance with the new operation's list
    private void fullBalanceAndChartUpdate() {
        updateViewPagerAdapter();
        balanceTextView.setText(String.format(getString(R.string.balance_placeholder), balanceForSelectedPeriod));
    }

    private void countBalance() {
        balanceForSelectedPeriod = new BigDecimal(0);
        for (Operation operation : operationList) {
            if (operation.getIsOperationIncome())
                balanceForSelectedPeriod = balanceForSelectedPeriod.add(operation.getAmount());
            else
                balanceForSelectedPeriod = balanceForSelectedPeriod.subtract(operation.getAmount());
        }
    }

    //Is new date is out of period
    private boolean isOutOfPeriod(Date operationDate) {
        Calendar operDate = Calendar.getInstance();
        operDate.setTime(operationDate);
        Calendar endOfPeriod = DateUtils.getEndOfPeriod(operDate, currentPeriod);
        Calendar startPeriod = DateUtils.getStartOfPeriod(operDate, currentPeriod);

        boolean result;

        //if out of range and selected period isn't for all time
        if (currentPeriod == PeriodsOfTime.ALL_TIME)
            result = false;
        else if (currentPeriod == PeriodsOfTime.DAY &&
                lastSelectedDay.get(Calendar.DAY_OF_YEAR) == endOfPeriod.get(Calendar.DAY_OF_YEAR)
                && lastSelectedDay.get(Calendar.YEAR) == endOfPeriod.get(Calendar.YEAR))
            result = false;
        else if (lastSelectedDay.get(Calendar.YEAR) < startPeriod.get(Calendar.YEAR) ||
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
    public void onClick(View v) {
        Intent intent = new Intent(this, MoneyCalculatorActivity.class);
        boolean isIncome = false;
        switch (v.getId()) {
            case R.id.incomeButton:
                intent.putExtra(MESSAGE_KEY, INCOME_VALUE);
                isIncome = true;
                break;
            case R.id.outcomeButton:
                intent.putExtra(MESSAGE_KEY, OUTCOME_VALUE);
                isIncome = false;
                break;

        }

        Bundle extras = new Bundle();
        //extras.putSerializable(ACCOUNTS_KEY, getAccountSpinnerItemListFromDb());
        extras.putBoolean(ARE_CATEGORIES_INPUT_KEY, isIncome);//getCategorySpinnerItemListFromDb(isIncome));
        extras.putSerializable(USER_ID_KEY, userId);
        extras.putLong(DATE_KEY, lastSelectedDay.getTimeInMillis());
        intent.putExtras(extras);

        startActivityForResult(intent, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggleActionBar.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    public PeriodsOfTime getPeriodBySpinnerSelected(int positionInSpinner) {
        switch (positionInSpinner) {
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

    private void fullUpdate() {
        int accId = ((SpinnerItem)accountsSpinner.getSelectedItem()).getId();
        List<Operation> operationList = database.getOperations(accId, currentPeriod, lastSelectedDay);

        this.operationList.clear();
        this.operationList.addAll(operationList);
        operationsAdapter.notifyDataSetChanged();

        countBalance();
        fullBalanceAndChartUpdate();
    }

    private void updateViewPagerAdapter() {//TODO: extract dependencies, create method for adding only one operation
        String viewPagerDateString = DateUtils.getStringDateByPeriod(currentPeriod, lastSelectedDay);
        viewPagerAdapter.setOperationList(operationList);
        viewPagerAdapter.setDateString(viewPagerDateString);
        viewPagerAdapter.notifyDataSetChanged();
    }

    private void showDeleteDialog(final int position) {
        CharSequence[] buttonsDialog = new CharSequence[]{getString(R.string.delete), getString(R.string.cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_dialog_text));
        builder.setItems(buttonsDialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) deleteOperation(position);
            }
        });
        builder.show();
    }

    private void deleteOperation(int position) {
        database.deleteOperation(operationList.get(position));

        removeOperationFromTheList(position);
    }

    private void removeOperationFromTheList(int position) {
        Operation deletedOperation = operationList.remove(position);
        operationsAdapter.notifyItemRemoved(position);
        updateBalanceForDeletedOper(deletedOperation);
    }

    private void changeOperation(int position) {
        positionInList = position;
        Intent intent = new Intent(this, MoneyCalculatorActivity.class);
        Bundle extras = new Bundle();
        Operation operation = operationList.get(position);
        operationId = operation.getId();
        extras.putSerializable(OPERATION_KEY, operation);
        //extras.putSerializable(ACCOUNTS_KEY, getAccountSpinnerItemListFromDb());
        extras.putSerializable(USER_ID_KEY, userId);
        extras.putBoolean(ARE_CATEGORIES_INPUT_KEY, operation.getCategory().getIsInputCategory());//getCategorySpinnerItemListFromDb());

        intent.putExtras(extras);
        intent.putExtra(DATE_KEY, operation.getOperationDate().getTime());
        intent.putExtra(MESSAGE_KEY, OPERATION_VALUE);
        intent.putExtra(AMOUNT_KEY, operation.getAmount().toString());

        operationAccIdBeforeChange = operation.getAccountId();

        lastOperationAmount = operation.getAmount();
        startActivityForResult(intent, 1);
    }

    //Getting spinner items collection for accounts
    private List<SpinnerItem> getAccountSpinnerItemListFromDb() {
        List<Account> accountList = new ArrayList<>(database.getAllAccounts(userId));
        List<SpinnerItem> spinnerItemList = new ArrayList<>();
        spinnerItemList.add(new SpinnerItem(ACCOUNT_ALL_ID, getString(R.string.all), R.drawable.dollar));
        spinnerItemList.addAll(SpinnerItemMapper.mapAccountsToSpinnerItems(accountList));
        return spinnerItemList;
    }

    public void buttonLeftClick(View view) {
        slideDate(false);
    }

    public void buttonRightClick(View view) {
        slideDate(true);
    }

    private void slideDate(boolean isRightSlide) {
        if (currentPeriod == PeriodsOfTime.ALL_TIME)
            return;

        boolean isChanged = false;
        int slideDays = 0;

        switch (currentPeriod) {
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

        if (isRightSlide) {
            lastSelectedDay.set(Calendar.DAY_OF_YEAR, lastSelectedDay.get(Calendar.DAY_OF_YEAR) + slideDays);
            isChanged = true;
        } else {
            lastSelectedDay.set(Calendar.DAY_OF_YEAR, lastSelectedDay.get(Calendar.DAY_OF_YEAR) - slideDays);
            isChanged = true;
        }

        //if a new date get out from the today date
        if (lastSelectedDay.compareTo(Calendar.getInstance()) > 0) {
            isChanged = false;
            lastSelectedDay = Calendar.getInstance();
        }

        //if date changed
        if (isChanged) fullUpdate();
    }
}
