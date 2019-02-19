package com.example.vlad.financemanager.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.example.vlad.financemanager.data.models.Account
import com.example.vlad.financemanager.utils.DateUtils
import com.example.vlad.financemanager.data.models.Category
import com.example.vlad.financemanager.data.models.Operation
import com.example.vlad.financemanager.data.enums.PeriodsOfTime
import com.example.vlad.financemanager.R

import java.math.BigDecimal
import java.util.ArrayList
import java.util.Calendar
import java.util.Date

class DatabaseHelper private constructor(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {

        private const val DB_NAME = "finance_manager.db"
        private const val DB_VERSION = 1
        private const val USER_ID_PRIMARY = 0
        private const val DATABASE_DATE_PATTERN = "yyyy-MM-dd"

        private var databaseHelper: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            if (databaseHelper == null) {
                databaseHelper = DatabaseHelper(context)
            }

            return databaseHelper as DatabaseHelper
        }
    }

    val operationsCount: Int
        get() {
            val countQuery = "SELECT * FROM " + OperationTable.TABLE_NAME
            val db = this.readableDatabase
            val cursor = db.rawQuery(countQuery, null)

            val count = cursor.count
            cursor.close()

            return count
        }


    override fun onCreate(database: SQLiteDatabase) {
        val CREATE_USERS_TABLE = "CREATE TABLE " +
                UserTable.TABLE_NAME + " (" +
                UserTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserTable.COLUMN_NAME + " TEXT NOT NULL, " +
                UserTable.COLUMN_BALANCE + " TEXT NOT NULL" +
                ");"

        val CREATE_OPERATIONS_TABLE = "CREATE TABLE " +
                OperationTable.TABLE_NAME + " (" +
                OperationTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                OperationTable.COLUMN_AMOUNT + " TEXT NOT NULL, " +
                OperationTable.COLUMN_DATE + " TEXT NOT NULL, " +
                OperationTable.COLUMN_COMMENT + " TEXT NOT NULL, " +
                OperationTable.COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                OperationTable.COLUMN_IS_INCOME + " INTEGER NOT NULL, " +
                OperationTable.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                OperationTable.COLUMN_TIME_STAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                OperationTable.COLUMN_ACCOUNT_ID + " INTEGER NOT NULL" +
                ");"

        val CREATE_CATEGORY_TABLE = "CREATE TABLE " +
                CategoryTable.TABLE_NAME + " (" +
                CategoryTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CategoryTable.COLUMN_NAME + " TEXT NOT NULL, " +
                CategoryTable.COLUMN_ICON + " INTEGER NOT NULL, " +
                CategoryTable.COLUMN_IS_CUSTOM + " INTEGER NOT NULL, " +
                CategoryTable.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                CategoryTable.COLUMN_IS_INPUT_CATEGORY + " INTEGER NOT NULL" +
                ");"

        val CREATE_ACCOUNTS_TABLE = "CREATE TABLE " +
                AccountTable.TABLE_NAME + " (" +
                AccountTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                AccountTable.COLUMN_NAME + " TEXT NOT NULL, " +
                AccountTable.COLUMN_ICON + " INTEGER NOT NULL, " +
                AccountTable.COLUMN_USER_ID + " INTEGER NOT NULL" +
                ");"

        database.execSQL(CREATE_USERS_TABLE)
        database.execSQL(CREATE_OPERATIONS_TABLE)
        database.execSQL(CREATE_CATEGORY_TABLE)
        database.execSQL(CREATE_ACCOUNTS_TABLE)

        dbFirstInit(database, USER_ID_PRIMARY)
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val DROP_IF_EXISTS = "DROP TABLE IF EXISTS "

        database.execSQL(DROP_IF_EXISTS + UserTable.TABLE_NAME)
        database.execSQL(DROP_IF_EXISTS + OperationTable.TABLE_NAME)
        database.execSQL(DROP_IF_EXISTS + CategoryTable.TABLE_NAME)
        database.execSQL(DROP_IF_EXISTS + AccountTable.TABLE_NAME)

        onCreate(database)
    }

    //returns ID of inserted operation
    fun insertOperation(operation: Operation, userId: Int, accountId: Int): Long {
        val db = writableDatabase
        val dateString = getDateStringForDb(operation.operationDate)

        val cv = ContentValues()
        cv.put(OperationTable.COLUMN_AMOUNT, operation.amount.toString())
        cv.put(OperationTable.COLUMN_IS_INCOME, if (operation.isOperationIncome) 1 else 0)
        cv.put(OperationTable.COLUMN_COMMENT, operation.comment)
        cv.put(OperationTable.COLUMN_DATE, dateString)
        cv.put(OperationTable.COLUMN_CATEGORY_ID, operation.category.id)
        cv.put(OperationTable.COLUMN_USER_ID, userId)
        cv.put(OperationTable.COLUMN_ACCOUNT_ID, accountId)

        return db.insertOrThrow(OperationTable.TABLE_NAME, null, cv)
    }

    fun insertCategory(name: String, icon: Int, isCustom: Boolean, isInputCategory: Boolean, userId: Int, db: SQLiteDatabase?): Long {
        var db = db
        if (db == null)
            db = writableDatabase

        val cv = ContentValues()
        cv.put(CategoryTable.COLUMN_ICON, icon)
        cv.put(CategoryTable.COLUMN_IS_CUSTOM, isCustom)
        cv.put(CategoryTable.COLUMN_IS_INPUT_CATEGORY, isInputCategory)
        cv.put(CategoryTable.COLUMN_USER_ID, userId)
        cv.put(CategoryTable.COLUMN_NAME, name)

        return db!!.insert(CategoryTable.TABLE_NAME, null, cv)
    }

    fun insertAccount(name: String, icon: Int, userId: Int, db: SQLiteDatabase?): Long {
        var db = db
        if (db == null)
            db = writableDatabase

        val cv = ContentValues()
        cv.put(AccountTable.COLUMN_ICON, icon)
        cv.put(AccountTable.COLUMN_NAME, name)
        cv.put(AccountTable.COLUMN_USER_ID, userId)

        return db!!.insert(AccountTable.TABLE_NAME, null, cv)
    }


    fun getOperation(operationId: Long, userId: Int): Operation? {
        val db = readableDatabase

        val operationCursor = db.query(OperationTable.TABLE_NAME, null,
                OperationTable.COLUMN_ID + " = ?" + " AND " + OperationTable.COLUMN_USER_ID + " = ?",
                arrayOf(operationId.toString(), userId.toString()), null, null, null, null)

        var category: Category?
        if (operationCursor.moveToFirst()) {
            val categoryId = operationCursor.getInt(operationCursor.getColumnIndex(OperationTable.COLUMN_CATEGORY_ID))
            category = getCategory(categoryId)
            if (category == null) return null
        } else return null

        val dateString = operationCursor.getString(operationCursor.getColumnIndex(OperationTable.COLUMN_DATE))
        val operationDate = convertStringFromDbtoDate(dateString)

        val operation = getOperationFromCursor(operationCursor, category, operationDate)
        operationCursor.close()

        return operation
    }

    fun getMinOperationDate(userId: Int): Date? {//TODO: Optimize this
        val db = readableDatabase

        val dateCursor = db.rawQuery("SELECT MIN(" + OperationTable.COLUMN_DATE + ") FROM "
                + OperationTable.TABLE_NAME + " WHERE " + OperationTable.COLUMN_USER_ID + " = ?;", arrayOf(userId.toString() + ""))

        var date = Date()
        if (dateCursor.columnCount > 0 && dateCursor.moveToFirst()) {
            val dateString = dateCursor.getString(0) ?: return null
            date = convertStringFromDbtoDate(dateString)
        }
        return date
    }

    fun getMinOperationDateId(userId: Int): Int {//TODO: Optimize this
        val db = readableDatabase

        val dateCursor = db.rawQuery("SELECT " + OperationTable.COLUMN_ID + ", MIN(" + OperationTable.COLUMN_DATE + ") FROM "
                + OperationTable.TABLE_NAME + " WHERE " + OperationTable.COLUMN_USER_ID + " = ?;", arrayOf(userId.toString() + ""))
        var id = -1
        if (dateCursor.columnCount > 0) {
            if (dateCursor.moveToFirst()) {
                id = dateCursor.getInt(0)
            }
        }

        return id
    }

    private fun getCategoryFromCursor(cursorCategory: Cursor): Category {
        val category = Category(
                cursorCategory.getInt(cursorCategory.getColumnIndex(CategoryTable.COLUMN_ID)),
                cursorCategory.getString(cursorCategory.getColumnIndex(CategoryTable.COLUMN_NAME)),
                cursorCategory.getInt(cursorCategory.getColumnIndex(CategoryTable.COLUMN_ICON)),
                cursorCategory.getInt(cursorCategory.getColumnIndex(CategoryTable.COLUMN_IS_CUSTOM)) > 0,
                cursorCategory.getInt(cursorCategory.getColumnIndex(CategoryTable.COLUMN_IS_INPUT_CATEGORY)) > 0
        )
        category.id = cursorCategory.getInt(cursorCategory.getColumnIndex(CategoryTable.COLUMN_ID))
        return category
    }

    private fun getOperationFromCursor(cursorOperations: Cursor, category: Category, operationDate: Date): Operation {
        return Operation(BigDecimal(cursorOperations.getString(cursorOperations.getColumnIndex(OperationTable.COLUMN_AMOUNT))),
                operationDate,
                cursorOperations.getString(cursorOperations.getColumnIndex(OperationTable.COLUMN_COMMENT)),
                cursorOperations.getInt(cursorOperations.getColumnIndex(OperationTable.COLUMN_IS_INCOME)) > 0,
                category,
                cursorOperations.getInt(cursorOperations.getColumnIndex(OperationTable.COLUMN_ID)),
                cursorOperations.getInt(cursorOperations.getColumnIndex(OperationTable.COLUMN_ACCOUNT_ID)))
    }


    fun getAllCategories(userId: Int, isIncome: Boolean): MutableList<Category> {
        val categoryList = ArrayList<Category>()
        val db = readableDatabase

        val categoriesCursor = db.query(CategoryTable.TABLE_NAME, null,
                CategoryTable.COLUMN_USER_ID + " = ?" + " AND " + CategoryTable.COLUMN_IS_INPUT_CATEGORY + " = ?",
                arrayOf(userId.toString(), (if (isIncome) 1 else 0).toString()), null, null, null, null)

        if (categoriesCursor.moveToFirst()) {
            do {
                val category = getCategoryFromCursor(categoriesCursor)
                categoryList.add(category)
            } while (categoriesCursor.moveToNext())
        }
        return categoryList
    }

    fun getAllAccounts(userId: Int): MutableList<Account> {
        val accountList = ArrayList<Account>()
        val db = readableDatabase

        val cursor = db.query(AccountTable.TABLE_NAME, null, AccountTable.COLUMN_USER_ID + " = ?",
                arrayOf(userId.toString()), null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val account = getAccountFormCursor(cursor)
                accountList.add(account)
            } while (cursor.moveToNext())
        }
        return accountList
    }

    fun getAccount(id: Int): Account? {
        val db = readableDatabase
        var account: Account? = null

        val accountCursor = db.query(AccountTable.TABLE_NAME, null, AccountTable.COLUMN_ID + " = ?",
                arrayOf(id.toString()), null, null, null, null)

        if (accountCursor.moveToFirst()) {
            account = getAccountFormCursor(accountCursor)
        }
        return account
    }

    private fun getAccountFormCursor(accountCursor: Cursor): Account {
        val account: Account = Account(accountCursor.getInt(accountCursor.getColumnIndex(AccountTable.COLUMN_ID)),
                accountCursor.getString(accountCursor.getColumnIndex(AccountTable.COLUMN_NAME)),
                accountCursor.getInt(accountCursor.getColumnIndex(AccountTable.COLUMN_ICON)))
        return account
    }

    fun getCategory(categoryId: Int): Category? {
        val db = readableDatabase
        var category: Category

        val cursorCategory = db.query(CategoryTable.TABLE_NAME, null, CategoryTable.COLUMN_ID + " = ?",
                arrayOf(categoryId.toString()), null, null, null, null)

        if (cursorCategory.moveToFirst()) {
            category = getCategoryFromCursor(cursorCategory)
        } else {
            return null
        }
        return category
    }


    // If accountId < 0 method selects all accounts
    fun getOperations(accountId: Int?, period: PeriodsOfTime, currDay: Calendar): MutableList<Operation>? {
        val operationsList = ArrayList<Operation>()
        val db = readableDatabase
        val operationsCursor = getOperationCursor(accountId!!, DateUtils.getEndOfPeriod(currDay, period), db, period)

        //Looping through the operations cursor, init category and operation, add operation to list
        if (operationsCursor.moveToFirst()) {
            do {
                val categoryId = operationsCursor.getInt(operationsCursor.getColumnIndex(OperationTable.COLUMN_CATEGORY_ID))
                val category = getCategory(categoryId) ?: return null

                val dateString = operationsCursor.getString(operationsCursor.getColumnIndex(OperationTable.COLUMN_DATE))
                val operationDate = convertStringFromDbtoDate(dateString)

                val operation = getOperationFromCursor(operationsCursor, category, operationDate)
                operationsList.add(operation)
            } while (operationsCursor.moveToNext())
        }

        return operationsList
    }

    private fun getOperationCursor(accountId: Int, endOfPeriod: Calendar, db: SQLiteDatabase, period: PeriodsOfTime): Cursor {
        val endOfPeriodDateString = getDateStringForDb(endOfPeriod.time)

        var selectionArgs: Array<String>? = null
        var selectionString: String? = null

        when (period) {
            PeriodsOfTime.ALL_TIME -> if (accountId > 0) {
                selectionArgs = arrayOf(accountId.toString() + "")
                selectionString = OperationTable.COLUMN_ACCOUNT_ID + " = ?"
            }
            PeriodsOfTime.DAY -> if (accountId < 0) {
                selectionArgs = arrayOf(endOfPeriodDateString)
                selectionString = OperationTable.COLUMN_DATE + " = ?"
            } else {
                selectionArgs = arrayOf(accountId.toString() + "", endOfPeriodDateString)
                selectionString = OperationTable.COLUMN_ACCOUNT_ID + " = ?" + " AND " + OperationTable.COLUMN_DATE + " = ?"
            }
            else -> {
                val startDate = DateUtils.getStartOfPeriod(endOfPeriod, period)

                if (accountId < 0) {
                    selectionString = OperationTable.COLUMN_DATE + " BETWEEN " + "?" + " AND " + "?"
                    val startOfPeriodDateString = getDateStringForDb(startDate.time)
                    selectionArgs = arrayOf(startOfPeriodDateString, endOfPeriodDateString)
                } else {
                    selectionString = OperationTable.COLUMN_ACCOUNT_ID + " = ?" + " AND " +
                            OperationTable.COLUMN_DATE + " BETWEEN " + "?" + " AND " + "?"
                    val startOfPeriodDateString = getDateStringForDb(startDate.time)
                    selectionArgs = arrayOf(accountId.toString() + "", startOfPeriodDateString, endOfPeriodDateString)
                }
            }
        }

        return db.query(OperationTable.TABLE_NAME, null, selectionString,
                selectionArgs, null, null, null, null)
    }

    fun updateCategory(category: Category, userId: Int): Int {
        val db = writableDatabase

        val cv = ContentValues()
        cv.put(CategoryTable.COLUMN_NAME, category.name)
        cv.put(CategoryTable.COLUMN_ICON, category.icon)
        cv.put(CategoryTable.COLUMN_USER_ID, userId)
        cv.put(CategoryTable.COLUMN_IS_CUSTOM, category.isCustom)
        cv.put(CategoryTable.COLUMN_IS_INPUT_CATEGORY, category.isInputCategory)


        return db.update(OperationTable.TABLE_NAME, cv, CategoryTable.COLUMN_ID + " = ?",
                arrayOf(category.id.toString()))
    }

    fun updateAccount(account: Account, userId: Int): Int {
        val db = writableDatabase

        val cv = ContentValues()
        cv.put(AccountTable.COLUMN_NAME, account.name)
        cv.put(AccountTable.COLUMN_ICON, account.icon)
        cv.put(AccountTable.COLUMN_USER_ID, userId)


        return db.update(OperationTable.TABLE_NAME, cv, CategoryTable.COLUMN_ID + " = ?",
                arrayOf(account.id.toString()))
    }

    fun updateOperation(operation: Operation, userId: Int, accountId: Int): Int {
        val db = writableDatabase

        val dateString = getDateStringForDb(operation.operationDate)

        val cv = ContentValues()
        cv.put(OperationTable.COLUMN_AMOUNT, operation.amount.toString())
        cv.put(OperationTable.COLUMN_IS_INCOME, if (operation.isOperationIncome) 1 else 0)
        cv.put(OperationTable.COLUMN_COMMENT, operation.comment)
        cv.put(OperationTable.COLUMN_DATE, dateString)
        cv.put(OperationTable.COLUMN_CATEGORY_ID, operation.category.id)
        cv.put(OperationTable.COLUMN_USER_ID, userId)
        cv.put(OperationTable.COLUMN_ACCOUNT_ID, accountId)


        return db.update(OperationTable.TABLE_NAME, cv, OperationTable.COLUMN_ID + " = ?",
                arrayOf(operation.id.toString()))
    }

    //Save date in DB in the string format
    private fun getDateStringForDb(date: Date): String {
        return DateUtils.getStringDate(date, DATABASE_DATE_PATTERN)
    }

    private fun convertStringFromDbtoDate(dbDateString: String): Date {
        return DateUtils.getDateFromString(dbDateString, DATABASE_DATE_PATTERN)
    }

    fun deleteOperation(operation: Operation) {
        val db = writableDatabase
        db.delete(OperationTable.TABLE_NAME, OperationTable.COLUMN_ID + " = ?",
                arrayOf(operation.id.toString()))

    }

    fun deleteAccount(account: Account) {
        val db = writableDatabase
        db.delete(AccountTable.TABLE_NAME, AccountTable.COLUMN_ID + " = ?",
                arrayOf(account.id.toString()))

    }

    fun deleteCategory(category: Category) {
        val db = writableDatabase
        db.delete(CategoryTable.TABLE_NAME, CategoryTable.COLUMN_ID + " = ?",
                arrayOf(category.id.toString()))

    }

    private fun dbFirstInit(db: SQLiteDatabase, userId: Int) {
        //ResourcesCompat.getDrawable(context.getResources(), R.drawable.credit_card, null)
        insertCategory("Transport", R.drawable.transport, false, false, userId, db)
        insertCategory("Food", R.drawable.food, false, false, userId, db)
        insertCategory("Cafe", R.drawable.cafe, false, false, userId, db)
        insertCategory("House", R.drawable.house, false, false, userId, db)
        insertCategory("Car", R.drawable.car, false, false, userId, db)
        insertCategory("Education", R.drawable.education, false, false, userId, db)
        insertCategory("Sport", R.drawable.sport, false, false, userId, db)
        insertCategory("Clothes", R.drawable.clothes, false, false, userId, db)
        insertCategory("Present", R.drawable.presents, false, false, userId, db)
        insertCategory("Health", R.drawable.health, false, false, userId, db)
        insertCategory("Entertainment", R.drawable.entertainment, false, false, userId, db)

        insertCategory("Salary", R.drawable.salary, false, true, userId, db)
        insertCategory("Present", R.drawable.presents, false, true, userId, db)
        insertCategory("Savings", R.drawable.savings, false, true, userId, db)

        insertAccount("Cash", R.drawable.money, userId, db)
        insertAccount("Credit card", R.drawable.credit_card, userId, db)
        insertAccount("Salary", R.drawable.salary, userId, db)
    }
}
