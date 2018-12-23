package com.example.ideal.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class myCalendar extends AppCompatActivity implements View.OnClickListener {

    // Идея! не давать пользователю войти в дни, которые уже прошли при помощи compareDates
    // Идея! если нет дней, позже сегодняшнего с расписанием, писать, что нет актуальных дней на запись
    final String TAG = "DBInf";
    final String SERVICE_ID = "service id";
    final String WORKING_DAYS_ID = "working days id";
    final String STATUS_USER_BY_SERVICE = "status user";

    String date;
    String statusUser;
    long currentIdDay;

    CalendarView calendarView;
    Button nextBtn;

    DBHelper dbHelper;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_calendar);

        nextBtn = findViewById(R.id.continueMyCalendarBtn);
        calendarView = findViewById(R.id.calendarCalendar);
        dbHelper = new DBHelper(this);

        // получаем статус, чтобы определить, кто зашел, worker or user
        statusUser = getIntent().getStringExtra(STATUS_USER_BY_SERVICE);

        if(statusUser.equals("worker")){
            Date currentDate = new Date();
            LocalDate localDate = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            date = localDate.getYear() + "-" + localDate.getMonthValue() + "-" + localDate.getDayOfMonth();

            calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                    date = year + "-" + (month+1) + "-" + dayOfMonth;
                }
            });
        }
        else {
            // Mark
            date = getDateWithCurrentServiceId();
            Log.d(TAG, date);
            if(!date.equals("0")){
                String[] dates = date.split("-");
                LocalDate myLocalDate = LocalDate.of(
                        Integer.valueOf(dates[0]),
                        Integer.valueOf(dates[1]),
                        Integer.valueOf(dates[2]));

                Date nowDate = Date.from(myLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                long millis = nowDate.getTime();
                calendarView.setDate(millis, true, true); // нужно установить дату из таблицы сервиса и даты
                currentIdDay = checkCurrentDay();

                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                        date = year + "-" + (month+1) + "-" + dayOfMonth;
                        currentIdDay = checkCurrentDay();
                        Log.d(TAG, " cur id day"+currentIdDay);
                    }
                });
            }
            //иначе просто ничего не выводит
        }
        nextBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.continueMyCalendarBtn:
                if(statusUser.equals("worker")){
                    addWorkingDay();
                    Log.d(TAG, "IT IS WORKER");
                }
                else {
                    goToMyTime(currentIdDay,statusUser);
                    Log.d(TAG, "IT IS USER!");
                }
                break;
            default:
                break;
        }
    }

    private void addWorkingDay() {
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        long id = checkCurrentDay();

        if(id != 0){
            goToMyTime(id,statusUser);
        } else {
            ContentValues contentValues = new ContentValues();

            contentValues.put(DBHelper.KEY_DATE_WORKING_DAYS, date);
            contentValues.put(DBHelper.KEY_SERVICE_ID_WORKING_DAYS, serviceId);

            id = database.insert(DBHelper.TABLE_WORKING_DAYS, null, contentValues);

            goToMyTime(id,statusUser);
        }
    }

    private long checkCurrentDay() {
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        Cursor cursor = database.query(
                DBHelper.TABLE_WORKING_DAYS,
                new String[]{DBHelper.KEY_ID, DBHelper.KEY_DATE_WORKING_DAYS},
                DBHelper.KEY_SERVICE_ID_WORKING_DAYS + " = ? AND " + DBHelper.KEY_DATE_WORKING_DAYS + " = ? ",
                new String[]{String.valueOf(serviceId), date},
                null,
                null,
                null,
                null);

        if(cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);

            return Long.valueOf(cursor.getString(indexId));
        }
        cursor.close();
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getDateWithCurrentServiceId(){
        //возвращаем дату
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);
        Log.d(TAG, "" + serviceId);

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        // провереям не ли такого дня в бд
        Cursor cursor = database.query(
                DBHelper.TABLE_WORKING_DAYS,
                new String[]{DBHelper.KEY_DATE_WORKING_DAYS},
                DBHelper.KEY_SERVICE_ID_WORKING_DAYS + " = ?",
                new String[]{String.valueOf(serviceId)},
                null,
                null,
                null,
                null);
            // while не равно или будет позже сегодняшнего дня, делаем moveToNext,
        // таким образом можно вывести на день точный и непрошедший, если нет мув то некст,
        // то выводим на сегодняшний день
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateAndTime = sdf.format(Calendar.getInstance().getTime());

        if(cursor.moveToFirst()) {
            int indexDate = cursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);

            do{

               Log.d(TAG,"cur date = " + currentDateAndTime);
               Log.d(TAG,"date from db = " + cursor.getString(indexDate));

            }while (compareDates(currentDateAndTime,cursor.getString(indexDate)) < 0 && cursor.moveToNext());
                // почему-то не работает, если нет даты, которая позже сегодня
            try{
                return cursor.getString(indexDate);
            }
            catch (Exception exception){
                return "0";
            }

        }
        cursor.close();
        return "nul";
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Integer compareDates(String date, String secondDate){
        // спросить Валентина про алгоритм сравнения, правильный ли он?
        // date = current date, а secondDate = динамическая дата, получаемая из бд
        // если date > secondDate - то return -1| если =, то 0| иначе 1
        String [] datesFirst = date.split("-"); // curDate
        LocalDate myLocalDatesFirst = LocalDate.of(
                Integer.valueOf(datesFirst[0]),
                Integer.valueOf(datesFirst[1]),
                Integer.valueOf(datesFirst[2]));
        Date nowDatesFirst = Date.from(myLocalDatesFirst.atStartOfDay(ZoneId.systemDefault()).toInstant());
        long firstMillsSecond = nowDatesFirst.getTime();

        String [] datesSecond = secondDate.split("-");
        LocalDate myLocalDatesSecond = LocalDate.of(
                Integer.valueOf(datesSecond[0]),
                Integer.valueOf(datesSecond[1]),
                Integer.valueOf(datesSecond[2]));
        Date nowDatesSecond = Date.from(myLocalDatesSecond.atStartOfDay(ZoneId.systemDefault()).toInstant());
        long secondMillsSecond = nowDatesSecond.getTime();
        Log.d(TAG, "firstMillsSecond: " + firstMillsSecond);
        Log.d(TAG, "SecondMillsSecond: " + secondMillsSecond);
        Log.d(TAG, "razn " + (firstMillsSecond-secondMillsSecond));
        if(firstMillsSecond>secondMillsSecond){
            return -1;
        }
        else if(firstMillsSecond==secondMillsSecond){
            return 0;
        }else{
            return 1;
        }
    }

    private void goToMyTime(long dayId, String statusUser){
        Intent intent = new Intent(this, myTime.class);
        intent.putExtra(WORKING_DAYS_ID, dayId);
        intent.putExtra(STATUS_USER_BY_SERVICE, statusUser);

        startActivity(intent);
    }

}

