package com.example.vlad.financemanager.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.vlad.financemanager.R;
import com.example.vlad.financemanager.data.database.DatabaseHelper;
import com.example.vlad.financemanager.data.mappers.SpinnerItemMapper;
import com.example.vlad.financemanager.data.models.Account;
import com.example.vlad.financemanager.data.models.Category;
import com.example.vlad.financemanager.data.models.Operation;
import com.example.vlad.financemanager.data.enums.PeriodsOfTime;
import com.example.vlad.financemanager.data.models.SpinnerItem;
import com.example.vlad.financemanager.ui.adapters.ImageSpinnerAdapter;
import com.example.vlad.financemanager.ui.adapters.SimpleSpinnerAdapter;
import com.example.vlad.financemanager.ui.adapters.ViewPagerAdapter;
import com.example.vlad.financemanager.ui.fragments.TabFragment;
import com.example.vlad.financemanager.utils.DateUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.vlad.financemanager.ui.activities.MoneyCalculatorActivity.DATE_KEY;


public class MainActivity extends AppCompatActivity implements TabFragment.OnChangeOperationClickListener {

    public static final String OPERATION_KEY = "operation";
    public static final String IS_OPERATION_INCOME = "is_operation_income";
    public static final String AMOUNT_KEY = "amount_key";
    public static final String USER_ID_KEY = "user_id_key";
    public static final String IS_MODIFYING_OPERATION = "is_modifying_operation";

    public static final int ACCOUNT_ALL_ID = -1;
    public static final int NEW_OPERATION_REQUEST_CODE = 0;
    public static final int CHANGE_OPERATION_REQUEST_CODE = 1;
    public static final int USER_ID = 0;

    @BindView(R.id.periodsSpinner) Spinner dateSpinner;
    @BindView(R.id.accountsSpinner) Spinner accountsSpinner;
    @BindView(R.id.bottomNavigation) BottomNavigationView bottomNavigationView;
    @BindView(R.id.newOperationButton) FloatingActionButton operationFloatingActionButton;
    @BindView(R.id.pieChartViewPager) ViewPager viewPager;

    private Calendar endOfPeriod; //TODO: remove this

    {
        endOfPeriod = Calendar.getInstance();
        endOfPeriod.setTime(new Date());
    }

    private ViewPagerAdapter viewPagerAdapter;
    private PeriodsOfTime currentPeriod = PeriodsOfTime.DAY;
    private DatabaseHelper database;
    private Date minOperationDate;

    private boolean isIncome = true;
    private int accountId = ACCOUNT_ALL_ID;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        database = DatabaseHelper.Companion.getInstance(getApplicationContext());

