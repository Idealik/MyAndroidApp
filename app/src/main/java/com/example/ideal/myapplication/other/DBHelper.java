package com.example.ideal.myapplication.other;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final  int DATABASE_VERSION = 44;
    public static final String DATABASE_NAME = "MyFirstDB";

    //tables name
    public static final String TABLE_CONTACTS_USERS = "users";
    public static final String TABLE_CONTACTS_SERVICES = "services";
    public static final String TABLE_WORKING_DAYS = "working_days";
    public static final String TABLE_WORKING_TIME = "working_time";
    public static final String TABLE_DIALOGS = "dialogs";
    public static final String TABLE_MESSAGE_ORDERS = "messages";
    public static final String TABLE_MESSAGE_REVIEWS = "message_reviews";
    public static final String TABLE_REVIEWS_FOR_SERVICE = "reviews_for_service";
    public static final String TABLE_REVIEWS_FOR_USERS = "reviews_for_users";

    //for all
    public  static final  String KEY_ID = "_id";

    // users
    public  static final  String KEY_USER_ID = "phone";
    public  static final  String KEY_NAME_USERS = "user_name";
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

    // dialogs
    public  static final  String KEY_FIRST_USER_ID_DIALOGS = "first_phone";
    public  static final  String KEY_SECOND_USER_ID_DIALOGS = "second_phone";

    // messages_orders
    public  static final  String KEY_DIALOG_ID_MESSAGES = "dialog_id";
    public  static final  String KEY_MESSAGE_TIME_MESSAGES = "message_time";
    public  static final  String KEY_IS_CANCELED_MESSAGE_ORDERS = "is_canceled";
    public  static final  String KEY_TIME_ID_MESSAGES = "time_id";

    //message_reviews
    public  static final  String KEY_IS_RATE_BY_USER_MESSAGE_REVIEWS = "is_rate_by_user";
    public  static final  String KEY_IS_RATE_BY_WORKER_MESSAGE_REVIEWS = "is_rate_by_worker";

    //reviews for service
    public  static final  String KEY_REVIEW_REVIEWS_FOR_SERVICE = "review";
    public  static final  String KEY_RATING_REVIEWS_FOR_SERVICE = "rating";
    public  static final  String KEY_SERVICE_ID_REVIEWS_FOR_SERVICE = "rating";
    public  static final  String KEY_VALUING_PHONE_REVIEWS_FOR_SERVICE = "valuing_phone";


    //reviews for user
    public  static final  String KEY_VALUING_PHONE_REVIEWS_FOR_USER = "valuing_phone";
    public  static final  String KEY_ESTIMATED_PHONE_REVIEWS_FOR_USER = "estimated_phone";
    public  static final  String KEY_REVIEW_REVIEWS_FOR_USER = "review";
    public  static final  String KEY_RATING_REVIEWS_FOR_USER = "rating";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String users = "create table "+ TABLE_CONTACTS_USERS
                + "("
                + KEY_USER_ID + " text primary key,"
                + KEY_NAME_USERS + " text,"
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
        String dialogs = "create table "+ TABLE_DIALOGS
                + "("
                + KEY_ID + " text primary key,"
                + KEY_FIRST_USER_ID_DIALOGS + " text,"
                + KEY_SECOND_USER_ID_DIALOGS + " text"
                + ")";
        String messagesOrders = "create table "+ TABLE_MESSAGE_ORDERS
                + "("
                + KEY_ID + " text primary key,"
                + KEY_DIALOG_ID_MESSAGES + " text,"
                + KEY_IS_CANCELED_MESSAGE_ORDERS + " text,"
                + KEY_TIME_ID_MESSAGES + " text,"
                + KEY_MESSAGE_TIME_MESSAGES + " text"
                + ")";

        String messagesReviews = "create table "+ TABLE_MESSAGE_REVIEWS
                + "("
                + KEY_ID + " text primary key,"
                + KEY_DIALOG_ID_MESSAGES + " text,"
                + KEY_IS_RATE_BY_USER_MESSAGE_REVIEWS + " text,"
                + KEY_IS_RATE_BY_WORKER_MESSAGE_REVIEWS+ " text,"
                + KEY_TIME_ID_MESSAGES + " text,"
                + KEY_MESSAGE_TIME_MESSAGES + " text"
                + ")";

        String reviewsForService = "create table "+ TABLE_REVIEWS_FOR_SERVICE
                + "("
                + KEY_ID + " text primary key,"
                + KEY_MESSAGE_TIME_MESSAGES + " text,"
                + KEY_VALUING_PHONE_REVIEWS_FOR_SERVICE + " text,"
                + KEY_REVIEW_REVIEWS_FOR_SERVICE + " text,"
                + KEY_RATING_REVIEWS_FOR_SERVICE + " text"
                + ")";

        String reviewsForUser = "create table "+ TABLE_REVIEWS_FOR_USERS
                + "("
                + KEY_ID + " text primary key,"
                + KEY_MESSAGE_TIME_MESSAGES + " text,"
                + KEY_VALUING_PHONE_REVIEWS_FOR_USER + " text,"
                + KEY_ESTIMATED_PHONE_REVIEWS_FOR_USER + " text,"
                + KEY_REVIEW_REVIEWS_FOR_USER + " text,"
                + KEY_RATING_REVIEWS_FOR_USER + " text"
                + ")";

        // create users table
        db.execSQL(users);

        // create service table
        db.execSQL(services);

        // create working days table
        db.execSQL(workingDays);

        // create working time table
        db.execSQL(workingTime);

        //create dialogs
        db.execSQL(dialogs);

        //create messages_orders
        db.execSQL(messagesOrders);

        //create messages_reviews
        db.execSQL(messagesReviews);

        //create reviews for service
        db.execSQL(reviewsForService);

        //create reviews for user
        db.execSQL(reviewsForUser);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_CONTACTS_USERS);
        db.execSQL("drop table if exists " + TABLE_CONTACTS_SERVICES);
        db.execSQL("drop table if exists " + TABLE_WORKING_DAYS);
        db.execSQL("drop table if exists " + TABLE_WORKING_TIME);
        db.execSQL("drop table if exists " + TABLE_DIALOGS);
        db.execSQL("drop table if exists " + TABLE_MESSAGE_ORDERS);
        db.execSQL("drop table if exists " + TABLE_MESSAGE_REVIEWS);
        db.execSQL("drop table if exists " + TABLE_REVIEWS_FOR_SERVICE);
        db.execSQL("drop table if exists " + TABLE_REVIEWS_FOR_USERS);

        onCreate(db);
    }
}
