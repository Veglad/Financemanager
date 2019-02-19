package com.example.vlad.financemanager.data.models

import java.math.BigDecimal

data class User(var balance: BigDecimal?, var name: String?) {

    val id: Int = 0

    private val accounts = mutableListOf<Account>()

    private val userCategories = mutableListOf<Category>()

    fun getAccounts(): List<Account> {
        return accounts
    }

    fun addNewAccount(account: Account): Boolean {
        //Checking if we have a category with the same name
        val isUniqueAccount = accounts.all {
            it.id != account.id
        }

        if(isUniqueAccount) accounts.add(account)

        return isUniqueAccount
    }

    fun deleteAccount(accountId: Int): Boolean {
        val accountToDelete = accounts.filter {
            it.id == accountId
        }

        return if(accountToDelete.size == 1) {
            accounts.remove(accountToDelete[0])
            true
        } else {
            false
        }
    }

    fun addNewCustomCategory(category: Category): Boolean {
        val isUniqueCategory = userCategories.all {
            it.id != category.id
        }

        if(isUniqueCategory) userCategories.add(category)

        return isUniqueCategory
    }

    fun deleteCustomCategory(categoryId: Int): Boolean {
        val categoryToDelete = userCategories.filter {
            it.id == categoryId
        }

        return if(categoryToDelete.size == 1) {
            userCategories.remove(categoryToDelete[0])
            true
        } else {
            false
        }
    }

    fun getUserCategories(): List<Category>? {
        return userCategories
    }

}
