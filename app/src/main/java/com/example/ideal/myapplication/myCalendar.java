package com.example.ideal.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Calendar;

public class myCalendar extends AppCompatActivity implements View.OnClickListener {

    private final String FILE_NAME = "Info";
    private final String TAG = "DBInf";
    private final String PHONE = "phone";
    private final String SERVICE_ID = "service id";
    private final String WORKING_DAYS_ID = "working days id";
    private final String STATUS_USER_BY_SERVICE = "status user";
    private final int WEEKS_COUNT = 4;
    private final int DAYS_COUNT = 7;

    String statusUser;
    String date;

    Button[][] dayBtns;
    Button nextBtn;

    RelativeLayout mainLayout;

    DBHelper dbHelper;
    SharedPreferences sPref;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_calendar);

        mainLayout = findViewById(R.id.mainMyCalendarLayout);
        nextBtn = findViewById(R.id.continueMyCalendarBtn2);
        dayBtns = new Button[WEEKS_COUNT][DAYS_COUNT];

        dbHelper = new DBHelper(this);

        // получаем статус, чтобы определить, кто зашел, worker or user
        statusUser = getIntent().getStringExtra(STATUS_USER_BY_SERVICE);

        // создаём календарь
        createCalendar();

        // проверяем имеется ли у данного пользователя запись на данную услугу
        if(statusUser.equals("user")){
            checkOrder();
        }
        else {
            selectWorkingDayWithTime();
        }
        nextBtn.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.continueMyCalendarBtn2:
                if(statusUser.equals("worker")){
                    if(isDaySelected()) {
                        addWorkingDay();
                    } else {
                        Toast.makeText(this, "Выбирите дату, на которую хотите настроить расписание", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    if(isDaySelected()) {
                        goToMyTime(checkCurrentDay(date), statusUser);
                    } else {
                        Toast.makeText(this, "Выбирите дату, на которую хотите записаться", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            default:
                Button btn = (Button) v;
                // Проверка былв ли кнопка выбрана
                if (!Boolean.valueOf(btn.getTag(R.string.selectedId).toString())) {
                    // На была выбрана - фокусируемся на ней
                    btn.setBackgroundResource(R.drawable.selected_day_button);
                    for (int i = 0; i < WEEKS_COUNT; i++) {
                        for (int j = 0; j < DAYS_COUNT; j++) {
                            if(Boolean.valueOf(dayBtns[i][j].getTag(R.string.selectedId).toString())) {
                                dayBtns[i][j].setTag(R.string.selectedId, false);
                                dayBtns[i][j].setBackgroundResource(R.drawable.day_button);
                                break;
                            }
                        }
                    }
                    date = convertDate(btn.getText().toString(), btn.getTag(R.string.yearId).toString());
                    btn.setTag(R.string.selectedId, true);
                } else {
                    // Была выбрана - снимаем выделение
                    btn.setTag(R.string.selectedId, false);
                    btn.setBackgroundResource(R.drawable.day_button);
                }
                break;
        }
    }

    //Выделяет рабочие дни
    private void selectWorkingDayWithTime() {
        String dayAndMonth, year;
        long dayId;
        int dayWithTimesColor = ContextCompat.getColor(this, R.color.dayWithTimes);
        for (int i = 0; i < WEEKS_COUNT; i++) {
            for (int j = 0; j < DAYS_COUNT; j++) {
                dayAndMonth = dayBtns[i][j].getText().toString();
                year = dayBtns[i][j].getTag(R.string.yearId).toString();

                dayId = checkCurrentDay(convertDate(dayAndMonth, year));
                if (dayId != 0) {
                    if (hasSomeTime(dayId)) {
                        Log.d(TAG, "selectWorkingDayWithTime: " + dayId);
                        dayBtns[i][j].setTextColor(dayWithTimesColor);
                    }
                }
            }
        }
    }

    // проверяет имеется ли у данного пользователя запись на данную услугу
    private void checkOrder(){
        //Если пользователь записан на какой-то день выделить только его
        date = getOrderDate();
        if(date != "") {
            String[] arrDate = date.split("-");
            String orderDate = arrDate[0] + " " + monthToString(Integer.valueOf(arrDate[1]));

            for (int i = 0; i < WEEKS_COUNT; i++) {
                for (int j = 0; j < DAYS_COUNT; j++) {
                    if(orderDate.equals(dayBtns[i][j].getText().toString()) && arrDate[2].equals(dayBtns[i][j].getTag(R.string.yearId).toString())) {
                        dayBtns[i][j].setBackgroundResource(R.drawable.selected_day_button);
                        dayBtns[i][j].setTag(R.string.selectedId, true);
                    } else {
                        dayBtns[i][j].setTag(R.string.selectedId, false);
                        dayBtns[i][j].setEnabled(false);
                        dayBtns[i][j].setBackgroundResource(R.drawable.disabled_button);
                    }
                }
            }
        } else {
            // Если записи на данный сервис нет, отключаем всё нерабочие дни
            String dayAndMonth, year;
            long dayId;

            for (int i = 0; i < WEEKS_COUNT; i++) {
                for (int j = 0; j < DAYS_COUNT; j++) {
                    dayAndMonth = dayBtns[i][j].getText().toString();
                    year = dayBtns[i][j].getTag(R.string.yearId).toString();

                    dayId = checkCurrentDay(convertDate(dayAndMonth, year));

                    if (dayId == 0) {
                        dayBtns[i][j].setEnabled(false);
                        dayBtns[i][j].setBackgroundResource(R.drawable.disabled_button);
                    } else {
                        if (!hasSomeTime(dayId)) {
                            dayBtns[i][j].setEnabled(false);
                            dayBtns[i][j].setBackgroundResource(R.drawable.disabled_button);
                        }
                    }
                }
            }
        }
    }

    //Возвращает дату записи
    private String getOrderDate() {
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String userId = getUserId();
        // Получает дату записи
        // Таблицы: рабочии дни, рабочие время
        // Условия: связываем таблицы по id рабочего дня; уточняем id сервиса и id пользователя
        String sqlQuery =
                "SELECT " + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_DATE_WORKING_DAYS
                        + " FROM " + DBHelper.TABLE_WORKING_TIME + ", " + DBHelper.TABLE_WORKING_DAYS
                        + " WHERE " + DBHelper.KEY_SERVICE_ID_WORKING_DAYS + " = ? AND "
                        + DBHelper.KEY_USER_ID + " = ? "
                        + " AND "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_ID + " = " + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME;

        Cursor cursor = database.rawQuery(sqlQuery, new String[] {String.valueOf(serviceId), userId});

        if(cursor.moveToFirst()) {
            int indexDate = cursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);
            String orderDate = cursor.getString(indexDate);
            return orderDate;
        } else {
            return "";
        }
    }

    private void createCalendar() {
        Calendar calendar = Calendar.getInstance();

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        int dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK)+5)%DAYS_COUNT;
        int dayOfMonth, month, year;
        String stringMonth;

        //Создание календаря
        calendar.add(Calendar.DATE, -dayOfWeek);
        for(int i = 0; i < WEEKS_COUNT; i++) {
            for (int j = 0; j < DAYS_COUNT; j++) {
                dayBtns[i][j] = new Button(this);

                //положение, бэкграунд, размеры
                dayBtns[i][j].setX(j * width / DAYS_COUNT);
                dayBtns[i][j].setY(i * height / (2*WEEKS_COUNT));
                dayBtns[i][j].setBackgroundResource(R.drawable.day_button);
                dayBtns[i][j].setLayoutParams(new ViewGroup.LayoutParams(width / DAYS_COUNT-5, height / (2*WEEKS_COUNT)-35));

                //тэги
                dayBtns[i][j].setTag(R.string.selectedId, false);
                year = calendar.get(Calendar.YEAR);
                dayBtns[i][j].setTag(R.string.yearId, year);

                //надпись
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                month = calendar.get(Calendar.MONTH)+1;
                stringMonth = monthToString(month);
                dayBtns[i][j].setTextSize(11);
                dayBtns[i][j].setText(dayOfMonth + " " + stringMonth);


                //отрисовываем прошедше дни
                if ((j < dayOfWeek) && (i == 0)) {
                    dayBtns[i][j].setEnabled(false);
                    dayBtns[i][j].setBackgroundResource(R.drawable.disabled_button);
                } else {
                    dayBtns[i][j].setOnClickListener(this);
                }

                if (dayBtns[i][j].getParent() != null) {
                    ((ViewGroup) dayBtns[i][j].getParent()).removeView(dayBtns[i][j]);
                }
                mainLayout.addView(dayBtns[i][j]);
                calendar.add(Calendar.DATE, 1);
            }
        }
    }

    // Возвращает выбран ли како-либо день
    private boolean isDaySelected() {
        for (int i = 0; i < WEEKS_COUNT; i++) {
            for (int j = 0; j < DAYS_COUNT; j++) {
                if(dayBtns[i][j].isEnabled()) {
                    if(Boolean.valueOf(dayBtns[i][j].getTag(R.string.selectedId).toString())) {
                        date = convertDate(dayBtns[i][j].getText().toString(), dayBtns[i][j].getTag(R.string.yearId).toString());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Возвращает есть ли в рабочем дне рабочие часы
    private boolean hasSomeTime(long dayId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // Получает id рабочего дня
        // Таблицы: рабочие время
        // Условия: уточняем id рабочего дня
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_ID
                        + " FROM "
                        + DBHelper.TABLE_WORKING_TIME
                        + " WHERE "
                        + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + " = ? ";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{String.valueOf(dayId)});

        if(cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    // Преобразует дату в формат БД
    private String convertDate(String dayAndMonth, String year) {
        String[] arrDate = dayAndMonth.split(" ");
        int month = monthToInt(arrDate[1]);
        String convertedDate = arrDate[0] + "-" + month + "-" + year;

        return convertedDate;
    }

    // Добавляе рабочий день в БД
    private void addWorkingDay() {
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);

        long id = checkCurrentDay(date);

        // Проверка на существование такого дня
        if(id != 0){
            goToMyTime(id,statusUser);
        } else {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.KEY_DATE_WORKING_DAYS, date);
            contentValues.put(DBHelper.KEY_SERVICE_ID_WORKING_DAYS, serviceId);

            id = database.insert(DBHelper.TABLE_WORKING_DAYS, null, contentValues);

            goToMyTime(id,statusUser);
        }
    }

    //Возвращает id дня по id данного сервиса и дате
    private long checkCurrentDay(String day) {
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Получает id рабочего дня
        // Таблицы: рабочии дни
        // Условия: уточняем id сервиса и дату
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_ID
                        + " FROM "
                        + DBHelper.TABLE_WORKING_DAYS
                        + " WHERE "
                        + DBHelper.KEY_SERVICE_ID_WORKING_DAYS + " = ? AND "
                        + DBHelper.KEY_DATE_WORKING_DAYS + " = ? ";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{String.valueOf(serviceId), day});

        if(cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);

            return Long.valueOf(cursor.getString(indexId));
        }
        return 0;
    }

    private  String getUserId(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        String userId = sPref.getString(PHONE, "-");

        return userId;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(statusUser.equals("user")){
            checkOrder();
        }
        else {
            selectWorkingDayWithTime();
        }
    }

    private void goToMyTime(long dayId, String statusUser){

        Intent intent = new Intent(this, myTime.class);
        intent.putExtra(WORKING_DAYS_ID, dayId);
        intent.putExtra(STATUS_USER_BY_SERVICE, statusUser);

        startActivity(intent);
    }

    private String monthToString(int month) {
        switch (month) {
            case 1:
                return "янв";
            case 2:
                return "фев";
            case 3:
                return "мар";
            case 4:
                return "апр";
            case 5:
                return "май";
            case 6:
                return "июнь";
            case 7:
                return "июль";
            case 8:
                return "авг";
            case 9:
                return "сен";
            case 10:
                return "окт";
            case 11:
                return "ноя";
            case 12:
                return "дек";
        }

        return "";
    }

    private int monthToInt(String month) {
        switch (month) {
            case "янв":
                return 1;
            case "фев":
                return 2;
            case "мар":
                return 3;
            case "апр":
                return 4;
            case "май":
                return 5;
            case "июнь":
                return 6;
            case "июль":
                return 7;
            case "авг":
                return 8;
            case "сен":
                return 9;
            case "окт":
                return 10;
            case "ноя":
                return 11;
            case "дек":
                return 12;
        }

        return -1;
    }
}
