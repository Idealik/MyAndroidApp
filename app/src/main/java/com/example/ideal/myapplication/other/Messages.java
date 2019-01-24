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
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.fragments.Message;
import com.example.ideal.myapplication.fragments.MessageOrderElement;

public class Messages extends AppCompatActivity {

    private static final String TAG = "DBInf";

    private static final String FILE_NAME = "Info";
    private static final String PHONE_NUMBER = "Phone number";

    private static final String DIALOG_ID = "dialog id";

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
            createMessages(dialogId, senderPhone);
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

    private void createMessages(String dialogId, String otherPhone) {

        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Message message = new Message();
        // Получает id сообщения, время сообщения, отменена ли запись, id дня записи
        // Таблицы: messages
        // Условия: уточняем id диалога
        String messageQuery =
                "SELECT "
                        + DBHelper.KEY_ID + ", "
                        + DBHelper.KEY_MESSAGE_TIME_MESSAGES + ", "
                        + DBHelper.KEY_IS_CANCELED_MESSAGE_ORDERS + ", "
                        + DBHelper.KEY_TIME_ID_MESSAGES
                        + " FROM "
                        + DBHelper.TABLE_MESSAGE_ORDERS
                        + " WHERE "
                        + DBHelper.KEY_DIALOG_ID_MESSAGES + " = ?"
                        + " ORDER BY "
                        + DBHelper.KEY_MESSAGE_TIME_MESSAGES;
        Cursor messageCursor = database.rawQuery(messageQuery, new String[]{dialogId});

        if (messageCursor.moveToFirst()) {
            int indexMessageId = messageCursor.getColumnIndex(DBHelper.KEY_ID);
            int indexTime = messageCursor.getColumnIndex(DBHelper.KEY_MESSAGE_TIME_MESSAGES);
            int indexIsCanceled = messageCursor.getColumnIndex(DBHelper.KEY_IS_CANCELED_MESSAGE_ORDERS);
            int indexTimeId = messageCursor.getColumnIndex(DBHelper.KEY_TIME_ID_MESSAGES);
            do {
                boolean isCanceled = Boolean.valueOf(messageCursor.getString(indexIsCanceled));

                String timeId = messageCursor.getString(indexTimeId);

                // Получает дату записи, id сервиса
                // Таблицы: working days
                // Условия: уточняем id дня
                String dayQuery =
                        "SELECT "
                                + DBHelper.TABLE_WORKING_DAYS +"."+ DBHelper.KEY_ID + ", "
                                + DBHelper.KEY_DATE_WORKING_DAYS + ", "
                                + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + ", "
                                + DBHelper.KEY_SERVICE_ID_WORKING_DAYS
                                + " FROM "
                                + DBHelper.TABLE_WORKING_DAYS + ", "
                                + DBHelper.TABLE_WORKING_TIME
                                + " WHERE "
                                + DBHelper.TABLE_WORKING_TIME + "." + DBHelper.KEY_ID + " = ?"
                                +" AND "
                                + DBHelper.TABLE_WORKING_DAYS +"."+ DBHelper.KEY_ID
                                + " = "
                                + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME;

                Cursor dayCursor = database.rawQuery(dayQuery, new String[]{timeId});

                if (dayCursor.moveToFirst()) {
                    int indexDate = dayCursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);
                    int indexServiceId = dayCursor.getColumnIndex(DBHelper.KEY_SERVICE_ID_WORKING_DAYS);
                    int indexDayId = dayCursor.getColumnIndex(DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME);
                    String dayId = dayCursor.getString(indexDayId);

                    String serviceId = dayCursor.getString(indexServiceId);
                    if (!isCanceled) {
                        if (isMyService(serviceId)) {

                            // Получает время записи
                            // Таблицы: working time
                            // Условия: уточняем по id пользователя и id дня
                            String timeQuery =
                                    "SELECT "
                                            + DBHelper.KEY_TIME_WORKING_TIME + ", "
                                            + DBHelper.KEY_ID
                                            + " FROM "
                                            + DBHelper.TABLE_WORKING_TIME
                                            + " WHERE "
                                            + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + " = ?"
                                            + " AND "
                                            + DBHelper.KEY_USER_ID + " = ?";
                            Cursor timeCursor = database.rawQuery(timeQuery, new String[]{dayId, otherPhone});

                            if (timeCursor.moveToFirst()) {
                                int indexOrderTime = timeCursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);
                                int indexTimeIdSecond = timeCursor.getColumnIndex(DBHelper.KEY_ID);
                                message.setId(messageCursor.getString(indexMessageId));
                                message.setServiceName(getService(serviceId));
                                message.setDate(dayCursor.getString(indexDate));
                                message.setTime(messageCursor.getString(indexTime));
                                message.setIsCanceled(Boolean.valueOf(messageCursor.getString(indexIsCanceled)));
                                message.setUserName(getSenderName(otherPhone));
                                message.setOrderTime(timeCursor.getString(indexOrderTime));
                                message.setDialogId(dialogId);
                                message.setTimeId(timeCursor.getString(indexTimeIdSecond));

                                addToScreen(message);
                            }
                        }
                    } else {
                        if (!isMyService(serviceId)) {
                            message.setUserName(getSenderName(otherPhone));
                            message.setDate(dayCursor.getString(indexDate));
                            message.setServiceName(getService(serviceId));
                            message.setTime(messageCursor.getString(indexTime));

                            addNotificationToScreen(message);
                        }
                    }
                }
            } while (messageCursor.moveToNext());
        }
    }

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
                + message.getDate()
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
