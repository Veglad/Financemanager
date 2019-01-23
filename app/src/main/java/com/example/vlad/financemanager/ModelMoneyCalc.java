package com.example.vlad.financemanager;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Class - that implements logic of calculator
 * Model of the MoneyCalculatorActivity view
 * @author Vlad Shcheglov
 * @version 1.0
 * @see MoneyCalculatorActivity
 */
public class ModelMoneyCalc {
    /**  Our current number represented as text*/
    private String resultText;
    /** First operand (Accumulator)*/
    private BigDecimal accumulator;
    /** Second operand */
    private BigDecimal secondOperand;
    /** Field for keeping some data in specific situations */
    private BigDecimal tempOperand;
    /** operation to perform */
    private CalcOperations operation;
    /** Field for keeping name of operation */
    private CalcOperations tempOperation;
    /** is temp operand empty or no. Using for complex sequence of operations */
    private boolean isTempOperEmpty;
    /** Flag - used for identifying if calculation has just completed */
    private boolean justCount;
    /** Flag - used for identifying if there is a sequence of the operations with the same prioriy */
    private boolean multiSamePriorityOper;

    /** constant - max BigDecimal value allowed int the program, used for comparing */
    public final BigDecimal maxAllowedValue = new BigDecimal("99999999");
    /** constant - zero BigDecimal value , used for comparing */
    public final BigDecimal zeroBDvalue = new BigDecimal("0");

    /**
     * Constructor - creating new object
     * Setting all params to 0
     */
    public ModelMoneyCalc(){
        resultText = "0";
        accumulator = new BigDecimal(0);
        secondOperand = new BigDecimal(0);
        operation = CalcOperations.None;
        tempOperation = CalcOperations.None;
        isTempOperEmpty = true;
        justCount = false;
        multiSamePriorityOper = false;
    }

    /**
     *Method for getting field's value {@link ModelMoneyCalc#resultText}
     * @return field's value
     */
    public String getResulText(){
        return resultText;
    }

    /**
     * Method - Analyzes the chosen operation and decides to calculate or prepare for calculating
     * @param chosenOperation - Chosen operation
     * @return is input data correct
     */
    public boolean mathOperBtnClick(CalcOperations chosenOperation){
        boolean result = true;

       switch (chosenOperation){
            case Add:
                result = SubOrAddOperChosen(CalcOperations.Add);
                break;
            case Sub:
                result = SubOrAddOperChosen(CalcOperations.Sub);
                break;
            case Mul:
                result = MulOrDivOperChosen(CalcOperations.Mul);
                break;
            case Div:
                result = MulOrDivOperChosen(CalcOperations.Div);
                break;
        }
        if(isTempOperEmpty && !multiSamePriorityOper)
            resultText = "0";

        return result;
    }

    /**
     * Method - performs Addition or Subtraction or prepares for them
     * @return is input data correct
     */
    private boolean SubOrAddOperChosen(CalcOperations chosenOperation){
        boolean result = true;
        if(operation != CalcOperations.None){
            multiSamePriorityOper = true;
            result = calculate();
        }
        else
            accumulator = new BigDecimal(resultText);
        operation = chosenOperation;

        return result;
    }


    /**
     * Method - performs Multiply or Division or prepares for them
     * @return is input data correct
     */
    private boolean MulOrDivOperChosen(CalcOperations chosenOperation){
        boolean result = true;

        //Example 2+3*4* = 2+12*
        if(!isTempOperEmpty){
            operation = chosenOperation;
            result = calculate();
        }
        else if(operation == CalcOperations.Sub || operation == CalcOperations.Add) {
            isTempOperEmpty = false;
            tempOperand = accumulator;
            accumulator = new BigDecimal(resultText);
            resultText = "0";
            //saving current operation
            tempOperation = operation == CalcOperations.Add ? CalcOperations.Add : CalcOperations.Sub;
        }
        else if(operation != CalcOperations.None){
            multiSamePriorityOper = true;
            result = calculate();
        }
        else{
            accumulator = new BigDecimal(resultText);
        }
        operation = chosenOperation;

        return result;
    }

