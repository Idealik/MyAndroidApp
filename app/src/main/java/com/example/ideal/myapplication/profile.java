package com.example.ideal.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.ideal.myapplication.fragments.foundElement;

public class profile extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "DBInf";
    private final String PHONE = "phone";
    final String FILE_NAME = "Info";
    final String STATUS = "status";

    Button logOutBtn;
    Button findServicesBtn;
    Button addServicesBtn;
    Button mainScreenBtn;

    LinearLayout resultLayout;

    SharedPreferences sPref;
    DBHelper dbHelper;

    private foundElement fElement;
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

        resultLayout = findViewById(R.id.resultProfileLayout);

        logOutBtn.setOnClickListener(this);
        findServicesBtn.setOnClickListener(this);
        addServicesBtn.setOnClickListener(this);
        mainScreenBtn.setOnClickListener(this);

        dbHelper = new DBHelper(this);

        manager = getSupportFragmentManager();

        createMainScreen();
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

    private void createMainScreen(){
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //получаем id пользователя
        long userId = getUserId();

        //получаем все сервисы, которые пренадлежат юзеру
        getMyServicesInThisCity(database, userId);
    }

    //получить id-phone пользователя
    private  long getUserId(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        long userId = Long.valueOf(sPref.getString(PHONE, "0"));

        return userId;
    }

    private void getMyServicesInThisCity(SQLiteDatabase database, long userId) {
        // создаем это в 3х местах может запариться и написать олтдельный класс?

        // нужно вернуть, имя, фамилию, город, название услуги, цену, оценку (пока без оценки)
        // используем 2 таблицы - юзеры и сервисы
        // связываем их по номеру телефона юзеров

        String sqlQuery =
                "SELECT " + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_NAME_USERS + ", "
                        + DBHelper.KEY_SURNAME_USERS + ", " + DBHelper.KEY_CITY_USERS
                        + ", " +  DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_NAME_SERVICES
                        + ", " + DBHelper.KEY_MIN_COST_SERVICES + ", " + DBHelper.KEY_ID
                        + " FROM " + DBHelper.TABLE_CONTACTS_SERVICES + ", " + DBHelper.TABLE_CONTACTS_USERS
                        + " WHERE " + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_USER_ID + " = ?"
                        + " AND "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_USER_ID +
                        " = "
                        + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_USER_ID;

        Log.d(TAG, "query " + sqlQuery);
        Cursor cursor = database.rawQuery(sqlQuery, new String[] {String.valueOf(userId)});

        if(cursor.moveToFirst()){
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexNameUser = cursor.getColumnIndex(DBHelper.KEY_NAME_USERS);
            int indexSurname = cursor.getColumnIndex(DBHelper.KEY_SURNAME_USERS);
            int indexCity = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);

            int indexNameService = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);

            do{
                //  формирую сообщения, в будущем тут будем формировать объект
                String foundId = cursor.getString(indexId);
                String foundNameUser= cursor.getString(indexNameUser);
                Log.d(TAG, "FOUND NAME" + foundNameUser);
                String foundSurname = cursor.getString(indexSurname);
                String foundCity = cursor.getString(indexCity);
                String foundNameService = cursor.getString(indexNameService);
                String foundCost = cursor.getString(indexMinCost);


                addToScreen(foundId, foundNameUser, foundSurname, foundCity, foundNameService, foundCost);
            }while (cursor.moveToNext());
        }
        else {
            Log.d(TAG, "CURSOR IS EMPTY");
        }
        cursor.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        int visibleCount = resultLayout.getChildCount();
        long userId = Long.valueOf(sPref.getString(PHONE, "0"));

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String sqlQuery =
                "SELECT " + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_NAME_USERS + ", "
                        + DBHelper.KEY_SURNAME_USERS + ", " + DBHelper.KEY_CITY_USERS
                        + ", " + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_NAME_SERVICES
                        + ", " + DBHelper.KEY_MIN_COST_SERVICES + ", " + DBHelper.KEY_ID
                        + " FROM " + DBHelper.TABLE_CONTACTS_SERVICES + ", " + DBHelper.TABLE_CONTACTS_USERS
                        + " WHERE " + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_USER_ID + " = ?"
                        + " AND "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_USER_ID +
                        " = "
                        + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_USER_ID;

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{String.valueOf(userId)});

        if (cursor.getCount() > visibleCount) {
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexNameUser = cursor.getColumnIndex(DBHelper.KEY_NAME_USERS);
            int indexSurname = cursor.getColumnIndex(DBHelper.KEY_SURNAME_USERS);
            int indexCity = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);

            int indexNameService = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);

            do {
                //  формирую сообщения, в будущем тут будем формировать объект
                String foundId = cursor.getString(indexId);
                String foundNameUser = cursor.getString(indexNameUser);
                String foundSurname = cursor.getString(indexSurname);
                String foundCity = cursor.getString(indexCity);
                String foundNameService = cursor.getString(indexNameService);
                String foundCost = cursor.getString(indexMinCost);
                Log.d(TAG , "NAME USER" + foundNameUser);

                addToScreen(foundId, foundNameUser, foundSurname, foundCity, foundNameService, foundCost);

             }while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void addToScreen(String id, String foundNameUser, String foundSurname, String foundCity,
                             String foundNameService, String foundCost ) {

        Log.d(TAG , "NAME USER" + foundNameUser);
        fElement = new foundElement(id, foundNameUser, foundSurname, foundCity, foundNameService, foundCost);

        transaction = manager.beginTransaction();
        transaction.add(R.id.resultProfileLayout, fElement);
        transaction.commit();
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
        /*Intent intent = new Intent(this, myCalendar.class);
        startActivity(intent);*/
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