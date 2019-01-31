package com.example.ideal.myapplication.other;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.fragments.Message;
import com.example.ideal.myapplication.fragments.MessageOrderElement;
import com.example.ideal.myapplication.fragments.MessageReviewElement;
import com.example.ideal.myapplication.fragments.notification_canceled_element;
import com.example.ideal.myapplication.fragments.notification_no_canceled_element;

public class Messages extends AppCompatActivity {

    private static final String TAG = "DBInf";

    private static final String FILE_NAME = "Info";
    private static final String PHONE_NUMBER = "Phone number";

    private static final String DIALOG_ID = "dialog id";
    private static final String TIME_ID = "time id";
    private static final String IS_RATE = "is rate";

    private String myPhone;
    private String dialogId;
    private String senderPhone;
    private DBHelper dbHelper;

    private FragmentManager manager;

    LinearLayout messagesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages);

        dialogId = getIntent().getStringExtra(DIALOG_ID);
        myPhone = getUserId();
        manager = getSupportFragmentManager();
        dbHelper = new DBHelper(this);

        messagesLayout = findViewById(R.id.resultsMessageLayout);

        // получаем телефон нашего собеседеника
        senderPhone = getSenderPhone(dialogId);

        /*if (!senderPhone.equals("0")) {
            //выводим на экран сообщения из LocalStorage для воркера
            //createMessages(dialogId, senderPhone);
            //updateMessages(dialogId,senderPhone);
        }*/
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

    private void createMessages() {

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
            int indexMessageTime = messageCursor.getColumnIndex(DBHelper.KEY_MESSAGE_TIME_MESSAGES);
            int indexIsCanceled = messageCursor.getColumnIndex(DBHelper.KEY_IS_CANCELED_MESSAGE_ORDERS);
            int indexTimeId = messageCursor.getColumnIndex(DBHelper.KEY_TIME_ID_MESSAGES);

            // Цикл по всем сообщениям в диалоге пользователя
            do {
                String timeId = messageCursor.getString(indexTimeId);

                // Получает дату и время записи, id сервиса
                // Таблицы: working days, working time
                // Условия: уточняем id времени, связываем таблицы по id дня
                String dayQuery =
                        "SELECT "
                                + DBHelper.KEY_DATE_WORKING_DAYS + ", "
                                + DBHelper.KEY_SERVICE_ID_WORKING_DAYS + ", "
                                + DBHelper.KEY_TIME_WORKING_TIME
                                + " FROM "
                                + DBHelper.TABLE_WORKING_DAYS + ", "
                                + DBHelper.TABLE_WORKING_TIME
                                + " WHERE "
                                + DBHelper.TABLE_WORKING_TIME + "." + DBHelper.KEY_ID + " = ?"
                                +" AND "
                                + DBHelper.TABLE_WORKING_DAYS +"."+ DBHelper.KEY_ID
                                + " = "
                                + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME;

                Cursor dayTimeCursor = database.rawQuery(dayQuery, new String[]{timeId});

                if (dayTimeCursor.moveToFirst()) {
                    int indexDate = dayTimeCursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);
                    int indexServiceId = dayTimeCursor.getColumnIndex(DBHelper.KEY_SERVICE_ID_WORKING_DAYS);
                    int indexOrderTime = dayTimeCursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);

                    String serviceId = dayTimeCursor.getString(indexServiceId);
                    if (!serviceId.equals(null)) {
                        boolean isCanceled = Boolean.valueOf(messageCursor.getString(indexIsCanceled));

                        boolean isMyService = isMyService(serviceId);
                        if (isCanceled) {
                            if (!isMyService) {
                                //сообщение для юзера
                                message.setUserName(getSenderName(senderPhone));
                                message.setDate(dayTimeCursor.getString(indexDate));
                                message.setServiceName(getService(serviceId));
                                message.setMessageTime(messageCursor.getString(indexMessageTime));
                                message.setOrderTime(dayTimeCursor.getString(indexOrderTime));
                                message.setTimeId(timeId);

                                addNotificationCanceledToScreen(message);
                                checkMessageReview(message,"user");
                                dayTimeCursor.close();
                            }
                        } else {
                            if (isMyService) {
                                // сообщение об отказе для воркера
                                message.setId(messageCursor.getString(indexMessageId));
                                message.setMessageTime(messageCursor.getString(indexMessageTime));
                                message.setIsCanceled(Boolean.valueOf(messageCursor.getString(indexIsCanceled)));
                                message.setServiceName(getService(serviceId));
                                message.setDate(dayTimeCursor.getString(indexDate));
                                message.setUserName(getSenderName(senderPhone));
                                message.setOrderTime(dayTimeCursor.getString(indexOrderTime));
                                message.setDialogId(dialogId);
                                message.setTimeId(timeId);

                                addToScreen(message);
                                checkMessageReview(message,"worker");
                                dayTimeCursor.close();
                            } else {
                                // Выводить сообщение - "Вы успешно записались на услугу ..."
                                message.setUserName(getSenderName(senderPhone));
                                message.setDate(dayTimeCursor.getString(indexDate));
                                message.setServiceName(getService(serviceId));
                                message.setMessageTime(messageCursor.getString(indexMessageTime));
                                message.setOrderTime(dayTimeCursor.getString(indexOrderTime));
                                message.setTimeId(timeId);

                                addNotificationNoCanceledToScreen(message);
                                checkMessageReview(message,"user");
                                dayTimeCursor.close();
                            }
                        }
                    }
                }
            } while (messageCursor.moveToNext());
            messageCursor.close();
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

    private void addNotificationCanceledToScreen(Message message) {
        notification_canceled_element fElement = new notification_canceled_element(message);

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.resultsMessageLayout, fElement);
        transaction.commit();
    }

    private void addNotificationNoCanceledToScreen(Message message) {

        notification_no_canceled_element fElement = new notification_no_canceled_element(message);

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.resultsMessageLayout, fElement);
        transaction.commit();

    }

    private void checkMessageReview(Message orderMessage,String status) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Message message = new Message();
        // Получает id сообщения, время сообщения, отменена ли запись, id дня записи
        // Таблицы: messagesO, messagesR
        // Условия: уточняем id диалога, связываем MO & MR по timeId
        String messageQuery =
                "SELECT "
                        + DBHelper.TABLE_MESSAGE_REVIEWS + "." + DBHelper.KEY_ID + ", "
                        + DBHelper.TABLE_MESSAGE_REVIEWS + "." + DBHelper.KEY_MESSAGE_TIME_MESSAGES + ", "
                        + DBHelper.KEY_IS_RATE_BY_USER_MESSAGE_REVIEWS+ ", "
                        + DBHelper.KEY_IS_RATE_BY_WORKER_MESSAGE_REVIEWS+ ", "
                        + DBHelper.TABLE_MESSAGE_REVIEWS + "." + DBHelper.KEY_TIME_ID_MESSAGES
                        + " FROM "
                        + DBHelper.TABLE_MESSAGE_REVIEWS + ", "
                        + DBHelper.TABLE_MESSAGE_ORDERS
                        + " WHERE "
                        + DBHelper.TABLE_MESSAGE_REVIEWS + "." + DBHelper.KEY_DIALOG_ID_MESSAGES + " = ?"
                        + " AND "
                        + DBHelper.TABLE_MESSAGE_ORDERS + "." +DBHelper.KEY_TIME_ID_MESSAGES + " = ?"
                        + " AND "
                        + DBHelper.TABLE_MESSAGE_REVIEWS + "." + DBHelper.KEY_TIME_ID_MESSAGES
                        + " = "
                        + DBHelper.TABLE_MESSAGE_ORDERS + "." + DBHelper.KEY_TIME_ID_MESSAGES;
        Cursor messageCursor = database.rawQuery(messageQuery, new String[]{dialogId, orderMessage.getTimeId()});

        if (messageCursor.moveToFirst()) {
            int indexMessageId = messageCursor.getColumnIndex(DBHelper.KEY_ID);
            int indexMessageTime = messageCursor.getColumnIndex(DBHelper.KEY_MESSAGE_TIME_MESSAGES);
            int indexIsRateByUser = messageCursor.getColumnIndex(DBHelper.KEY_IS_RATE_BY_USER_MESSAGE_REVIEWS);
            int indexIsRateByWorker = messageCursor.getColumnIndex(DBHelper.KEY_IS_RATE_BY_WORKER_MESSAGE_REVIEWS);
            int indexTimeId = messageCursor.getColumnIndex(DBHelper.KEY_TIME_ID_MESSAGES);

            // Цикл по всем сообщениям в диалоге пользователя
                String timeId = messageCursor.getString(indexTimeId);
                Log.d(TAG, "id " + timeId);

                // Получает дату и время записи, id сервиса
                // Таблицы: working days, working time
                // Условия: уточняем id времени, связываем таблицы по id дня
                String dayQuery =
                        "SELECT "
                                + DBHelper.KEY_DATE_WORKING_DAYS + ", "
                                + DBHelper.KEY_SERVICE_ID_WORKING_DAYS + ", "
                                + DBHelper.KEY_TIME_WORKING_TIME
                                + " FROM "
                                + DBHelper.TABLE_WORKING_DAYS + ", "
                                + DBHelper.TABLE_WORKING_TIME
                                + " WHERE "
                                + DBHelper.TABLE_WORKING_TIME + "." + DBHelper.KEY_ID + " = ?"
                                + " AND "
                                + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_ID
                                + " = "
                                + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME;

                Cursor dayTimeCursor = database.rawQuery(dayQuery, new String[]{timeId});

                if (dayTimeCursor.moveToFirst()) {
                    int indexDate = dayTimeCursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);
                    int indexServiceId = dayTimeCursor.getColumnIndex(DBHelper.KEY_SERVICE_ID_WORKING_DAYS);
                    int indexOrderTime = dayTimeCursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);

                    message.setId(messageCursor.getString(indexMessageId));
                    message.setUserName(getSenderName(senderPhone));
                    message.setServiceName(getService(dayTimeCursor.getString(indexServiceId)));
                    message.setDate(dayTimeCursor.getString(indexDate));
                    message.setMessageTime(messageCursor.getString(indexMessageTime));
                    message.setIsRateByUser(Boolean.valueOf(messageCursor.getString(indexIsRateByUser)));
                    message.setIsRateByWorker(Boolean.valueOf(messageCursor.getString(indexIsRateByWorker)));
                    message.setOrderTime(dayTimeCursor.getString(indexOrderTime));

                    addMessageReviewToScreen(message, dayTimeCursor.getString(indexServiceId),status);
                }
            messageCursor.close();
        }

    }

    private void addMessageReviewToScreen(Message message, String serviceId,String status) {
        //статус нужен, чтобы мы могли понять от кого сообщение и в соответствие с этим
        //вывести подходящее сообщение для юзера или для воркера
        MessageReviewElement fElement = new MessageReviewElement(message, serviceId,status);

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.resultsMessageLayout, fElement);
        transaction.commit();
    }

    private String getUserId(){
        SharedPreferences sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);

        return sPref.getString(PHONE_NUMBER, "-");
    }

    @Override
    protected void onResume() {
        super.onResume();

        messagesLayout.removeAllViews();
        createMessages();
    }
}
