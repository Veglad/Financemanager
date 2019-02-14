package com.example.vlad.financemanager.data.models

import java.math.BigDecimal
import java.util.ArrayList

class User(var balance: BigDecimal?, var name: String?) {

    val id: Int = 0

    private val accounts: MutableList<Account>

    private val userCategories: MutableList<Category>? = null

    init {
        accounts = ArrayList()
    }

    fun getAccounts(): List<Account> {
        return accounts
    }


    fun addNewAccount(account: Account): Boolean {
        //Checking if we have a category with the same name
        for (acc in accounts) {
            if (account.name == account.name)
                return false
        }

        accounts.add(account)

        return true
    }

    fun deleteAccount(accountId: Int): Boolean {
        for (account in accounts) {
            if (account.id == accountId) {
                accounts.remove(account)
                return true
            }
        }

        return false
    }

    fun addNewCustomCategory(category: Category): Boolean {
        //Checking if we have a category with the same name
        for (categor in userCategories!!) {
            if (category.name == category.name)
                return false
        }

        userCategories.add(category)
        //UPDATE DB!!!!!!!!!!!!!!!!!!

        return true
    }

    fun deleteCustomCategory(categoryId: Int): Boolean {
        for (i in userCategories!!.indices) {
            if (userCategories[i].id == categoryId) {
                userCategories.removeAt(i)
                //UPDATE DB!!!!!!!!!!!!!!!!!!
                return true
            }
        }

        return false
    }

    fun getUserCategories(): List<Category>? {
        return userCategories
    }

}
