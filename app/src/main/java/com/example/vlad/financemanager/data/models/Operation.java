package com.example.vlad.financemanager.data.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Operation implements Serializable {

    private transient BigDecimal amount;
    private Date operationDate;
    private String comment;
    private boolean isOperationIncome;
    private Category category;
    private int id;
    private int accountId;

    public Operation(int id, int accountId, BigDecimal amount, Date operationDate, String comment,
                     boolean isOperationIncome, Category category) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.operationDate = operationDate;
        this.comment = comment;
        this.isOperationIncome = isOperationIncome;
        this.category = category;
    }

    public Operation() {
        this(0, 0, new BigDecimal(0), new Date(), "NoComment", false, new Category());
    }

    public int getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean getIsOperationIncome() {
        return isOperationIncome;
    }

    public void setIsOperationIncome(boolean isOperationIncome) {
        this.isOperationIncome = isOperationIncome;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(Date operationDate) {
        this.operationDate = operationDate;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccountId() {
        return accountId;
    }
}
