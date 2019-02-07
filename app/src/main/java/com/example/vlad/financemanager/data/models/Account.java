package com.example.vlad.financemanager.data.models;

import java.util.ArrayList;

public class Account {

    private int id;
    private int icon;
    ArrayList<Operation> operations;
    String name;

    public Account(ArrayList<Operation> operations, String name) {
        this.name = name;
        this.operations = operations;
    }

    public Account() {
        this(new ArrayList<Operation>(), "Unknown");
    }

    public Account(int id, String name, int icon) {
        this.id = id;
        operations = new ArrayList<>();
        this.name = name;
        this.icon = icon;

    }

    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getId() {
        return id;
    }

    /**
     * collection of the operation for current account
     */
    ArrayList<Operation> getOperations() {
        return operations;
    }

    public void setId(int id) {
        this.id = id;
    }
}