        initBottomNavigation();
        initAccountsSpinner();
        initPeriodsSpinner();
        initViewPagerWithTabs();
    }

    private void initBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_income:
                        isIncome = true;
                        operationFloatingActionButton.setImageResource(R.drawable.ic_add_white_48dp);
                        break;
                    case R.id.action_outcome:
                        isIncome = false;
                        operationFloatingActionButton.setImageResource(R.drawable.ic_remove_white_48dp);
                        break;
                    default:
                        break;
                }

                viewPagerAdapter.setIsIncome(isIncome);
                viewPagerAdapter.notifyDataSetChanged();
                TabFragment currentTabFragment = viewPagerAdapter.getRegisteredFragment(viewPager.getCurrentItem());
                currentTabFragment.scrollToTop();
                return true;
            }
        });
    }

    public void initAccountsSpinner() {
        accountsSpinner.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        List<SpinnerItem> spinnerAccountItems = getAccountSpinnerItemListFromDb();

        ImageSpinnerAdapter adapter = new ImageSpinnerAdapter(this, R.layout.image_spinner_item, spinnerAccountItems,
                R.color.white, R.color.dark_black);
        accountsSpinner.setAdapter(adapter);

        accountsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected,
                                       int selectedItemPosition, long selectedId) {
                int selectedAccountId = ((SpinnerItem) parent.getSelectedItem()).getId();
                if (accountId != selectedAccountId) {
                    accountId = selectedAccountId;
                    viewPagerAdapter.setAccountId(selectedAccountId);
                    viewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initPeriodsSpinner() {
        dateSpinner.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        SimpleSpinnerAdapter adapter = new SimpleSpinnerAdapter(this, R.layout.simple_spinner_item, getResources().getStringArray(R.array.time_periods),
               R.color.white, R.color.dark_black);
        dateSpinner.setAdapter(adapter);
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PeriodsOfTime selectedPeriod = getPeriodBySpinnerSelected(position);
                if (currentPeriod != selectedPeriod) {
                    currentPeriod = selectedPeriod;
                    viewPagerAdapter.setCurrentPeriod(selectedPeriod);
                    TabFragment currentFragment = viewPagerAdapter.getRegisteredFragment(viewPager.getCurrentItem());
                    if(currentFragment != null) {
                        endOfPeriod = currentFragment.getCurrentEndOfPeriod();
                    }
                    initViewPagerWithTabs();
                    viewPager.setCurrentItem(DateUtils.getSutedDateIndexByDateFromList(endOfPeriod, viewPagerAdapter.getEndOfPeriodList()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initViewPagerWithTabs() {
        minOperationDate = database.getMinOperationDate(USER_ID);
        if (minOperationDate == null) {
            minOperationDate = new Date();
        }
        List<String> titles = new ArrayList<>();
        List<Calendar> endOfPeriodList = new ArrayList<>();
        Date maxDate = new Date();
        initViewPagerEntriesByPeriod(titles, endOfPeriodList, minOperationDate, maxDate, true);

        initViewPagerWithEntries(titles, endOfPeriodList);
    }

    private void initViewPagerWithEntries(List<String> titles, List<Calendar> endOfPeriodList) {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles,
                endOfPeriodList, currentPeriod, accountId, isIncome);
        viewPager.setAdapter(viewPagerAdapter);
        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(endOfPeriodList.size() - 1);
    }

    private void initViewPagerEntriesByPeriod(List<String> titles, List<Calendar> endOfPeriodList, Date minOperationDate, Date maxDate, boolean includeLast) {
        Calendar currentPagerListDate = Calendar.getInstance();
        currentPagerListDate.setTime(minOperationDate);
        currentPagerListDate = DateUtils.getEndOfPeriod(currentPagerListDate, currentPeriod);

        do {
            String tabTitle = DateUtils.getStringDateByPeriod(currentPeriod, currentPagerListDate);
            titles.add(tabTitle);

            Calendar endOfPeriod = Calendar.getInstance();
            endOfPeriod.setTime(currentPagerListDate.getTime());
            endOfPeriodList.add(endOfPeriod);
        }
        while (DateUtils.slideDateIfAble(currentPagerListDate, true, currentPeriod, minOperationDate, maxDate, includeLast));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle extras;
        if (data == null) return;
        if (resultCode != 0 || (extras = data.getExtras()) == null) {
            Toast.makeText(this, getString(R.string.operation_save_error), Toast.LENGTH_SHORT).show();
            return;
        }

        Operation operation = getOperationFromExtras(extras);
        TabFragment currentTabFragment = viewPagerAdapter.getRegisteredFragment(viewPager.getCurrentItem());
        PeriodsOfTime currentPeriod = currentTabFragment.getCurrentPeriod();
        Calendar currentDate = currentTabFragment.getCurrentEndOfPeriod();

        if (requestCode == NEW_OPERATION_REQUEST_CODE) {
            long id = database.insertOperation(operation, USER_ID, operation.getAccountId());
            operation.setId((int) id);
            if (isNeedToUpdateViewPagerItems(operation, currentPeriod, currentDate)) {
                updateViewPagerItemsAtStart(database.getMinOperationDate(USER_ID));
            } else {
                currentTabFragment.updateUiViaNewOperation(operation);
            }
        } else {
            database.updateOperation(operation, USER_ID, operation.getAccountId());
            if (isOperationFitsToCurrPeriodAndAccount(operation, currentPeriod, currentDate)) {
                currentTabFragment.updateUiViaModifiedOperation(operation);
            } else {
                currentTabFragment.removeModifiedOperation();
            }
        }
        viewPagerAdapter.notifyDataSetChanged();
    }

    private boolean isNeedToUpdateViewPagerItems(Operation operation, PeriodsOfTime currentPeriod, Calendar currentDate) {
        return operation.getId() == database.getMinOperationDateId(MainActivity.USER_ID) &&
                !isOperationFitsToCurrPeriodAndAccount(operation, currentPeriod, currentDate);
    }

    private boolean isOperationFitsToCurrPeriodAndAccount(Operation operation, PeriodsOfTime currentPeriod, Calendar currentDate) {

        boolean isInPeriod = !DateUtils.isOutOfPeriod(operation.getOperationDate(),
                currentPeriod, currentDate);
        boolean isSuiteToCurrentAccount = accountId == operation.getAccountId() || accountId == MainActivity.ACCOUNT_ALL_ID;
        return isInPeriod && isSuiteToCurrentAccount;
    }

    private void updateViewPagerItemsAtStart(Date minOperationDate) {
        List<String> newTabTitles = new ArrayList<>();
        List<Calendar> newEndOfPeriodList = new ArrayList<>();
        initViewPagerEntriesByPeriod(newTabTitles, newEndOfPeriodList, minOperationDate,
                DateUtils.substractOneDay(this.minOperationDate), false);

        List<String> tabTitles = viewPagerAdapter.getTabTitles();
        tabTitles.addAll(0, newTabTitles);
        List<Calendar> endOfPeriodList = viewPagerAdapter.getEndOfPeriodList();
        endOfPeriodList.addAll(0, newEndOfPeriodList);

        this.minOperationDate = minOperationDate;

        int position = viewPager.getCurrentItem();
        position += newTabTitles.size();

        initViewPagerWithEntries(tabTitles, endOfPeriodList);
        viewPager.setCurrentItem(position);
    }

    private Operation getOperationFromExtras(Bundle extras) {

        Operation operation = (Operation) extras.getSerializable(OPERATION_KEY);
        String amountString = extras.getString(AMOUNT_KEY);
        operation.setAmount(new BigDecimal(amountString));

        Category category = database.getCategory(operation.getCategory().getId());//TODO: Required operation with initialized category
        operation.setCategory(category);
        return operation;
    }

    @OnClick({R.id.newOperationButton})
    public void onClick(View v) {
        Intent intent = new Intent(this, MoneyCalculatorActivity.class);
        intent.putExtra(IS_OPERATION_INCOME, isIncome);
        intent.putExtra(IS_MODIFYING_OPERATION, false);
        Bundle extras = new Bundle();
        extras.putSerializable(USER_ID_KEY, USER_ID);
        extras.putLong(DATE_KEY, endOfPeriod.getTimeInMillis());
        intent.putExtras(extras);

        startActivityForResult(intent, NEW_OPERATION_REQUEST_CODE);
    }

    public PeriodsOfTime getPeriodBySpinnerSelected(int positionInSpinner) {
        switch (positionInSpinner) {
            case SpinnerItem.POSITION_DAY:
                return PeriodsOfTime.DAY;
            case SpinnerItem.POSITION_WEEK:
                return PeriodsOfTime.WEEK;
            case SpinnerItem.POSITION_MONTH:
                return PeriodsOfTime.MONTH;
            case SpinnerItem.POSITION_YEAR:
                return PeriodsOfTime.YEAR;
            default:
                return PeriodsOfTime.ALL_TIME;
        }
    }

    public void onChangeOperationClick(Operation operation) {
        Intent intent = new Intent(this, MoneyCalculatorActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable(OPERATION_KEY, operation);
        extras.putSerializable(USER_ID_KEY, USER_ID);
        extras.putSerializable(DATE_KEY, operation.getOperationDate().getTime());
        extras.putSerializable(IS_MODIFYING_OPERATION, true);
        extras.putSerializable(IS_OPERATION_INCOME, operation.isOperationIncome());
        extras.putSerializable(AMOUNT_KEY, operation.getAmount().toString());
        intent.putExtras(extras);

        startActivityForResult(intent, CHANGE_OPERATION_REQUEST_CODE);
    }

    //Getting spinner items collection for accounts
    private List<SpinnerItem> getAccountSpinnerItemListFromDb() {
        List<Account> accountList = new ArrayList<>(database.getAllAccounts(USER_ID));
        List<SpinnerItem> spinnerItemList = new ArrayList<>();
        spinnerItemList.add(new SpinnerItem(ACCOUNT_ALL_ID, getString(R.string.all), R.drawable.dollar));
        spinnerItemList.addAll(SpinnerItemMapper.Companion.mapAccountsToSpinnerItems(accountList));
        return spinnerItemList;
    }
}
