package com.example.ideal.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.ideal.myapplication.fragments.foundOrderElement;
import com.example.ideal.myapplication.fragments.foundServiceProfileElement;

public class profile extends AppCompatActivity implements View.OnClickListener {

    final String TAG = "DBInf";
    final String PHONE = "phone";
    final String FILE_NAME = "Info";
    final String STATUS = "status";
    static final String USER_NAME = "my name";
    static final String USER_SURNAME = "my surname";
    static final String USER_CITY = "my city";
    final String SERVICE_ID = "service id";

    Button logOutBtn;
    Button findServicesBtn;
    Button addServicesBtn;
    Button mainScreenBtn;
    Button editProfileBtn;

    TextView nameText;
    TextView surnameText;
    TextView cityText;

    ScrollView servicesScroll;
    ScrollView ordersScroll;
    LinearLayout servicesLayout;
    LinearLayout ordersLayout;

    SwitchCompat servicesOrOrdersSwitch;

    SharedPreferences sPref;
    DBHelper dbHelper;

    private foundServiceProfileElement fServiceElement;
    private foundOrderElement fOrderElement;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        logOutBtn = (Button) findViewById(R.id.logOutProfileBtn);
        findServicesBtn = (Button) findViewById(R.id.findServicesProfileBtn);
        addServicesBtn = (Button) findViewById(R.id.addServicesProfileBtn);
        mainScreenBtn = (Button) findViewById(R.id.mainScreenProfileBtn);
        editProfileBtn = (Button) findViewById(R.id.editProfileBtn);

        servicesOrOrdersSwitch = findViewById(R.id.servicesOrOrdersProfileSwitch);

        servicesScroll = findViewById(R.id.servicesProfileScroll);
        ordersScroll = findViewById(R.id.orderProfileScroll);
        servicesLayout = findViewById(R.id.servicesProfileLayout);
        ordersLayout = findViewById(R.id.ordersProfileLayout);

        nameText = findViewById(R.id.nameProfileText);
        surnameText= findViewById(R.id.surnameProfileText);
        cityText = findViewById(R.id.cityProfileText);

        dbHelper = new DBHelper(this);

        manager = getSupportFragmentManager();

