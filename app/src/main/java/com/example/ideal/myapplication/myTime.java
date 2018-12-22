package com.example.ideal.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class myTime extends AppCompatActivity  implements View.OnClickListener {
    final String TAG = "DBInf";
    final String FILE_NAME = "Info";
    final String PHONE = "phone";
    final String WORKING_DAYS_ID = "working days id";
    final String STATUS_USER_BY_SERVICE = "status user";

    String statusUser;

    Button[][] timeBtns;
    Button saveBtn;

    // чтобы сохранять несколько выбранных часов надо создать массив,
    // куда будем добавлять текст выбранной кнопки?
    ArrayList<String> workingHours;
    ArrayList<String> removedHours;

    DBHelper dbHelper;
    SharedPreferences sPref;
    RelativeLayout mainLayout;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_time);

        statusUser = getIntent().getStringExtra(STATUS_USER_BY_SERVICE);

        dbHelper = new DBHelper(this);

        mainLayout = findViewById(R.id.mainMyTimeLayout);

        timeBtns = new Button[7][4];
        saveBtn = findViewById(R.id.saveMyTimeBtn);

        workingHours = new ArrayList<>();
        removedHours = new ArrayList<>();

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        for (int i=0; i<6; i++) {
            for (int j=0; j<4; j++) {
                timeBtns[i][j]= new Button(this);
                timeBtns[i][j].setWidth(50);
                timeBtns[i][j].setHeight(30);
                timeBtns[i][j].setX(j*width/4);
                timeBtns[i][j].setY(i*height/12);
                timeBtns[i][j].setBackgroundResource(R.drawable.time_button);
                timeBtns[i][j].setTag(false);
                timeBtns[i][j].setOnClickListener(this);
                String hour = String.valueOf((i*4+j)/2);
                String min = (j%2==0) ? "00":"30";
                timeBtns[i][j].setText(hour + ":" + min);
                if(timeBtns[i][j].getParent() != null) {
                    ((ViewGroup)timeBtns[i][j].getParent()).removeView(timeBtns[i][j]);
                }
                mainLayout.addView(timeBtns[i][j]);
            }
        }
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        checkCurrentTimes(database);

        if(statusUser.equals("worker")){
            saveBtn.setOnClickListener(this);
        }
        else {
            saveBtn.setVisibility(View.GONE);
        }
    }

    private void checkCurrentTimes(SQLiteDatabase database) {
        String workingDaysId = String.valueOf(getIntent().getLongExtra(WORKING_DAYS_ID, -1));

        Cursor cursor = database.query(
                DBHelper.TABLE_WORKING_TIME,
                new String[]{DBHelper.KEY_TIME_WORKING_TIME},
                DBHelper.KEY_TIME_WORKING_DAYS_ID + " = ?",
                new String[]{workingDaysId},
                null,
                null,
                null,
                null);

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                String time = (String) timeBtns[i][j].getText();
                if(hasTime(cursor, time)){
                    timeBtns[i][j].setBackgroundResource(R.drawable.pressed_button);
                    timeBtns[i][j].setTag(true);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.saveMyTimeBtn:
                if(workingHours.size() > 0) {
                    addTime();
                }

                if(removedHours.size() > 0 ) {
                    deleteTime();
                }

                break;

            default:
                Button btn = (Button) v;

                if(Boolean.valueOf((btn.getTag()).toString())) {
                    btn.setBackgroundResource(R.drawable.time_button);
                    workingHours.remove(btn.getText().toString());
                    removedHours.add(btn.getText().toString());
                    btn.setTag(false);
                } else {
                    btn.setBackgroundResource(R.drawable.pressed_button);
                    workingHours.add(btn.getText().toString());
                    removedHours.remove(btn.getText().toString());
                    btn.setTag(true);
                }
                break;
        }


    }

    private void addTime(){
        long workingDaysId = getIntent().getLongExtra(WORKING_DAYS_ID, -1);

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        Cursor cursor = database.query(
                DBHelper.TABLE_WORKING_TIME,
                new String[]{DBHelper.KEY_TIME_WORKING_TIME},
                DBHelper.KEY_TIME_WORKING_DAYS_ID + " = ? ",
                new String[]{String.valueOf(workingDaysId)},
                null,
                null,
                null,
                null);

        ContentValues contentValues = new ContentValues();
        for (String time: workingHours) {
            if(!hasTime(cursor, time)) {
                contentValues.put(DBHelper.KEY_TIME_WORKING_TIME, time);
                contentValues.put(DBHelper.KEY_USER_ID, getUserId());
                contentValues.put(DBHelper.KEY_TIME_WORKING_DAYS_ID, workingDaysId);

                database.insert(DBHelper.TABLE_WORKING_TIME,null,contentValues);
            }
        }

        readDB(database);
        workingHours.clear();
    }

    private void deleteTime() {
        long workingDaysId = getIntent().getLongExtra(WORKING_DAYS_ID, -1);

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        Cursor cursor = database.query(
                DBHelper.TABLE_WORKING_TIME,
                new String[]{DBHelper.KEY_TIME_WORKING_TIME},
                DBHelper.KEY_TIME_WORKING_DAYS_ID + " = ? ",
                new String[]{String.valueOf(workingDaysId)},
                null,
                null,
                null,
                null);

        for (String time: removedHours) {
            if(hasTime(cursor, time)) {
                database.delete(
                        DBHelper.TABLE_WORKING_TIME,
                        DBHelper.KEY_TIME_WORKING_TIME + " = ? AND " + DBHelper.KEY_TIME_WORKING_DAYS_ID + " = ?",
                        new String[]{time, String.valueOf(workingDaysId)});
            }
        }

        readDB(database);
        removedHours.clear();
    }

    private  void readDB(SQLiteDatabase database){
        Cursor cursor = database.query(
                DBHelper.TABLE_WORKING_TIME,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        if(cursor.moveToFirst()){
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexWorkingDayId = cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_DAYS_ID);
            int indexUserId = cursor.getColumnIndex(DBHelper.KEY_USER_ID);
            int indexWorkingTime = cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);

            do{
                Log.d(TAG, cursor.getString(indexId)
                        + " "
                        + cursor.getString(indexWorkingDayId)
                        + " "
                        + cursor.getString(indexUserId)
                        + " "
                        + cursor.getString(indexWorkingTime)
                        + " "
                );
            } while (cursor.moveToNext());
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
        Log.d(TAG, "Done!");
    }

    private boolean hasTime(Cursor cursor, String time) {
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

    private  String getUserId(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        String userId = sPref.getString(PHONE, "-");

        return userId;
    }

}
