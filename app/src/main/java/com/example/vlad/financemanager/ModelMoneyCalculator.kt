package com.example.vlad.financemanager

import com.example.vlad.financemanager.data.enums.CalculatorOperations

import java.math.BigDecimal
import java.math.RoundingMode

internal class ModelMoneyCalculator {
    private val RESULT_MAX_LENGTH = 8
    private val maxAllowedValue = BigDecimal("99999999")
    val zeroBigDecimalValue = BigDecimal("0")

    var resultText: String? = null
        private set
    /**
     * First operand (Accumulator)
     */
    private var accumulator: BigDecimal? = null
    /**
     * Second operand
     */
    private var secondOperand: BigDecimal? = null
    /**
     * Field for keeping some data in specific situations
     */
    private var tempOperand: BigDecimal? = null
    /**
     * operation to perform
     */
    private var operation: CalculatorOperations? = null
    /**
     * Field for keeping name of operation
     */
    private var tempOperation: CalculatorOperations? = null
    /**
     * is temp operand empty or no. Using for complex sequence of operations
     */
    private var isTempOperationEmpty: Boolean = false
    /**
     * Flag - used for identifying if calculation has just completed
     */
    private var justCount: Boolean = false
    /**
     * Flag - used for identifying if there is a sequence of the operations with the same prioriy
     */
    private var multiSamePriorityOperation: Boolean = false

    init {
        resultText = "0"
        accumulator = BigDecimal(0)
        secondOperand = BigDecimal(0)
        operation = CalculatorOperations.NONE
        tempOperation = CalculatorOperations.NONE
        isTempOperationEmpty = true
        justCount = false
        multiSamePriorityOperation = false
    }

    fun mathOperationBtnClick(chosenOperation: CalculatorOperations): Boolean {
        var result = true

        when (chosenOperation) {
            CalculatorOperations.ADD -> result = countLowPriorityOperation(CalculatorOperations.ADD)
            CalculatorOperations.SUB -> result = countLowPriorityOperation(CalculatorOperations.SUB)
            CalculatorOperations.MUL -> result = countHighPriorityOperation(CalculatorOperations.MUL)
            CalculatorOperations.DIV -> result = countHighPriorityOperation(CalculatorOperations.DIV)
        }
        if (isTempOperationEmpty && !multiSamePriorityOperation) resultText = "0"

        return result
    }

    private fun countLowPriorityOperation(chosenOperation: CalculatorOperations): Boolean {
        var result = true
        if (operation !== CalculatorOperations.NONE) {
            multiSamePriorityOperation = true
            result = calculate()
        } else {
            accumulator = BigDecimal(resultText)
        }
        operation = chosenOperation

        return result
    }

    private fun countHighPriorityOperation(chosenOperation: CalculatorOperations): Boolean {
        var result = true

        //Example 2+3*4* = 2+12*
        if (!isTempOperationEmpty) {
            operation = chosenOperation
            result = calculate()
        } else if (operation === CalculatorOperations.SUB || operation === CalculatorOperations.ADD) {
            isTempOperationEmpty = false
            tempOperand = accumulator
            accumulator = BigDecimal(resultText)
            resultText = "0"
            //saving current operation
            tempOperation = if (operation === CalculatorOperations.ADD) CalculatorOperations.ADD else CalculatorOperations.SUB
        } else if (operation !== CalculatorOperations.NONE) {
            multiSamePriorityOperation = true
            result = calculate()
        } else {
            accumulator = BigDecimal(resultText)
        }
        operation = chosenOperation

        return result
    }

    private fun calculate(): Boolean {
        secondOperand = BigDecimal(resultText)
        when (operation) {
            CalculatorOperations.ADD -> accumulator = accumulator!!.add(secondOperand)
            CalculatorOperations.SUB -> accumulator = accumulator!!.subtract(secondOperand)
            CalculatorOperations.MUL -> {
                accumulator = accumulator!!.multiply(secondOperand)
                accumulator = accumulator!!.setScale(2, BigDecimal.ROUND_HALF_EVEN)
            }
            CalculatorOperations.DIV -> {
                //division by zero
                if (secondOperand!!.compareTo(zeroBigDecimalValue) == 0) return false

                accumulator = accumulator!!.divide(secondOperand, 2, RoundingMode.CEILING)
                accumulator = accumulator!!.setScale(2, BigDecimal.ROUND_HALF_EVEN)
            }
        }
        //If the new number is too big
        if (accumulator!!.compareTo(maxAllowedValue) > 0) return false

        resultText = accumulator!!.toString()
        justCount = true
        return true
    }

    fun calculatePress(): Boolean {
        if (!calculate()) return false

        if (!isTempOperationEmpty) {
            secondOperand = accumulator
            accumulator = tempOperand
            operation = tempOperation
            if (!calculate()) return false
        }

        //If the result value is less or equals 0
        if (accumulator!!.compareTo(zeroBigDecimalValue) <= 0) return false
        //If the new number is too big
        if (accumulator!!.compareTo(maxAllowedValue) > 0) return false

        //Reset accumulator and second operand
        isTempOperationEmpty = true
        accumulator = BigDecimal(0)
        secondOperand = BigDecimal(0)
        operation = CalculatorOperations.NONE

        return true
    }

    fun isNewResultCorrect(newChar: Char): Boolean {
        val lastIndexOfSeparator = resultText!!.lastIndexOf('.')

        if (newChar == '.' && resultText!!.contains("."))
            return false
        if (lastIndexOfSeparator != NOT_FOUND_INDEX && lastIndexOfSeparator <= resultText!!.length - (DECIMAL_PLACES_MAX_COUNT - 1))
            return false
        return if (lastIndexOfSeparator == NOT_FOUND_INDEX && resultText!!.length == RESULT_MAX_LENGTH && newChar != '.') false else true

    }

    fun clearLastSymbol(): String {
        if (resultText!!.length == 1 || resultText!!.contains("E") || resultText!!.contains("e"))
            resultText = "0"
        else
            resultText = resultText!!.substring(0, resultText!!.length - 1)

        return resultText
    }

    fun clearNumber(): String {
        resultText = "0"

        isTempOperationEmpty = true
        tempOperation = CalculatorOperations.NONE
        operation = CalculatorOperations.NONE
        multiSamePriorityOperation = false
        justCount = false

        accumulator = BigDecimal(0)
        secondOperand = BigDecimal(0)

        return resultText
    }

    fun calculationError(): String? {
        clearNumber()
        return resultText
    }

    fun newResultText(lastChar: Char): String {
        if (resultText!!.length == 1 && resultText!![0] == '0' && lastChar != '.')
            resultText = lastChar + ""
        else if (justCount && lastChar != '.') {
            justCount = false
            resultText = lastChar + ""
        } else
            resultText += lastChar

        return resultText
    }

    fun setResultText(newResult: BigDecimal) {
        resultText = newResult.toString()
    }

    fun initFotChangeOperation(newResult: BigDecimal) {
        resultText = newResult.toString()
        accumulator = newResult
    }

    companion object {
        private val NOT_FOUND_INDEX = -1
        private val DECIMAL_PLACES_MAX_COUNT = 2
    }
}
