package com.example.vlad.financemanager.data.mappers;

import com.example.vlad.financemanager.data.models.Account;
import com.example.vlad.financemanager.data.models.Category;
import com.example.vlad.financemanager.data.models.SpinnerItem;

import java.util.ArrayList;
import java.util.List;

public final class SpinnerItemMapper {

    private SpinnerItemMapper() {
    }

    public static SpinnerItem mapAccountToSpinnerItem(Account account) {
        return new SpinnerItem(account.getId(), account.getName(), account.getIcon());
    }

    public static List<SpinnerItem> mapAccountsToSpinnerItems(List<Account> accountList) {
        List<SpinnerItem> spinnerItemList = new ArrayList<>();

        for (Account account : accountList) {
            spinnerItemList.add(new SpinnerItem(account.getId(), account.getName(), account.getIcon()));
        }

        return spinnerItemList;
    }

    public static SpinnerItem mapCategoryToSpinnerItem(Category category) {
        return new SpinnerItem(category.getId(), category.getName(), category.getIcon());
    }

    public static List<SpinnerItem> mapCategoryToSpinnerItems(List<Category> categoryList) {
        List<SpinnerItem> spinnerItemList = new ArrayList<>();

        for (Category category : categoryList) {
            spinnerItemList.add(new SpinnerItem(category.getId(), category.getName(), category.getIcon()));
        }

        return spinnerItemList;
    }
}
