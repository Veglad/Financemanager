package com.example.vlad.financemanager;

public class SpinnerItem {
    private String accountName;
    private int image;

    public SpinnerItem(String accountName, int image){
        this.accountName = accountName;
        this.image = image;
    }

    public String getAccountName(){
        return accountName;
    }

    public int getImage(){
        return  image;
    }
}
