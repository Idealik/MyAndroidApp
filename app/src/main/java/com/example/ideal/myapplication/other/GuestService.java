package com.example.ideal.myapplication.other;

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

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.createService.MyCalendar;
import com.example.ideal.myapplication.editing.EditService;

public class GuestService extends AppCompatActivity implements View.OnClickListener {
    //
    private final String PHONE = "phone";
    private final String FILE_NAME = "Info";
    private final String TAG = "DBInf";
    private final String SERVICE_ID = "service id";
    private final String NAME_SERVICE = "name service";
    private final String COST_SERVICE = "cost service";
    private final String DESCRIPTION_SERVICE = "description service";
    private final String STATUS_USER_BY_SERVICE = "status User";

    Boolean isMyService = false;

    TextView nameText;
    TextView costText;
    TextView descriptionText;

    Button editScheduleBtn;
    Button editServiceBtn;
    Button profileBtn;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_service);

        nameText = findViewById(R.id.nameGuestServiceText);
        costText = findViewById(R.id.costGuestServiceText);
        descriptionText = findViewById(R.id.descriptionGuestServiceText);

        editScheduleBtn = findViewById(R.id.editScheduleGuestServiceBtn);
        editServiceBtn = findViewById(R.id.editServiceGuestServiceBtn);
        profileBtn = findViewById(R.id.profileGuestServiceBtn);

        dbHelper = new DBHelper(this);
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);
        //получаем данные о сервисе
        getDataAboutService(serviceId);

        long userId = getUserId();
        // мой сервис или нет?
        isMyService = isMyService(serviceId,userId);

        if(isMyService){
            editScheduleBtn.setText("Редактировать расписание");
        }
        else {
            editScheduleBtn.setText("Расписание");
        }

        if(isMyService){
            editServiceBtn.setVisibility(View.VISIBLE);
            editServiceBtn.setText("Редактировать сервис");
        }
        editScheduleBtn.setOnClickListener(this);
        editServiceBtn.setOnClickListener(this);
        profileBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editScheduleGuestServiceBtn:
                // если мой сервис, то иду, как воркер
                if(isMyService){
                    goToMyCalendar("worker");
                }
                else {
                    goToMyCalendar("User");
                }
                break;
            case R.id.editServiceGuestServiceBtn:
                goToEditService();
            break;
            case R.id.profileGuestServiceBtn:
                goToProfile();
            default: break;
        }
    }

    private void getDataAboutService(long serviceId) {
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
        // вернуть имя сервиса
        // из таблицы Services
        // где фиксированное serviceId и номер телефона пользователя
        String sqlQuery =
                "SELECT " + DBHelper.KEY_NAME_SERVICES
                        + " FROM " + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE " + DBHelper.KEY_ID + " = ? AND " + DBHelper.KEY_USER_ID + " = ? ";
        Cursor cursor = database.rawQuery(sqlQuery, new String[] {String.valueOf(serviceId), String.valueOf(userId)});

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

        Intent intent = new Intent(this, MyCalendar.class);
        intent.putExtra(SERVICE_ID, serviceId);
        intent.putExtra(STATUS_USER_BY_SERVICE, status);

        startActivity(intent);
    }

    private void goToEditService() {
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);
        String name = nameText.getText().toString();
        String cost = costText.getText().toString();
        String description = descriptionText.getText().toString();

        Intent intent = new Intent(this, EditService.class);
        intent.putExtra(SERVICE_ID, serviceId);
        intent.putExtra(NAME_SERVICE, name);
        intent.putExtra(COST_SERVICE, cost);
        intent.putExtra(DESCRIPTION_SERVICE, description);

        startActivity(intent);
    }

    private void goToProfile(){
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);

        Intent intent = new Intent(this, Profile.class);
        intent.putExtra(SERVICE_ID, serviceId);

        startActivity(intent);
    }

}