    /**
     * Method - performs operations that was chosen before with accumulator and second operand
     * @return result of the calculation
     */
    public boolean calculate(){
        secondOperand = new BigDecimal(resultText);
        switch (operation){
            case Add:
                accumulator = accumulator.add(secondOperand);
                break;
            case Sub:
                accumulator = accumulator.subtract(secondOperand);
                break;
            case Mul:
                accumulator = accumulator.multiply(secondOperand);
                accumulator = accumulator.setScale(2,BigDecimal.ROUND_HALF_EVEN);
                break;
            case Div:
                //division by zero
                if(secondOperand.compareTo(zeroBDvalue) == 0)
                    return  false;

                accumulator = accumulator.divide(secondOperand,2, RoundingMode.CEILING);
                accumulator = accumulator.setScale(2, BigDecimal.ROUND_HALF_EVEN);
                break;
        }

        //If the new number is too big
        if(accumulator.compareTo(maxAllowedValue) > 0)
            return false;

        resultText = accumulator.toString();

        justCount = true;
        return true;
    }

    /**
     * Method - performs full calculation
     * @return is result correct
     */
    public  boolean calculatePress(){
        if(!calculate())
            return  false;


        if(!isTempOperEmpty){
            secondOperand = accumulator;
            accumulator = tempOperand;
            operation = tempOperation;
            if(!calculate())
                return false;
        }

        //If the result value is less or equals 0
        if(accumulator.compareTo(zeroBDvalue) <= 0)
            return false;
        //If the new number is too big
        if(accumulator.compareTo(maxAllowedValue) > 0)
            return false;

        //Reset accumulator and second operand
        isTempOperEmpty = true;
        accumulator = new BigDecimal(0);
        secondOperand = new BigDecimal(0);
        operation = CalcOperations.None;



        return true;
    }

    /**
     * Checking if userє's input is correct for updating number's value
     * @param newChar - char for validation
     * @return boolean if new input is correct
     */
    public boolean isNewResultCorrect(char newChar){
        String testString = resultText + newChar;
        int lastIndexOfSeparator = resultText.lastIndexOf('.');

        if(newChar == '.' && resultText.contains("."))
                return false;
        if(lastIndexOfSeparator != -1 && lastIndexOfSeparator <= resultText.length()-3)
            return false;
        if(lastIndexOfSeparator == -1 && resultText.length() == 8 && newChar != '.')
            return false;

        return true;
    }

    /**
     * Method - used for removing last digit from the number in calculator
     * @return updated result as text
     */
    public String clearLast(){

        if(resultText.length() == 1 || resultText.contains("E") || resultText.contains("e"))
            resultText = "0";
        else
            resultText= resultText.substring(0,resultText.length()-1);

        return resultText;
    }

    /**
     * Method - clear the number and all operands
     * @return new cleared string
     */
    public String clearNumber(){
        resultText = "0";

        isTempOperEmpty = true;
        tempOperation = CalcOperations.None;
        operation = CalcOperations.None;
        multiSamePriorityOper = false;
        justCount = false;

        accumulator = new BigDecimal(0);
        secondOperand = new BigDecimal(0);

        return  resultText;
    }


    /**
     * Clears input and returns cleared result
     * @return
     */
    public String calculationError(){
        clearNumber();
        return  resultText;
    }

    /**
     * Get new number value by adding new char to the end of the textNumber
     * @param lastChar - new char
     * @return - updated number
     */
    public String newResultText(char lastChar){
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

    public void setResultText(BigDecimal newResult){
        resultText = newResult.toString();
    }

    /**
     * Init accumulator and result with the values of the previous operation
     * @param newResult - previous amount value
     */
    public void initFotChangeOperation(BigDecimal newResult){
        resultText = newResult.toString();
        accumulator = newResult;
    }
}
