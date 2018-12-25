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
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.DoubleSummaryStatistics;

public class secondCalendar extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "DBInf";
    final String SERVICE_ID = "service id";
    final String WORKING_DAYS_ID = "working days id";
    final String STATUS_USER_BY_SERVICE = "status user";

    String statusUser;
    DBHelper dbHelper;

    RelativeLayout mainLayout;
    String date;

    Button[][] dayBtns;
    Button nextBtn;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_calendar);

        mainLayout = findViewById(R.id.mainMyCalendarLayout);
        nextBtn = findViewById(R.id.continueMyCalendarBtn2);
        dayBtns = new Button[4][7];

        dbHelper = new DBHelper(this);

        // получаем статус, чтобы определить, кто зашел, worker or user
        statusUser = getIntent().getStringExtra(STATUS_USER_BY_SERVICE);

        createCalendar();

        if(statusUser.equals("user")){
            String dayAndMonth, year;
            long dayId;

            for(int i = 0; i < 4; i++) {
                for (int j = 0; j < 7; j++) {
                    dayAndMonth = dayBtns[i][j].getText().toString();
                    year = dayBtns[i][j].getTag(R.string.yearId).toString();
                    
                    dayId = checkCurrentDay(convertDate(dayAndMonth, year));

                    if(dayId == 0) {
                        dayBtns[i][j].setEnabled(false);
                    } else {
                        if(!hasSomeTime(dayId)) {
                            dayBtns[i][j].setEnabled(false);
                        }
                    }

                }
            }

        }

        nextBtn.setOnClickListener(this);
    }

    private void createCalendar() {
        Calendar calendar = Calendar.getInstance();

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        calendar.add(Calendar.DATE, 1);
        int dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK)+5)%7;
        int dayOfMonth, month, year;
        String stringMonth;

        //Создание календаря
        calendar.add(Calendar.DATE, -dayOfWeek);
        for(int i = 0; i < 4; i++) {
            for (int j = 0; j < 7; j++) {
                dayBtns[i][j] = new Button(this);

                //положение, бэкграунд, размеры
                dayBtns[i][j].setX(j * width / 7);
                dayBtns[i][j].setY(i * height / 8);
                dayBtns[i][j].setBackgroundResource(R.drawable.day_button);
                dayBtns[i][j].setLayoutParams(new ViewGroup.LayoutParams(width / 7-5, height / 8-35));

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


                if ((j < dayOfWeek) && (i == 0)) {
                    dayBtns[i][j].setEnabled(false);
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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.continueMyCalendarBtn2:
                if(statusUser.equals("worker")){
                    if(isDaySelected()) {
                        Log.d(TAG, date);
                        addWorkingDay();
                        Log.d(TAG, "IT IS WORKER");
                    } else {
                        Toast.makeText(this, "Выбирите дату, на которую хотите настроить расписание", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    if(isDaySelected()) {
                        goToMyTime(checkCurrentDay(date), statusUser);
                        Log.d(TAG, "IT IS USER!");
                    } else {
                        Toast.makeText(this, "Выбирите дату, на которую хотите записаться", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            default:
                Button btn = (Button) v;
                //Log.d(TAG, btn.getTag(R.string.selectedId).toString());
                if (!Boolean.valueOf(btn.getTag(R.string.selectedId).toString())) {
                    //Log.d(TAG, (btn.getTag(R.string.selectedId)).toString());
                    btn.setBackgroundResource(R.drawable.selected_day_button);
                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 7; j++) {
                            if(Boolean.valueOf(dayBtns[i][j].getTag(R.string.selectedId).toString())) {
                                dayBtns[i][j].setTag(R.string.selectedId, false);
                                dayBtns[i][j].setBackgroundResource(R.drawable.day_button);
                                break;
                            }
                        }
                    }
                    date = convertDate(btn.getText().toString(), btn.getTag(R.string.yearId).toString());
                    Log.d(TAG, date);
                    btn.setTag(R.string.selectedId, true);
                } else {
                    btn.setTag(R.string.selectedId, false);
                    btn.setBackgroundResource(R.drawable.day_button);
                }
            break;
        }
    }

    private boolean isDaySelected() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 7; j++) {
                if(dayBtns[i][j].isEnabled()) {
                    if(Boolean.valueOf(dayBtns[i][j].getTag(R.string.selectedId).toString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasSomeTime(long dayId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor = database.query(
                DBHelper.TABLE_WORKING_TIME,
                new String[]{DBHelper.KEY_ID},
                DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + " = ? ",
                new String[]{String.valueOf(dayId)},
                null,
                null,
                null,
                null);

        if(cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    private String convertDate(String dayAndMonth, String year) {
        String[] arrDate = dayAndMonth.split(" ");
        int month = monthToInt(arrDate[1]);
        String convertedDate = arrDate[0] + "-" + month + "-" + year;

        return convertedDate;
    }

    private void addWorkingDay() {
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);
        
        long id = checkCurrentDay(date);
        Log.d(TAG, id+"");

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

    private long checkCurrentDay(String day) {
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        Cursor cursor = database.query(
                DBHelper.TABLE_WORKING_DAYS,
                new String[]{DBHelper.KEY_ID, DBHelper.KEY_DATE_WORKING_DAYS},
                DBHelper.KEY_SERVICE_ID_WORKING_DAYS + " = ? AND " + DBHelper.KEY_DATE_WORKING_DAYS + " = ? ",
                new String[]{String.valueOf(serviceId), day},
                null,
                null,
                null,
                null);

        if(cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);

            return Long.valueOf(cursor.getString(indexId));
        }
        return 0;
    }

    /*
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
    */

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
