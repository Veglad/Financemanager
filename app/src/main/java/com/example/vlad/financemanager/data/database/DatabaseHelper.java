package com.example.vlad.financemanager.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.vlad.financemanager.data.models.Account;
import com.example.vlad.financemanager.utils.CalendarSettings;
import com.example.vlad.financemanager.data.models.Category;
import com.example.vlad.financemanager.data.models.Operation;
import com.example.vlad.financemanager.data.enums.PeriodsOfTime;
import com.example.vlad.financemanager.R;
import com.example.vlad.financemanager.data.database.FinanceManagerContract.*;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "finance_manager.db";
    private static final int DB_VERSION = 1;
    private final String patternDate = "yyyy-MM-dd";
    private Context context;


    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
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
                Operations.COLUMN_ISINCOME + " INTEGER NOT NULL, " +
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

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_OPERATIONS_TABLE);
        db.execSQL(CREATE_CATEGORY_TABLE);
        db.execSQL(CREATE_ACCOUNTS_TABLE);

        dbFirstInit(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ Users.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ Operations.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ Categories.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ Accounts.TABLE_NAME);

        onCreate(db);
    }

    public long insertOperation(String amount, boolean isIncome, String comment, Date operDate,
                               int categoryId, int userId, int accountId){
        SQLiteDatabase db = getWritableDatabase();

        String dateStr = new SimpleDateFormat(patternDate).format(operDate);

        ContentValues cv = new ContentValues();
        cv.put(Operations.COLUMN_AMOUNT, amount);
        cv.put(Operations.COLUMN_ISINCOME, isIncome?1:0);
        cv.put(Operations.COLUMN_COMMENT, comment);
        cv.put(Operations.COLUMN_DATE, dateStr);
        cv.put(Operations.COLUMN_CATEGORY_ID, categoryId);
        cv.put(Operations.COLUMN_USER_ID, userId);
        cv.put(Operations.COLUMN_ACCOUNT_ID, accountId);

        long newId = db.insertOrThrow(Operations.TABLE_NAME, null, cv);


        return  newId;
    }

    public long insertCategory(String name, int icon, boolean isCustom, boolean isInputCategory, int userId, SQLiteDatabase db){
        if(db == null)
            db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(Categories.COLUMN_ICON, icon);
        cv.put(Categories.COLUMN_IS_CUSTOM, isCustom);
        cv.put(Categories.COLUMN_IS_INPUT_CATEGORY, isInputCategory);
        cv.put(Categories.COLUMN_USER_ID, userId);
        cv.put(Categories.COLUMN_NAME, name);

        long newId = db.insert(Categories.TABLE_NAME, null, cv);

        return  newId;
    }

    public long insertAccount(String name, int icon, int userId, SQLiteDatabase db){
        if(db == null)
            db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(Accounts.COLUMN_ICON, icon);
        cv.put(Accounts.COLUMN_NAME, name);
        cv.put(Accounts.COLUMN_USER_ID, userId);

        long newId = db.insert(Accounts.TABLE_NAME, null, cv);

        return  newId;
    }

    public Operation getOperation(long id, int userId){
        SQLiteDatabase db = getReadableDatabase();
        SQLiteDatabase dbRead = getReadableDatabase();

        Cursor cursor = db.query(Operations.TABLE_NAME,
                new String[]{Operations.COLUMN_ID, Operations.COLUMN_AMOUNT,
                Operations.COLUMN_ISINCOME , Operations.COLUMN_COMMENT,
                Operations.COLUMN_DATE, Operations.COLUMN_TIME_STAMP,
                Operations.COLUMN_CATEGORY_ID, Operations.COLUMN_USER_ID, Operations.COLUMN_ACCOUNT_ID },
                Operations.COLUMN_ID + " = ?" +" AND "+ Operations.COLUMN_USER_ID + " = ?" ,
                new String[]{String.valueOf(id), String.valueOf(userId)},null,null,null,null);

        Cursor cursorCategory;
        Category category = null;
        if(cursor.moveToFirst()) {
            int categoryId = cursor.getInt(cursor.getColumnIndex(Operations.COLUMN_CATEGORY_ID));
            cursorCategory = db.query(Categories.TABLE_NAME,
                    new String[]{Categories.COLUMN_ID, Categories.COLUMN_NAME,
                            Categories.COLUMN_ICON , Categories.COLUMN_IS_CUSTOM,
                            Categories.COLUMN_IS_INPUT_CATEGORY },
                    Categories.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(categoryId)},null,null,null,null);
            //Creating Category object

            if(cursorCategory.moveToFirst()){
                category = new Category(
                        cursorCategory.getInt(cursorCategory.getColumnIndex(Categories.COLUMN_ID)),
                        cursorCategory.getString(cursorCategory.getColumnIndex(Categories.COLUMN_NAME)),
                        cursorCategory.getInt(cursorCategory.getColumnIndex(Categories.COLUMN_ICON)),
                        cursorCategory.getInt(cursorCategory.getColumnIndex(Categories.COLUMN_IS_CUSTOM)) > 0,
                        cursorCategory.getInt(cursorCategory.getColumnIndex(Categories.COLUMN_IS_INPUT_CATEGORY)) > 0
                );
                category.setId(cursorCategory.getInt(cursorCategory.getColumnIndex(Categories.COLUMN_ID)));
            }
        }

        //Date converting
        DateFormat format = new SimpleDateFormat(patternDate);
        String dateString = cursor.getString(cursor.getColumnIndex(Operations.COLUMN_DATE));
        Date operDate  = new Date();
        try{
            operDate = format.parse(dateString);
        }catch (Exception e){}


        /*              Creating Operation          */
        Operation operation = new Operation(
                cursor.getInt(cursor.getColumnIndex(Operations.COLUMN_ID)),
                cursor.getInt(cursor.getColumnIndex(Operations.COLUMN_ACCOUNT_ID)),
                new BigDecimal(cursor.getString(cursor.getColumnIndex(Operations.COLUMN_AMOUNT))),
                operDate,
                cursor.getString(cursor.getColumnIndex(Operations.COLUMN_COMMENT)),
                cursor.getInt(cursor.getColumnIndex(Operations.COLUMN_ISINCOME)) > 0,
                category
                );

        cursor.close();

        return operation;
    }


    public List<Category> getAllCategories(int userId, boolean isIncome){
        List<Category> categoryList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(Categories.TABLE_NAME,
                new String[]{Categories.COLUMN_ID, Categories.COLUMN_NAME,
                        Categories.COLUMN_ICON , Categories.COLUMN_IS_CUSTOM,
                        Categories.COLUMN_IS_INPUT_CATEGORY, Categories.COLUMN_USER_ID},
                Categories.COLUMN_USER_ID + " = ?" + " AND " + Categories.COLUMN_IS_INPUT_CATEGORY + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(isIncome?1:0)},null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                Category category = new Category(cursor.getInt(cursor.getColumnIndex(Categories.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(Categories.COLUMN_NAME)),
                        cursor.getInt(cursor.getColumnIndex(Categories.COLUMN_ICON)),
                        cursor.getInt(cursor.getColumnIndex(Categories.COLUMN_IS_CUSTOM))>0,
                        cursor.getInt(cursor.getColumnIndex(Categories.COLUMN_IS_INPUT_CATEGORY))>0);

                categoryList.add(category);
            }while (cursor.moveToNext());
        }
        return categoryList;
    }

    public List<Account> getAllAccounts(int userId){
        List<Account> accountList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(Accounts.TABLE_NAME,
                new String[]{Accounts.COLUMN_ID, Accounts.COLUMN_NAME,
                        Accounts.COLUMN_ICON , Accounts.COLUMN_USER_ID},
                Accounts.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                Account account = new Account(cursor.getInt(cursor.getColumnIndex(Accounts.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(Accounts.COLUMN_NAME)),
                        cursor.getInt(cursor.getColumnIndex(Accounts.COLUMN_ICON)));

                accountList.add(account);
            }while (cursor.moveToNext());
        }
        return accountList;
    }

    public Account getAccount(int id){
        SQLiteDatabase db = getReadableDatabase();
        Account account = null;

        Cursor cursor = db.query(Accounts.TABLE_NAME,
                new String[]{Accounts.COLUMN_ID, Accounts.COLUMN_NAME,
                        Accounts.COLUMN_ICON , Accounts.COLUMN_USER_ID},
                Accounts.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                account = new Account(cursor.getInt(cursor.getColumnIndex(Accounts.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(Accounts.COLUMN_NAME)),
                        cursor.getInt(cursor.getColumnIndex(Accounts.COLUMN_ICON)));
            }while (cursor.moveToNext());
        }
        return account;
    }

    public Category getCategory(int id){
        SQLiteDatabase db = getReadableDatabase();
        Category category = null;

        Cursor cursor = db.query(Categories.TABLE_NAME,
                new String[]{Categories.COLUMN_ID, Categories.COLUMN_NAME,
                        Categories.COLUMN_ICON , Categories.COLUMN_IS_CUSTOM,
                        Categories.COLUMN_IS_INPUT_CATEGORY, Categories.COLUMN_USER_ID},
                Categories.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                category = new Category(cursor.getInt(cursor.getColumnIndex(Categories.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(Categories.COLUMN_NAME)),
                        cursor.getInt(cursor.getColumnIndex(Categories.COLUMN_ICON)),
                        cursor.getInt(cursor.getColumnIndex(Categories.COLUMN_IS_CUSTOM))>0,
                        cursor.getInt(cursor.getColumnIndex(Categories.COLUMN_IS_INPUT_CATEGORY))>0);

            }while (cursor.moveToNext());
        }
        return category;
    }


    public List<Operation> getAllOperations(int accountId, PeriodsOfTime period, Calendar currDay) {
        List<Operation> operationsList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        cursor = getCursorForSelectedTime(accountId, CalendarSettings.getEndOfPeriod(currDay,period), db, period);

        Cursor cursor1;

        //looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do{
                int categoryId = cursor.getInt(cursor.getColumnIndex(Operations.COLUMN_CATEGORY_ID));
                cursor1 = db.query(Categories.TABLE_NAME,
                        new String[]{Categories.COLUMN_ID, Categories.COLUMN_NAME,
                                Categories.COLUMN_ICON , Categories.COLUMN_IS_CUSTOM,
                                Categories.COLUMN_IS_INPUT_CATEGORY },
                        Categories.COLUMN_ID + " = ?",
                        new String[]{String.valueOf(categoryId)},null,null,null,null);
                Category category = null;
                if(cursor1.moveToFirst()){

                    //category init
                    category = new Category(
                            cursor1.getInt(cursor1.getColumnIndex(Categories.COLUMN_ID)),
                            cursor1.getString(cursor1.getColumnIndex(Categories.COLUMN_NAME)),
                            cursor1.getInt(cursor1.getColumnIndex(Categories.COLUMN_ICON)),
                            cursor1.getInt(cursor1.getColumnIndex(Categories.COLUMN_IS_CUSTOM)) > 0,
                            cursor1.getInt(cursor1.getColumnIndex(Categories.COLUMN_IS_INPUT_CATEGORY)) > 0
                    );
                    category.setId(cursor1.getInt(cursor1.getColumnIndex(Categories.COLUMN_ID)));
                }

                //Date converting
                DateFormat format = new SimpleDateFormat(patternDate);
                String dateString = cursor.getString(cursor.getColumnIndex(Operations.COLUMN_DATE));
                Date operDate = new Date();
                try {
                    operDate = format.parse(dateString);
                } catch (Exception e) {
                }

                //Operation init
                Operation operation = new Operation(
                        cursor.getInt(cursor.getColumnIndex(Operations.COLUMN_ID)),
                        cursor.getInt(cursor.getColumnIndex(Operations.COLUMN_ACCOUNT_ID)),
                        new BigDecimal(cursor.getString(cursor.getColumnIndex(Operations.COLUMN_AMOUNT))),
                        operDate,
                        cursor.getString(cursor.getColumnIndex(Operations.COLUMN_COMMENT)),
                        cursor.getInt(cursor.getColumnIndex(Operations.COLUMN_ISINCOME)) > 0,
                        category
                );

                operationsList.add(operation);
            } while (cursor.moveToNext()) ;
        }


        return operationsList;
    }

    private Cursor getCursorForSelectedTime(int accountId, Calendar endOfPeriod, SQLiteDatabase db, PeriodsOfTime period){
        Cursor cursor;
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(endOfPeriod.getTime());

        String toDate = new SimpleDateFormat(patternDate).format(endOfPeriod.getTime());

        //accountId == -1 => select for all accounts
        switch (period){
            case ALL_TIME:
                if(accountId == -1)
                    cursor = db.query(Operations.TABLE_NAME, null,
                            null, null,null,null,null,null);
                else
                    cursor = db.query(Operations.TABLE_NAME, null,
                            Operations.COLUMN_ACCOUNT_ID + " = ?",
                            new String[]{String.valueOf(accountId)},null,null,null,null);
                return cursor;
            case YEAR:
                startDate.set(Calendar.DAY_OF_YEAR, 1);
                break;
            case MONTH:
                startDate.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case WEEK:
                startDate.set(Calendar.DAY_OF_YEAR, endOfPeriod.get(Calendar.DAY_OF_YEAR) - 6);
                break;
            case DAY:
                if(accountId == -1)
                    cursor = db.query(Operations.TABLE_NAME, null,
                            Operations.COLUMN_DATE + " = ?",
                            new String[]{toDate},null,null,null,null);
                else
                    cursor = db.query(Operations.TABLE_NAME, null,
                            Operations.COLUMN_ACCOUNT_ID + " = ?" +" AND " + Operations.COLUMN_DATE +
                                    " = ?",
                            new String[]{String.valueOf(accountId), toDate},null,null,null,null);
                return cursor;
        }

        String fromDate = new SimpleDateFormat(patternDate).format(startDate.getTime());

        if(accountId == -1)
            cursor = db.query(Operations.TABLE_NAME,null,
                    Operations.COLUMN_DATE +
                            " BETWEEN " + "?" + " AND "+ "?",
                    new String[]{fromDate, toDate},null,null,null,null);
        else
            cursor = db.query(Operations.TABLE_NAME,null,
                    Operations.COLUMN_ACCOUNT_ID + " = ?" +" AND " + Operations.COLUMN_DATE +
                            " BETWEEN " + "?" + " AND "+ "?",
                    new String[]{String.valueOf(accountId), fromDate, toDate},null,null,null,null);

        return cursor;
    }

    public int updateCategory(Category category, int userId){
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

    public int updateAccount(Account account, int userId){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(Accounts.COLUMN_NAME, account.getName());
        cv.put(Accounts.COLUMN_ICON, account.getIcon());
        cv.put(Accounts.COLUMN_USER_ID, userId);


        return db.update(Operations.TABLE_NAME, cv, Categories.COLUMN_ID + " = ?",
                new String[]{String.valueOf(account.getId())});
    }

    public int updateOperation(Operation operation, int userId, int accountId){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(Operations.COLUMN_AMOUNT, operation.getAmount().toString());
        cv.put(Operations.COLUMN_ISINCOME, operation.getIsOperationIncome()? 1:0);
        cv.put(Operations.COLUMN_COMMENT, operation.getComment());
        cv.put(Operations.COLUMN_DATE, new SimpleDateFormat(patternDate).format(operation.getOperationDate()));
        cv.put(Operations.COLUMN_CATEGORY_ID, operation.getCategory().getId());
        cv.put(Operations.COLUMN_USER_ID, userId);
        cv.put(Operations.COLUMN_ACCOUNT_ID, accountId);


        return db.update(Operations.TABLE_NAME, cv, Operations.COLUMN_ID + " = ?",
        new String[]{String.valueOf(operation.getId())});
    }

    public int getOperationsCount(){
        String countQuery = "SELECT * FROM " + Operations.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public void deleteOperation(Operation operation){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Operations.TABLE_NAME, Operations.COLUMN_ID + " = ?",
                new String[]{String.valueOf(operation.getId())});

    }

    public void deleteAccount(Account account){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Accounts.TABLE_NAME, Accounts.COLUMN_ID + " = ?",
                new String[]{String.valueOf(account.getId())});

    }

    public void deleteCategory(Category category){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Categories.TABLE_NAME, Categories.COLUMN_ID + " = ?",
                new String[]{String.valueOf(category.getId())});

    }

    public void dbFirstInit(SQLiteDatabase db){
        //ResourcesCompat.getDrawable(context.getResources(), R.drawable.credit_card, null)
        insertCategory("Transport", R.drawable.transport , false, false, 0, db);
        insertCategory("Food", R.drawable.food, false, false, 0,db);
        insertCategory("Cafe", R.drawable.cafe, false, false, 0,db);
        insertCategory("House", R.drawable.house, false, false, 0,db);
        insertCategory("Car", R.drawable.car, false, false, 0,db);
        insertCategory("Education", R.drawable.education, false, false, 0,db);
        insertCategory("Sport", R.drawable.sport, false, false, 0,db);
        insertCategory("Clothes", R.drawable.clothes, false, false, 0,db);
        insertCategory("Present", R.drawable.presents, false, false, 0,db);
        insertCategory("Health", R.drawable.health, false, false, 0,db);
        insertCategory("Entertainment", R.drawable.entertainment, false, false, 0,db);

        insertCategory("Salary", R.drawable.salary, false, true, 0,db);
        insertCategory("Present", R.drawable.presents, false, true, 0,db);
        insertCategory("Savings", R.drawable.savings, false, true, 0,db);

        insertAccount("Cash", R.drawable.money , 0,db);
        insertAccount("Credit card",R.drawable.credit_card , 0,db);
        insertAccount("Salary",R.drawable.salary , 0,db);


    }
}
