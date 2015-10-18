package com.example.mobile.smartcycledemo.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;

/**
 * Created by mobile on 15/10/15.
 */
public class MyDatabase {

    private static MyDatabase commonDatabase = null;

    static final String DATABASE_NAME = "SmartCycleDatabase";
    static final int DATABASE_VERSION = 1;

    final Context mContext;
    DatabaseHelper mDatabaseHelper;
    SQLiteDatabase mDatabase;

    private MyDatabase(Context context){
        this.mContext = context;
        mDatabaseHelper = new DatabaseHelper(mContext);
    }

    public static MyDatabase getInstance(Context context){
        if (commonDatabase == null){
            commonDatabase = new MyDatabase(context);
        }
        return commonDatabase;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(UserProfile.DATABASE_CREATE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            db.execSQL("DROP TABLE IF EXISTS "+ UserProfile.DATABASE_TABLE);
            onCreate(db);
        }
    }

    public MyDatabase open() throws SQLException{
        mDatabase = mDatabaseHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        mDatabaseHelper.close();
    }

    /**
     * the following functions are for user basic table
     */
    public long insertUserBasic(String account, String username, String password,
                                String age, int gender, int height, int weight, int level, int total_time,
                                int total_times, String last_date, int continue_days, int total_energy){
        return UserProfile.insertUserBasic(mDatabase, account, username, password, age, gender, height, weight, level,
                total_time, total_times, last_date, continue_days, total_energy);
    }

    public boolean findUserBasicAccount(String user_account){
        return UserProfile.findUserBasicAccount(mDatabase, user_account);
    }

    public Cursor checkUserBasicPassword(String user_account, String password){
        return UserProfile.checkUserBasicPassword(mDatabase, user_account, password);
    }

    public long updateUserBasic(String account, String username, String password,
                                String age, int gender, int height, int weight, int level, int total_time,
                                int total_times, String last_date, int continue_days, int total_energy){
        return UserProfile.updateUserBasic(mDatabase, account, username, password, age, gender, height, weight, level,
                total_time, total_times, last_date, continue_days, total_energy);
    }

    public static class UserProfile{
        // table name
        static final String DATABASE_TABLE = "user_basic";
        // table column
        public static final String KEY_ROWID = "_id";
        public static final String KEY_ACCOUNT = "account";
        public static final String KEY_NAME = "username";
        public static final String KEY_PASSWORD = "password";
        public static final String KEY_AGE = "age";
        public static final String KEY_GENDER = "gender";
        public static final String KEY_HEIGHT = "height";
        public static final String KEY_WEIGHT = "weight";
        public static final String KEY_LEVEL = "level";
        public static final String KEY_TOTAL_TIME = "total_time";
        public static final String KEY_TOTAL_TIMES = "total_times";
        public static final String KEY_LAST_DATE = "last_date";
        public static final String KEY_CONTINUE_DAYS = "continue_days";
        public static final String KEY_TOTAL_ENERGY = "total_energy";
        static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " ( " +
                KEY_ROWID + " integer primary key autoincrement, " +
                KEY_ACCOUNT + " text not null, " +
                KEY_NAME + " text not null, " +
                KEY_PASSWORD + " text not null, " +
                KEY_AGE + " text not null, " +
                KEY_GENDER + " integer not null, " +
                KEY_HEIGHT + " integer not null, " +
                KEY_WEIGHT + " integer not null, " +
                KEY_LEVEL + " integer not null, " +
                KEY_TOTAL_TIME + " integer not null, " +
                KEY_TOTAL_TIMES + " integer not null, " +
                KEY_LAST_DATE + " text not null, " +
                KEY_CONTINUE_DAYS + " integer not null, " +
                KEY_TOTAL_ENERGY + " integer not null " + ");";

        // insert a new user
        static long insertUserBasic(SQLiteDatabase database, String account, String username, String password,
                                    String age, int gender, int height, int weight, int level, int total_time,
                                    int total_times, String last_date, int continue_days, int total_energy){
            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_ACCOUNT, account);
            contentValues.put(KEY_NAME, username);
            contentValues.put(KEY_PASSWORD, password);
            contentValues.put(KEY_AGE, age);
            contentValues.put(KEY_GENDER, gender);
            contentValues.put(KEY_HEIGHT, height);
            contentValues.put(KEY_WEIGHT, weight);
            contentValues.put(KEY_LEVEL, level);
            contentValues.put(KEY_TOTAL_TIME, total_time);
            contentValues.put(KEY_TOTAL_TIMES, total_times);
            contentValues.put(KEY_LAST_DATE, last_date);
            contentValues.put(KEY_CONTINUE_DAYS, continue_days);
            contentValues.put(KEY_TOTAL_ENERGY, total_energy);
            return database.insert(DATABASE_TABLE, null, contentValues);
        }

        // find the existence of an account
        static boolean findUserBasicAccount(SQLiteDatabase database, String account){
            Cursor c = database.rawQuery("select * from "+ DATABASE_TABLE + " where "+ KEY_ACCOUNT + " = ? ",
                    new String[]{account});
            c.moveToFirst();
            return c.getCount() == 0;
        }

        // check the match of account and password
        static Cursor checkUserBasicPassword(SQLiteDatabase database, String account, String password){
            return database.rawQuery("select * from "+ DATABASE_TABLE + " where "+ KEY_ACCOUNT + "=? and "
                    + KEY_PASSWORD + "=?", new String[]{account, password});
        }

        // update the item
        static long updateUserBasic(SQLiteDatabase database, String account, String username, String password,
                                    String age, int gender, int height, int weight, int level, int total_time,
                                    int total_times, String last_date, int continue_days, int total_energy){
            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_ACCOUNT, account);
            contentValues.put(KEY_NAME, username);
            contentValues.put(KEY_PASSWORD, password);
            contentValues.put(KEY_AGE, age);
            contentValues.put(KEY_GENDER, gender);
            contentValues.put(KEY_HEIGHT, height);
            contentValues.put(KEY_WEIGHT, weight);
            contentValues.put(KEY_LEVEL, level);
            contentValues.put(KEY_TOTAL_TIME, total_time);
            contentValues.put(KEY_TOTAL_TIMES, total_times);
            contentValues.put(KEY_LAST_DATE, last_date);
            contentValues.put(KEY_CONTINUE_DAYS, continue_days);
            contentValues.put(KEY_TOTAL_ENERGY, total_energy);
            if(checkUserBasicPassword(database, account, password).getCount() == 0){
                return -1;
            }
            return database.update(DATABASE_TABLE, contentValues, KEY_ACCOUNT + "=?", new String[]{account});
        }
    }

}
