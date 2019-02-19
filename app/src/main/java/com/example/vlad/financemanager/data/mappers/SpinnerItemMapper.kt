package com.example.vlad.financemanager.data.mappers

import com.example.vlad.financemanager.data.models.Account
import com.example.vlad.financemanager.data.models.Category
import com.example.vlad.financemanager.data.models.SpinnerItem

import java.util.ArrayList

class SpinnerItemMapper {

    companion object {
        fun mapAccountToSpinnerItem(account: Account): SpinnerItem {
            return SpinnerItem(account.id, account.name, account.icon)
        }

        fun mapAccountsToSpinnerItems(accountList: List<Account>): List<SpinnerItem> {
            val spinnerItemList = ArrayList<SpinnerItem>()

            for (account in accountList) {
                spinnerItemList.add(SpinnerItem(account.id, account.name, account.icon))
            }

            return spinnerItemList
        }

        fun mapCategoryToSpinnerItem(category: Category): SpinnerItem {
            return SpinnerItem(category.id, category.name, category.icon)
        }

        fun mapCategoryToSpinnerItems(categoryList: List<Category>): List<SpinnerItem> {
            val spinnerItemList = ArrayList<SpinnerItem>()

            for (category in categoryList) {
                spinnerItemList.add(SpinnerItem(category.id, category.name, category.icon))
            }

            return spinnerItemList
        }
    }
}
