package com.example.ideal.myapplication.other;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.fragments.Message;
import com.example.ideal.myapplication.fragments.MessageOrderElement;
import com.example.ideal.myapplication.fragments.Service;
import com.example.ideal.myapplication.fragments.User;
import com.example.ideal.myapplication.fragments.foundServiceElement;

public class Messages extends AppCompatActivity {

    //мне приходит айди диалога
    final  String TAG = "DBInf";
    private final String FILE_NAME = "Info";
    private final String PHONE_NUMBER = "Phone number";
    private final String DIALOG_ID = "dialog id";

    private String dialogId;
    private DBHelper dbHelper;

    private LinearLayout resultLayout;

    private MessageOrderElement fElement;
    private FragmentManager manager;
    private FragmentTransaction transaction;
    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages);

        dialogId = getIntent().getStringExtra(DIALOG_ID);
        Log.d(TAG, "onCreate: " + dialogId);
        resultLayout = findViewById(R.id.resultsMessageLayout);
        manager = getSupportFragmentManager();

        dbHelper = new DBHelper(this);
        // получаем телефон нашего собеседеника
        String senderPhone = getSenderPhone(dialogId);
        //обработка исключения
        if (!senderPhone.equals("0")) {
            //получаем само сообщение (в цикле по количеству messages)
            createMessage(dialogId,senderPhone);
        }
    }

    private String getSenderPhone(String dialogId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        // Получает телефоны из диалога
        // Таблицы: dialogs
        // Условия: уточняем id диалога
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_FIRST_USER_ID_DIALOGS + ", "
                        + DBHelper.KEY_SECOND_USER_ID_DIALOGS
                        + " FROM "
                        + DBHelper.TABLE_DIALOGS
                        + " WHERE "
                        + DBHelper.KEY_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{dialogId});

        if(cursor.moveToFirst()){
            int indexFirstPhone = cursor.getColumnIndex(DBHelper.KEY_FIRST_USER_ID_DIALOGS);
            int indexSecondPhone = cursor.getColumnIndex(DBHelper.KEY_SECOND_USER_ID_DIALOGS);

            String firstPhone = cursor.getString(indexFirstPhone);
            String secondPhone = cursor.getString(indexSecondPhone);
            if(firstPhone.equals(getUserId())){
                cursor.close();
                return secondPhone;
            }
            else {
                cursor.close();
                return firstPhone;
            }
        }
        cursor.close();
        return "0";
    }

    private String getSender(String senderPhone) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        // Получает всю информацию о юзере, который прислал нам сообщение
        // Таблицы: services
        // Условия: уточняем id сервиса
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_NAME_USERS
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_USERS
                        + " WHERE "
                        + DBHelper.KEY_USER_ID + " = ?";
        Cursor cursor = database.rawQuery(sqlQuery, new String[]{senderPhone});
        if(cursor.moveToFirst()){

            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_USERS);
            return cursor.getString(indexName);
        }
        cursor.close();
        return "";
    }

    private void createMessage(String dialogId,String senderPhone) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Message message = new Message();
        // Получает времяы, отменен, сервис id из message && дату из working days
        // Таблицы: messages, working days
        // Условия: уточняем id диалога, связь таблиц по id дня
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_TIME_MESSAGES + ", "
                        + DBHelper.KEY_DATE_WORKING_DAYS + ", "
                        + DBHelper.KEY_IS_CANCELED_MESSAGES + ", "
                        + DBHelper.KEY_SERVICE_ID_WORKING_DAYS
                        + " FROM "
                        + DBHelper.TABLE_MESSAGES + ", " + DBHelper.TABLE_WORKING_DAYS
                        + " WHERE "
                        + DBHelper.KEY_DIALOG_ID_MESSAGES + " = ?"
                        + " AND "
                        + DBHelper.KEY_DAY_ID_MESSAGES
                        + " = "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_ID;

        Log.d(TAG, "createMessage: " + sqlQuery);
        //order by по дате и времени?
        Cursor cursor = database.rawQuery(sqlQuery, new String[]{dialogId});

        if(cursor.moveToFirst()){
            Log.d(TAG, "createMessage: ");
            int indexTime = cursor.getColumnIndex(DBHelper.KEY_TIME_MESSAGES);
            int indexIsCanceled = cursor.getColumnIndex(DBHelper.KEY_IS_CANCELED_MESSAGES);
            int indexDateWorkingDay = cursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);
            int indexServiceId =  cursor.getColumnIndex(DBHelper.KEY_SERVICE_ID_WORKING_DAYS);
            do {
                message.setDate(cursor.getString(indexDateWorkingDay));
                message.setTime(cursor.getString(indexTime));
                message.setIsCanceled(Boolean.valueOf(cursor.getString(indexIsCanceled)));

                String serviceId = cursor.getString(indexServiceId);
                message.setServiceName(getService(serviceId));

                message.setUserName(getSender(senderPhone));
                addToScreen(message);
            }while (cursor.moveToNext());
        }
        else {
            Log.d(TAG, "PIDOR");
        }
        cursor.close();
    }

    private String getService(String serviceId) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        // Получает всю информацию о сервисе
        // Таблицы: services
        // Условия: уточняем id сервиса
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_NAME_SERVICES
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE "
                        + DBHelper.KEY_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{serviceId});

        if(cursor.moveToFirst()){

            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);

            return cursor.getString(indexName);
        }
        cursor.close();
        return "";
    }


    private void addToScreen(Message message) {

        fElement = new MessageOrderElement(message);

        transaction = manager.beginTransaction();
        transaction.add(R.id.resultsMessageLayout, fElement);
        transaction.commit();
    }

    private  String getUserId(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);

        return sPref.getString(PHONE_NUMBER, "-");
    }
}
