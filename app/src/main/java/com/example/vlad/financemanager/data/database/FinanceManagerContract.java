package com.example.vlad.financemanager.data.database;

import android.provider.BaseColumns;

interface FinanceManagerContract {

    String DATABASE_DATE_PATTERN = "yyyy-MM-dd";

    interface Users extends BaseColumns {
        String TABLE_NAME = "usersList";
        String COLUMN_NAME = "name";
        String COLUMN_ID = "id";
        String COLUMN_BALANCE = "balance";
    }

    interface Operations extends BaseColumns {
        String TABLE_NAME = "operationList";
        String COLUMN_ID = "id";
        String COLUMN_AMOUNT = "amount";
        String COLUMN_IS_INCOME = "is_income";
        String COLUMN_COMMENT = "comment";
        String COLUMN_DATE = "date";
        String COLUMN_CATEGORY_ID = "category_id";
        String COLUMN_USER_ID = "user_id";
        String COLUMN_TIME_STAMP = "time_stamp";
        String COLUMN_ACCOUNT_ID = "account_id";
    }

    interface Categories extends BaseColumns {
        String TABLE_NAME = "categoryList";
        String COLUMN_NAME = "name";
        String COLUMN_ID = "id";
        String COLUMN_ICON = "icon";
        String COLUMN_IS_CUSTOM = "is_custom";
        String COLUMN_USER_ID = "user_id";
        String COLUMN_IS_INPUT_CATEGORY = "is_input_category";
    }

    interface Accounts extends BaseColumns {
        String TABLE_NAME = "accountList";
        String COLUMN_NAME = "name";
        String COLUMN_ID = "id";
        String COLUMN_ICON = "icon";
        String COLUMN_USER_ID = "user_id";
    }
}
