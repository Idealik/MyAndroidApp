package com.example.ideal.myapplication.other;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final  int DATABASE_VERSION = 26;
    public static final String DATABASE_NAME = "MyFirstDB";

    //tables name
    public static final String TABLE_CONTACTS_USERS = "users";
    public static final String TABLE_CONTACTS_SERVICES = "services";
    public static final String TABLE_WORKING_DAYS = "working_days";
    public static final String TABLE_WORKING_TIME = "working_time";

    //for all
    public  static final  String KEY_ID = "_id";

    // users
    public  static final  String KEY_USER_ID = "phone";
    public  static final  String KEY_NAME_USERS = "user_name";
    public  static final  String KEY_SURNAME_USERS = "surname";
    public  static final  String KEY_CITY_USERS = "city";
    public  static final  String KEY_PASS_USERS = "pass";
    public  static final  String KEY_RATING_USERS = "user_rating";
    public  static final  String KEY_BIRTHDAY_USERS = "birthday";
    public  static final  String KEY_COUNT_OF_RATES_USERS = "count_of_rates";
    public  static final  String KEY_PHOTO_LINK_USERS = "photo_link";

    //services
    public  static final  String KEY_NAME_SERVICES = "service_name";
    public  static final  String KEY_DESCRIPTION_SERVICES = "description";
    public  static final  String KEY_RATING_SERVICES = "service_rating";
    public  static final  String KEY_COUNT_OF_RATES_SERVICES = "count_of_rates";
    public  static final  String KEY_MIN_COST_SERVICES = "minCost";

    //working days
    public  static final  String KEY_DATE_WORKING_DAYS = "date";
    public  static final  String KEY_SERVICE_ID_WORKING_DAYS = "service_id";

    // working time
    public  static final  String KEY_TIME_WORKING_TIME = "time";
    public  static final  String KEY_WORKING_DAYS_ID_WORKING_TIME = "id_Working_Days";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String users = "create table "+ TABLE_CONTACTS_USERS
                + "("
                + KEY_USER_ID + " text primary key,"
                + KEY_NAME_USERS + " text,"
                + KEY_SURNAME_USERS + " text,"
                + KEY_PASS_USERS + " text,"
                + KEY_CITY_USERS + " text,"
                + KEY_RATING_USERS + " text,"
                + KEY_BIRTHDAY_USERS + " text,"
                + KEY_COUNT_OF_RATES_USERS + " text,"
                + KEY_PHOTO_LINK_USERS + " text"
                + ")";
        String services = "create table "+ TABLE_CONTACTS_SERVICES
                + "(" + KEY_ID + " text primary key,"
                + KEY_NAME_SERVICES + " text,"
                + KEY_DESCRIPTION_SERVICES+ " text,"
                + KEY_RATING_SERVICES + " text,"
                + KEY_COUNT_OF_RATES_SERVICES + " text,"
                + KEY_MIN_COST_SERVICES + " text,"
                + KEY_USER_ID + " text"
                + ")";
        String workingDays = "create table "+ TABLE_WORKING_DAYS
                + "("
                + KEY_ID + " text primary key,"
                + KEY_DATE_WORKING_DAYS + " date,"
                + KEY_SERVICE_ID_WORKING_DAYS + " text"
                + ")";
        String workingTime = "create table "+ TABLE_WORKING_TIME
                + "("
                + KEY_ID + " text primary key,"
                + KEY_TIME_WORKING_TIME + " text,"
                + KEY_USER_ID + " text,"
                + KEY_WORKING_DAYS_ID_WORKING_TIME + " integer"
                + ")";


        // create users table
        db.execSQL(users);

        // create service table
        db.execSQL(services);

        // create working days table
        db.execSQL(workingDays);

        // create working time table
        db.execSQL(workingTime);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("drop table if exists "+TABLE_CONTACTS_USERS);
    db.execSQL("drop table if exists "+TABLE_CONTACTS_SERVICES);
    db.execSQL("drop table if exists "+TABLE_WORKING_DAYS);
    db.execSQL("drop table if exists "+TABLE_WORKING_TIME);

    onCreate(db);
    }
}
