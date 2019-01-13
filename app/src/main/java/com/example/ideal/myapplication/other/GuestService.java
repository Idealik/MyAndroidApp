package com.example.ideal.myapplication.other;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.createService.MyCalendar;
import com.example.ideal.myapplication.editing.EditService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class GuestService extends AppCompatActivity implements View.OnClickListener {
    //
    private static final String PHONE_NUMBER = "Phone number";
    private final String FILE_NAME = "Info";
    private final String TAG = "DBInf";
    private final String SERVICE_ID = "service id";
    private static final String WORKING_DAYS = "working days/";
    private static final String WORKING_TIME = "working time/";
    private final String WORKING_DAYS_ID = "working day id";

    private final String STATUS_USER_BY_SERVICE = "status User";

    Boolean isMyService = false;
    String serviceId;
    Integer countOfDate;

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
        serviceId = getIntent().getStringExtra(SERVICE_ID);
        //получаем данные о сервисе
        getDataAboutService(serviceId);

        String userId = getUserId();
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
                countOfDate=0;
                if(isMyService){
                    String status = "worker";
                    loadSchedule(status);
                }
                else {
                    String status = "User";
                    loadSchedule(status);
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

    private void getDataAboutService(String serviceId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        // получаем сервис с указанным ID
        String sqlQuery =
                "SELECT " + DBHelper.TABLE_CONTACTS_SERVICES + ".*"
                        + " FROM " + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE " + DBHelper.KEY_ID +" = ? ";
        Cursor cursor = database.rawQuery(sqlQuery, new String[] {serviceId});

        if(cursor.moveToFirst()) {
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);
            int indexDescription = cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION_SERVICES);

            nameText.setText(cursor.getString(indexName));
            costText.setText(cursor.getString(indexMinCost));
            descriptionText.setText(cursor.getString(indexDescription));
        }

        cursor.close();
    }

    private boolean isMyService(String serviceId, String userId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        // вернуть имя сервиса
        // из таблицы Services
        // где фиксированное serviceId и номер телефона пользователя
        String sqlQuery =
                "SELECT " + DBHelper.KEY_NAME_SERVICES
                        + " FROM " + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE " + DBHelper.KEY_ID + " = ? AND " + DBHelper.KEY_USER_ID + " = ? ";
        Cursor cursor = database.rawQuery(sqlQuery, new String[] {String.valueOf(serviceId), userId});

        // такой существует ?
        if(cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }
    private void loadSchedule(final String status) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        //возвращает все дни определенного сервиса
        final Query query = database.getReference(WORKING_DAYS).
                orderByChild(SERVICE_ID).
                equalTo(serviceId);
        //загружаем рабочие дни
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshotDate) {
                //если воркер и нет расписания
                if((dataSnapshotDate.getChildrenCount() == 0) && (status.equals("worker"))){
                    goToMyCalendar(status);
                }
                //если юзер и у воркера еще нету расписания на этот сервис
                if((dataSnapshotDate.getChildrenCount() == 0) && (status.equals("User"))) {
                    attentionThisScheduleIsEmpty();
                }
                for (DataSnapshot schedule : dataSnapshotDate.getChildren()){
                    String dayId = String.valueOf(schedule.getKey());
                    String dayDate = String.valueOf(schedule.child("data").getValue());
                    addScheduleInLocalStorage(dayId,dayDate);

                    //загружаем часы работы
                    final Query queryTime = database.getReference(WORKING_TIME).
                            orderByChild(WORKING_DAYS_ID).
                            equalTo(dayId);

                    queryTime.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            countOfDate++;

                            for (DataSnapshot time : dataSnapshot.getChildren()) {
                                String timeId = String.valueOf(time.getKey());
                                String timeDate = String.valueOf(time.child("time").getValue());
                                String timeUserId = String.valueOf(time.child("user id").getValue());
                                String timeWorkingDayId = String.valueOf(time.child("working day id")
                                        .getValue());

                                addTimeInLocalStorage(timeId, timeDate,timeUserId,timeWorkingDayId);
                            }
                            //если прошли по всем дням, идем в календарь
                            if(dataSnapshotDate.getChildrenCount()==countOfDate) goToMyCalendar(status);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: GUEST SERiVCE");
            }
        });
    }
    private void addScheduleInLocalStorage(String dayId, String dayDate) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_ID, dayId);
        contentValues.put(DBHelper.KEY_DATE_WORKING_DAYS, dayDate);
        contentValues.put(DBHelper.KEY_SERVICE_ID_WORKING_DAYS, serviceId);

        database.insert(DBHelper.TABLE_WORKING_DAYS, null, contentValues);

    }

    private void addTimeInLocalStorage(String timeId, String timeDate,
                                       String timeUserId, String timeWorkingDayId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.KEY_ID, timeId);
        contentValues.put(DBHelper.KEY_TIME_WORKING_TIME, timeDate);
        contentValues.put(DBHelper.KEY_USER_ID,timeUserId);
        contentValues.put(DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME, timeWorkingDayId);

        database.insert(DBHelper.TABLE_WORKING_TIME,null,contentValues);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void attentionThisScheduleIsEmpty(){
        Toast.makeText(
                this,
                "Пользователь еще не написал расписание к этому сервису.",
                Toast.LENGTH_SHORT).show();
    }
    private String getUserId() {
        SharedPreferences sPref;
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String userId = sPref.getString(PHONE_NUMBER, "0");

        return  userId;
    }

    private void goToMyCalendar(String status) {
        Intent intent = new Intent(this, MyCalendar.class);
        intent.putExtra(SERVICE_ID, serviceId);
        intent.putExtra(STATUS_USER_BY_SERVICE, status);

        startActivity(intent);
    }

    private void goToEditService() {
        Intent intent = new Intent(this, EditService.class);
        intent.putExtra(SERVICE_ID, serviceId);

        startActivity(intent);
    }

    private void goToProfile(){
        Intent intent = new Intent(this, Profile.class);
        intent.putExtra(SERVICE_ID, serviceId);

        startActivity(intent);
    }

}