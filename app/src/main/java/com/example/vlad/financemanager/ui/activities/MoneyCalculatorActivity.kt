package com.example.vlad.financemanager.ui.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import com.example.vlad.financemanager.data.database.DatabaseHelper
import com.example.vlad.financemanager.data.mappers.SpinnerItemMapper
import com.example.vlad.financemanager.data.models.Account
import com.example.vlad.financemanager.data.models.Category
import com.example.vlad.financemanager.ui.IMoneyCalculation
import com.example.vlad.financemanager.PresenterMoneyCalculator
import com.example.vlad.financemanager.R
import com.example.vlad.financemanager.data.models.Operation
import com.example.vlad.financemanager.data.models.SpinnerItem
import com.example.vlad.financemanager.ui.adapters.ImageSpinnerAdapter
import com.example.vlad.financemanager.ui.fragments.DatePickerFragment

import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class MoneyCalculatorActivity : AppCompatActivity(), IMoneyCalculation, DatePickerDialog.OnDateSetListener, View.OnClickListener, TextWatcher {

    private val sdf = SimpleDateFormat("E, dd MMMM")
    private val sdfWithYear = SimpleDateFormat("E, MMMM dd, yyyy")

    private lateinit var resultText: TextView
    private lateinit var amountEditText: EditText
    private lateinit var comment: EditText
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var accountsSpinner: Spinner
    private lateinit var categoriesSpinner: Spinner
    private lateinit var dateButton: Button
    private lateinit var saveButton: Button
    private lateinit var closeButton: Button
    private lateinit var btnBack: ImageButton

    private lateinit var presenter: PresenterMoneyCalculator
    private var operationDate = Date()
    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var categorySpinnerItemList: List<SpinnerItem>
    private lateinit var accountSpinnerItemList: List<SpinnerItem>

    private var isOperationIncome: Boolean = false
    private var accountId: Int = 0
    private var categoryId: Int = 0

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_money_calculator)

        resultText = findViewById(R.id.calculationResultTextView)
        amountEditText = findViewById(R.id.amountMoneyActivityEditText)
        amountEditText.addTextChangedListener(this)
        comment = findViewById(R.id.commentMoneyActivityEditText)
        toolbar = findViewById(R.id.calculatorActivityToolbar)
        toolbarTitle = findViewById(R.id.toolbarTitleTextView)
        accountsSpinner = findViewById(R.id.accountSpinner)
        categoriesSpinner = findViewById(R.id.categorySpinner)
        dateButton = findViewById(R.id.operationDateButton)
        saveButton = findViewById(R.id.saveRecordButton)
        saveButton.setOnClickListener(this)
        closeButton = findViewById(R.id.closeOperationButton)
        closeButton.setOnClickListener(this)
        btnBack = findViewById(R.id.calculatorBackButton)

        databaseHelper = DatabaseHelper.getInstance(applicationContext)
        presenter = PresenterMoneyCalculator(this)

        btnBack.setOnLongClickListener {
            presenter.clearNumber()
            true
        }

        initAmountEditText()
        initUiViaExtras(intent.extras)
    }

    private fun initAmountEditText() {
        amountEditText.requestFocus()
        amountEditText.setOnKeyListener { _, keyCode, keyEvent ->
            if (!(keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)) {
                presenter.calculatorReset()
            }

            false
        }
    }


    private fun initSpinnersItemLists(userId: Int) {
        val accountList = databaseHelper.getAllAccounts(userId)
        accountSpinnerItemList = SpinnerItemMapper.mapAccountsToSpinnerItems(accountList)
        val categoryList = databaseHelper.getAllCategories(userId, isOperationIncome)
        categorySpinnerItemList = SpinnerItemMapper.mapCategoryToSpinnerItems(categoryList)
    }

    private fun initToolbar(isIncome: Boolean) {
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
        toolbar.navigationIcon!!.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        toolbar.setNavigationOnClickListener { finish() }

        val title = if (isIncome) getString(R.string.income) else getString(R.string.outcome)
        toolbarTitle.text = title
    }

    private fun initDateTimePicker(operationDate: Date) {
        dateButton.text = sdf.format(Date())
        dateButton.setOnClickListener {
            val df = DatePickerFragment()
            df.setCalendar(operationDate)
            df.show(supportFragmentManager, DATE_PICKER_TAG)
        }
    }

    private fun getDateButtonTitleByDate(dateInMillis: Long?): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateInMillis!!
        operationDate = calendar.time

        val dateButtonTitle: String
        dateButtonTitle = if (calendar.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)) {
            sdfWithYear.format(calendar.time)
        } else {
            sdf.format(calendar.time)
        }

        return dateButtonTitle
    }

    override fun getCategoryId(): Int {
        return categoryId
    }

    override fun getAccountId(): Int {
        return accountId
    }

    override fun getOperationDate(): Date {
        return operationDate
    }

    override fun getComment(): String {
        return comment.text.toString()
    }

    override fun getIsOperationInput(): Boolean {
        return isOperationIncome
    }

    override fun getAmount(): String {
        return amountEditText.text.toString()
    }

    override fun setAmountResultText(result: String) {
        amountEditText.setText(result)
        resultText.text = result
    }

    override fun setCalculatorToZero() {
        resultText.text = "0"
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
        val intent = Intent()
        val amount = operation.amount.toString()
        val extras = Bundle()
        extras.putString(MainActivity.AMOUNT_KEY, amount)
        extras.putSerializable(MainActivity.OPERATION_KEY, operation)
        intent.putExtras(extras)

        setResult(0, intent)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        operationDate = calendar.time
        dateButton.text = getDateButtonTitleByDate(operationDate.time)
    }

    private fun initUiViaExtras(extras: Bundle?) {
        if(extras == null) return

        val userId = extras.getInt(MainActivity.USER_ID_KEY)
        isOperationIncome = extras.getBoolean(MainActivity.IS_OPERATION_INCOME)
        val isModifyingOperation = extras.getBoolean(MainActivity.IS_MODIFYING_OPERATION)

        initSpinnersItemLists(userId)
        initSpinnersWithItemLists(accountSpinnerItemList, categorySpinnerItemList)
        initToolbar(isOperationIncome)

        if (isModifyingOperation) {//Init UI via operation
            val operation = initOperationFromExtras(extras)
            operation.operationDate = Date(extras.getLong(DATE_KEY))
            initUiViaOperationValues(operation)
        } else {
            primaryUiInit()
        }
    }

    private fun primaryUiInit() {
        initDateTimePicker(Date())
        val dateButtonTitle = getDateButtonTitleByDate(Date().time)
        dateButton.text = dateButtonTitle
    }

    private fun initUiViaOperationValues(operation: Operation) {
        val dateButtonTitle: String = getDateButtonTitleByDate(operation.operationDate.time)

        initDateTimePicker(Date(operation.operationDate.time))
        dateButton.text = dateButtonTitle
        comment.setText(operation.comment)
        setAmountResultText(operation.amount.toString())
        presenter.settingResultText(operation.amount)
        selectSpinnerItemMatchesToId(operation.category.id, categorySpinnerItemList, categoriesSpinner)
        selectSpinnerItemMatchesToId(operation.accountId, accountSpinnerItemList, accountsSpinner)
    }

    private fun selectSpinnerItemMatchesToId(id: Int, categorySpinnerItemList: List<SpinnerItem>, spinner: Spinner?) {
        for (i in categorySpinnerItemList.indices) {
            if (categorySpinnerItemList[i].id == id) {
                spinner!!.setSelection(i)
            }
        }
    }

    private fun initOperationFromExtras(extras: Bundle): Operation {
        val operation = extras.getSerializable(MainActivity.OPERATION_KEY) as Operation
        operation.amount = BigDecimal(intent.extras!!.get(MainActivity.AMOUNT_KEY)!!.toString())
        presenter.setModifyingOperationId(operation.id)
        return operation
    }

    private fun initSpinnersWithItemLists(accountSpinnerItemList: List<SpinnerItem>, categorySpinnerItemList: List<SpinnerItem>) {
        val accountSpinnerAdapter = ImageSpinnerAdapter(this, R.layout.image_spinner_item, accountSpinnerItemList,
                R.color.dark_gray, R.color.dark_black)
        accountsSpinner.adapter = accountSpinnerAdapter
        accountsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        itemSelected: View, selectedItemPosition: Int, selectedId: Long) {

                accountId = (parent.getItemAtPosition(selectedItemPosition) as SpinnerItem).id
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val categoriesSpinnerAdapter = ImageSpinnerAdapter(this, R.layout.image_spinner_item, categorySpinnerItemList,
                R.color.dark_gray, R.color.dark_black)
        categoriesSpinner.adapter = categoriesSpinnerAdapter
        categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        itemSelected: View, selectedItemPosition: Int, selectedId: Long) {

                categoryId = (parent.getItemAtPosition(selectedItemPosition) as SpinnerItem).id
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.closeOperationButton -> finishActivity()
            R.id.saveRecordButton -> presenter.onButtonSaveClick()
        }
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (before > count) presenter.calculatorReset()
    }

    override fun afterTextChanged(editable: Editable) {

    }

    companion object {
        const val DATE_KEY = "date_key"
        private const val DATE_PICKER_TAG = "date picker"
    }
}