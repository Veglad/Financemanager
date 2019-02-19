package com.example.vlad.financemanager

import android.support.annotation.IdRes

import com.example.vlad.financemanager.data.enums.CalculatorOperations
import com.example.vlad.financemanager.data.models.Category
import com.example.vlad.financemanager.data.models.Operation
import com.example.vlad.financemanager.ui.IMoneyCalculation

import java.math.BigDecimal

class PresenterMoneyCalculator(private val view: IMoneyCalculation) {
    private val model = ModelMoneyCalculator()
    private var modifyingOperationId = 0

    fun calculatorBtnOnClick(@IdRes btnId: Int, numberValue: String = "") {
        when (btnId) {
            R.id.calculatorBackButton -> view.setAmountResultText(model.clearLastSymbol())
            R.id.additionCalculatorButton -> {
                performOperation(CalculatorOperations.ADD)
                view.setAmountResultText(model.resultText)
            }
            R.id.substractionCalculatorButton -> {
                performOperation(CalculatorOperations.SUB)
                view.setAmountResultText(model.resultText)
            }
            R.id.multiplicationCalculatorButton -> {
                performOperation(CalculatorOperations.MUL)
                view.setAmountResultText(model.resultText)
            }
            R.id.divisionCalculatorButton -> {
                performOperation(CalculatorOperations.DIV)
                view.setAmountResultText(model.resultText)
            }
            R.id.equalsCalculatorButton -> if (model.calculatePress()) {
                view.setAmountResultText(model.resultText)
            } else {
                view.setAmountResultText(model.calculationError())
                view.calculationErrorSignal()
            }
            else -> if (model.isNewResultCorrect(numberValue[0])) {
                view.setAmountResultText(model.newResultText(numberValue[0]))
            }
        }
    }

    private fun performOperation(add: CalculatorOperations) {
        if (!model.mathOperationBtnClick(add)) {
            model.clearNumber()
            view.calculationErrorSignal()
        }
    }

    fun clearNumber() {
        view.setAmountResultText(model.clearNumber())
    }

    fun onButtonSaveClick() {
        val category = Category()
        category.id = view.categoryId

        if (BigDecimal(view.amount).compareTo(model.zeroBigDecimalValue) == 0) {
            view.calculationErrorSignal("Set the amount, please")
            return
        }

        val operation = Operation(BigDecimal(view.amount), view.operationDate, view.comment.trim { it <= ' ' },
                view.isOperationInput, category, modifyingOperationId, view.accountId)
        view.sendNewOperation(operation)
        view.finishActivity()
    }

    fun setModifyingOperationId(modifyingOperationId: Int) {
        this.modifyingOperationId = modifyingOperationId
    }

    fun calculatorReset() {
        model.clearNumber()
        view.setCalculatorToZero()
    }

    fun initUiViaOperationValues(operation: Operation) {
        view.initUiViaOperationValues(operation)
        model.initFotChangeOperation(operation.amount)
    }
}
