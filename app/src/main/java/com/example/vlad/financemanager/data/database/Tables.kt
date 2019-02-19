package com.example.vlad.financemanager.data.database

object UserTable {
    const val TABLE_NAME = "usersList"
    const val COLUMN_NAME = "name"
    const val COLUMN_ID = "id"
    const val COLUMN_BALANCE = "balance"
}

object OperationTable {
    const val TABLE_NAME = "operationList"
    const val COLUMN_ID = "id"
    const val COLUMN_AMOUNT = "amount"
    const val COLUMN_IS_INCOME = "is_income"
    const val COLUMN_COMMENT = "comment"
    const val COLUMN_DATE = "date"
    const val COLUMN_CATEGORY_ID = "category_id"
    const val COLUMN_USER_ID = "user_id"
    const val COLUMN_TIME_STAMP = "time_stamp"
    const val COLUMN_ACCOUNT_ID = "account_id"
}

object CategoryTable {
    const val TABLE_NAME = "categoryList"
    const val COLUMN_NAME = "name"
    const val COLUMN_ID = "id"
    const val COLUMN_ICON = "icon"
    const val COLUMN_IS_CUSTOM = "is_custom"
    const val COLUMN_USER_ID = "user_id"
    const val COLUMN_IS_INPUT_CATEGORY = "is_input_category"
}

object AccountTable {
    const val TABLE_NAME = "accountList"
    const val COLUMN_NAME = "name"
    const val COLUMN_ID = "id"
    const val COLUMN_ICON = "icon"
    const val COLUMN_USER_ID = "user_id"
}