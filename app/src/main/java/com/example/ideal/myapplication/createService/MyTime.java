package com.example.ideal.myapplication.createService;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.ideal.myapplication.other.DBHelper;
import com.example.ideal.myapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class MyTime extends AppCompatActivity  implements View.OnClickListener {

    private final String FILE_NAME = "Info";
    private static final String PHONE_NUMBER = "Phone number";
    private final String WORKING_DAYS_ID = "working day id";
    private static final String WORKING_TIME = "working time/";
    private final String WORKING_DAYS = "working days";
    private final String SERVICES = "services";

    private static final String DIALOGS = "dialogs/";
    private static final String MESSAGES = "message orders/";
    private static final String USER_ID = "user id";
    private final String TAG = "DBInf";

    private final String SERVICE_ID = "service id";
    private final String STATUS_USER_BY_SERVICE = "status User";
    private final int ROWS_COUNT = 6;
    private final int COLUMNS_COUNT = 4;

    String statusUser;
    String userId;
    String workingDaysId;
    String serviceId;
    int width;
    int height;

    Button[][] timeBtns;
    Button saveBtn;

    //временный буфер добавленного рабочего времени
    ArrayList<String> workingHours;
    //временный буфер удалённого рабочего времени
    ArrayList<String> removedHours;
    //
    ArrayList<String> currentHours;

    SwitchCompat amOrPmMyTimeSwitch;

    DBHelper dbHelper;
    SharedPreferences sPref;
    RelativeLayout mainLayout;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_time);

        statusUser = getIntent().getStringExtra(STATUS_USER_BY_SERVICE);
        userId = getUserId();

        mainLayout = findViewById(R.id.mainMyTimeLayout);

        timeBtns = new Button[ROWS_COUNT][COLUMNS_COUNT];
        saveBtn = findViewById(R.id.saveMyTimeBtn);

        amOrPmMyTimeSwitch = findViewById(R.id.amOrPmMyTimeSwitch);

        //инициализация буферов
        workingHours = new ArrayList<>();
        removedHours = new ArrayList<>();
        currentHours = new ArrayList<>();

        serviceId = getIntent().getStringExtra(SERVICE_ID);

        //получение парамтров экрана
        Display display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();

        addButtonsOnScreen(width,height, false);

        dbHelper = new DBHelper(this);

        checkCurrentTimes();

        amOrPmMyTimeSwitch.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isPm) {
                // Очищаем layout
                mainLayout.removeAllViews();
                if(isPm) {
                    buttonView.setText("Вторая половина дня");
                    // создаем кнопки с нужным временем
                    addButtonsOnScreen(width,height, true);

                } else {
                    buttonView.setText("Первая половина дня");
                    // создаем кнопки с нужным временем
                    addButtonsOnScreen(width,height, false);
                }
                // Выделяет кнопки
                checkCurrentTimes();
                // Выделяет кнопки хронящиеся в буфере рабочих дней
                checkWorkingHours();
                // Снимает выделение с кнопок хронящихся в буфере удалённых дней
                checkRemovedHours();
            }
        });

        saveBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveMyTimeBtn:
                if(statusUser.equals("worker")) {
                    if (workingHours.size() > 0) {
                        // Добавляем время из буфера workingHours в БД
                        addTime();
                    }
                    if (removedHours.size() > 0) {
                        // Удаляем время сохранённое в буфере removeHours в БД
                        deleteTime();
                    }
                    Toast.makeText(this, "Расписанеие обновлено", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (workingHours.size() == 1) {
                        // Обновляем id пользователя в таблице рабочего времени
                        makeOrder();
                        Toast.makeText(this, "Запрос отправлен пользователю", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            default:
                Button btn = (Button) v;
                String btnText = btn.getText().toString();
                // Проверка мой ли это сервис (я - worker)
                if(statusUser.equals("worker")){
                    // Это мой сервис (я - worker)

                    if(Boolean.valueOf((btn.getTag(R.string.selectedId)).toString())) {
                        btn.setBackgroundResource(R.drawable.time_button);
                        workingHours.remove(btnText);
                        removedHours.add(btnText);
                        btn.setTag(R.string.selectedId, false);
                    } else {
                        btn.setBackgroundResource(R.drawable.pressed_button);
                        workingHours.add(btnText);
                        removedHours.remove(btnText);
                        btn.setTag(R.string.selectedId, true);
                    }
                }
                else {
                    // Это не мой сервис (я - User)

                    // Проверка была ли кнопка выбрана до нажатия
                    if(Boolean.valueOf((btn.getTag(R.string.selectedId)).toString())) {
                        // Кнопка была уже нажата

                        btn.setBackgroundResource(R.drawable.time_button);
                        workingHours.remove(btnText);
                        btn.setTag(R.string.selectedId, false);
                    } else {
                        // Кнопка не была нажата до клика

                        String selectedTime;
                        //Если уже существует выбранное время
                        if(workingHours.size() == 1){
                            selectedTime =  workingHours.get(0);
                            removeSelection(selectedTime);
                            workingHours.clear();
                        }
                        btn.setBackgroundResource(R.drawable.pressed_button);
                        workingHours.add(btnText);
                        btn.setTag(R.string.selectedId, true);
                    }
                }
                break;
        }
    }

    // Снимает выделение с кнопок хронящихся в буфере удалённых дней
    private void checkRemovedHours() {
        for (int i = 0; i < ROWS_COUNT; i++) {
            for (int j = 0; j < COLUMNS_COUNT; j++) {
                String time = (String) timeBtns[i][j].getText();
                if (removedHours.contains(time)) {
                    timeBtns[i][j].setBackgroundResource(R.drawable.day_button);
                    timeBtns[i][j].setTag(R.string.selectedId, false);
                }
            }
        }
    }

    // Выделяет кнопки хронящиеся в буфере рабочих дней
    private void checkWorkingHours() {
        for (int i = 0; i < ROWS_COUNT; i++) {
            for (int j = 0; j < COLUMNS_COUNT; j++) {
                String time = (String) timeBtns[i][j].getText();
                if (workingHours.contains(time)) {
                    timeBtns[i][j].setBackgroundResource(R.drawable.pressed_button);
                    timeBtns[i][j].setTag(R.string.selectedId, true);
                }
            }
        }
    }

    //Выделяет необходимые кнопки
    private void checkCurrentTimes() {
        workingDaysId = getIntent().getStringExtra(WORKING_DAYS_ID);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Получает время и id пользователя который записан на это время
        // Таблицы: рабочие время
        // Условия: уточняем id рабочего дня
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_TIME_WORKING_TIME + ", "
                        + DBHelper.KEY_USER_ID
                        + " FROM "
                        + DBHelper.TABLE_WORKING_TIME
                        + " WHERE "
                        + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{workingDaysId});

        // Проверка на то, что это мой сервис
        if (statusUser.equals("worker")) {
            // Это мой сервис (я - worker)

            selectBtsForWorker(cursor);
        } else {
            // Это не мой сервис (я - User)
            selectBtsForUser(cursor);
        }
    }

    // Выделяет кнопки (worker)
    private void selectBtsForWorker(Cursor cursor) {
        for (int i = 0; i < ROWS_COUNT; i++) {
            for (int j = 0; j < COLUMNS_COUNT; j++) {
                String time = (String) timeBtns[i][j].getText();

                //Проверка является ли данное время рабочим
                if (checkTimeForWorker(cursor, time)) {
                    timeBtns[i][j].setBackgroundResource(R.drawable.pressed_button);
                    timeBtns[i][j].setTag(R.string.selectedId, true);

                    // Проверка записан ли кто-то на это время
                    if (!isFreeTime(cursor, time)) {
                        timeBtns[i][j].setEnabled(false);
                    }
                }
            }
        }
    }

    // Выделяет кнопки (User)
    private void selectBtsForUser(Cursor cursor) {
        // Время на которое я записан
        String myOrderTime = checkMyOrder(cursor);

        for (int i = 0; i < ROWS_COUNT; i++) {
            for (int j = 0; j < COLUMNS_COUNT; j++) {
                String time = (String) timeBtns[i][j].getText();


                //Проверка на наличие записи на данный день
                if (!myOrderTime.equals("")) {
                    // Есть запись

                    // Проверка на то, что я записан на данное время
                    if (myOrderTime.equals(time)) {
                        timeBtns[i][j].setClickable(false);
                        timeBtns[i][j].setBackgroundResource(R.drawable.pressed_button);
                        timeBtns[i][j].setTag(R.string.selectedId, true);
                    } else {
                        timeBtns[i][j].setBackgroundResource(R.drawable.disabled_button);
                        timeBtns[i][j].setEnabled(false);
                    }
                } else {
                    // Записи нет

                    // Проверка является ли данное время свободным
                    if (isFreeTime(cursor, time)) {
                        timeBtns[i][j].setBackgroundResource(R.drawable.time_button);
                        timeBtns[i][j].setTag(R.string.selectedId, false);
                    } else {
                        timeBtns[i][j].setBackgroundResource(R.drawable.disabled_button);
                        timeBtns[i][j].setEnabled(false);
                    }
                }

            }
        }
    }

    //Снимает выделение с кнопки с данным временем
    private void removeSelection(String selectedTime){
        for (int i = 0; i < ROWS_COUNT; i++) {
            for (int j = 0; j < COLUMNS_COUNT; j++) {
                String time = (String) timeBtns[i][j].getText();

                if(time.equals(selectedTime)){
                    timeBtns[i][j].setBackgroundResource(R.drawable.time_button);
                    timeBtns[i][j].setTag(R.string.selectedId, false);
                }
            }
        }
    }

    // Добавляем время из буфера workingTime в БД
    private void addTime(){
        workingDaysId = getIntent().getStringExtra(WORKING_DAYS_ID);

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Получает время
        // Таблицы: рабочие время
        // Условия: уточняем id рабочего дня
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_TIME_WORKING_TIME
                        + " FROM "
                        + DBHelper.TABLE_WORKING_TIME
                        + " WHERE "
                        + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{String.valueOf(workingDaysId)});
        ContentValues contentValues = new ContentValues();

        for (String time: workingHours) {
            if(!checkTimeForWorker(cursor, time)) {

                FirebaseDatabase fdatabase = FirebaseDatabase.getInstance();
                DatabaseReference myRef = fdatabase.getReference(WORKING_TIME);

                Map<String,Object> items = new HashMap<>();
                items.put("time",time);
                items.put("user id", "0");
                items.put("working day id", workingDaysId);

                String timeId =  myRef.push().getKey();
                myRef = fdatabase.getReference(WORKING_TIME).child(timeId);
                myRef.updateChildren(items);

                putDataInLocalStorage(timeId, time,contentValues,database);
            }
        }

        workingHours.clear();
        cursor.close();
    }

    private void putDataInLocalStorage(String timeId, String time, ContentValues contentValues,
                                       SQLiteDatabase database) {

        contentValues.put(DBHelper.KEY_ID, timeId);
        contentValues.put(DBHelper.KEY_TIME_WORKING_TIME, time);
        contentValues.put(DBHelper.KEY_USER_ID,"0");
        contentValues.put(DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME, workingDaysId);

        database.insert(DBHelper.TABLE_WORKING_TIME,null,contentValues);
    }

    // Проверяет есть ли запись на данный день
    private String checkMyOrder(Cursor cursor) {
        if(cursor.moveToFirst()) {
            int indexUserId = cursor.getColumnIndex(DBHelper.KEY_USER_ID);
            int indexTime = cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);
            String userId = getUserId();
            do {
                if(cursor.getString(indexUserId).equals(userId)) {
                    String orderTime = cursor.getString(indexTime);
                    return orderTime;
                }
            } while (cursor.moveToNext());
        }
        return "";
    }
    // Позволяет получать id
    private void makeOrder(){
        workingDaysId = getIntent().getStringExtra(WORKING_DAYS_ID);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        //получаю время кнопки, на которую нажал
        final String timeBtn = workingHours.get(0);

        //получаем все время этого дня
       final Query query = database.getReference(WORKING_TIME)
                .orderByChild(WORKING_DAYS_ID)
                .equalTo(workingDaysId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //делаем запрос по такому дню, такому времени
                for (DataSnapshot time : dataSnapshot.getChildren()) {
                    if(String.valueOf(time.child("time").getValue()).equals(timeBtn)) {
                        String timeId = String.valueOf(time.getKey());

                        //возвращает все дни определенного сервиса
                        DatabaseReference myRef = database.getReference(WORKING_TIME + timeId); //+ id времени

                        Map<String, Object> items = new HashMap<>();
                        items.put(USER_ID, userId);

                        myRef.updateChildren(items);
                        updateLocalStorageTime();
                        // DatabaseReference myRef = database.getReference(WORKING_TIME + timeId);
                        createDialog(workingDaysId);
                        checkCurrentTimes();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: "); }
        });
    }

    private void createDialog(final String workingDaysId) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dayReference = database.getReference(WORKING_DAYS).child(workingDaysId);
        dayReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot day) {
                String serviceId = String.valueOf(day.child(SERVICE_ID).getValue());
                DatabaseReference serviceReference = database.getReference(SERVICES).child(serviceId);
                serviceReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot service) {
                        String workerId =  String.valueOf(service.child(USER_ID).getValue());

                        DatabaseReference reference = database.getReference(DIALOGS);
                        String dialogId =  reference.push().getKey();
                        reference = reference.child(dialogId);

                        Map<String,Object> items = new HashMap<>();
                        items.put("first phone", workerId);
                        items.put("second phone", userId);

                        reference.updateChildren(items);

                        createMessage(dialogId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void createMessage(final String dialogId) {

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        Date dateNow = new Date();

        DatabaseReference myRef = database.getReference(MESSAGES);
        Map<String, Object> items = new HashMap<>();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("HH:mm:ss");
        formatForDateNow.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));

        items.put("service id", serviceId);
        items.put("dialog id", dialogId);
        items.put("time", formatForDateNow.format(dateNow));
        items.put("is canceled", false);

        String messageId =  myRef.push().getKey();
        myRef = database.getReference(MESSAGES).child(messageId);
        myRef.updateChildren(items);
    }

    private void updateLocalStorageTime() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Получает время
        // Таблицы: рабочие время
        // Условия: уточняем id рабочего дня
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_TIME_WORKING_TIME
                        + " FROM "
                        + DBHelper.TABLE_WORKING_TIME
                        + " WHERE "
                        + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{String.valueOf(workingDaysId)});

        ContentValues contentValues = new ContentValues();
        String userId  = getUserId();
        for (String time: workingHours) {
            contentValues.put(DBHelper.KEY_USER_ID, userId);
            database.update(DBHelper.TABLE_WORKING_TIME, contentValues,
                    DBHelper.KEY_TIME_WORKING_TIME + " = ? AND " + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + " = ? ",
                    new String []{time, String.valueOf(workingDaysId)});
        }
        workingHours.clear();
        cursor.close();
    }

    private void deleteTime(){
        workingDaysId = getIntent().getStringExtra(WORKING_DAYS_ID);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        //получаю время кнопки, на которую нажал

        //получаем все время этого дня

        final Query query = database.getReference(WORKING_TIME)
                .orderByChild(WORKING_DAYS_ID)
                .equalTo(workingDaysId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //делаем запрос по такому дню, такому времени
                //удаляем время из базы данных
                for (String hours : removedHours) {
                    for (DataSnapshot time : dataSnapshot.getChildren()) {
                        if (String.valueOf(time.child("time").getValue()).equals(hours)) {
                            String timeId = String.valueOf(time.getKey());

                            //возвращает все дни определенного сервиса
                            DatabaseReference myRef = database.getReference(WORKING_TIME + timeId); //+ id времени

                            Map<String, Object> items = new HashMap<>();
                            items.put(USER_ID, null);
                            items.put("time", null);
                            items.put(WORKING_DAYS_ID, null);

                            myRef.updateChildren(items);
                            deleteTimeFromLocalStorage();
                        }
                    }
                }
                //очищаяем массив
                removedHours.clear();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: ");
            }
        });
    }

    private void deleteTimeFromLocalStorage() {
        workingDaysId = getIntent().getStringExtra(WORKING_DAYS_ID);

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Получает время
        // Таблицы: рабочие время
        // Условия: уточняем id рабочего дня
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_TIME_WORKING_TIME
                        + " FROM "
                        + DBHelper.TABLE_WORKING_TIME
                        + " WHERE "
                        + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{String.valueOf(workingDaysId)});

        for (String time: removedHours) {
            if(checkTimeForWorker(cursor, time)) {
                database.delete(
                        DBHelper.TABLE_WORKING_TIME,
                        DBHelper.KEY_TIME_WORKING_TIME + " = ? AND "
                                + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + " = ?",
                        new String[]{time, String.valueOf(workingDaysId)});
            }
        }
    }

    // Проверяет есть ли какие-либо записи на данное время
    private boolean checkTimeForWorker(Cursor cursor, String time) {
        if(cursor.moveToFirst()) {
            int indexTime = cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);
            do {
                if (time.equals(cursor.getString(indexTime))) {
                    return true;
                }
            } while (cursor.moveToNext());
        }
        return false;
    }

    // Проверяет свободно ли данное время
    private boolean isFreeTime(Cursor cursor, String time) {
        if(cursor.moveToFirst()) {
            int indexTime = cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);
            int indexUserId = cursor.getColumnIndex(DBHelper.KEY_USER_ID);
            do {
                if (cursor.getString(indexUserId).equals("0") && time.equals(cursor.getString(indexTime))) {
                    return true;
                }
            } while (cursor.moveToNext());
        }
        return false;
    }

    // Получает
    private String getUserId(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        userId = sPref.getString(PHONE_NUMBER, "-");

        return userId;
    }

    // Добавление кнопок со временем на экран
    private void addButtonsOnScreen(int width, int height, boolean isPm){
        //Дополнительные часы (am - 0, pm - 12)
        int extraHours = 0;
        if(isPm) {
            extraHours = 12;
        }

        for (int i=0; i<ROWS_COUNT; i++) {
            for (int j=0; j<COLUMNS_COUNT; j++) {
                timeBtns[i][j]= new Button(this);
                // установка параметров
                timeBtns[i][j].setWidth(50);
                timeBtns[i][j].setHeight(30);
                timeBtns[i][j].setX(j*width/COLUMNS_COUNT);
                timeBtns[i][j].setY(i*height/(2*ROWS_COUNT));
                timeBtns[i][j].setBackgroundResource(R.drawable.time_button);

                timeBtns[i][j].setTag(R.string.selectedId, false);
                timeBtns[i][j].setOnClickListener(this);
                // установка текста
                String hour = String.valueOf(extraHours + (i * COLUMNS_COUNT + j) / 2);
                String min = (j % 2 == 0) ? "00" : "30";
                timeBtns[i][j].setText(hour + ":" + min);

                if(timeBtns[i][j].getParent() != null) {
                    ((ViewGroup)timeBtns[i][j].getParent()).removeView(timeBtns[i][j]);
                }
                mainLayout.addView(timeBtns[i][j]);
            }
        }
    }

}
