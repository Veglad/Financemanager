package com.example.vlad.financemanager.ui

import com.example.vlad.financemanager.data.models.Operation

import java.util.Date

interface IMoneyCalculation {

    val categoryId: Int

    val accountId: Int

    val operationDate: Date

    val comment: String

    val amount: String

    val isOperationInput: Boolean

    fun setAmountResultText(resultText: String)

    fun setCalculatorToZero()

    fun finishActivity()

    fun calculationErrorSignal()

    fun calculationErrorSignal(msg: String)

    fun sendNewOperation(operation: Operation)

    fun initUiViaOperationValues(operation: Operation)

}