        //получаем id пользователя
        long userId = getUserId();
        // получаем сервис пользователя, если он заходит к себе в профиль, то -1
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);
        Log.d(TAG, "Before if user id = " + userId + "  sI = " + serviceId);

        // идет проверка, относится ли этот сервис к пользователю, чтобы дать соответствующий функционал
        if(isMyService(serviceId,userId)){
            // если это мой сервис - значит мой профиль
            // создаем свои сервисы
            createServicesList(userId);
            // создаем список записей
            createOrdersList(userId);
            // добавляем данные о пользователе
            createProfileData(userId);
            servicesLayout.setVisibility(View.INVISIBLE);
            servicesScroll.setVisibility(View.INVISIBLE);

            servicesOrOrdersSwitch.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        buttonView.setText("My services");
                        ordersLayout.setVisibility(View.INVISIBLE);
                        ordersScroll.setVisibility(View.INVISIBLE);
                        servicesLayout.setVisibility(View.VISIBLE);
                        servicesScroll.setVisibility(View.VISIBLE);
                    } else {
                        buttonView.setText("My orders");
                        servicesLayout.setVisibility(View.INVISIBLE);
                        servicesScroll.setVisibility(View.INVISIBLE);
                        ordersLayout.setVisibility(View.VISIBLE);
                        ordersScroll.setVisibility(View.VISIBLE);
                    }
                }
            });
            addServicesBtn.setOnClickListener(this);
            editProfileBtn.setOnClickListener(this);

        }else {
            //прячем функционал, мы же на чужом профиле
            servicesOrOrdersSwitch.setVisibility(View.INVISIBLE);
            addServicesBtn.setVisibility(View.INVISIBLE);
            editProfileBtn.setVisibility(View.INVISIBLE);
            // получаем id user, которому принадлжеит сервис
            userId = getUserId(serviceId);
            Log.d(TAG, "user id = " + userId);

            // подгружаем данные о пользователе
            createProfileData(userId);
            // подргужаем его сервисы
            createServicesList(userId);

            // отображаем все сервисы пользователя
            ordersLayout.setVisibility(View.INVISIBLE);
            ordersScroll.setVisibility(View.INVISIBLE);
            servicesLayout.setVisibility(View.VISIBLE);
            servicesScroll.setVisibility(View.VISIBLE);
        }

        logOutBtn.setOnClickListener(this);
        findServicesBtn.setOnClickListener(this);
        mainScreenBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addServicesProfileBtn:
                goToAddService();
                break;
            case R.id.findServicesProfileBtn:
                goToSearchService();
                break;
            case R.id.logOutProfileBtn:
                annulStatus();
                goToLogIn();
                break;
            case R.id.mainScreenProfileBtn:
                goToMainScreen();
                break;
            case R.id.editProfileBtn:
                goToEditProfile();
                break;
            default:
                break;
        }
    }

    private boolean isMyService(long serviceId, long userId) {
        //значит мы зашли сразу в профиль свой и это владелец
        if(serviceId == -1) return true;

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
    //получаем все заказы, которые принадлежат юзеру
    private void createOrdersList(Long userId){
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        String sqlQuery =
                "SELECT "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_ID + ", "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_NAME_SERVICES + ", "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_DATE_WORKING_DAYS + ", "
                        + DBHelper.TABLE_WORKING_TIME + "." + DBHelper.KEY_TIME_WORKING_TIME
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_SERVICES + ", "
                        + DBHelper.TABLE_WORKING_DAYS + ", "
                        + DBHelper.TABLE_WORKING_TIME
                        + " WHERE "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_ID + " = " + DBHelper.KEY_SERVICE_ID_WORKING_DAYS
                        + " AND "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_ID + " = " + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME
                        + " AND "
                        + DBHelper.TABLE_WORKING_TIME + "." + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[] {String.valueOf(userId)});

        if(cursor.moveToFirst()){
            int indexServiceId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexServiceName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexDate = cursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);
            int indexTime = cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);

            do{
                String foundId = cursor.getString(indexServiceId);
                String foundName = cursor.getString(indexServiceName);
                String foundDate = cursor.getString(indexDate);
                String foundTime = cursor.getString(indexTime);

                addOrderToScreen(foundId, foundName, foundDate, foundTime);
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    //получаем все сервисы, которые пренадлежат юзеру
    private void createServicesList(long userId){
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // нужно вернуть, имя, фамилию, город, название услуги, цену, оценку (пока без оценки)
        // используем 2 таблицы - юзеры и сервисы
        // связываем их по номеру телефона юзеров, уточняем юзера по его id
        String sqlQuery =
                "SELECT " +  DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_NAME_SERVICES
                        + ", " + DBHelper.KEY_ID
                        + " FROM " + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE " + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_USER_ID + " = ?";
        Cursor cursor = database.rawQuery(sqlQuery, new String[] {String.valueOf(userId)});

        if(cursor.moveToFirst()){
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);

            int indexNameService = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);

            do{
                //  формирую сообщения, в будущем тут будем формировать объект
                String foundId = cursor.getString(indexId);
                String foundNameService = cursor.getString(indexNameService);
                addServiceToScreen(foundId, foundNameService);
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    //получить id-phone пользователя
    private  long getUserId(){
        // возваращает id текущего пользователя
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        long userId = Long.valueOf(sPref.getString(PHONE, "0"));

        return userId;
    }

    //возвращает id юзера указанного сервиса
    private long getUserId(Long serviceId){
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        // нужно вернуть: userID из Users
        // используем таблицы: Services and Users
        // Условие: по заданному serviceID
        String sqlQuery =
                "SELECT "
                        + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_USER_ID
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_USERS + ", "
                        + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_ID + " = ?"
                        + " AND "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_USER_ID +
                        " = "
                        + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_USER_ID;

        Cursor cursor = database.rawQuery(sqlQuery,new String[] {String.valueOf(serviceId)});

        Long userId;

        if(cursor.moveToFirst()){
            int indexUserId = cursor.getColumnIndex(DBHelper.KEY_USER_ID);
            userId = cursor.getLong(indexUserId);
        }
        else {
            //при присвоении значения лонг, надо добюавлять "L"
            userId = -1L; // но такого быть не может
        }

        return userId;
    }
    // получаем данные о пользователе и отображаем в прфоиле
    private void createProfileData(Long _userId){

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String userId = String.valueOf(_userId);
        String sqlQuery =
                "SELECT "
                + DBHelper.KEY_NAME_USERS + ", "
                + DBHelper.KEY_SURNAME_USERS + ", "
                + DBHelper.KEY_CITY_USERS
                + " FROM "
                + DBHelper.TABLE_CONTACTS_USERS
                + " WHERE "
                + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery,new String[] {userId});

        if(cursor.moveToFirst()){
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_USERS);
            int indexSurname = cursor.getColumnIndex(DBHelper.KEY_SURNAME_USERS);
            int indexCity = cursor . getColumnIndex(DBHelper.KEY_CITY_USERS);
            nameText.setText(cursor.getString(indexName));
            surnameText.setText(cursor.getString(indexSurname));
            cityText.setText(cursor.getString(indexCity));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        long userId = getUserId();
        long serviceId = getIntent().getLongExtra(SERVICE_ID, -1);

        if(isMyService(serviceId,userId)){
            // если это мой сервис
            addNewOrders(userId);
            addNewServices(userId);
            createProfileData(userId);
        }
    }
    //Переписать курсор Валентину, написать комметарии к курсору и что выполняет метод
    private void addNewServices(Long userId) {
        int visibleCount = servicesLayout.getChildCount();

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(
                DBHelper.TABLE_CONTACTS_SERVICES,
                new String[]{DBHelper.KEY_ID, DBHelper.KEY_NAME_SERVICES},
                DBHelper.KEY_USER_ID + " = ? ",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null,
                null);

        if(cursor.getCount() > visibleCount) {
            if(cursor.moveToLast()){
                int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
                int indexNameService = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
                int countOfNewOrders = 0;

                do{
                    //  формирую сообщения, в будущем тут будем формировать объект
                    String foundId = cursor.getString(indexId);
                    String foundNameService = cursor.getString(indexNameService);

                    addServiceToScreen(foundId, foundNameService);

                    countOfNewOrders++;
                }while (cursor.moveToPrevious() && countOfNewOrders<(cursor.getCount() - visibleCount));
            }
        }
        cursor.close();
    }

    private void addNewOrders(Long userId) {
        int visibleCount = ordersLayout.getChildCount();

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String sqlQuery =
                "SELECT "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_ID + ", "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_NAME_SERVICES + ", "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_DATE_WORKING_DAYS + ", "
                        + DBHelper.TABLE_WORKING_TIME + "." + DBHelper.KEY_TIME_WORKING_TIME
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_SERVICES + ", "
                        + DBHelper.TABLE_WORKING_DAYS + ", "
                        + DBHelper.TABLE_WORKING_TIME
                        + " WHERE "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_ID + " = " + DBHelper.KEY_SERVICE_ID_WORKING_DAYS
                        + " AND "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_ID + " = " + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME
                        + " AND "
                        + DBHelper.TABLE_WORKING_TIME + "." + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[] {String.valueOf(userId)});

        if(cursor.getCount() > visibleCount) {
            if(cursor.moveToLast()){
                int indexServiceId = cursor.getColumnIndex(DBHelper.KEY_ID);
                int indexServiceName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
                int indexDate = cursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);
                int indexTime = cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);

                int countOfNewOrders = 0;

                do{
                    String foundId = cursor.getString(indexServiceId);
                    String foundName = cursor.getString(indexServiceName);
                    String foundDate = cursor.getString(indexDate);
                    String foundTime = cursor.getString(indexTime);

                    addOrderToScreen(foundId, foundName, foundDate, foundTime);

                    countOfNewOrders++;
                }while (cursor.moveToPrevious() && countOfNewOrders<(cursor.getCount() - visibleCount));
            }
        }
        cursor.close();
    }

    private void addServiceToScreen(String id, String foundNameService) {
        fServiceElement = new foundServiceProfileElement(id, foundNameService);

        transaction = manager.beginTransaction();
        transaction.add(R.id.servicesProfileLayout, fServiceElement);
        transaction.commit();
    }

    private void addOrderToScreen(String id, String name, String date, String time) {
        fOrderElement = new foundOrderElement(id, name, date, time);

        transaction = manager.beginTransaction();
        transaction.add(R.id.ordersProfileLayout, fOrderElement);
        transaction.commit();
    }
    //Анулировать статус при выходе
    private void annulStatus() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.remove(STATUS);
        editor.apply();
    }

    private void goToLogIn() {
        Intent intent = new Intent(this, authorization.class);
        startActivity(intent);
        finish();
    }
    private void goToAddService() {
        Intent intent = new Intent(this, addService.class);
        startActivity(intent);
    }

    private void goToSearchService() {
        Intent intent = new Intent(this, searchService.class);
        startActivity(intent);
    }

    private void goToMainScreen() {
        Intent intent = new Intent(this, mainScreen.class);
        startActivity(intent);
    }

    private void goToEditProfile() {
        Intent intent = new Intent(this, editProfile.class);
        intent.putExtra(USER_NAME, nameText.getText());
        intent.putExtra(USER_SURNAME, surnameText.getText());
        intent.putExtra(USER_CITY, cityText.getText());
        startActivity(intent);
    }


}