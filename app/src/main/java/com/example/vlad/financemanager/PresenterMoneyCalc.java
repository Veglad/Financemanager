package com.example.vlad.financemanager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import com.example.vlad.financemanager.FinanceManagerContract.*;

import java.math.BigDecimal;

public class PresenterMoneyCalc {


    private IMoneyCalculation view;
    private ModelMoneyCalc model;

    public PresenterMoneyCalc(IMoneyCalculation view, Context context){
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
            case R.id.btnBack:
                view.setCalcResultText(model.clearLast());
                break;
            case R.id.btnAdd:
                if(!model.mathOperBtnClick(CalcOperations.Add)){
                    model.clearNumber();
                    view.calculationErrorSignal();
                }
                view.setCalcResultText(model.getResulText());
                break;
            case R.id.btnSubstract:
                if(!model.mathOperBtnClick(CalcOperations.Sub)){
                    model.clearNumber();
                    view.calculationErrorSignal();
                }
                view.setCalcResultText(model.getResulText());
                break;
            case R.id.btnMul:
                if(!model.mathOperBtnClick(CalcOperations.Mul)){
                    model.clearNumber();
                    view.calculationErrorSignal();
                }
                view.setCalcResultText(model.getResulText());
                break;
            case R.id.btnDiv:
                if(!model.mathOperBtnClick(CalcOperations.Div)){
                    model.clearNumber();
                    view.calculationErrorSignal();
                }
                view.setCalcResultText(model.getResulText());
                break;
            case R.id.btnEquals:
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

        Operation operation = new Operation(0, view.getAccountId(),new BigDecimal(model.getResulText()), view.getOperationDate(),
                view.getComment().trim(), view.getIsOperationInput(), category);

        view.sendNewOperation(operation);

        view.finishActivity();
    }

    public void settingResultText(BigDecimal result){
        model.initFotChangeOperation(result);
    }
}
