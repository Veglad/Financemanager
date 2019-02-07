package com.example.vlad.financemanager.data.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class User {
    /**
     * users's id
     */
    private int id;
    /**
     * user's balance
     */
    private BigDecimal balance;
    /**
     * user's name
     */
    private String name;
    /**
     * users's accounts
     */
    private List<Account> accounts;
    /**
     * collection of the user's custom categories
     */
    private List<Category> userCategories;

    public User(BigDecimal balance, String name) {
        accounts = new ArrayList<>();
        this.balance = balance;
        this.name = name;
    }

    public User() {
        this(new BigDecimal(0), "Unknown");
    }

    public int getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Account> getAccaunts() {
        return accounts;
    }

    /**
     * Method - add new category to the user's custom category list
     *
     * @param account - new category
     * @return if we have a category with the same name
     */
    public boolean addNewAccount(Account account) {
        //Checking if we have a category with the same name
        for (Account acc : accounts) {
            if (account.getName().equals(account.getName()))
                return false;
        }

        accounts.add(account);

        return true;
    }

    /**
     * Method - Deletes specified category
     *
     * @param accountId - Category's id
     * @return success of the deleting
     */
    public boolean deleteAccount(int accountId) {
        for (Account account : accounts) {
            if (account.getId() == accountId) {
                accounts.remove(account);
                return true;
            }
        }

        return false;
    }

    /**
     * Method - add new category to the user's custom category list
     *
     * @param category - new category
     * @return if we have a category with the same name
     */
    public boolean addNewCustomCategory(Category category) {
        //Checking if we have a category with the same name
        for (Category categor : userCategories) {
            if (category.getName().equals(category.getName()))
                return false;
        }

        userCategories.add(category);
        //UPDATE DB!!!!!!!!!!!!!!!!!!

        return true;
    }

    /**
     * Method - Deletes specified category
     *
     * @param categoryId - Category's id
     * @return success of the deleting
     */
    public boolean deleteCustomCategory(int categoryId) {
        for (int i = 0; i < userCategories.size(); i++) {
            if (userCategories.get(i).getId() == categoryId) {
                userCategories.remove(i);
                //UPDATE DB!!!!!!!!!!!!!!!!!!
                return true;
            }
        }

        return false;
    }

    public List<Category> getUserCategories() {
        return userCategories;
    }

}
