package com.example.ideal.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final  int DATABASE_VERSION = 9;
    public static final String DATABASE_NAME = "MyFirstDB";
    //tables name
    public static final String TABLE_CONTACTS_USERS = "users";
    public static final String TABLE_CONTACTS_SERVICES = "services";

    //for all
    public  static final  String KEY_ID = "_id";

    // users
    public  static final  String KEY_USER_ID = "_userId";
    public  static final  String KEY_PASS_USERS = "pass";
    public  static final  String KEY_CITY_USERS = "city";

    //services
    public  static final  String KEY_NAME_SERVICES = "name";
    public  static final  String KEY_DESCRIPTION_SERVICES = "description";
    public  static final  String KEY_RATING_SERVICES = "rating";
    public  static final  String KEY_COUNT_OF_RATES_SERVICES = "count of rates";
    public  static final  String KEY_MIN_COST_SERVICES = "minCost";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String users = "create table "+ TABLE_CONTACTS_USERS
                + "("
                + KEY_USER_ID + " integer primary key,"
                + KEY_PASS_USERS + " text,"
                + KEY_CITY_USERS + " text"
                + ")";
        String services = "create table "+ TABLE_CONTACTS_SERVICES
                + "(" + KEY_ID + " integer primary key,"
                + KEY_NAME_SERVICES + " text,"
                + KEY_DESCRIPTION_SERVICES+ " text,"
                + KEY_RATING_SERVICES + " text,"
                + KEY_COUNT_OF_RATES_SERVICES + " text,"
                + KEY_MIN_COST_SERVICES + " text,"
                + KEY_USER_ID + " text"
                + ")";

        // create users table
        db.execSQL(users);

        // create service table
        db.execSQL(services);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("drop table if exists "+TABLE_CONTACTS_USERS);
    db.execSQL("drop table if exists "+TABLE_CONTACTS_SERVICES);

    onCreate(db);
    }
}
