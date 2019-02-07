package com.example.vlad.financemanager;

import android.support.annotation.IdRes;

import com.example.vlad.financemanager.data.enums.CalculatorOperations;
import com.example.vlad.financemanager.data.models.Category;
import com.example.vlad.financemanager.data.models.Operation;
import com.example.vlad.financemanager.ui.IMoneyCalculation;

import java.math.BigDecimal;

public class PresenterMoneyCalculator {


    private IMoneyCalculation view;
    private ModelMoneyCalculator model;
    private int modifyingOperationId;

    public PresenterMoneyCalculator(IMoneyCalculation view) {
        this.view = view;
        model = new ModelMoneyCalculator();
    }

    /**
     * Method - updates the result int the calculator if the result is correct
     *
     * @param btnId       Clicked button
     * @param numberValue Text value of this button (0,1,2..9, .)
     */
    public void calculatorBtnOnClick(@IdRes int btnId, String numberValue) {
        switch (btnId) {
            case R.id.calculatorBackButton:
                view.setCalcResultText(model.clearLastSymbol());
                break;
            case R.id.additionCalculatorButton:
                performOperation(CalculatorOperations.ADD);
                view.setCalcResultText(model.getResultText());
                break;
            case R.id.substractionCalculatorButton:
                performOperation(CalculatorOperations.SUB);
                view.setCalcResultText(model.getResultText());
                break;
            case R.id.multiplicationCalculatorButton:
                performOperation(CalculatorOperations.MUL);
                view.setCalcResultText(model.getResultText());
                break;
            case R.id.divisionCalculatorButton:
                performOperation(CalculatorOperations.DIV);
                view.setCalcResultText(model.getResultText());
                break;
            case R.id.equalsCalculatorButton:
                if (model.calculatePress())
                    view.setCalcResultText(model.getResultText());
                else {
                    view.setCalcResultText(model.calculationError());
                    view.calculationErrorSignal();
                }
                break;
            default:
                if (model.isNewResultCorrect(numberValue.charAt(0)))
                    view.setCalcResultText(model.newResultText(numberValue.charAt(0)));
                break;
        }

    }

    private void performOperation(CalculatorOperations add) {
        if (!model.mathOperationBtnClick(add)) {
            model.clearNumber();
            view.calculationErrorSignal();
        }
    }

    /**
     * Clears number, reset calculator
     */
    public void clearNumber() {
        view.setCalcResultText(model.clearNumber());
    }

    /**
     * Saving user's data as a new operation
     */
    public void onButtonSaveClick() {
        Category category = new Category();
        category.setId(view.getCategoryId());

        if (new BigDecimal(model.getResultText()).compareTo(model.zeroBigDecimalValue) == 0) {
            view.calculationErrorSignal("Set the amount, please");
            return;
        }

        Operation operation = new Operation();
        operation.initOperation(modifyingOperationId, view.getAccountId(), new BigDecimal(model.getResultText()), view.getOperationDate(),
                view.getComment().trim(), view.getIsOperationInput(), category);

        view.sendNewOperation(operation);

        view.finishActivity();
    }

    public void settingResultText(BigDecimal result) {
        model.initFotChangeOperation(result);
    }

    public void setModifyingOperationId(int modifyingOperationId) {
        this.modifyingOperationId = modifyingOperationId;
    }
}
