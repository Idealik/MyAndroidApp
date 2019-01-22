package com.example.ideal.myapplication.other;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.fragments.Message;
import com.example.ideal.myapplication.fragments.MessageOrderElement;

public class Messages extends AppCompatActivity {

    final  String TAG = "DBInf";
    private final String FILE_NAME = "Info";
    private final String PHONE_NUMBER = "Phone number";
    private final String DIALOG_ID = "dialog id";

    private String myPhone;
    private DBHelper dbHelper;

    private FragmentManager manager;

    LinearLayout messagesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages);

        String dialogId = getIntent().getStringExtra(DIALOG_ID);
        myPhone = getUserId();
        manager = getSupportFragmentManager();
        dbHelper = new DBHelper(this);

        messagesLayout = findViewById(R.id.resultsMessageLayout);

        // получаем телефон нашего собеседеника
        String senderPhone = getSenderPhone(dialogId);

        if (!senderPhone.equals("0")) {
            //выводим на экран сообщения из LocalStorage для воркера
            createMessages(dialogId,senderPhone, myPhone);

            //выводим на экран сообщения из LocalStorage для юзера
            createMessages(dialogId,myPhone, senderPhone);
            //updateMessages(dialogId,senderPhone);
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

        if(cursor.moveToFirst()) {
            int indexFirstPhone = cursor.getColumnIndex(DBHelper.KEY_FIRST_USER_ID_DIALOGS);
            int indexSecondPhone = cursor.getColumnIndex(DBHelper.KEY_SECOND_USER_ID_DIALOGS);

            String firstPhone = cursor.getString(indexFirstPhone);
            String secondPhone = cursor.getString(indexSecondPhone);
            if(firstPhone.equals(myPhone)){
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

    private String getSenderName(String senderPhone) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        // Получает имя пользователя, который отправил нам сообщение
        // Таблицы: Users
        // Условия: уточняем id пользователя
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

    private void createMessages(String dialogId, String userPhone, String workerPhone) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Message message = new Message();
        // Получает id сообщения, время сообщения, отменена ли запись, дату и время сеанса, id сервиса
        // Таблицы: messages, working days, working time
        // Условия: уточняем id диалога, id пользователя, связь таблиц по id дня
        String sqlQuery =
                "SELECT "
                        + DBHelper.TABLE_MESSAGES +"."+ DBHelper.KEY_ID + ", "
                        + DBHelper.KEY_TIME_MESSAGES + ", "
                        + DBHelper.KEY_IS_CANCELED_MESSAGES + ", "
                        + DBHelper.KEY_DATE_WORKING_DAYS + ", "
                        + DBHelper.KEY_SERVICE_ID_WORKING_DAYS + ", "
                        + DBHelper.KEY_TIME_WORKING_TIME
                        + " FROM "
                        + DBHelper.TABLE_MESSAGES + ", "
                        + DBHelper.TABLE_WORKING_DAYS + ", "
                        + DBHelper.TABLE_WORKING_TIME
                        + " WHERE "
                        + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME
                        + " = "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_ID
                        + " AND "
                        + DBHelper.KEY_DAY_ID_MESSAGES
                        + " = "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_ID
                        + " AND "
                        + DBHelper.KEY_DIALOG_ID_MESSAGES + " = ?"
                        + " AND "
                        + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{dialogId, userPhone});

        if(cursor.moveToFirst()){
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexTime = cursor.getColumnIndex(DBHelper.KEY_TIME_MESSAGES);
            int indexIsCanceled = cursor.getColumnIndex(DBHelper.KEY_IS_CANCELED_MESSAGES);
            int indexDateWorkingDay = cursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);
            int indexServiceId =  cursor.getColumnIndex(DBHelper.KEY_SERVICE_ID_WORKING_DAYS);
            int indexOrderTime =  cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);
            do {
                String serviceId = cursor.getString(indexServiceId);
                if(isMyService(serviceId)) {
                    message.setId(cursor.getString(indexId));
                    message.setServiceName(getService(serviceId));
                    message.setDate(cursor.getString(indexDateWorkingDay));
                    message.setTime(cursor.getString(indexTime));
                    message.setOrderTime(cursor.getString(indexOrderTime));
                    message.setIsCanceled(Boolean.valueOf(cursor.getString(indexIsCanceled)));
                    message.setUserName(getSenderName(userPhone));

                    addToScreen(message);
                } else {
                    boolean isCanceled = Boolean.valueOf(cursor.getString(indexIsCanceled));
                    if(isCanceled) {
                        message.setUserName(getSenderName(workerPhone));
                        message.setOrderTime(cursor.getString(indexOrderTime));
                        message.setDate(cursor.getString(indexDateWorkingDay));
                        message.setServiceName(getService(serviceId));
                        message.setTime(cursor.getString(indexTime));

                        addNotificationToScreen(message);
                    }
                }
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    // тест
    /*
    private void updateMessages(String dialogId, String senderPhone) {

        Query messagesQuery = FirebaseDatabase.getInstance().getReference("message orders")
                .orderByChild(DIALOG_ID)
                .equalTo(dialogId);

        messagesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    } */

    private boolean isMyService(String serviceId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        // Получает id сервиса
        // Таблицы: services
        // Условия: уточняем id сервиса и id воркера
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_ID
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE "
                        + DBHelper.KEY_ID + " = ? "
                        + " AND "
                        + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{serviceId, myPhone});
        cursor.close();
        return cursor.moveToFirst();
    }

    private String getService(String serviceId) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        // Получает имя сервиса
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
        MessageOrderElement fElement = new MessageOrderElement(message);

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.resultsMessageLayout, fElement);
        transaction.commit();
    }

    @SuppressLint("SetTextI18n")
    private void addNotificationToScreen(Message message) {
        TextView notificationText = new TextView(this);
        notificationText.setText("Работник " + message.getUserName()
                + " по некоторым причинам не сможет обслужить вас.\n Запись на "
                + message.getOrderTime() + " " + message.getDate()
                + " на услугу " + message.getServiceName() + " отменена.");
        notificationText.setBackgroundColor(Color.rgb(130, 216, 233));

        if(notificationText.getParent() != null) {
            ((ViewGroup)notificationText.getParent()).removeView(notificationText);
        }
        messagesLayout.addView(notificationText);
    }


    private  String getUserId(){
        SharedPreferences sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);

        return sPref.getString(PHONE_NUMBER, "-");
    }
}
