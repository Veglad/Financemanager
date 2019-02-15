package com.example.vlad.financemanager.ui.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView

import com.example.vlad.financemanager.R
import com.example.vlad.financemanager.data.database.DatabaseHelper
import com.example.vlad.financemanager.data.enums.PeriodsOfTime
import com.example.vlad.financemanager.data.models.Operation
import com.example.vlad.financemanager.ui.OnChangeOperationClickListener
import com.example.vlad.financemanager.ui.adapters.OperationsAdapter
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate

import java.math.BigDecimal
import java.util.ArrayList
import java.util.Calendar
import java.util.HashMap

class TabFragment : Fragment() {

    companion object {
        private const val END_OF_PERIOD_KEY = "endOfPeriod"
        private const val CURRENT_PERIOD_KEY = "currentPeriod"
        private const val IS_INCOME_KEY = "isIncome"
        private const val ACCOUNT_ID_KEY = "accountIdKey"
        private const val PIE_CHART_LABEL = "pieChartLabel"
        private const val DATE_TITLE_KEY = "dateTitleKey"
        private val EMPTY_CHART_COLOR = Color.rgb(186, 195, 209)

        fun newInstance(currentPeriod: PeriodsOfTime, endOfPeriod: Calendar, isIncome: Boolean, accountId: Int, dateTitle: String): TabFragment {
            val args = Bundle()
            args.putSerializable(END_OF_PERIOD_KEY, endOfPeriod)
            args.putSerializable(CURRENT_PERIOD_KEY, currentPeriod)
            args.putBoolean(IS_INCOME_KEY, isIncome)
            args.putInt(ACCOUNT_ID_KEY, accountId)
            args.putString(DATE_TITLE_KEY, dateTitle)

            val fragment = TabFragment()
            fragment.arguments = args

            return fragment
        }
    }

    private lateinit var pieChart: PieChart
    private lateinit var viewPagerDateTextView: TextView
    private lateinit var operationsRecyclerView: RecyclerView
    private lateinit var fragmentTabScrollView: ScrollView

