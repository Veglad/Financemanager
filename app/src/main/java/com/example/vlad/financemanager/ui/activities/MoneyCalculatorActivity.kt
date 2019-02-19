package com.example.vlad.financemanager.ui.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import butterknife.OnClick

import com.example.vlad.financemanager.data.database.DatabaseHelper
import com.example.vlad.financemanager.data.mappers.SpinnerItemMapper
import com.example.vlad.financemanager.ui.IMoneyCalculation
import com.example.vlad.financemanager.PresenterMoneyCalculator
import com.example.vlad.financemanager.R
import com.example.vlad.financemanager.data.models.Operation
import com.example.vlad.financemanager.data.models.SpinnerItem
import com.example.vlad.financemanager.ui.adapters.ImageSpinnerAdapter
import com.example.vlad.financemanager.ui.fragments.DatePickerFragment
import kotlinx.android.synthetic.main.activity_money_calculator.*

import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class MoneyCalculatorActivity : AppCompatActivity(), IMoneyCalculation,
        DatePickerDialog.OnDateSetListener {

    companion object {
        const val DATE_KEY = "date_key"
        private const val DATE_PICKER_TAG = "date picker"
    }

    override val comment: String
        get() {
            return commentMoneyActivityEditText.text.toString()
        }
    override val amount: String
        get() {
            return amountMoneyActivityEditText.text.toString()
        }

    private val mediumDateFormat = SimpleDateFormat("E, dd MMMM")
    private val longDateFormat = SimpleDateFormat("E, MMMM dd, yyyy")

    private lateinit var presenter: PresenterMoneyCalculator
    override var operationDate = Date()
    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var categorySpinnerItemList: List<SpinnerItem>
    private lateinit var accountSpinnerItemList: List<SpinnerItem>

    override var isOperationInput: Boolean = false
    override var accountId: Int = 0
    override var categoryId: Int = 0

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_money_calculator)

        databaseHelper = DatabaseHelper.getInstance(applicationContext)
        presenter = PresenterMoneyCalculator(this)

        calculatorBackButton.setOnLongClickListener {
            presenter.clearNumber()
            true
        }

        initViewsWithClickListeners()
        initAmountEditText()
        initUiViaExtras(intent.extras)
    }

    private fun initViewsWithClickListeners() {
        amountMoneyActivityEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (before > count) presenter.calculatorReset()
            }
        })

        closeOperationButton.setOnClickListener { finishActivity() }
        saveRecordButton.setOnClickListener { presenter.onButtonSaveClick() }
        calculatorBackButton.setOnClickListener {
            val pressedButton = it as ImageButton
            presenter.calculatorBtnOnClick(pressedButton.id)
        }
    }

    private fun initAmountEditText() {
        amountMoneyActivityEditText.apply {
            requestFocus()
            setOnKeyListener { _, keyCode, keyEvent ->
                if (!(keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)) {
                    presenter.calculatorReset()
                }
                false
            }
        }
    }


    private fun initSpinnersItemLists(userId: Int) {
        val accountList = databaseHelper.getAllAccounts(userId)
        accountSpinnerItemList = SpinnerItemMapper.mapAccountsToSpinnerItems(accountList)
        val categoryList = databaseHelper.getAllCategories(userId, isOperationInput)
        categorySpinnerItemList = SpinnerItemMapper.mapCategoryToSpinnerItems(categoryList)
    }

    private fun initToolbar(isIncome: Boolean) {
        setSupportActionBar(calculatorActivityToolbar)
        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
        calculatorActivityToolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        calculatorActivityToolbar.setNavigationOnClickListener { finish() }

        val title = if (isIncome) getString(R.string.income) else getString(R.string.outcome)
        toolbarTitleTextView.text = title
    }

    private fun initDateTimePicker(operationDate: Date) {
        operationDateButton.apply {
            text = mediumDateFormat.format(Date())
        }
        operationDateButton.setOnClickListener {
            DatePickerFragment().apply {
                setCalendar(operationDate)
                show(supportFragmentManager, DATE_PICKER_TAG)
            }
        }
    }

    private fun getDateButtonTitleByDate(dateInMillis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateInMillis
        operationDate = calendar.time

        val dateButtonTitle: String
        dateButtonTitle = if (calendar.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)) {
            longDateFormat.format(calendar.time)
        } else {
            mediumDateFormat.format(calendar.time)
        }

        return dateButtonTitle
    }

    override fun setAmountResultText(resultText: String) {
        amountMoneyActivityEditText.setText(resultText)
        calculationResultTextView.text = resultText
    }

    override fun setCalculatorToZero() {
        calculationResultTextView.text = "0"
    }

    fun calculatorBtnOnClick(view: View) {
        val pressedButton = view as Button
        presenter.calculatorBtnOnClick(pressedButton.id, pressedButton.text.toString())
    }

    override fun finishActivity() {
        finish()
    }

    override fun calculationErrorSignal() {
        Toast.makeText(this, getString(R.string.incorrect_input), Toast.LENGTH_SHORT).show()
    }

    override fun calculationErrorSignal(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun sendNewOperation(operation: Operation) {
        val amount = operation.amount.toString()
        val extras = Bundle().apply {
            putString(MainActivity.AMOUNT_KEY, amount)
            putParcelable(MainActivity.OPERATION_KEY, operation)
        }
        val intent = Intent().apply {
            putExtras(extras)
        }

        setResult(0, intent)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }

        operationDate = calendar.time
        operationDateButton.text = getDateButtonTitleByDate(operationDate.time)
    }

    private fun initUiViaExtras(extras: Bundle) {
        val userId = extras.getInt(MainActivity.USER_ID_KEY)
        isOperationInput = extras.getBoolean(MainActivity.IS_OPERATION_INCOME)
        val isModifyingOperation = extras.getBoolean(MainActivity.IS_MODIFYING_OPERATION)

        initSpinnersItemLists(userId)
        initSpinnersWithItemLists(accountSpinnerItemList, categorySpinnerItemList)
        initToolbar(isOperationInput)

        if (isModifyingOperation) {//Init UI via operation
            val operation = initOperationFromExtras(extras)
            operation.operationDate = Date(extras.getLong(DATE_KEY))
            presenter.initUiViaOperationValues(operation)
        } else {
            primaryUiInit()
        }
    }

    private fun primaryUiInit() {
        initDateTimePicker(Date())
        val dateButtonTitle = getDateButtonTitleByDate(Date().time)
        operationDateButton.text = dateButtonTitle
    }

    override fun initUiViaOperationValues(operation: Operation) {
        val dateButtonTitle: String = getDateButtonTitleByDate(operation.operationDate.time)

        initDateTimePicker(Date(operation.operationDate.time))
        operationDateButton.text = dateButtonTitle
        commentMoneyActivityEditText.setText(operation.comment)
        setAmountResultText(operation.amount.toString())
        selectSpinnerItemMatchesToId(operation.category.id, categorySpinnerItemList, categorySpinner)
        selectSpinnerItemMatchesToId(operation.accountId, accountSpinnerItemList, accountSpinner)
    }

    private fun selectSpinnerItemMatchesToId(id: Int, categorySpinnerItemList: List<SpinnerItem>, spinner: Spinner?) {
        val spinnerSelectionIndex = categorySpinnerItemList.indexOfFirst { it.id == id }
        spinner?.setSelection(spinnerSelectionIndex)
    }

    private fun initOperationFromExtras(extras: Bundle): Operation {
        val operation = extras.getParcelable(MainActivity.OPERATION_KEY) as Operation
        operation.amount = BigDecimal(extras.get(MainActivity.AMOUNT_KEY)!!.toString())
        presenter.setModifyingOperationId(operation.id)
        return operation
    }

    private fun initSpinnersWithItemLists(accountSpinnerItemList: List<SpinnerItem>, categorySpinnerItemList: List<SpinnerItem>) {
        val accountSpinnerAdapter = ImageSpinnerAdapter(this, R.layout.image_spinner_item, accountSpinnerItemList,
                R.color.dark_gray, R.color.dark_black)
        accountSpinner.apply {
            adapter = accountSpinnerAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            itemSelected: View, selectedItemPosition: Int, selectedId: Long) {
                    accountId = (parent.getItemAtPosition(selectedItemPosition) as SpinnerItem).id
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        val categoriesSpinnerAdapter = ImageSpinnerAdapter(this, R.layout.image_spinner_item, categorySpinnerItemList,
                R.color.dark_gray, R.color.dark_black)
        categorySpinner.apply {
            adapter = categoriesSpinnerAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            itemSelected: View, selectedItemPosition: Int, selectedId: Long) {
                    categoryId = (parent.getItemAtPosition(selectedItemPosition) as SpinnerItem).id
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }
}
