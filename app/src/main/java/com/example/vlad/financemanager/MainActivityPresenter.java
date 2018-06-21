package com.example.vlad.financemanager;

import android.content.Context;

public class MainActivityPresenter {

    private IMainActivity view;
    private MainModel model;

    public MainActivityPresenter (IMainActivity view, Context context){
        this.view = view;
        model = new MainModel();
    }

}
