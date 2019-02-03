package com.example.ideal.myapplication.other;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.createService.MyCalendar;
import com.example.ideal.myapplication.editing.EditService;
import com.example.ideal.myapplication.reviews.RatingBarForServiceElement;
import com.example.ideal.myapplication.fragments.objects.RatingReview;
import com.example.ideal.myapplication.fragments.objects.User;
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
    private static final String REVIEW = "review";
    private static final String USERS = "users";
    private static final String WORKING_DAYS = "working days";
    private static final String WORKING_TIME = "working time";
    private static final String VALUING_PHONE = "valuing phone";
    private static final String MESSAGE_TIME = "valuing phone";
    private static final String WORKING_DAYS_ID = "working day id";
    private static final String REVIEWS_FOR_SERVICE = "reviews for service";
    private static final String RATING = "rating";

    private static final String STATUS_USER_BY_SERVICE = "status User";
    private static final String OWNER_ID = "owner id";

    private Boolean isMyService;
    private Boolean haveTime;

    private String userId;
    private String serviceId;
    private String ownerId;
    private Integer countOfDate;

    private TextView nameText;
    private TextView costText;
    private TextView descriptionText;
    private WorkWithTimeApi workWithTimeApi;

    private FragmentManager manager;
    private LinearLayout ratingLayout;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_service);

        isMyService = false;
        haveTime = false;

        nameText = findViewById(R.id.nameGuestServiceText);
        costText = findViewById(R.id.costGuestServiceText);
        descriptionText = findViewById(R.id.descriptionGuestServiceText);

        Button editScheduleBtn = findViewById(R.id.editScheduleGuestServiceBtn);
        Button editServiceBtn = findViewById(R.id.editServiceGuestServiceBtn);
        Button profileBtn = findViewById(R.id.profileGuestServiceBtn);
        manager = getSupportFragmentManager();

        ratingLayout = findViewById(R.id.resultGuestServiceLayout);

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

        if (userId.equals(ownerId)) {
            editScheduleBtn.setText("Редактировать расписание");
            editServiceBtn.setVisibility(View.VISIBLE);
            editServiceBtn.setText("Редактировать сервис");
        } else {
            editScheduleBtn.setText("Расписание");
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

    private void getDataAboutService(String serviceId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        // получаем сервис с указанным ID
        String sqlQuery =
                "SELECT " + DBHelper.TABLE_CONTACTS_SERVICES + ".*"
                        + " FROM " + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE " + DBHelper.KEY_ID + " = ? ";
        Cursor cursor = database.rawQuery(sqlQuery, new String[]{serviceId});

        if (cursor.moveToFirst()) {
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
                if ((dataSnapshotDate.getChildrenCount() == 0) && (status.equals("worker"))) {
                    goToMyCalendar(status);
                }
                //если юзер и у воркера еще нету расписания на этот сервис
                if ((dataSnapshotDate.getChildrenCount() == 0) && (status.equals("User"))) {
                    attentionThisScheduleIsEmpty();
                }
                for (DataSnapshot schedule : dataSnapshotDate.getChildren()) {
                    final String dayId = String.valueOf(schedule.getKey());
                    String dayDate = String.valueOf(schedule.child("data").getValue());
                    addScheduleInLocalStorage(dayId, dayDate);

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

                            if (status.equals("User") && !haveTime) {
                                if (hasSomeTime(dayId)) {
                                    haveTime = true;
                                }
                            }

                            //если прошли по всем дням, идем в календарь
                            if ((dataSnapshotDate.getChildrenCount() == countOfDate)) {
                                if (status.equals("worker")) {
                                    goToMyCalendar("worker");
                                }

                                if (status.equals("User")) {
                                    if (haveTime) {
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

        if (cursor.moveToFirst()) {
            int indexDate = cursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);
            int indexTime = cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);
            String date, time;

            do {
                date = cursor.getString(indexDate);
                time = cursor.getString(indexTime);
                if (hasMoreThenTwoHours(date, time)) {
                    cursor.close();
                    return true;
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return false;
    }

    private boolean hasMoreThenTwoHours(String date, String time) {
        long twoHours = 2 * 60 * 60 * 1000;
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

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{dayId});

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_DATE_WORKING_DAYS, dayDate);
        contentValues.put(DBHelper.KEY_SERVICE_ID_WORKING_DAYS, serviceId);

        if (cursor.moveToFirst()) {
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

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{timeId});

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_TIME_WORKING_TIME, timeDate);
        contentValues.put(DBHelper.KEY_USER_ID, timeUserId);
        contentValues.put(DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME, timeWorkingDayId);

        if (cursor.moveToFirst()) {
            database.update(DBHelper.TABLE_WORKING_TIME, contentValues,
                    DBHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(timeId)});
        } else {
            contentValues.put(DBHelper.KEY_ID, timeId);
            database.insert(DBHelper.TABLE_WORKING_TIME, null, contentValues);
        }
        cursor.close();
    }

    private void loadProfileData() {
        DatabaseReference userReference = FirebaseDatabase.getInstance()
                .getReference(USERS)
                .child(ownerId);

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
                RatingReview ratingReview = new RatingReview();

                for (DataSnapshot rate : rates.getChildren()) {
                    sumRates += Float.valueOf(String.valueOf(rate.child(RATING).getValue()));
                    ratingReview.setId(String.valueOf(rate.getKey()));
                    ratingReview.setReview(String.valueOf(rate.child(REVIEW).getValue()));
                    ratingReview.setRating(String.valueOf(rate.child(RATING).getValue()));
                    ratingReview.setValuingPhone(String.valueOf(rate.child(VALUING_PHONE).getValue()));
                    ratingReview.setServiceId(String.valueOf(rate.child(SERVICE_ID).getValue()));
                    ratingReview.setMessageTime(String.valueOf(rate.child(MESSAGE_TIME).getValue()));
                    addReviewForServiceInLocalStorage(ratingReview);
                }
                ratingReview.setAvgRating(String.valueOf(sumRates / countOfRates));
                ratingReview.setCountOfRates(String.valueOf(countOfRates));
                addToScreen(ratingReview);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void addReviewForServiceInLocalStorage(RatingReview ratingReview) {

        SQLiteDatabase database= dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        String sqlQuery = "SELECT * FROM "
                + DBHelper.TABLE_REVIEWS_FOR_SERVICE
                + " WHERE "
                + DBHelper.KEY_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{ratingReview.getId()});

        contentValues.put(DBHelper.KEY_REVIEW_REVIEWS_FOR_SERVICE,ratingReview.getReview());
        contentValues.put(DBHelper.KEY_RATING_REVIEWS_FOR_SERVICE,ratingReview.getRating());
        contentValues.put(DBHelper.KEY_VALUING_PHONE_REVIEWS_FOR_SERVICE,ratingReview.getValuingPhone());
        contentValues.put(DBHelper.KEY_SERVICE_ID_REVIEWS_FOR_SERVICE,ratingReview.getServiceId());
        contentValues.put(DBHelper.KEY_MESSAGE_TIME_MESSAGES,ratingReview.getMessageTime());

        if (cursor.moveToFirst()) {
            database.update(DBHelper.TABLE_REVIEWS_FOR_SERVICE, contentValues,
                    DBHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(ratingReview.getId())});
        } else {
            contentValues.put(DBHelper.KEY_ID, ratingReview.getId());
            database.insert(DBHelper.TABLE_REVIEWS_FOR_SERVICE, null, contentValues);
        }
        cursor.close();
        getUserFromReviewForService(ratingReview.getValuingPhone());

    }

    private void getUserFromReviewForService(final String valuingPhone) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(USERS)
                .child(valuingPhone);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot user) {
                Log.d(TAG, "onDataChange: " + user.getValue());
                User localUser = new User();
                localUser.setPhone(valuingPhone);
                localUser.setName(String.valueOf(user.child(NAME).getValue()));
                localUser.setCity(String.valueOf(user.child(CITY).getValue()));
                addUserInLocalStorage(localUser);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addUserInLocalStorage(User localUser) {
        Log.d(TAG, "addUserInLocalStorage: ");
        SQLiteDatabase database= dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        String sqlQuery = "SELECT * FROM "
                + DBHelper.TABLE_CONTACTS_USERS
                + " WHERE "
                + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{localUser.getPhone()});

        contentValues.put(DBHelper.KEY_NAME_USERS,localUser.getName());
        contentValues.put(DBHelper.KEY_CITY_USERS,localUser.getCity());

        if (cursor.moveToFirst()) {
            Log.d(TAG, "addUserInLocalStorage: ");
            database.update(DBHelper.TABLE_CONTACTS_USERS, contentValues,
                    DBHelper.KEY_USER_ID + " = ?",
                    new String[]{String.valueOf(localUser.getPhone())});
            chechDb(localUser.getPhone());
        } else {
            // если только тут кладжешь, то в update пропадает keyId?
            contentValues.put(DBHelper.KEY_USER_ID, localUser.getPhone());
            Log.d(TAG, "addUserInLocalStorage: " + localUser.getPhone());
            database.insert(DBHelper.TABLE_CONTACTS_USERS, null, contentValues);
            chechDb(localUser.getPhone());
        }
        cursor.close();

    }

    private void chechDb(String phone) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        String sqlQuery = "SELECT * FROM "
                + DBHelper.TABLE_CONTACTS_USERS
                + " WHERE "
                + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{phone});
        Log.d(TAG, "chechDb: " + phone);
        if(cursor.moveToFirst()){
            Log.d(TAG, "chechDb: ");
            int indexNameUser = cursor.getColumnIndex(DBHelper.KEY_NAME_USERS);
            int indexCityUser = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);
            int indexPhoneUser = cursor.getColumnIndex(DBHelper.KEY_USER_ID);
            User user = new User();
            user.setName(cursor.getString(indexNameUser));
            user.setCity(cursor.getString(indexCityUser));
            user.setPhone(cursor.getString(indexPhoneUser));
            Log.d(TAG, "createReviews: " + user.getPhone());
            Log.d(TAG, "createReviews: " + user.getName());
        }
        cursor.close();

    }

    private void addToScreen(RatingReview ratingReview) {
        RatingBarForServiceElement fElement = new RatingBarForServiceElement(ratingReview);

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.resultGuestServiceLayout, fElement);
        transaction.commit();
    }
    //ПЕРЕПИСАТЬ
    private void putDataInLocalStorage(User user, String phoneNumber) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.KEY_NAME_USERS, user.getName());
        contentValues.put(DBHelper.KEY_CITY_USERS, user.getCity());
        contentValues.put(DBHelper.KEY_USER_ID, phoneNumber);

        database.insert(DBHelper.TABLE_CONTACTS_USERS, null, contentValues);
        goToProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void attentionThisScheduleIsEmpty() {
        Toast.makeText(
                this,
                "Пользователь еще не написал расписание к этому сервису.",
                Toast.LENGTH_SHORT).show();
    }

    private String getUserId() {
        SharedPreferences sPref;
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);

        return sPref.getString(PHONE_NUMBER, "0");
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

    private void goToProfile() {
        Intent intent = new Intent(this, Profile.class);
        intent.putExtra(OWNER_ID, ownerId);

        startActivity(intent);
    }

    private void attentionBadConnection() {
        Toast.makeText(this, "Плохое соединение", Toast.LENGTH_SHORT).show();
    }
}