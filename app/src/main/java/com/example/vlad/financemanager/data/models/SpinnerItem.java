package com.example.vlad.financemanager.data.models;

import java.io.Serializable;

public class SpinnerItem implements Serializable {
    public static final int POSITION_DAY = 0;
    public static final int POSITION_WEEK = 1;
    public static final int POSITION_MONTH = 2;
    public static final int POSITION_YEAR = 3;

    private String name;
    private int image;
    private int id;

    public SpinnerItem(int id, String accountName, int image) {
        this.name = accountName;
        this.image = image;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getImage() {
        return image;
    }

    public int getId() {
        return id;
    }
}
