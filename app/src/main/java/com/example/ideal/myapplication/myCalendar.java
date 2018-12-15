package com.example.ideal.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class myCalendar extends AppCompatActivity implements View.OnClickListener {

    final String SERVICE_ID = "service id";
    final String WORKING_DAYS_ID = "working days id";

    String date;

    CalendarView calendarView;
    Button nextBtn;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_calendar);

        nextBtn = findViewById(R.id.continueMyCalendarBtn);
        calendarView = findViewById(R.id.calendarCalendar);

        calendarView.setDate(calendarView.getDate(), true, true);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                date = year + "-" + month+1 + "-" + dayOfMonth;
            }
        });

        dbHelper = new DBHelper(this);

        nextBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.continueMyCalendarBtn:
                addWorkingDay();
                break;
            default:
                break;
        }
    }

    private void addWorkingDay() {
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.KEY_DATE_WORKING_DAYS, date);
        contentValues.put(DBHelper.KEY_SERVICE_ID_WORKING_DAYS, serviceId);

        long id = database.insert(DBHelper.TABLE_WORKING_DAYS,null,contentValues);

        goToMyTime(id);

    }


    private void goToMyTime(long id){
        Intent intent = new Intent(this,myTime.class);
        intent.putExtra(WORKING_DAYS_ID, id);
        startActivity(intent);
    }

}

