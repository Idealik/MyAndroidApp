package com.example.ideal.myapplication.other;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.createService.MyCalendar;
import com.example.ideal.myapplication.editing.EditService;
import com.example.ideal.myapplication.fragments.User;
import com.example.ideal.myapplication.helpApi.WorkWithTimeApi;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class GuestService extends AppCompatActivity implements View.OnClickListener {

    private static final String PHONE_NUMBER = "Phone number";
    private static final String CITY = "city";
    private static final String NAME = "name";
    private static final String FILE_NAME = "Info";
    private static final String TAG = "DBInf";
    private static final String SERVICE_ID = "service id";
    private static final String USERS = "users";
    private static final String WORKING_DAYS = "working days";
    private static final String WORKING_TIME = "working time";
    private static final String WORKING_DAYS_ID = "working day id";
    private static final String REVIEWS_FOR_SERVICE = "reviews for service";
    private static final String RATING = "rating";

    private static final String STATUS_USER_BY_SERVICE = "status User";
    private static final String OWNER_ID = "owner id";

    Boolean isMyService;
    Boolean haveTime;

    String userId;
    String serviceId;
    String ownerId;
    Integer countOfDate;

    TextView nameText;
    TextView costText;
    TextView descriptionText;
    TextView countOfRatesText;
    WorkWithTimeApi workWithTimeApi;

    Button editScheduleBtn;
    Button editServiceBtn;
    Button profileBtn;

    RatingBar ratingBar;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_service);

        isMyService = false;
        haveTime = false;

        nameText = findViewById(R.id.nameGuestServiceText);
        costText = findViewById(R.id.costGuestServiceText);
        descriptionText = findViewById(R.id.descriptionGuestServiceText);
        countOfRatesText = findViewById(R.id.countOfRatesGuestServiceText);

        editScheduleBtn = findViewById(R.id.editScheduleGuestServiceBtn);
        editServiceBtn = findViewById(R.id.editServiceGuestServiceBtn);
        profileBtn = findViewById(R.id.profileGuestServiceBtn);
        ratingBar = findViewById(R.id.ratingGuestService);

        dbHelper = new DBHelper(this);
        workWithTimeApi = new WorkWithTimeApi();
        serviceId = getIntent().getStringExtra(SERVICE_ID);
        //получаем данные о сервисе
        getDataAboutService(serviceId);
        //получаем рейтинг сервиса
        loadRating(serviceId);
        userId = getUserId();

        // мой сервис или нет?
        isMyService = userId.equals(ownerId);

        if(userId.equals(ownerId)){
            editScheduleBtn.setText("Редактировать расписание");
            editServiceBtn.setVisibility(View.VISIBLE);
            editServiceBtn.setText("Редактировать сервис");
        }
        else {
            editScheduleBtn.setText("Расписание");
        }

        //ratingBar.setClickable(false);
        ratingBar.setOnClickListener(this);

        editScheduleBtn.setOnClickListener(this);
        editServiceBtn.setOnClickListener(this);
        profileBtn.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editScheduleGuestServiceBtn:
                // если мой сервис, то иду, как воркер
                countOfDate = 0;
                haveTime = false;
                String status;
                if (isMyService) {
                    status = "worker";
                } else {
                    status = "User";
                }
                loadSchedule(status);
                break;
            case R.id.editServiceGuestServiceBtn:
                goToEditService();
                break;
            case R.id.profileGuestServiceBtn:
                loadProfileData();
            default:
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void addListenerOnRatingBar() {
        ratingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // TODO perform your action here
                }
                return true;
            }
        });

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
            int indexUserId = cursor.getColumnIndex(DBHelper.KEY_USER_ID);

            ownerId = cursor.getString(indexUserId);

            nameText.setText(cursor.getString(indexName));
            costText.setText(cursor.getString(indexMinCost));
            descriptionText.setText(cursor.getString(indexDescription));
        }

        cursor.close();
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
                    final String dayId = String.valueOf(schedule.getKey());
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
                                    String timeWorkingDayId = String.valueOf(time.child("working day id").getValue());
                                    addTimeInLocalStorage(timeId, timeDate, timeUserId, timeWorkingDayId);
                                }

                                if(status.equals("User") && !haveTime) {
                                    if(hasSomeTime(dayId)) {
                                        haveTime = true;
                                    }
                                }

                                //если прошли по всем дням, идем в календарь
                                if ((dataSnapshotDate.getChildrenCount() == countOfDate)) {
                                    if(status.equals("worker")) {
                                        goToMyCalendar("worker");
                                    }

                                    if(status.equals("User")) {
                                        if(haveTime) {
                                            goToMyCalendar("User");
                                        } else {
                                            attentionThisScheduleIsEmpty();
                                        }
                                    }
                                }
                            }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            attentionBadConnection();
                        }
                    });
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    // Возвращает есть ли в рабочем дне рабочие часы
    private boolean hasSomeTime(String dayId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // Получает id рабочего дня
        // Таблицы: рабочие время
        // Условия: уточняем id рабочего дня
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_TIME_WORKING_TIME + ", "
                        + DBHelper.KEY_DATE_WORKING_DAYS
                        + " FROM "
                        + DBHelper.TABLE_WORKING_TIME + ", "
                        + DBHelper.TABLE_WORKING_DAYS
                        + " WHERE "
                        + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + " = "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_ID
                        + " AND "
                        + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + " = ? "
                        + " AND ("
                        + DBHelper.KEY_USER_ID + " = 0"
                        + " OR "
                        + DBHelper.KEY_USER_ID + " = ?)";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{dayId, userId});

        if(cursor.moveToFirst()) {
            int indexDate = cursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);
            int indexTime = cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);
            String date, time;

            do {
                date = cursor.getString(indexDate);
                time = cursor.getString(indexTime);
                if(hasMoreThenTwoHours(date, time)) {
                    cursor.close();
                    return true;
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return false;
    }

    private boolean hasMoreThenTwoHours(String date, String time) {
        long twoHours = 2*60*60*1000;
        long sysdateLong = workWithTimeApi.getSysdateLong();
        long currentLong = workWithTimeApi.getMillisecondsStringDate(date + " " + time);

        return currentLong - sysdateLong >= twoHours;
    }

    private void addScheduleInLocalStorage(String dayId, String dayDate) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        String sqlQuery = "SELECT * FROM "
                + DBHelper.TABLE_WORKING_DAYS
                + " WHERE "
                + DBHelper.KEY_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[] {dayId});

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_DATE_WORKING_DAYS, dayDate);
        contentValues.put(DBHelper.KEY_SERVICE_ID_WORKING_DAYS, serviceId);

        if(cursor.moveToFirst()) {
            database.update(DBHelper.TABLE_WORKING_DAYS, contentValues,
                    DBHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(dayId)});
        } else {
            contentValues.put(DBHelper.KEY_ID, dayId);
            database.insert(DBHelper.TABLE_WORKING_DAYS, null, contentValues);
        }

        cursor.close();
    }

    private void addTimeInLocalStorage(String timeId, String timeDate,
                                       String timeUserId, String timeWorkingDayId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        String sqlQuery = "SELECT * FROM "
                + DBHelper.TABLE_WORKING_TIME
                + " WHERE "
                + DBHelper.KEY_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[] {timeId});

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_TIME_WORKING_TIME, timeDate);
        contentValues.put(DBHelper.KEY_USER_ID,timeUserId);
        contentValues.put(DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME, timeWorkingDayId);

        if(cursor.moveToFirst()) {
            database.update(DBHelper.TABLE_WORKING_TIME, contentValues,
                    DBHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(timeId)});
        } else {
            contentValues.put(DBHelper.KEY_ID, timeId);
            database.insert(DBHelper.TABLE_WORKING_TIME, null, contentValues);
        }
        cursor.close();
    }

    private void loadProfileData(){
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference(USERS).child(ownerId);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                String name = String.valueOf(userSnapshot.child(NAME).getValue());
                String city = String.valueOf(userSnapshot.child(CITY).getValue());
                User user = new User();

                user.setName(name);
                user.setCity(city);
                putDataInLocalStorage(user, ownerId);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    private void loadRating(String serviceId) {
        //зашружаю среднюю оценку, складываю все и делю их на количество
        // также усталавниваю количество оценок
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        Query query = database.getReference(REVIEWS_FOR_SERVICE)
                .orderByChild(SERVICE_ID)
                .equalTo(serviceId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot rates) {
                float sumRates = 0;
                long countOfRates = rates.getChildrenCount();

                for(DataSnapshot rate: rates.getChildren()){
                    sumRates += Float.valueOf(String.valueOf(rate.child(RATING).getValue()));
                }
                ratingBar.setRating(sumRates/countOfRates);
                countOfRatesText.setText(String.valueOf(countOfRates));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void putDataInLocalStorage(User user, String phoneNumber) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.KEY_NAME_USERS, user.getName());
        contentValues.put(DBHelper.KEY_CITY_USERS, user.getCity());
        contentValues.put(DBHelper.KEY_USER_ID, phoneNumber);

        database.insert(DBHelper.TABLE_CONTACTS_USERS,null,contentValues);
        goToProfile();
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

        return  sPref.getString(PHONE_NUMBER, "0");
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
        intent.putExtra(OWNER_ID, ownerId);

        startActivity(intent);
    }

    private void attentionBadConnection() {
        Toast.makeText(this,"Плохое соединение",Toast.LENGTH_SHORT).show();
    }
}