package com.example.vlad.financemanager.ui;

import com.example.vlad.financemanager.data.models.Operation;

import java.util.Date;

public interface IMoneyCalculation {

    int getCategoryId();

    int getAccountId();

    Date getOperationDate();

    String getComment();

    String getAmount();

    boolean getIsOperationInput();

    void setAmountResultText(String resultText);

    void setCalculatorToZero();

    void finishActivity();

    void calculationErrorSignal();

    void calculationErrorSignal(String msg);

    void sendNewOperation(Operation operation);


}
