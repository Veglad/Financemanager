package com.example.vlad.financemanager;

import java.util.Date;

public interface IMoneyCalculation {

    int getCategoryId();
    int getAccountId();
    Date getOperationDate();
    String getComment();
    boolean getIsOperationInput();
    void setCalcResultText(String resultText);
    void finishActivity();
    void calculationErrorSignal();
    void calculationErrorSignal(String msg);
    void sendNewOperation(Operation operation);


}
