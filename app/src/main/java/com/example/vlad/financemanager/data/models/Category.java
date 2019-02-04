package com.example.vlad.financemanager.data.models;

import java.io.Serializable;

public class Category implements Serializable{
    private int id;
    private String name;
    private int icon;
    private boolean isCustom;
    private boolean isInputCategory;

    public Category(int id, String name, int icon, boolean isCustom, boolean isInputCategory){
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.isCustom = isCustom;
        this.isInputCategory = isInputCategory;
    }

    public Category(){
        this(0, "Unknown", 0, true, false);
    }

    public boolean getIsCustom(){return  isCustom;}
    public boolean getIsInputCategory(){return isInputCategory;}

    public int getId(){
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws IllegalArgumentException{
        if(name != "")
            this.name = name;
        else
            throw new IllegalArgumentException();
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setId(int id){
        this.id = id;
    }
}
