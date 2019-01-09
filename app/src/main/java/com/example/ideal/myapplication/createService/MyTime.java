package com.example.ideal.myapplication.createService;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import com.example.ideal.myapplication.DBHelper;
import com.example.ideal.myapplication.R;

import java.util.ArrayList;

public class MyTime extends AppCompatActivity  implements View.OnClickListener {

    private final String FILE_NAME = "Info";
    private final String PHONE = "phone";
    private final String WORKING_DAYS_ID = "working days id";
    private final String STATUS_USER_BY_SERVICE = "status User";
    private final int ROWS_COUNT = 6;
    private final int COLUMNS_COUNT = 4;

    String statusUser;
    String userId;
    String workingDaysId;
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
                        checkCurrentTimes();
                    }
                    Toast.makeText(this, "Запрос отправлен пользователю", Toast.LENGTH_SHORT).show();
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
        workingDaysId = String.valueOf(getIntent().getLongExtra(WORKING_DAYS_ID, -1));
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
        long workingDaysId = getIntent().getLongExtra(WORKING_DAYS_ID, -1);

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
                contentValues.put(DBHelper.KEY_TIME_WORKING_TIME, time);
                contentValues.put(DBHelper.KEY_USER_ID,"0");
                contentValues.put(DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME, workingDaysId);

                database.insert(DBHelper.TABLE_WORKING_TIME,null,contentValues);
            }
        }

        workingHours.clear();
        cursor.close();
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

    // Обновляем id пользователя в таблице рабочего времени
    private void makeOrder(){
        long workingDaysId = getIntent().getLongExtra(WORKING_DAYS_ID, -1);
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

    private void deleteTime() {
        long workingDaysId = getIntent().getLongExtra(WORKING_DAYS_ID, -1);

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

        removedHours.clear();
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
        userId = sPref.getString(PHONE, "-");

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
