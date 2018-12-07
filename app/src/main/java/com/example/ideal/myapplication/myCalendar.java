package com.example.ideal.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;

public class myCalendar extends AppCompatActivity implements CalendarView.OnDateChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_calendar);

        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);
    }

        @Override
        public void onSelectedDayChange(CalendarView view, int year,
        int month, int dayOfMonth) {
            int mYear = year;
            int mMonth = month;
            int mDay = dayOfMonth;
            String selectedDate = new StringBuilder().append(mMonth + 1)
                    .append("-").append(mDay).append("-").append(mYear)
                    .append(" ").toString();
            Toast.makeText(getApplicationContext(), selectedDate, Toast.LENGTH_LONG).show();
        }
}



