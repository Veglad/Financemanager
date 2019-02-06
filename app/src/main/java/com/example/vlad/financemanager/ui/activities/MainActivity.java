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
    public static final int NEW_OPERATION_REQUEST_CODE = 0;
    public static final int CHANGE_OPERATION_REQUEST_CODE = 1;

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
    private Calendar endOfPeriod;
    private Operation operationBeforeChange;
    private ActionBarDrawerToggle toggleActionBar;

    {
        endOfPeriod = Calendar.getInstance();
        endOfPeriod.setTime(new Date());
    }

    private ViewPagerAdapter viewPagerAdapter;
    private OperationsAdapter operationsAdapter;
    private PeriodsOfTime currentPeriod = PeriodsOfTime.DAY;
    private DatabaseHelper database;

    private List<Operation> operationList;

    private int userId = 0;
    private int accountId = ACCOUNT_ALL_ID;
    private int modifiedOperationIndex;

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
                changeOperationClick(position);
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
        Bundle extras;
        if (resultCode != 0 || data == null || (extras = data.getExtras()) == null) {
            Toast.makeText(this, getString(R.string.operation_save_error), Toast.LENGTH_SHORT).show();
            return;
        }

        Operation operation = getOperationFromExtras(extras);

        if (requestCode == NEW_OPERATION_REQUEST_CODE) {
            database.insertOperation(operation, userId, operation.getAccountId());
            updateUiViaNewOperation(operation);
        } else {
            database.updateOperation(operation, userId, operation.getAccountId());
            updateUiViaModifiedOperation(operation);
        }
    }

    private void updateUiViaModifiedOperation(Operation operation) {
        if (isOperationFitsToCurrPeriodAndAccount(operation)) {
            operationList.set(modifiedOperationIndex, operation);
            operationsAdapter.notifyItemChanged(modifiedOperationIndex);
            recountBalanceViaOperation(operation, true);
            updateUiRelatedToBalance();
        } else {
            recountBalanceViaDeletedOperation(operation);
            updateUiRelatedToBalance();
            removeOperationFromTheList(modifiedOperationIndex);
        }
    }

    private void updateUiViaNewOperation(Operation operation) {
        if (isOperationFitsToCurrPeriodAndAccount(operation)) {
            operationList.add(0, operation);
            operationsAdapter.notifyDataSetChanged();
            recountBalanceViaOperation(operation, false);
            updateUiRelatedToBalance();
        }
    }

    private Operation getOperationFromExtras(Bundle extras) {
        Operation operation = (Operation) extras.getSerializable(OPERATION_KEY);
        String amountString = extras.getString(AMOUNT_KEY);
        operation.setAmount(new BigDecimal(amountString));

        Category category = database.getCategory(operation.getCategory().getId());//TODO: Required operation with initialized category
        operation.setCategory(category);
        return operation;
    }

    private void updateUiRelatedToBalance() {
        updateViewPagerAdapter();
        balanceTextView.setText(String.format(getString(R.string.balance_placeholder), balanceForSelectedPeriod));
    }

    private boolean isOperationFitsToCurrPeriodAndAccount(Operation operation) {
        int currentAccId = ((SpinnerItem)accountsSpinner.getSelectedItem()).getId();
        return !DateUtils.isOutOfPeriod(operation.getOperationDate(), currentPeriod, endOfPeriod) &&
                (currentAccId == operation.getAccountId() || currentAccId == ACCOUNT_ALL_ID);
    }

    private void recountBalanceViaOperation(Operation operation, boolean isModifiedOperation) {
        if (isModifiedOperation) {
            recountBalanceViaDeletedOperation(operationBeforeChange);
        }

        recountBalanceViaNewOperation(operation);
    }

    private void recountBalanceViaNewOperation(Operation newOperation) {
        if (newOperation.getIsOperationIncome())
            balanceForSelectedPeriod = balanceForSelectedPeriod.add(newOperation.getAmount());
        else
            balanceForSelectedPeriod = balanceForSelectedPeriod.subtract(newOperation.getAmount());
    }

    private void recountBalanceViaDeletedOperation(Operation canceledOperation) {
        if (canceledOperation.getIsOperationIncome())
            balanceForSelectedPeriod = balanceForSelectedPeriod.subtract(canceledOperation.getAmount());
        else
            balanceForSelectedPeriod = balanceForSelectedPeriod.add(canceledOperation.getAmount());
    }

    private void countBalance() {
        balanceForSelectedPeriod = new BigDecimal(0);
        for (Operation operation : operationList) {
            recountBalanceViaNewOperation(operation);
        }
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
            default: 
                break;
        }
        Bundle extras = new Bundle();
        extras.putBoolean(ARE_CATEGORIES_INPUT_KEY, isIncome);
        extras.putSerializable(USER_ID_KEY, userId);
        extras.putLong(DATE_KEY, endOfPeriod.getTimeInMillis());
        intent.putExtras(extras);

        startActivityForResult(intent, NEW_OPERATION_REQUEST_CODE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggleActionBar.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    public PeriodsOfTime getPeriodBySpinnerSelected(int positionInSpinner) {
        switch (positionInSpinner) {
            case 0: return PeriodsOfTime.DAY;
            case 1: return PeriodsOfTime.WEEK;
            case 2: return PeriodsOfTime.MONTH;
            case 3: return PeriodsOfTime.YEAR;
            default: return PeriodsOfTime.ALL_TIME;
        }
    }

    private void fullUpdate() {
        int accountId = ((SpinnerItem)accountsSpinner.getSelectedItem()).getId();
        List<Operation> operationList = database.getOperations(accountId, currentPeriod, endOfPeriod);
        updateUiOperationsList(operationList);

        countBalance();
        updateUiRelatedToBalance();
    }

    private void updateUiOperationsList(List<Operation> operationList) {
        this.operationList.clear();
        this.operationList.addAll(operationList);
        operationsAdapter.notifyDataSetChanged();
    }

    private void updateViewPagerAdapter() {//TODO: extract dependencies, create method for adding only one operation
        String viewPagerDateString = DateUtils.getStringDateByPeriod(currentPeriod, endOfPeriod);
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
        Operation operationToRemove = operationList.get(position);
        database.deleteOperation(operationToRemove);
        recountBalanceViaDeletedOperation(operationToRemove);
        updateUiRelatedToBalance();
        removeOperationFromTheList(position);
    }

    private void removeOperationFromTheList(int position) {
        operationList.remove(position);
        operationsAdapter.notifyItemRemoved(position);
    }

    private void changeOperationClick(int position) {
        modifiedOperationIndex = position;
        Operation operation = operationList.get(position);

        Intent intent = new Intent(this, MoneyCalculatorActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable(OPERATION_KEY, operation);
        extras.putSerializable(USER_ID_KEY, userId);
        extras.putSerializable(DATE_KEY, operation.getOperationDate().getTime());
        extras.putSerializable(MESSAGE_KEY, OPERATION_VALUE);
        extras.putSerializable(AMOUNT_KEY, operation.getAmount().toString());
        extras.putBoolean(ARE_CATEGORIES_INPUT_KEY, operation.getCategory().getIsInputCategory());
        intent.putExtras(extras);

        operationBeforeChange = operation;
        startActivityForResult(intent, CHANGE_OPERATION_REQUEST_CODE);
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

    private void slideDate(boolean isRightSlide) { //TODO: Refactor this
        if (currentPeriod == PeriodsOfTime.ALL_TIME)
            return;

        int slideDays;
        switch (currentPeriod) {
            case DAY:
                slideDays = 1;
                break;
            case WEEK:
                slideDays = 7;
                break;
            case MONTH:
                slideDays = endOfPeriod.getActualMaximum(Calendar.DAY_OF_MONTH);
                break;
            default:
                slideDays = endOfPeriod.getActualMaximum(Calendar.DAY_OF_YEAR);
                break;
        }

        if (isRightSlide) {
            endOfPeriod.set(Calendar.DAY_OF_YEAR, endOfPeriod.get(Calendar.DAY_OF_YEAR) + slideDays);
        } else {
            endOfPeriod.set(Calendar.DAY_OF_YEAR, endOfPeriod.get(Calendar.DAY_OF_YEAR) - slideDays);
        }

        //if a new date get out from the today date
        if (endOfPeriod.compareTo(Calendar.getInstance()) > 0) {
            endOfPeriod = Calendar.getInstance();
            return;
        }

        fullUpdate();
    }
}
