package com.example.ideal.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class myTime extends AppCompatActivity  implements View.OnClickListener {
    final String TAG = "DBInf";
    final String FILE_NAME = "Info";
    final String PHONE = "phone";
    final String WORKING_DAYS_ID = "working days id";


    String fullTime = "";

    Button[][] timeBtns;

    // чтобы сохранять несколько выбранных часов надо создать массив,
    // куда будем добавлять текст выбранной кнопки?

    DBHelper dbHelper;
    SharedPreferences sPref;
    RelativeLayout mainLayout;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_time);
        dbHelper = new DBHelper(this);

        mainLayout=findViewById(R.id.mainMyTimeLayout);

        timeBtns = new Button[7][4];

        for (int i=0; i<6; i++) {
            for (int j=0; j<4; j++) {
                timeBtns[i][j]= new Button(this);
                timeBtns[i][j].setWidth(50);
                timeBtns[i][j].setHeight(30);
                timeBtns[i][j].setX(j*200);
                timeBtns[i][j].setY(i*100);
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
    }

    @Override
    public void onClick(View v) {
        //SQLiteDatabase database = dbHelper.getWritableDatabase();

        Button btn = (Button) v;
        fullTime = btn.getText().toString();

        addTime();
    }

    private void addTime(){
        long workingDaysId = getIntent().getLongExtra(WORKING_DAYS_ID, -1);

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.KEY_TIME_WORKING_TIME, fullTime);
        contentValues.put(DBHelper.KEY_USER_ID, getUserId());
        contentValues.put(DBHelper.KEY_TIME_WORKING_DAYS_ID, workingDaysId);

        database.insert(DBHelper.TABLE_WORKING_TIME,null,contentValues);

        readDB(database);

    }

    private  void readDB(SQLiteDatabase database){
        String msg= "";
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
            int indexDate = cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_DAYS_ID);
            int indexUserId = cursor.getColumnIndex(DBHelper.KEY_USER_ID);
            int indexWorkingTime = cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);

            do{
                Log.d(TAG, cursor.getString(indexId)
                        + " "
                        + cursor.getString(indexDate)
                        + " "
                        + cursor.getString(indexUserId)
                        + " "
                        + cursor.getString(indexWorkingTime)
                        + "\n "
                );
            } while (cursor.moveToNext());
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
    }


    private  String getUserId(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        String userId = sPref.getString(PHONE, "-");

        return userId;
    }

}
