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

    private final String TAG = "DBInf";
    private final String PHONE = "phone";
    final String FILE_NAME = "Info";
    final String STATUS = "status";

    Button logOutBtn;
    Button findServicesBtn;
    Button addServicesBtn;
    Button mainScreenBtn;

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

        servicesOrOrdersSwitch = findViewById(R.id.servicesOrOrdersProfileSwitch);

        servicesScroll = findViewById(R.id.servicesProfileScroll);
        ordersScroll = findViewById(R.id.orderProfileScroll);
        servicesLayout = findViewById(R.id.servicesProfileLayout);
        ordersLayout = findViewById(R.id.ordersProfileLayout);

        nameText = findViewById(R.id.nameProfileText);
        surnameText= findViewById(R.id.surnameProfileText);
        cityText = findViewById(R.id.cityProfileText);

        logOutBtn.setOnClickListener(this);
        findServicesBtn.setOnClickListener(this);
        addServicesBtn.setOnClickListener(this);
        mainScreenBtn.setOnClickListener(this);

        dbHelper = new DBHelper(this);

        manager = getSupportFragmentManager();

        createServicesList();
        createOrdersList();
        createProfileData();

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
            default:
                break;
        }
    }

    private void createOrdersList(){
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //получаем id пользователя
        long userId = getUserId();

        //получаем все сервисы, которые пренадлежат юзеру
        getMyOrders(database, userId);
    }

    private void createServicesList(){
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //получаем id пользователя
        long userId = getUserId();

        //получаем все сервисы, которые пренадлежат юзеру
        getMyServices(database, userId);
    }



    //получить id-phone пользователя
    private  long getUserId(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        long userId = Long.valueOf(sPref.getString(PHONE, "0"));

        return userId;
    }

    private void createProfileData(){

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String userId = String.valueOf(getUserId());
        String sqlQuert =
                "SELECT "
                + DBHelper.KEY_NAME_USERS + ", "
                + DBHelper.KEY_SURNAME_USERS + ", "
                + DBHelper.KEY_CITY_USERS
                + " FROM "
                + DBHelper.TABLE_CONTACTS_USERS
                + " WHERE "
                + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuert,new String[] {userId});

        if(cursor.moveToFirst()){
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_USERS);
            int indexSurname = cursor.getColumnIndex(DBHelper.KEY_SURNAME_USERS);
            int indexCity = cursor . getColumnIndex(DBHelper.KEY_CITY_USERS);

            Log.d(TAG, " ");
            nameText.setText(cursor.getString(indexName));
            surnameText.setText(cursor.getString(indexSurname));
            cityText.setText(cursor.getString(indexCity));
        }
    }

    private void getMyOrders(SQLiteDatabase database, long userId) {

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
        else {
            Log.d(TAG, "Cursor is empty");
        }
        cursor.close();
    }

    private void getMyServices(SQLiteDatabase database, long userId) {

        // нужно вернуть, имя, фамилию, город, название услуги, цену, оценку (пока без оценки)
        // используем 2 таблицы - юзеры и сервисы
        // связываем их по номеру телефона юзеров, уточняем юзера по его id
        String sqlQuery =
                "SELECT " +  DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_NAME_SERVICES
                        + ", " + DBHelper.KEY_ID
                        + " FROM " + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE " + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_USER_ID + " = ?";

        Log.d(TAG, "query " + sqlQuery);
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
        else {
            Log.d(TAG, "CURSOR IS EMPTY");
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

    @Override
    protected void onResume() {
        super.onResume();

        addNewOrders();
        addNewServices();
    }

    private void addNewServices() {
        int visibleCount = servicesLayout.getChildCount();
        long userId = Long.valueOf(sPref.getString(PHONE, "0"));

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

    private void addNewOrders() {
        int visibleCount = ordersLayout.getChildCount();
        long userId = Long.valueOf(sPref.getString(PHONE, "0"));

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

    //Анулировать статус
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


}