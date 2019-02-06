package com.example.vlad.financemanager;

import com.example.vlad.financemanager.data.enums.CalcOperations;
import com.example.vlad.financemanager.data.models.Category;
import com.example.vlad.financemanager.data.models.Operation;
import com.example.vlad.financemanager.ui.IMoneyCalculation;

import java.math.BigDecimal;

public class PresenterMoneyCalc {


    private IMoneyCalculation view;
    private ModelMoneyCalc model;
    private int modifyingOperationId;

    public PresenterMoneyCalc(IMoneyCalculation view){
        this.view = view;
        model = new ModelMoneyCalc();
    }

    /**
     * Method - updates the result int the calculator if the result is correct
     * @param btnId Clicked button
     * @param numberValue Text value of this button (0,1,2..9, .)
     */
    public void calculatorBtnOnClick(int btnId, String numberValue){
        switch (btnId){
            case R.id.calculatorBackButton:
                view.setCalcResultText(model.clearLast());
                break;
            case R.id.aditionCalculatorButton:
                if(!model.mathOperBtnClick(CalcOperations.ADD)){
                    model.clearNumber();
                    view.calculationErrorSignal();
                }
                view.setCalcResultText(model.getResulText());
                break;
            case R.id.substractionCalculatorButton:
                if(!model.mathOperBtnClick(CalcOperations.SUB)){
                    model.clearNumber();
                    view.calculationErrorSignal();
                }
                view.setCalcResultText(model.getResulText());
                break;
            case R.id.multiplicationCalculatorButton:
                if(!model.mathOperBtnClick(CalcOperations.MUL)){
                    model.clearNumber();
                    view.calculationErrorSignal();
                }
                view.setCalcResultText(model.getResulText());
                break;
            case R.id.divisionCalculatorButton:
                if(!model.mathOperBtnClick(CalcOperations.DIV)){
                    model.clearNumber();
                    view.calculationErrorSignal();
                }
                view.setCalcResultText(model.getResulText());
                break;
            case R.id.equalsCalculatorButton:
                if(model.calculatePress())
                    view.setCalcResultText(model.getResulText());
                else{
                    view.setCalcResultText(model.calculationError());
                    view.calculationErrorSignal();
                }
                break;
            default:
                if(model.isNewResultCorrect(numberValue.charAt(0)))
                    view.setCalcResultText(model.newResultText(numberValue.charAt(0)));
                break;

        }

    }


    /** Clears number, reset calculator */
    public void clearNumber(){
        view.setCalcResultText(model.clearNumber());
    }

    /** Saving user's data as a new operation */
    public void btnSaveOnClick(){

        //////////////////////
        Category category = new Category();
        category.setId(view.getCategoryId());

        if(new BigDecimal(model.getResulText()).compareTo( model.zeroBDvalue)==0){
            view.calculationErrorSignal("Set the amount, please");
            return;
        }

        Operation operation = new Operation(modifyingOperationId, view.getAccountId(),new BigDecimal(model.getResulText()), view.getOperationDate(),
                view.getComment().trim(), view.getIsOperationInput(), category);

        view.sendNewOperation(operation);

        view.finishActivity();
    }

    public void settingResultText(BigDecimal result){
        model.initFotChangeOperation(result);
    }

    public void setModifyingOperationId(int modifyingOperationId) {
        this.modifyingOperationId = modifyingOperationId;
    }
}