    private lateinit var operationsAdapter: OperationsAdapter
    private lateinit var database: DatabaseHelper
    private var onChangeOperationClickListener: OnChangeOperationClickListener? = null
    private var operationList: MutableList<Operation> = mutableListOf()
    lateinit var currentEndOfPeriod: Calendar
        private set
    lateinit var currentPeriod: PeriodsOfTime
        private set
    lateinit var dateTitle: String
        private set
    private var balanceString: String? = null
    private var isIncome: Boolean = false
    private var modifiedOperationIndex: Int = 0
    private var accountId: Int = 0

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnChangeOperationClickListener) {
            onChangeOperationClickListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = DatabaseHelper.getInstance(context!!.applicationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstance: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tab, container, false)

        pieChart = view.findViewById(R.id.pieChart)
        viewPagerDateTextView = view.findViewById(R.id.viewPagerDateTextView)
        operationsRecyclerView = view.findViewById(R.id.operationsRecyclerView)
        fragmentTabScrollView =view.findViewById(R.id.fragmentTabScrollView)

        initOperationListAdapter()
        val extras = arguments
        extras?.let {
            //Get extras
            accountId = extras.getInt(ACCOUNT_ID_KEY)
            currentPeriod = extras.getSerializable(CURRENT_PERIOD_KEY) as PeriodsOfTime
            currentEndOfPeriod = extras.getSerializable(END_OF_PERIOD_KEY) as Calendar
            currentEndOfPeriod.firstDayOfWeek = Calendar.MONDAY
            dateTitle = extras.getString(DATE_TITLE_KEY)
            isIncome = arguments!!.getBoolean(IS_INCOME_KEY)

            fullTabFragmentUpdate(currentPeriod, currentEndOfPeriod, isIncome, accountId, dateTitle)
        }
        pieChart.setUsePercentValues(true)

        return view
    }

    private fun getBalance(operationList: List<Operation>): BigDecimal {
        var balance = BigDecimal(0)
        for (operation in operationList) {
            balance = balance.add(operation.amount)
        }

        return balance
    }

    private fun getOperationsByIsIncome(isIncome: Boolean, operationList: List<Operation>): MutableList<Operation> {
        val operationListFiltered = ArrayList<Operation>()
        for (operation in operationList) {
            if (operation.isOperationIncome == isIncome) {
                operationListFiltered.add(operation)
            }
        }

        return operationListFiltered
    }

    //Full tab fragment update
    fun fullTabFragmentUpdate(currentPeriod: PeriodsOfTime, endOfPeriod: Calendar, isIncome: Boolean, accountId: Int, dateTitle: String?) {
        operationList = database.getOperations(accountId, currentPeriod, endOfPeriod) ?: return

        operationList = getOperationsByIsIncome(isIncome, operationList)
        val balance = getBalance(operationList)
        balanceString = getBalanceString(isIncome, balance)

        updateTabFragment(isIncome, dateTitle, balanceString, operationList)
    }

    fun getBalanceString(isIncome: Boolean, balance: BigDecimal): String {
        val placeholderBalanceString = getString(if (isIncome) R.string.income_balance_placeholder else R.string.outcome_balance_placeholder)
        return String.format(placeholderBalanceString, balance)
    }

    fun updateTabFragment(isIncome: Boolean, dateTitle: String?, balanceString: String?, operationList: List<Operation>) {
        updateOperationList(operationList)

        pieChart.centerText = balanceString
        drawPieChart(isIncome, operationList)
        viewPagerDateTextView.text = dateTitle
    }

    fun scrollToTop() {
        fragmentTabScrollView.fullScroll(ScrollView.FOCUS_UP)
    }

    private fun drawPieChart(isIncome: Boolean, operations: List<Operation>) {
        val entries = getPieEntries(operations)

        val dataSet = PieDataSet(entries, PIE_CHART_LABEL)
        dataSet.setColors(*if (isIncome) ColorTemplate.MATERIAL_COLORS else ColorTemplate.COLORFUL_COLORS)

        dataSet.sliceSpace = 3f

        val data = PieData(dataSet)

        if (entries.size == 0) {
            entries.add(setEmptyEntry())
            dataSet.color = EMPTY_CHART_COLOR
            dataSet.setDrawValues(false)
        } else
            data.setValueFormatter(PercentFormatter())

        pieChart.data = data
        pieChart.isRotationEnabled = false
        pieChart.getPaint(Chart.PAINT_HOLE)
        pieChart.setCenterTextSize(18f)
        pieChart.setCenterTextColor(ContextCompat.getColor(context!!, R.color.white))
        pieChart.setHoleColor(ContextCompat.getColor(context!!, R.color.transparent))

        val description = Description()
        description.text = ""
        pieChart.description = description
        pieChart.legend.isEnabled = false

        pieChart.invalidate()
    }

    //get pie entries for current operation list
    private fun getPieEntries(operations: List<Operation>): MutableList<PieEntry> {
        val entries = ArrayList<PieEntry>()
        val categoriesMap = getCategoryToAmountMap(operations)

        //Loop all hashMap and get entries
        for ((categoryName, value) in categoriesMap) {
            val amountOfMoney = java.lang.Float.parseFloat(value.toString())
            entries.add(PieEntry(amountOfMoney, categoryName))
        }

        return entries
    }

    private fun getCategoryToAmountMap(operations: List<Operation>): HashMap<String, BigDecimal> {
        //getting amount for every category
        val categoriesMap = HashMap<String, BigDecimal>()
        for ((amount, _, _, _, category) in operations) {
            val lastValue: BigDecimal = if (categoriesMap[category.name] == null) {
                BigDecimal(0)
            } else {
                categoriesMap[category.name]!!
            }

            val newValue = lastValue.add(amount)
            categoriesMap[category.name] = newValue
        }
        return categoriesMap
    }

    private fun initOperationListAdapter() {
        operationsRecyclerView.isFocusable = false
        operationsRecyclerView.itemAnimator = DefaultItemAnimator()
        operationsRecyclerView.layoutManager = LinearLayoutManager(context!!.applicationContext)

        operationsAdapter = OperationsAdapter(context, operationList)
        operationsAdapter.setOnItemClickListener { position -> changeOperationClick(position) }
        operationsAdapter.setOnItemDeleteClickListener { position -> deleteOperation(position) }
        operationsRecyclerView.adapter = operationsAdapter
    }

    private fun changeOperationClick(position: Int) {
        modifiedOperationIndex = position
        val operation = operationList[position]
        onChangeOperationClickListener?.onChangeOperationClick(operation)
    }

    private fun deleteOperation(position: Int) {
        val operationToRemove = operationList[position]
        database.deleteOperation(operationToRemove)

        fullTabFragmentUpdate(currentPeriod, currentEndOfPeriod, isIncome, accountId, dateTitle)
    }

    private fun removeOperationFromTheList(position: Int) {
        operationList.removeAt(position)
        operationsAdapter.notifyItemRemoved(position)
        updateTabFragment(isIncome, dateTitle, balanceString, operationList)
    }

    private fun updateOperationList(operationList: List<Operation>) {
        operationsAdapter.setOperationList(operationList)
        operationsAdapter.notifyDataSetChanged()
    }

    fun updateUiViaModifiedOperation(operation: Operation) {
        operationList[modifiedOperationIndex] = operation
        operationsAdapter.notifyItemChanged(modifiedOperationIndex)
        updateTabFragment(isIncome, dateTitle, balanceString, operationList)
    }

    fun updateUiViaNewOperation(operation: Operation) {
        operationList.add(0, operation)
        operationsAdapter.notifyDataSetChanged()//TODO: If operation date is less than min operation date
    }

    private fun setEmptyEntry(): PieEntry {
        return PieEntry(1f, "")
    }

    fun removeModifiedOperation() {
        removeOperationFromTheList(modifiedOperationIndex)
    }

    fun getOperationList(): List<Operation>? {
        return operationList
    }
}
