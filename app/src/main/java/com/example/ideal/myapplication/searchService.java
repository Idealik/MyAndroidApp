package com.example.ideal.myapplication;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.ideal.myapplication.fragments.foundServiceElement;

public class searchService extends FragmentActivity implements View.OnClickListener {
    // сначала идут константы
    final String TAG = "DBInf";
    final String FILE_NAME = "Info";
    final String PHONE = "phone";


    // кнопки
    Button findBtn;

    //editTEXT
    EditText searchLineSearchServiceInput;

    //Вертикальный лэйаут
    LinearLayout resultLayout;

    //бд
    DBHelper dbHelper;

    SharedPreferences sPref;

    private foundServiceElement fElement;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_service);

        findBtn = findViewById(R.id.findServiceSearchServiceBtn);

        searchLineSearchServiceInput = findViewById(R.id.searchLineSearchServiceInput);

        resultLayout = findViewById(R.id.resultSearchServiceLayout);

        dbHelper = new DBHelper(this);
        manager = getSupportFragmentManager();

        createMainScreen();

        findBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.findServiceSearchServiceBtn:
                search();
                break;
            default:
                break;
        }
    }

    private void createMainScreen() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //получаем id пользователя
        String userId = getUserId();
        //получаем город юзера
        String userCity = getUserCity(database, userId);

        //получаем все сервисы, которые находятся в городе юзера
        getServicesInThisCity(database, userCity);
    }

    private String getUserId() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String userId = sPref.getString(PHONE, "-");

        return userId;
    }

    private String getUserCity(SQLiteDatabase database, String userId) {
        Cursor cursor = database.query(DBHelper.TABLE_CONTACTS_USERS,
                new String[]{DBHelper.KEY_CITY_USERS},
                DBHelper.KEY_USER_ID + " = ?",
                new String[]{userId},
                null, null, null);

        int indexCity = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);

        String city = "dubna"; // дефолтное значение
        if (cursor.moveToFirst()) {
            city = cursor.getString(indexCity);
        }
        cursor.close();
        return city;
    }

    private void getServicesInThisCity(SQLiteDatabase database, String userCity) {
        int limitOfService = 10; //максимальное количество выводимых предложений
        //запрос в бд сравнивает номера в табилце юзер и сервис, а также учитывает город
        String sqlQuery =
                "SELECT " + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_NAME_USERS + ", "
                        + DBHelper.KEY_SURNAME_USERS + ", " + DBHelper.KEY_CITY_USERS
                        + ", " + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_NAME_SERVICES
                        + ", " + DBHelper.KEY_MIN_COST_SERVICES + ", " + DBHelper.KEY_ID
                        + " FROM " + DBHelper.TABLE_CONTACTS_SERVICES + ", " + DBHelper.TABLE_CONTACTS_USERS
                        + " WHERE " + "LOWER(" + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_CITY_USERS + ") = ?"
                        + " AND "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_USER_ID +
                        " = "
                        + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_USER_ID;
        ;
        Cursor cursor = database.rawQuery(sqlQuery, new String[]{userCity.toLowerCase()});

        if (cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexNameUser = cursor.getColumnIndex(DBHelper.KEY_NAME_USERS);
            int indexSurname = cursor.getColumnIndex(DBHelper.KEY_SURNAME_USERS);
            int indexCity = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);

            int indexNameService = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);
            int countOfFoundServices = 0;
            do {
                //  формирую сообщения, в будущем тут будем формировать объект
                String foundId = cursor.getString(indexId);
                String foundNameUser = cursor.getString(indexNameUser);
                String foundSurname = cursor.getString(indexSurname);
                String foundCity = cursor.getString(indexCity);
                String foundNameService = cursor.getString(indexNameService);
                String foundCost = cursor.getString(indexMinCost);

                addToScreen(foundId, foundNameUser, foundSurname, foundCity, foundNameService, foundCost);

                countOfFoundServices++;
            } while (cursor.moveToNext() && countOfFoundServices < limitOfService);
        } else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
    }

    // после нажатия на кнопку
    private void search() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        String searchingLine = searchLineSearchServiceInput.getText().toString();
        Log.d(TAG, "search: ");
        // нужно вернуть, имя, фамилию, город, название услуги, цену, оценку (пока без оценки)
        // используем 2 таблицы - юзеры и сервисы
        // связываем их по номеру телефона юзеров
        //написать курсор, который возвращает с заданными параметрами
        String sqlQuery =
                "SELECT " + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_NAME_USERS + ", "
                        + DBHelper.KEY_SURNAME_USERS + ", " + DBHelper.KEY_CITY_USERS
                        + ", " + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_NAME_SERVICES
                        + ", " + DBHelper.KEY_MIN_COST_SERVICES + ", " + DBHelper.KEY_ID
                        + " FROM " + DBHelper.TABLE_CONTACTS_SERVICES + ", " + DBHelper.TABLE_CONTACTS_USERS
                        + " WHERE ( "
                        + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_NAME_USERS + " = ? "
                        + " OR " + DBHelper.KEY_SURNAME_USERS + " = ? "
                        + " OR " + DBHelper.KEY_CITY_USERS + " = ? "
                        + " OR " + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_NAME_SERVICES + " = ? "
                        + " OR " + DBHelper.KEY_MIN_COST_SERVICES + " = ? "
                        + ") AND "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_USER_ID +
                        " = "
                        + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_USER_ID;
        ;
        Log.d(TAG, "query" + sqlQuery);
        Cursor cursor = database.rawQuery(sqlQuery, searchingLine.split(" "));

        if (cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexNameUser = cursor.getColumnIndex(DBHelper.KEY_NAME_USERS);
            int indexSurname = cursor.getColumnIndex(DBHelper.KEY_SURNAME_USERS);
            int indexCity = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);

            int indexNameService = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);

            // проверяем только по имени причем в нижнем регистре
            do {
                String foundId = cursor.getString(indexId);
                String foundNameUser = cursor.getString(indexNameUser);
                String foundSurname = cursor.getString(indexSurname);
                String foundCity = cursor.getString(indexCity);
                String foundNameService = cursor.getString(indexNameService);
                String foundCost = cursor.getString(indexMinCost);
                Log.d(TAG, "in if");

                //  формируем объект layout c некоторыми элементами
                addToScreen(foundId, foundNameUser, foundSurname, foundCity, foundNameService, foundCost);
            }while (cursor.moveToNext());
        }
        cursor.close();
    }



    private void addToScreen(String id, String foundNameUser, String foundSurname, String foundCity,
                             String foundNameService, String foundCost ) {
        resultLayout.removeAllViews();

        fElement = new foundServiceElement(id, foundNameUser, foundSurname, foundCity, foundNameService, foundCost);

        transaction = manager.beginTransaction();
        transaction.add(R.id.resultSearchServiceLayout, fElement);
        transaction.commit();
    }

}