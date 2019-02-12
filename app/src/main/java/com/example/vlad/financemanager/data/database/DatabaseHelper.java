package com.example.vlad.financemanager.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.vlad.financemanager.data.models.Account;
import com.example.vlad.financemanager.utils.DateUtils;
import com.example.vlad.financemanager.data.models.Category;
import com.example.vlad.financemanager.data.models.Operation;
import com.example.vlad.financemanager.data.enums.PeriodsOfTime;
import com.example.vlad.financemanager.R;
import com.example.vlad.financemanager.data.database.FinanceManagerContract.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "finance_manager.db";
    private static final int DB_VERSION = 1;
    private static final int USER_ID_PRIMARY = 0;
    private static DatabaseHelper databaseHelper;

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(context);
        }

        return databaseHelper;
    }


    @Override
    public void onCreate(SQLiteDatabase database) {
        final String CREATE_USERS_TABLE = "CREATE TABLE " +
                Users.TABLE_NAME + " (" +
                Users.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Users.COLUMN_NAME + " TEXT NOT NULL, " +
                Users.COLUMN_BALANCE + " TEXT NOT NULL" +
                ");";

        final String CREATE_OPERATIONS_TABLE = "CREATE TABLE " +
                Operations.TABLE_NAME + " (" +
                Operations.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Operations.COLUMN_AMOUNT + " TEXT NOT NULL, " +
                Operations.COLUMN_DATE + " TEXT NOT NULL, " +
                Operations.COLUMN_COMMENT + " TEXT NOT NULL, " +
                Operations.COLUMN_CATEGORY_ID + " INTEGER NOT NULL, " +
                Operations.COLUMN_IS_INCOME + " INTEGER NOT NULL, " +
                Operations.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                Operations.COLUMN_TIME_STAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                Operations.COLUMN_ACCOUNT_ID + " INTEGER NOT NULL" +
                ");";

        final String CREATE_CATEGORY_TABLE = "CREATE TABLE " +
                Categories.TABLE_NAME + " (" +
                Categories.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Categories.COLUMN_NAME + " TEXT NOT NULL, " +
                Categories.COLUMN_ICON + " INTEGER NOT NULL, " +
                Categories.COLUMN_IS_CUSTOM + " INTEGER NOT NULL, " +
                Categories.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                Categories.COLUMN_IS_INPUT_CATEGORY + " INTEGER NOT NULL" +
                ");";

        final String CREATE_ACCOUNTS_TABLE = "CREATE TABLE " +
                Accounts.TABLE_NAME + " (" +
                Accounts.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Accounts.COLUMN_NAME + " TEXT NOT NULL, " +
                Accounts.COLUMN_ICON + " INTEGER NOT NULL, " +
                Accounts.COLUMN_USER_ID + " INTEGER NOT NULL" +
                ");";

        database.execSQL(CREATE_USERS_TABLE);
        database.execSQL(CREATE_OPERATIONS_TABLE);
        database.execSQL(CREATE_CATEGORY_TABLE);
        database.execSQL(CREATE_ACCOUNTS_TABLE);

        dbFirstInit(database, USER_ID_PRIMARY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        final String DROP_IF_EXISTS = "DROP TABLE IF EXISTS ";

        database.execSQL(DROP_IF_EXISTS + Users.TABLE_NAME);
        database.execSQL(DROP_IF_EXISTS + Operations.TABLE_NAME);
        database.execSQL(DROP_IF_EXISTS + Categories.TABLE_NAME);
        database.execSQL(DROP_IF_EXISTS + Accounts.TABLE_NAME);

        onCreate(database);
    }

    //returns ID of inserted operation
    public long insertOperation(Operation operation, int userId, int accountId) {
        SQLiteDatabase db = getWritableDatabase();

        String dateString = getDateStringForDb(operation.getOperationDate());

        ContentValues cv = new ContentValues();
        cv.put(Operations.COLUMN_AMOUNT, operation.getAmount().toString());
        cv.put(Operations.COLUMN_IS_INCOME, operation.getIsOperationIncome() ? 1 : 0);
        cv.put(Operations.COLUMN_COMMENT, operation.getComment());
        cv.put(Operations.COLUMN_DATE, dateString);
        cv.put(Operations.COLUMN_CATEGORY_ID, operation.getCategory().getId());
        cv.put(Operations.COLUMN_USER_ID, userId);
        cv.put(Operations.COLUMN_ACCOUNT_ID, accountId);

        return db.insertOrThrow(Operations.TABLE_NAME, null, cv);
    }

    public long insertCategory(String name, int icon, boolean isCustom, boolean isInputCategory, int userId, SQLiteDatabase db) {
        if (db == null)
            db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(Categories.COLUMN_ICON, icon);
        cv.put(Categories.COLUMN_IS_CUSTOM, isCustom);
        cv.put(Categories.COLUMN_IS_INPUT_CATEGORY, isInputCategory);
        cv.put(Categories.COLUMN_USER_ID, userId);
        cv.put(Categories.COLUMN_NAME, name);

        long newId = db.insert(Categories.TABLE_NAME, null, cv);

        return newId;
    }

    public long insertAccount(String name, int icon, int userId, SQLiteDatabase db) {
        if (db == null)
            db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(Accounts.COLUMN_ICON, icon);
        cv.put(Accounts.COLUMN_NAME, name);
        cv.put(Accounts.COLUMN_USER_ID, userId);

        long newId = db.insert(Accounts.TABLE_NAME, null, cv);

        return newId;
    }


    public Operation getOperation(long operationId, int userId) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor operationCursor = db.query(Operations.TABLE_NAME, null,
                Operations.COLUMN_ID + " = ?" + " AND " + Operations.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(operationId), String.valueOf(userId)}, null, null, null, null);

        Category category = null;
        if (operationCursor.moveToFirst()) {
            int categoryId = operationCursor.getInt(operationCursor.getColumnIndex(Operations.COLUMN_CATEGORY_ID));
            category = getCategory(categoryId);
        }

        String dateString = operationCursor.getString(operationCursor.getColumnIndex(Operations.COLUMN_DATE));
        Date operationDate = convertStringFromDbtoDate(dateString);

        Operation operation = getOperationFromCursor(operationCursor, category, operationDate);
        operationCursor.close();

        return operation;
    }

    @Nullable
    public Date getMinOperationDate(int userId) {//TODO: Optimize this
        SQLiteDatabase db = getReadableDatabase();

        Cursor dateCursor = db.rawQuery("SELECT MIN("+Operations.COLUMN_DATE+") FROM "
                + Operations.TABLE_NAME + " WHERE " + Operations.COLUMN_USER_ID + " = ?;", new String[]{userId+""});

        Date date = new Date();
        if(dateCursor.getColumnCount() > 0) {
            if (dateCursor.moveToFirst()) {
                String dateString = dateCursor.getString(0);
                if(dateString == null) return null;
                date = convertStringFromDbtoDate(dateString);
            }
        }

        return date;
    }

    @Nullable
    public int getMinOperationDateId(int userId) {//TODO: Optimize this
        SQLiteDatabase db = getReadableDatabase();

        Cursor dateCursor = db.rawQuery("SELECT "+Operations.COLUMN_ID+", MIN("+Operations.COLUMN_DATE+") FROM "
                + Operations.TABLE_NAME + " WHERE " + Operations.COLUMN_USER_ID + " = ?;", new String[]{userId+""});
        int id = -1;
        if(dateCursor.getColumnCount() > 0) {
            if (dateCursor.moveToFirst()) {
                id = dateCursor.getInt(0);
            }
        }

        return id;
    }

    @NonNull
    private Category getCategoryFromCursor(Cursor cursorCategory) {
        Category category;
        category = new Category(
                cursorCategory.getInt(cursorCategory.getColumnIndex(Categories.COLUMN_ID)),
                cursorCategory.getString(cursorCategory.getColumnIndex(Categories.COLUMN_NAME)),
                cursorCategory.getInt(cursorCategory.getColumnIndex(Categories.COLUMN_ICON)),
                cursorCategory.getInt(cursorCategory.getColumnIndex(Categories.COLUMN_IS_CUSTOM)) > 0,
                cursorCategory.getInt(cursorCategory.getColumnIndex(Categories.COLUMN_IS_INPUT_CATEGORY)) > 0
        );
        category.setId(cursorCategory.getInt(cursorCategory.getColumnIndex(Categories.COLUMN_ID)));
        return category;
    }

    @NonNull
    private Operation getOperationFromCursor(Cursor cursorOperations, Category category, Date operationDate) {
        Operation operation = new Operation();
        operation.initOperation(cursorOperations.getInt(cursorOperations.getColumnIndex(Operations.COLUMN_ID)),
                cursorOperations.getInt(cursorOperations.getColumnIndex(Operations.COLUMN_ACCOUNT_ID)),
                new BigDecimal(cursorOperations.getString(cursorOperations.getColumnIndex(Operations.COLUMN_AMOUNT))),
                operationDate,
                cursorOperations.getString(cursorOperations.getColumnIndex(Operations.COLUMN_COMMENT)),
                cursorOperations.getInt(cursorOperations.getColumnIndex(Operations.COLUMN_IS_INCOME)) > 0,
                category);
        return operation;
    }


    public List<Category> getAllCategories(int userId, boolean isIncome) {
        List<Category> categoryList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor categoriesCursor = db.query(Categories.TABLE_NAME, null,
                Categories.COLUMN_USER_ID + " = ?" + " AND " + Categories.COLUMN_IS_INPUT_CATEGORY + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(isIncome ? 1 : 0)}, null, null, null, null);

        if (categoriesCursor.moveToFirst()) {
            do {
                Category category = getCategoryFromCursor(categoriesCursor);
                categoryList.add(category);
            } while (categoriesCursor.moveToNext());
        }
        return categoryList;
    }

    public List<Account> getAllAccounts(int userId) {
        List<Account> accountList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(Accounts.TABLE_NAME, null, Accounts.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Account account = getAccountFormCursor(cursor);
                accountList.add(account);
            } while (cursor.moveToNext());
        }
        return accountList;
    }

    public Account getAccount(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Account account = null;

        Cursor accountCursor = db.query(Accounts.TABLE_NAME, null, Accounts.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (accountCursor.moveToFirst()) {
            account = getAccountFormCursor(accountCursor);
        }
        return account;
    }

    @NonNull
    private Account getAccountFormCursor(Cursor accountCursor) {
        Account account;
        account = new Account(accountCursor.getInt(accountCursor.getColumnIndex(Accounts.COLUMN_ID)),
                accountCursor.getString(accountCursor.getColumnIndex(Accounts.COLUMN_NAME)),
                accountCursor.getInt(accountCursor.getColumnIndex(Accounts.COLUMN_ICON)));
        return account;
    }

    public Category getCategory(int categoryId) {
        SQLiteDatabase db = getReadableDatabase();
        Category category = null;

        Cursor cursorCategory = db.query(Categories.TABLE_NAME, null, Categories.COLUMN_ID + " = ?",
                new String[]{String.valueOf(categoryId)}, null, null, null, null);

        if (cursorCategory.moveToFirst()) {
            category = getCategoryFromCursor(cursorCategory);
        }
        return category;
    }


    // If accountId < 0 method selects all accounts
    public List<Operation> getOperations(Integer accountId, PeriodsOfTime period, Calendar currDay) {
        List<Operation> operationsList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor operationsCursor = getOperationCursor(accountId, DateUtils.getEndOfPeriod(currDay, period), db, period);

        //Looping through the operations cursor, init category and operation, add operation to list
        if (operationsCursor.moveToFirst()) {
            do {
                int categoryId = operationsCursor.getInt(operationsCursor.getColumnIndex(Operations.COLUMN_CATEGORY_ID));
                Category category = getCategory(categoryId);

                String dateString = operationsCursor.getString(operationsCursor.getColumnIndex(Operations.COLUMN_DATE));
                Date operationDate = convertStringFromDbtoDate(dateString);

                Operation operation = getOperationFromCursor(operationsCursor, category, operationDate);
                operationsList.add(operation);
            } while (operationsCursor.moveToNext());
        }

        return operationsList;
    }

    private Cursor getOperationCursor(int accountId, Calendar endOfPeriod, SQLiteDatabase db, PeriodsOfTime period) {
        String endOfPeriodDateString = getDateStringForDb(endOfPeriod.getTime());

        String[] selectionArgs = null;
        String selectionString = null;

        switch (period) {
            case ALL_TIME:
                if (accountId > 0) {
                    selectionArgs = new String[]{accountId + ""};
                    selectionString = Operations.COLUMN_ACCOUNT_ID + " = ?";
                }
                break;
            case DAY:
                if (accountId < 0) {
                    selectionArgs = new String[]{endOfPeriodDateString};
                    selectionString = Operations.COLUMN_DATE + " = ?";
                } else {
                    selectionArgs = new String[]{accountId + "", endOfPeriodDateString};
                    selectionString = Operations.COLUMN_ACCOUNT_ID + " = ?" + " AND " + Operations.COLUMN_DATE + " = ?";
                }
                break;
            default:
                Calendar startDate = DateUtils.getStartOfPeriod(endOfPeriod, period);

                if (accountId < 0) {
                    selectionString = Operations.COLUMN_DATE + " BETWEEN " + "?" + " AND " + "?";
                    String startOfPeriodDateString = getDateStringForDb(startDate.getTime());
                    selectionArgs = new String[]{startOfPeriodDateString, endOfPeriodDateString};
                } else {
                    selectionString = Operations.COLUMN_ACCOUNT_ID + " = ?" + " AND " +
                            Operations.COLUMN_DATE + " BETWEEN " + "?" + " AND " + "?";
                    String startOfPeriodDateString = getDateStringForDb(startDate.getTime());
                    selectionArgs = new String[]{accountId + "", startOfPeriodDateString, endOfPeriodDateString};
                }
        }

        return db.query(Operations.TABLE_NAME, null,
                selectionString,
                selectionArgs,
                null, null, null, null);
    }

    public int updateCategory(Category category, int userId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(Categories.COLUMN_NAME, category.getName());
        cv.put(Categories.COLUMN_ICON, category.getIcon());
        cv.put(Categories.COLUMN_USER_ID, userId);
        cv.put(Categories.COLUMN_IS_CUSTOM, category.getIsCustom());
        cv.put(Categories.COLUMN_IS_INPUT_CATEGORY, category.getIsInputCategory());


        return db.update(Operations.TABLE_NAME, cv, Categories.COLUMN_ID + " = ?",
                new String[]{String.valueOf(category.getId())});
    }

    public int updateAccount(Account account, int userId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(Accounts.COLUMN_NAME, account.getName());
        cv.put(Accounts.COLUMN_ICON, account.getIcon());
        cv.put(Accounts.COLUMN_USER_ID, userId);


        return db.update(Operations.TABLE_NAME, cv, Categories.COLUMN_ID + " = ?",
                new String[]{String.valueOf(account.getId())});
    }

    public int updateOperation(Operation operation, int userId, int accountId) {
        SQLiteDatabase db = getWritableDatabase();

        String dateString = getDateStringForDb(operation.getOperationDate());

        ContentValues cv = new ContentValues();
        cv.put(Operations.COLUMN_AMOUNT, operation.getAmount().toString());
        cv.put(Operations.COLUMN_IS_INCOME, operation.getIsOperationIncome() ? 1 : 0);
        cv.put(Operations.COLUMN_COMMENT, operation.getComment());
        cv.put(Operations.COLUMN_DATE, dateString);
        cv.put(Operations.COLUMN_CATEGORY_ID, operation.getCategory().getId());
        cv.put(Operations.COLUMN_USER_ID, userId);
        cv.put(Operations.COLUMN_ACCOUNT_ID, accountId);


        return db.update(Operations.TABLE_NAME, cv, Operations.COLUMN_ID + " = ?",
                new String[]{String.valueOf(operation.getId())});
    }

    //Save date in DB in the string format
    private String getDateStringForDb(Date date) {
        return DateUtils.getStringDate(date, FinanceManagerContract.DATABASE_DATE_PATTERN);
    }

    private Date convertStringFromDbtoDate(String dbDateString) {
        return DateUtils.getDateFromString(dbDateString, FinanceManagerContract.DATABASE_DATE_PATTERN);
    }

    public int getOperationsCount() {
        String countQuery = "SELECT * FROM " + Operations.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public void deleteOperation(Operation operation) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Operations.TABLE_NAME, Operations.COLUMN_ID + " = ?",
                new String[]{String.valueOf(operation.getId())});

    }

    public void deleteAccount(Account account) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Accounts.TABLE_NAME, Accounts.COLUMN_ID + " = ?",
                new String[]{String.valueOf(account.getId())});

    }

    public void deleteCategory(Category category) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Categories.TABLE_NAME, Categories.COLUMN_ID + " = ?",
                new String[]{String.valueOf(category.getId())});

    }

    private void dbFirstInit(SQLiteDatabase db, int userId) {
        //ResourcesCompat.getDrawable(context.getResources(), R.drawable.credit_card, null)
        insertCategory("Transport", R.drawable.transport, false, false, userId, db);
        insertCategory("Food", R.drawable.food, false, false, userId, db);
        insertCategory("Cafe", R.drawable.cafe, false, false, userId, db);
        insertCategory("House", R.drawable.house, false, false, userId, db);
        insertCategory("Car", R.drawable.car, false, false, userId, db);
        insertCategory("Education", R.drawable.education, false, false, userId, db);
        insertCategory("Sport", R.drawable.sport, false, false, userId, db);
        insertCategory("Clothes", R.drawable.clothes, false, false, userId, db);
        insertCategory("Present", R.drawable.presents, false, false, userId, db);
        insertCategory("Health", R.drawable.health, false, false, userId, db);
        insertCategory("Entertainment", R.drawable.entertainment, false, false, userId, db);

        insertCategory("Salary", R.drawable.salary, false, true, userId, db);
        insertCategory("Present", R.drawable.presents, false, true, userId, db);
        insertCategory("Savings", R.drawable.savings, false, true, userId, db);

        insertAccount("Cash", R.drawable.money, userId, db);
        insertAccount("Credit card", R.drawable.credit_card, userId, db);
        insertAccount("Salary", R.drawable.salary, userId, db);
    }
}
