package com.example.ideal.myapplication.other;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.fragments.DialogElement;
import com.example.ideal.myapplication.fragments.Message;
import com.example.ideal.myapplication.fragments.User;
import com.example.ideal.myapplication.helpApi.WorkWithTimeApi;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class Dialogs extends AppCompatActivity {

    private static final String TAG = "DBInf";

    private static final String FILE_NAME = "Info";
    private static final String PHONE_NUMBER = "Phone number";

    private static final String DIALOGS = "dialogs";
    private static final String FIRST_PHONE = "first phone";
    private static final String SECOND_PHONE = "second phone";

    private static final String USERS = "users";
    private static final String USER_ID = "user id";
    private static final String NAME = "name";
    private static final String CITY = "city";

    private static final String MESSAGE_ORDERS = "message orders";
    private static final String MESSAGE_TIME = "message time";
    private static final String DATE = "date";
    private static final String DIALOG_ID = "dialog id";
    private static final String IS_CANCELED = "is canceled";

    private static final String WORKING_TIME = "working time";
    private static final String WORKING_DAYS_ID = "working day id";
    private static final String TIME = "time";

    private static final String WORKING_DAYS = "working days";
    private static final String SERVICE_ID = "service id";

    private static final String SERVICES = "services";
    private static final String MESSAGE_REVIEWS = "message reviews" ;
    private static final String IS_RATE_BY_USER = "is rate by user" ;
    private static final String IS_RATE_BY_WORKER = "is rate by worker" ;
    private static final String TIME_ID = "time id";

    WorkWithTimeApi workWithTimeApi;
    SharedPreferences sPref;
    DBHelper dbHelper;

    DialogElement dElement;
    FragmentManager manager;
    FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogs);

        manager = getSupportFragmentManager();
        dbHelper = new DBHelper(this);
        workWithTimeApi = new WorkWithTimeApi();
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(DBHelper.TABLE_MESSAGE_ORDERS, null, null);
        database.delete(DBHelper.TABLE_MESSAGE_REVIEWS, null, null);

        database.delete(DBHelper.TABLE_DIALOGS, null, null);

    }

    // Подгружает все мои диалоги и всё что с ними связано из Firebase
    private void loadDialogs() {

        // Загрузка диалогов, где мой номер стоит на 1-м месте
        loadDialogsByFirsPhone();

        // Загрузка диалогов, где мой номер стоит на 2-м месте
        loadDialogsBySecondPhone();
    }

    private void loadDialogsByFirsPhone() {
        final String myPhone = getUserPhone();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        Query query1 = database.getReference(DIALOGS)
                .orderByChild(FIRST_PHONE)
                .equalTo(myPhone);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dialogs) {
                for(DataSnapshot dialog:dialogs.getChildren()) {
                    String secondPhone = String.valueOf(dialog.child(SECOND_PHONE).getValue());
                    String dialogId = dialog.getKey();

                    // Добавляет(обновляет) диалог в LocalStorage и возвращает true если это диалог новый
                    boolean isNew = addDialogInLocalStorage(dialogId, myPhone, secondPhone);

                    loadInterlocutor(secondPhone, dialogId, isNew);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    private void loadDialogsBySecondPhone() {
        final String myPhone = getUserPhone();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        Query query2 = database.getReference(DIALOGS)
                .orderByChild(SECOND_PHONE)
                .equalTo(myPhone);
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dialogs) {
                for(DataSnapshot dialog:dialogs.getChildren()){
                    final String firstPhone = String.valueOf(dialog.child(FIRST_PHONE).getValue());
                    final String dialogId = dialog.getKey();

                    boolean isNew = addDialogInLocalStorage(dialogId, firstPhone, myPhone);

                    loadInterlocutor(firstPhone, dialogId, isNew);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    private void loadInterlocutor(final String phone, final String dialogId, final boolean isNew) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USERS).child(phone);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {

                String name = String.valueOf(userSnapshot.child(NAME).getValue());
                String city = String.valueOf(userSnapshot.child(CITY).getValue());

                User user = new User();
                user.setPhone(phone);
                user.setName(name);
                user.setCity(city);

                addUserInLocalStorage(user);

                addMessagesInLocalStorage(dialogId);

                if(isNew) {
                    addToScreen(dialogId, name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    private void addUserInLocalStorage(User user) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String phone = user.getPhone();

        String sqlQuery = "SELECT * FROM "
                + DBHelper.TABLE_CONTACTS_USERS
                + " WHERE "
                + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[] {phone});

        contentValues.put(DBHelper.KEY_NAME_USERS, user.getName());
        contentValues.put(DBHelper.KEY_CITY_USERS, user.getCity());

        if(cursor.moveToFirst()) {
            database.update(DBHelper.TABLE_CONTACTS_USERS, contentValues,
                    DBHelper.KEY_USER_ID + " = ?",
                    new String[]{String.valueOf(phone)});
        } else {
            contentValues.put(DBHelper.KEY_USER_ID, phone);
            database.insert(DBHelper.TABLE_CONTACTS_USERS, null, contentValues);
        }
        cursor.close();
    }

    private boolean addDialogInLocalStorage(String dialogId, String firstPhone, String secondPhone) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        String sqlQuery = "SELECT * FROM "
                + DBHelper.TABLE_DIALOGS
                + " WHERE "
                + DBHelper.KEY_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[] {dialogId});

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_FIRST_USER_ID_DIALOGS, firstPhone);
        contentValues.put(DBHelper.KEY_SECOND_USER_ID_DIALOGS, secondPhone);

        if(cursor.moveToFirst()) {
            database.update(DBHelper.TABLE_DIALOGS,
                    contentValues,
                    DBHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(dialogId)});
            cursor.close();
            return false;
        } else {
            contentValues.put(DBHelper.KEY_ID, dialogId);
            database.insert(DBHelper.TABLE_DIALOGS, null, contentValues);
            cursor.close();
            return true;
        }
    }

    private void addMessagesInLocalStorage(final String dialogId) {
        //если разница между timeId 24 часа и у message isCanceled не отменено, мы генерим 2 ревью
        loadMessageOrders(dialogId);

        loadMessageReviews(dialogId);
    }

    private void loadMessageOrders(final String dialogId){
        //message_orders
        Query messagesQuery = FirebaseDatabase.getInstance().getReference(MESSAGE_ORDERS)
                .orderByChild(DIALOG_ID)
                .equalTo(dialogId);

        messagesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot messages) {
                SQLiteDatabase database = dbHelper.getWritableDatabase();

                // берем всю информацию из таблицы MR, чтобы либо сделать update, либо insert
                String sqlQuery = "SELECT * FROM "
                        + DBHelper.TABLE_MESSAGE_ORDERS
                        + " WHERE "
                        + DBHelper.KEY_ID + " = ?";
                Cursor cursor;

                for(DataSnapshot message:messages.getChildren()){
                    // идем по всем сообщениям
                    Message myMessage = new Message();

                    String messageId = message.getKey();
                    String messageTimeId = String.valueOf(message.child(TIME_ID).getValue());
                    String isCanceled = String.valueOf(message.child(IS_CANCELED).getValue());
                    String time = String.valueOf(message.child(TIME).getValue());

                    myMessage.setId(messageId); // для отладки
                    myMessage.setTimeId(messageTimeId);
                    myMessage.setIsCanceled(Boolean.valueOf(isCanceled));
                    myMessage.setDialogId(dialogId);

                    // подргужаем время, потом день и сам сервис этого сообщения
                    addTimeInLocalStorage(myMessage);

                    cursor = database.rawQuery(sqlQuery, new String[] {messageId});

                    //метод, который добавляет ревью в firebase, если !isCanceled и sysdate - timeId + date > 24h

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DBHelper.KEY_DIALOG_ID_MESSAGES, dialogId);
                    contentValues.put(DBHelper.KEY_TIME_ID_MESSAGES, messageTimeId);
                    contentValues.put(DBHelper.KEY_IS_CANCELED_MESSAGE_ORDERS, isCanceled);
                    contentValues.put(DBHelper.KEY_MESSAGE_TIME_MESSAGES, time);

                    // update || insert
                    if(cursor.moveToFirst()) {
                        database.update(DBHelper.TABLE_MESSAGE_ORDERS, contentValues,
                                DBHelper.KEY_ID + " = ?",
                                new String[]{String.valueOf(messageId)});
                    } else {
                        contentValues.put(DBHelper.KEY_ID, messageId);
                        database.insert(DBHelper.TABLE_MESSAGE_ORDERS, null, contentValues);
                    }
                    cursor.close();
                    updateWorkingTimeInLocalStorage(messageId);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    private void loadMessageReviews(final String dialogId) {
        //загружаем message reviews
        //делаем запрос в fireBase по dialogId, который получаем при загрузке страницы
        Query messagesQuery = FirebaseDatabase.getInstance().getReference(MESSAGE_REVIEWS)
                .orderByChild(DIALOG_ID)
                .equalTo(dialogId);

        messagesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot messages) {
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                // берем всю информацию из таблицы MR, чтобы либо сделать update, либо insert
                String sqlQuery = "SELECT * FROM "
                        + DBHelper.TABLE_MESSAGE_REVIEWS
                        + " WHERE "
                        + DBHelper.KEY_ID + " = ?";
                Cursor cursor;
                for(DataSnapshot message:messages.getChildren()){
                    // идем по всем сообщениям
                    Message myMessage = new Message();
                    String messageId = message.getKey();
                    String messageTimeId = String.valueOf(message.child(TIME_ID).getValue());
                    String isRateByUser = String.valueOf(message.child(IS_RATE_BY_USER).getValue());
                    String isRateByWorker = String.valueOf(message.child(IS_RATE_BY_WORKER).getValue());
                    String time = String.valueOf(message.child(TIME).getValue());

                    myMessage.setId(messageId); // для отладки
                    myMessage.setTimeId(messageTimeId);
                    myMessage.setIsCanceled(true);
                    myMessage.setDialogId(dialogId);

                    // подргужаем время, потом день и сам сервис этого сообщения
                    addTimeInLocalStorage(myMessage);

                    cursor = database.rawQuery(sqlQuery, new String[] {messageId});

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DBHelper.KEY_DIALOG_ID_MESSAGES, dialogId);
                    contentValues.put(DBHelper.KEY_TIME_ID_MESSAGES, messageTimeId);
                    contentValues.put(DBHelper.KEY_IS_RATE_BY_USER_MESSAGE_REVIEWS, isRateByUser);
                    contentValues.put(DBHelper.KEY_IS_RATE_BY_WORKER_MESSAGE_REVIEWS, isRateByWorker);
                    contentValues.put(DBHelper.KEY_MESSAGE_TIME_MESSAGES, time);

                    // update || insert
                    if(cursor.moveToFirst()) {
                        database.update(DBHelper.TABLE_MESSAGE_REVIEWS, contentValues,
                                DBHelper.KEY_ID + " = ?",
                                new String[]{String.valueOf(messageId)});
                    } else {
                        contentValues.put(DBHelper.KEY_ID, messageId);
                        database.insert(DBHelper.TABLE_MESSAGE_REVIEWS, null, contentValues);
                    }
                    cursor.close();
                    updateWorkingTimeInLocalStorage(messageId);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });

    }

    private void addTimeInLocalStorage(final Message message) {
        //берем время из fireBase и сохраняем его в sqlLite
        final String timeId =  message.getTimeId();

        DatabaseReference timeRef = FirebaseDatabase.getInstance().
                getReference(WORKING_TIME)
                .child(timeId);

        timeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot time) {
                final SQLiteDatabase database = dbHelper.getWritableDatabase();
                final ContentValues contentValues = new ContentValues();

                String sqlQuery = "SELECT * FROM "
                        + DBHelper.TABLE_WORKING_TIME
                        + " WHERE "
                        + DBHelper.KEY_ID + " = ?";

                Cursor cursor = database.rawQuery(sqlQuery, new String[]{timeId});

                String myTime = String.valueOf(time.child("time").getValue());
                String userId = String.valueOf(time.child("user id").getValue());
                String dayId = String.valueOf(time.child("working day id").getValue());

                message.setOrderTime(myTime);

                // получаем день этого сообщения
                addDayInLocalStorage(dayId,message);

                contentValues.put(DBHelper.KEY_TIME_WORKING_TIME, myTime);
                contentValues.put(DBHelper.KEY_USER_ID, userId);
                contentValues.put(DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME, dayId);
                if (cursor.moveToFirst()) {
                    database.update(DBHelper.TABLE_WORKING_TIME, contentValues,
                            DBHelper.KEY_ID + " = ?",
                            new String[]{String.valueOf(timeId)});
                } else {
                    contentValues.put(DBHelper.KEY_ID, timeId);
                    database.insert(DBHelper.TABLE_WORKING_TIME, null, contentValues);
                }
                cursor.close();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    private void addDayInLocalStorage(final String dayId, final Message message) {

        // загружаем дни, которые связаны с сообщением
        DatabaseReference dayRef = FirebaseDatabase.getInstance().getReference(WORKING_DAYS)
                .child(dayId);


        dayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot day) {
                SQLiteDatabase database = dbHelper.getWritableDatabase();

                String sqlQuery = "SELECT * FROM "
                        + DBHelper.TABLE_WORKING_DAYS
                        + " WHERE "
                        + DBHelper.KEY_ID + " = ?";
                Cursor cursor = database.rawQuery(sqlQuery, new String[]{dayId});

                String date = String.valueOf(day.child("data").getValue());
                String serviceId = String.valueOf(day.child(SERVICE_ID).getValue());

                long sysdate = workWithTimeApi.getSysdateLong();
                long orderDate =  workWithTimeApi.getMillisecondsStringDate(date + " "+ message.getOrderTime());
                long dayLong = 86400000;
                Log.d(TAG, "ID: " + message.getId());
                //если не отменено и прошел день, то мы проверяем есть ли такие ревью уже
                if(!message.getIsCanceled() && (sysdate-orderDate>dayLong)){
                    checkMessageReview(message);
                }
                loadServiceInLocalStorage(serviceId);

                ContentValues contentValues = new ContentValues();
                contentValues.put(DBHelper.KEY_DATE_WORKING_DAYS, date);
                contentValues.put(DBHelper.KEY_SERVICE_ID_WORKING_DAYS, serviceId);
                if (cursor.moveToFirst()) {
                    database.update(DBHelper.TABLE_WORKING_DAYS, contentValues,
                            DBHelper.KEY_ID + " = ?",
                            new String[]{String.valueOf(dayId)});
                } else {
                    contentValues.put(DBHelper.KEY_ID, dayId);
                    database.insert(DBHelper.TABLE_WORKING_DAYS, null, contentValues);
                }
                cursor.close();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    private void checkMessageReview(final Message message) {

        //делаем запрос по диалог id этого order, но так как у них они одинаковые с review
        //то он найдет также и review из таблицы review
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query query = database.getReference(MESSAGE_REVIEWS)
                .orderByChild("dialog id")
                .equalTo(message.getDialogId());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot times) {
                //если  = 0, значит ревью точно нету, все хорошо
                int countEqualTimes = 0;
                if (times.getChildrenCount() == 0) {
                    createMessageReview(message);
                    loadMessageReviews(message.getDialogId());
                } else {//проверить на сравнение времени!
                    for (DataSnapshot time : times.getChildren()) {
                        //если ни разу время не совпало (countEqualTimes = 0), то такого ревью нету и можно добавить
                        if (time.child("time id").getValue().equals(message.getTimeId())){ // и не оценено?
                            countEqualTimes++;
                        }

                    }

                    if(countEqualTimes == 0){
                        createMessageReview(message);
                        loadMessageReviews(message.getDialogId());
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    private void loadServiceInLocalStorage(final String serviceId) {
        DatabaseReference serviceRef = FirebaseDatabase.getInstance().getReference(SERVICES)
                .child(serviceId);

        serviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot service) {
                SQLiteDatabase database = dbHelper.getWritableDatabase();

                String sqlQuery = "SELECT * FROM "
                        + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE "
                        + DBHelper.KEY_ID + " = ?";
                Cursor cursor = database.rawQuery(sqlQuery, new String[]{serviceId});

                String name = String.valueOf(service.child(NAME).getValue());

                ContentValues contentValues = new ContentValues();
                contentValues.put(DBHelper.KEY_NAME_SERVICES, name);

                if (cursor.moveToFirst()) {
                    database.update(DBHelper.TABLE_CONTACTS_SERVICES, contentValues,
                            DBHelper.KEY_ID + " = ?",
                            new String[]{String.valueOf(serviceId)});
                } else {
                    contentValues.put(DBHelper.KEY_ID, serviceId);
                    database.insert(DBHelper.TABLE_CONTACTS_SERVICES, null, contentValues);
                }
                cursor.close();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    private void updateWorkingTimeInLocalStorage(String messageId) {
        //получить id message
        //получить date (id working days)
        //сделать query по date в working time и получить id времени
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(MESSAGE_ORDERS)
                .child(messageId)
                .child(DATE);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dateId) {
                String date = String.valueOf(dateId.getValue());

                Query query = database.getReference(WORKING_TIME)
                        .orderByChild(WORKING_DAYS_ID)
                        .equalTo(date);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot time: dataSnapshot.getChildren()) {
                            final String timeId = String.valueOf(time.getKey());

                            DatabaseReference myRef = database.getReference(WORKING_TIME)
                                    .child(timeId)
                                    .child(USER_ID);

                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    SQLiteDatabase database = dbHelper.getWritableDatabase();

                                    String phone =  String.valueOf(dataSnapshot.getValue());

                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put(DBHelper.KEY_USER_ID, phone);

                                    database.update(DBHelper.TABLE_WORKING_TIME, contentValues,
                                            DBHelper.KEY_ID + " = ? ",
                                            new String[]{String.valueOf(timeId)});
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    attentionBadConnection();
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        attentionBadConnection();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    private String getUserPhone() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);

        return  sPref.getString(PHONE_NUMBER, "-");
    }

    private void createMessageReview(Message message){
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference(MESSAGE_REVIEWS);
        Map<String,Object> items = new HashMap<>();

        String dateNow = workWithTimeApi.getCurDateInFormatHMS();

        items.put(DIALOG_ID, message.getDialogId());
        items.put(MESSAGE_TIME, dateNow);
        items.put(TIME_ID, message.getTimeId());
        items.put(IS_RATE_BY_USER, false);
        items.put(IS_RATE_BY_WORKER, false);

        String messageId =  myRef.push().getKey();
        myRef = database.getReference(MESSAGE_REVIEWS).child(messageId);
        myRef.updateChildren(items);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadDialogs();
    }

    private void addToScreen(String dialogId, String name) {
        dElement = new DialogElement(dialogId, name);
        transaction = manager.beginTransaction();
        transaction.add(R.id.mainDialogsLayout, dElement);
        transaction.commit();
    }

    private void attentionBadConnection() {
        Toast.makeText(this,"Плохое соединение",Toast.LENGTH_SHORT).show();
    }
}