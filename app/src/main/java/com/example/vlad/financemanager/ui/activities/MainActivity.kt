package com.example.vlad.financemanager.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.AdapterView
import android.widget.Toast

import com.example.vlad.financemanager.R
import com.example.vlad.financemanager.data.database.DatabaseHelper
import com.example.vlad.financemanager.data.mappers.SpinnerItemMapper
import com.example.vlad.financemanager.data.models.Operation
import com.example.vlad.financemanager.data.enums.PeriodsOfTime
import com.example.vlad.financemanager.data.models.SpinnerItem
import com.example.vlad.financemanager.ui.OnChangeOperationClickListener
import com.example.vlad.financemanager.ui.adapters.ImageSpinnerAdapter
import com.example.vlad.financemanager.ui.adapters.SimpleSpinnerAdapter
import com.example.vlad.financemanager.ui.adapters.ViewPagerAdapter
import com.example.vlad.financemanager.utils.DateUtils

import java.math.BigDecimal
import java.util.ArrayList
import java.util.Calendar
import java.util.Date

import com.example.vlad.financemanager.ui.activities.MoneyCalculatorActivity.Companion.DATE_KEY
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnChangeOperationClickListener {

    companion object {
        const val OPERATION_KEY = "operation"
        const val IS_OPERATION_INCOME = "is_operation_income"
        const val AMOUNT_KEY = "amount_key"
        const val USER_ID_KEY = "user_id_key"
        const val IS_MODIFYING_OPERATION = "is_modifying_operation"

        const val ACCOUNT_ALL_ID = -1
        const val NEW_OPERATION_REQUEST_CODE = 0
        const val CHANGE_OPERATION_REQUEST_CODE = 1
        const val USER_ID = 0
    }

    private var endOfPeriod: Calendar //TODO: remove this

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var currentPeriod = PeriodsOfTime.DAY
    private lateinit var database: DatabaseHelper
    private var minOperationDate: Date = Date()

    private var isIncome = true
    private var accountId = ACCOUNT_ALL_ID

    //Getting spinner items collection for accounts
    private val accountSpinnerItemListFromDb: List<SpinnerItem>
        get() {
            val accountList = ArrayList(database.getAllAccounts(USER_ID))
            val spinnerItemList = ArrayList<SpinnerItem>()
            spinnerItemList.add(SpinnerItem(ACCOUNT_ALL_ID, getString(R.string.all), R.drawable.dollar))
            spinnerItemList.addAll(SpinnerItemMapper.mapAccountsToSpinnerItems(accountList))
            return spinnerItemList
        }

    init {
        endOfPeriod = Calendar.getInstance()
        endOfPeriod.time = Date()
        endOfPeriod.firstDayOfWeek = Calendar.MONDAY
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = DatabaseHelper.getInstance(applicationContext)

        initBottomNavigation()
        initAccountsSpinner()
        initPeriodsSpinner()
        initViewPagerWithTabs()
    }

    private fun initBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_income -> {
                    isIncome = true
                    newOperationButton.setImageResource(R.drawable.ic_add_white_48dp)
                }
                R.id.action_outcome -> {
                    isIncome = false
                    newOperationButton.setImageResource(R.drawable.ic_remove_white_48dp)
                }
                else -> {
                }
            }

            viewPagerAdapter.setIsIncome(isIncome)
            viewPagerAdapter.notifyDataSetChanged()
            val currentTabFragment = viewPagerAdapter.getRegisteredFragment(pieChartViewPager.currentItem)
            currentTabFragment.scrollToTop()
            true
        }
    }

    private fun initAccountsSpinner() {
        accountsSpinner.background.setColorFilter(ContextCompat.getColor(this,  R.color.white), PorterDuff.Mode.SRC_ATOP)
        val spinnerAccountItems = accountSpinnerItemListFromDb

        val adapter = ImageSpinnerAdapter(this, R.layout.image_spinner_item, spinnerAccountItems,
                R.color.white, R.color.dark_black)
        accountsSpinner.adapter = adapter

        accountsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, itemSelected: View,
                                        selectedItemPosition: Int, selectedId: Long) {
                val selectedAccountId = (parent.selectedItem as SpinnerItem).id
                if (accountId != selectedAccountId) {
                    accountId = selectedAccountId
                    viewPagerAdapter.setAccountId(selectedAccountId)
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun initPeriodsSpinner() {
        periodsSpinner.background.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP)
        val adapter = SimpleSpinnerAdapter(this, R.layout.simple_spinner_item, resources.getStringArray(R.array.time_periods),
                R.color.white, R.color.dark_black)
        periodsSpinner.adapter = adapter
        periodsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedPeriod = getPeriodBySpinnerSelected(position)
                if (currentPeriod !== selectedPeriod) {
                    currentPeriod = selectedPeriod
                    viewPagerAdapter.setCurrentPeriod(selectedPeriod)
                    val currentFragment = viewPagerAdapter.getRegisteredFragment(pieChartViewPager.currentItem)
                    endOfPeriod = currentFragment.currentEndOfPeriod
                    initViewPagerWithTabs()
                    pieChartViewPager.currentItem = DateUtils.getSutedDateIndexByDateFromList(endOfPeriod, viewPagerAdapter.endOfPeriodList)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun initViewPagerWithTabs() {
        minOperationDate = database.getMinOperationDate(USER_ID)?: Date()

        val titles = mutableListOf<String>()
        val endOfPeriodList = mutableListOf<Calendar>()
        val maxDate = Date()
        initViewPagerEntriesByPeriod(titles, endOfPeriodList, minOperationDate, maxDate, true)

        initViewPagerWithEntries(titles, endOfPeriodList)
    }

    private fun initViewPagerWithEntries(titles: MutableList<String>, endOfPeriodList: MutableList<Calendar>) {
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, titles,
                endOfPeriodList, currentPeriod, accountId, isIncome)
        pieChartViewPager.adapter = viewPagerAdapter
        viewPagerAdapter.notifyDataSetChanged()
        pieChartViewPager.currentItem = endOfPeriodList.size - 1
    }

    private fun initViewPagerEntriesByPeriod(titles: MutableList<String>, endOfPeriodList: MutableList<Calendar>, minOperationDate: Date?, maxDate: Date, includeLast: Boolean) {
        var currentPagerListDate = Calendar.getInstance()
        currentPagerListDate.time = minOperationDate
        currentPagerListDate = DateUtils.getEndOfPeriod(currentPagerListDate, currentPeriod)

        do {
            val tabTitle = DateUtils.getStringDateByPeriod(currentPeriod, currentPagerListDate)
            titles.add(tabTitle)

            val endOfPeriod = Calendar.getInstance()
            endOfPeriod.time = currentPagerListDate.time
            endOfPeriodList.add(endOfPeriod)
        } while (DateUtils.slideDateIfAble(currentPagerListDate, true, currentPeriod, minOperationDate!!, maxDate, includeLast))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) return
        val extras: Bundle? = data.extras
        if (resultCode != 0 || extras == null) {
            Toast.makeText(this, getString(R.string.operation_save_error), Toast.LENGTH_SHORT).show()
            return
        }

        val operation = getOperationFromExtras(extras)
        val currentTabFragment = viewPagerAdapter.getRegisteredFragment(pieChartViewPager.currentItem)
        val currentPeriod = currentTabFragment.currentPeriod
        val currentDate = currentTabFragment.currentEndOfPeriod

        if (requestCode == NEW_OPERATION_REQUEST_CODE) {
            val id = database.insertOperation(operation, USER_ID, operation.accountId)
            operation.id = id.toInt()
            if (isNeedToUpdateViewPagerItems(operation, currentPeriod, currentDate)) {
                updateViewPagerItemsAtStart(database.getMinOperationDate(USER_ID)?: Date())
            } else {
                currentTabFragment.updateUiViaNewOperation(operation)
            }
        } else {
            database.updateOperation(operation, USER_ID, operation.accountId)
            if (isOperationFitsToCurrPeriodAndAccount(operation, currentPeriod, currentDate)) {
                currentTabFragment.updateUiViaModifiedOperation(operation)
            } else {
                currentTabFragment.removeModifiedOperation()
            }
        }
        viewPagerAdapter.notifyDataSetChanged()
    }

    private fun isNeedToUpdateViewPagerItems(operation: Operation, currentPeriod: PeriodsOfTime, currentDate: Calendar): Boolean {
        return operation.id == database.getMinOperationDateId(MainActivity.USER_ID) && !isOperationFitsToCurrPeriodAndAccount(operation, currentPeriod, currentDate)
    }

    private fun isOperationFitsToCurrPeriodAndAccount(operation: Operation, currentPeriod: PeriodsOfTime, currentDate: Calendar): Boolean {

        val isInPeriod = !DateUtils.isOutOfPeriod(operation.operationDate,
                currentPeriod, currentDate)
        val isSuiteToCurrentAccount = accountId == operation.accountId || accountId == MainActivity.ACCOUNT_ALL_ID
        return isInPeriod && isSuiteToCurrentAccount
    }

    private fun updateViewPagerItemsAtStart(minOperationDate: Date) {
        val newTabTitles = mutableListOf<String>()
        val newEndOfPeriodList = mutableListOf<Calendar>()
        initViewPagerEntriesByPeriod(newTabTitles, newEndOfPeriodList, minOperationDate,
                DateUtils.substractOneDay(this.minOperationDate), false)

        val tabTitles = viewPagerAdapter.tabTitles
        tabTitles.addAll(0, newTabTitles)
        val endOfPeriodList = viewPagerAdapter.endOfPeriodList
        endOfPeriodList.addAll(0, newEndOfPeriodList)

        this.minOperationDate = minOperationDate

        var position = pieChartViewPager.currentItem
        position += newTabTitles.size

        initViewPagerWithEntries(tabTitles, endOfPeriodList)
        pieChartViewPager.currentItem = position
    }

    private fun getOperationFromExtras(extras: Bundle): Operation {

        val operation = extras.getSerializable(OPERATION_KEY) as Operation
        val amountString = extras.getString(AMOUNT_KEY)
        operation.amount = BigDecimal(amountString)

        val category = database.getCategory(operation.category.id)//TODO: Required operation with initialized category
        operation.category = category!!
        return operation
    }

    fun onNewOperationButtonClick(v: View) {
        val intent = Intent(this, MoneyCalculatorActivity::class.java)
        intent.putExtra(IS_OPERATION_INCOME, isIncome)
        intent.putExtra(IS_MODIFYING_OPERATION, false)
        val extras = Bundle()
        extras.putSerializable(USER_ID_KEY, USER_ID)
        extras.putLong(DATE_KEY, endOfPeriod.timeInMillis)
        intent.putExtras(extras)

        startActivityForResult(intent, NEW_OPERATION_REQUEST_CODE)
    }

    fun getPeriodBySpinnerSelected(positionInSpinner: Int): PeriodsOfTime {
        return when (positionInSpinner) {
            SpinnerItem.POSITION_DAY -> PeriodsOfTime.DAY
            SpinnerItem.POSITION_WEEK -> PeriodsOfTime.WEEK
            SpinnerItem.POSITION_MONTH -> PeriodsOfTime.MONTH
            SpinnerItem.POSITION_YEAR -> PeriodsOfTime.YEAR
            else -> PeriodsOfTime.ALL_TIME
        }
    }

    override fun onChangeOperationClick(operation: Operation) {
        val intent = Intent(this, MoneyCalculatorActivity::class.java)
        val extras = Bundle()
        extras.putSerializable(OPERATION_KEY, operation)
        extras.putSerializable(USER_ID_KEY, USER_ID)
        extras.putSerializable(DATE_KEY, operation.operationDate.time)
        extras.putSerializable(IS_MODIFYING_OPERATION, true)
        extras.putSerializable(IS_OPERATION_INCOME, operation.isOperationIncome)
        extras.putSerializable(AMOUNT_KEY, operation.amount.toString())
        intent.putExtras(extras)

        startActivityForResult(intent, CHANGE_OPERATION_REQUEST_CODE)
    }
}
