package com.example.vlad.financemanager;

import com.example.vlad.financemanager.data.enums.CalculatorOperations;

import java.math.BigDecimal;
import java.math.RoundingMode;

class ModelMoneyCalculator {
    private String resultText;
    /** First operand (Accumulator)*/
    private BigDecimal accumulator;
    /** Second operand */
    private BigDecimal secondOperand;
    /** Field for keeping some data in specific situations */
    private BigDecimal tempOperand;
    /** operation to perform */
    private CalculatorOperations operation;
    /** Field for keeping name of operation */
    private CalculatorOperations tempOperation;
    /** is temp operand empty or no. Using for complex sequence of operations */
    private boolean isTempOperationEmpty;
    /** Flag - used for identifying if calculation has just completed */
    private boolean justCount;
    /** Flag - used for identifying if there is a sequence of the operations with the same prioriy */
    private boolean multiSamePriorityOperation;

    private final BigDecimal maxAllowedValue = new BigDecimal("99999999");
    final BigDecimal zeroBigDecimalValue = new BigDecimal("0");

    ModelMoneyCalculator(){
        resultText = "0";
        accumulator = new BigDecimal(0);
        secondOperand = new BigDecimal(0);
        operation = CalculatorOperations.NONE;
        tempOperation = CalculatorOperations.NONE;
        isTempOperationEmpty = true;
        justCount = false;
        multiSamePriorityOperation = false;
    }

    String getResultText(){
        return resultText;
    }

    boolean mathOperationBtnClick(CalculatorOperations chosenOperation){
        boolean result = true;

       switch (chosenOperation){
            case ADD:
                result = SubOrAddOperationChosen(CalculatorOperations.ADD);
                break;
            case SUB:
                result = SubOrAddOperationChosen(CalculatorOperations.SUB);
                break;
            case MUL:
                result = MulOrDivOperationChosen(CalculatorOperations.MUL);
                break;
            case DIV:
                result = MulOrDivOperationChosen(CalculatorOperations.DIV);
                break;
        }
        if(isTempOperationEmpty && !multiSamePriorityOperation) resultText = "0";

        return result;
    }

    private boolean SubOrAddOperationChosen(CalculatorOperations chosenOperation){
        boolean result = true;
        if(operation != CalculatorOperations.NONE){
            multiSamePriorityOperation = true;
            result = calculate();
        } else {
            accumulator = new BigDecimal(resultText);
        }
        operation = chosenOperation;

        return result;
    }

    private boolean MulOrDivOperationChosen(CalculatorOperations chosenOperation){
        boolean result = true;

        //Example 2+3*4* = 2+12*
        if(!isTempOperationEmpty){
            operation = chosenOperation;
            result = calculate();
        }
        else if(operation == CalculatorOperations.SUB || operation == CalculatorOperations.ADD) {
            isTempOperationEmpty = false;
            tempOperand = accumulator;
            accumulator = new BigDecimal(resultText);
            resultText = "0";
            //saving current operation
            tempOperation = operation == CalculatorOperations.ADD ? CalculatorOperations.ADD : CalculatorOperations.SUB;
        }
        else if(operation != CalculatorOperations.NONE){
            multiSamePriorityOperation = true;
            result = calculate();
        }
        else{
            accumulator = new BigDecimal(resultText);
        }
        operation = chosenOperation;

        return result;
    }

    private boolean calculate(){
        secondOperand = new BigDecimal(resultText);
        switch (operation){
            case ADD:
                accumulator = accumulator.add(secondOperand);
                break;
            case SUB:
                accumulator = accumulator.subtract(secondOperand);
                break;
            case MUL:
                accumulator = accumulator.multiply(secondOperand);
                accumulator = accumulator.setScale(2,BigDecimal.ROUND_HALF_EVEN);
                break;
            case DIV:
                //division by zero
                if(secondOperand.compareTo(zeroBigDecimalValue) == 0) return  false;

                accumulator = accumulator.divide(secondOperand,2, RoundingMode.CEILING);
                accumulator = accumulator.setScale(2, BigDecimal.ROUND_HALF_EVEN);
                break;
        }
        //If the new number is too big
        if(accumulator.compareTo(maxAllowedValue) > 0) return false;

        resultText = accumulator.toString();
        justCount = true;
        return true;
    }

    boolean calculatePress(){
        if(!calculate()) return  false;

        if(!isTempOperationEmpty){
            secondOperand = accumulator;
            accumulator = tempOperand;
            operation = tempOperation;
            if(!calculate()) return false;
        }

        //If the result value is less or equals 0
        if(accumulator.compareTo(zeroBigDecimalValue) <= 0) return false;
        //If the new number is too big
        if(accumulator.compareTo(maxAllowedValue) > 0) return false;

        //Reset accumulator and second operand
        isTempOperationEmpty = true;
        accumulator = new BigDecimal(0);
        secondOperand = new BigDecimal(0);
        operation = CalculatorOperations.NONE;

        return true;
    }

    boolean isNewResultCorrect(char newChar){
        int lastIndexOfSeparator = resultText.lastIndexOf('.');

        if(newChar == '.' && resultText.contains("."))
                return false;
        if(lastIndexOfSeparator != -1 && lastIndexOfSeparator <= resultText.length()-3)
            return false;
        if(lastIndexOfSeparator == -1 && resultText.length() == 8 && newChar != '.')
            return false;

        return true;
    }

    String clearLast(){

        if(resultText.length() == 1 || resultText.contains("E") || resultText.contains("e"))
            resultText = "0";
        else
            resultText= resultText.substring(0,resultText.length()-1);

        return resultText;
    }

    String clearNumber(){
        resultText = "0";

        isTempOperationEmpty = true;
        tempOperation = CalculatorOperations.NONE;
        operation = CalculatorOperations.NONE;
        multiSamePriorityOperation = false;
        justCount = false;

        accumulator = new BigDecimal(0);
        secondOperand = new BigDecimal(0);

        return  resultText;
    }

    String calculationError(){
        clearNumber();
        return  resultText;
    }

    String newResultText(char lastChar){
        if(resultText.length() == 1 && resultText.charAt(0) == '0' && lastChar != '.' )
            resultText = lastChar + "";
        else if(justCount && lastChar != '.'){
            justCount = false;
            resultText = lastChar + "";
        }
        else
            resultText += lastChar;

        return  resultText;
    }

    void setResultText(BigDecimal newResult){
        resultText = newResult.toString();
    }

    void initFotChangeOperation(BigDecimal newResult){
        resultText = newResult.toString();
        accumulator = newResult;
    }
}
