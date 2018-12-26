package com.example.ideal.myapplication;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.ideal.myapplication.fragments.foundServiceElement;

public class mainScreen extends AppCompatActivity {

    //добавить обнавление layot, чтобы сразу появлилсь новые сервисы!
    final String TAG = "DBInf";
    final String FILE_NAME = "Info";
    final String PHONE = "phone";

    DBHelper dbHelper;

    SharedPreferences sPref;

    LinearLayout resultLayout;

    private foundServiceElement fElement;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        resultLayout = findViewById(R.id.resultsMainScreenLayout);

        manager = getSupportFragmentManager();

        dbHelper = new DBHelper(this);

        createMainScreen();
    }


    private void createMainScreen(){
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //получаем id пользователя
        String userId = getUserId();
        //получаем город юзера
        String userCity = getUserCity(database,userId);

        //получаем все сервисы, которые находятся в городе юзера
        getServicesInThisCity(database, userCity);
    }

    private  String getUserId(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        String userId = sPref.getString(PHONE, "-");

        return userId;
    }

    private String getUserCity(SQLiteDatabase database,String userId){
        Cursor cursor = database.query(DBHelper.TABLE_CONTACTS_USERS,
                new String[] {DBHelper.KEY_CITY_USERS},
                DBHelper.KEY_USER_ID + " = ?",
                new String[] {userId},
                null, null, null);

        int indexCity= cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);

        String city="dubna"; // дефолтное значение
        if(cursor.moveToFirst()) {
            city = cursor.getString(indexCity);
        }
        cursor.close();
        return city;
    }

    private  void getServicesInThisCity(SQLiteDatabase database,String userCity){
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
             Cursor cursor = database.rawQuery(sqlQuery, new String[] {userCity.toLowerCase()});
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
            }while (cursor.moveToNext() && countOfFoundServices < limitOfService);
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
    }

    private void addToScreen(String id, String foundNameUser, String foundSurname, String foundCity,
                             String foundNameService, String foundCost ) {

        fElement = new foundServiceElement(id, foundNameUser, foundSurname, foundCity, foundNameService, foundCost);

        transaction = manager.beginTransaction();
        transaction.add(R.id.resultsMainScreenLayout, fElement);
        transaction.commit();
    }

}