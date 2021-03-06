package com.example.vlad.financemanager;

import android.provider.BaseColumns;

public class FinanceManagerContract {

    public FinanceManagerContract(){}

    public static final class Users implements BaseColumns{
        public static final String TABLE_NAME = "usersList";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_BALANCE = "balance";
    }

    public static final class Operations implements BaseColumns{
        public static final String TABLE_NAME = "operationList";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_CURRENCY_ID = "currency_ID";
        public static final String COLUMN_ISINCOME = "is_income";
        public static final String COLUMN_COMMENT = "comment";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_CATEGORY_ID = "category_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_TIME_STAMP= "time_stamp";
        public static final String COLUMN_ACCOUNT_ID= "account_id";
    }

    public static final class Categories implements BaseColumns{
        public static final String TABLE_NAME = "categoryList";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_IS_CUSTOM = "is_custom";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_IS_INPUT_CATEGORY = "is_input_category";
    }

    public static final class Accounts implements BaseColumns{
        public static final String TABLE_NAME = "accountList";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_ICON = "icon";
        public static final String COLUMN_USER_ID = "user_id";
    }
}
