package com.example.ideal.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class guestService extends AppCompatActivity implements View.OnClickListener {

    private final String PHONE = "phone";
    private final String FILE_NAME = "Info";
    private final String TAG = "DBInf";
    private final String SERVICE_ID = "service id";
    private final String STATUS_USER_BY_SERVICE = "status user";

    Boolean isMyService = false;

    TextView nameText;
    TextView costText;
    TextView descriptionText;

    Button editServiceBtn;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_service);

        nameText = findViewById(R.id.nameGuestServiceText);
        costText = findViewById(R.id.costGuestServiceText);
        descriptionText = findViewById(R.id.descriptionGuestServiceText);

        editServiceBtn = findViewById(R.id.editServiceGuestServiceBtn);

        dbHelper = new DBHelper(this);
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);

        getData(serviceId);
        long userId = getUserId();
        // спрашиваю мой сервис или нет?
        // в переменной, чтобы использовать много где и постоянно не вызывать метод
        isMyService = isMyService(serviceId,userId);

        if(isMyService){
            editServiceBtn.setText("Редактировать сервис");
        }
        else {
            editServiceBtn.setText("Расписание");
        }

        editServiceBtn.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editServiceGuestServiceBtn:
                if(isMyService){
                    goToMyEditService("worker");
                }
                else {
                    goToMyCalendar("user");
                }

        }
    }

    private void getData(long serviceId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        // получаем сервис с указанным ID
        String sqlQuery =
                "SELECT " + DBHelper.TABLE_CONTACTS_SERVICES + ".*"
                        + " FROM " + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE " + DBHelper.KEY_ID +" = ? ";
        Cursor cursor = database.rawQuery(sqlQuery, new String[] {String.valueOf(serviceId)});

        if(cursor.moveToFirst()) {
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);
            int indexDescription = cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION_SERVICES);

            nameText.setText(cursor.getString(indexName));
            costText.setText(cursor.getString(indexMinCost));
            descriptionText.setText(cursor.getString(indexDescription));
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
    }

    private boolean isMyService(long serviceId, long userId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //Картеж этого сервиса с id текущего пользователя
        Cursor cursor = database.query(
            DBHelper.TABLE_CONTACTS_SERVICES,
            new String[]{DBHelper.KEY_NAME_SERVICES},
            DBHelper.KEY_ID + " = ? AND " + DBHelper.KEY_USER_ID + " = ? ",
            new String[]{String.valueOf(serviceId), String.valueOf(userId)},
            null,
            null,
            null,
            null);

        // такой существует ?
        if(cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    private long getUserId() {
        SharedPreferences sPref;
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        long userId = Long.valueOf(sPref.getString(PHONE, "0"));

        return  userId;
    }

    private void goToMyCalendar(String status) {
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);
        Log.d(TAG, serviceId + " ");
        Log.d(TAG, status + " ");

        Intent intent = new Intent(this, myCalendar.class);
        intent.putExtra(SERVICE_ID, serviceId);
        intent.putExtra(STATUS_USER_BY_SERVICE, status);

        startActivity(intent);
    }

    private void goToMyEditService(String status) {
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);
        Log.d(TAG, serviceId + " ");
        Log.d(TAG, status + " ");

        Intent intent = new Intent(this, editService.class);
        intent.putExtra(SERVICE_ID, serviceId);
        intent.putExtra(STATUS_USER_BY_SERVICE, status);

        startActivity(intent);
    }

}