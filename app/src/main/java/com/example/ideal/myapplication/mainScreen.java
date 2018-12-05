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

import com.example.ideal.myapplication.fragments.foundElement;

public class mainScreen extends AppCompatActivity {

    // перемешивать значения
    // выводим 5 объявлений,
    //  1 2 3 4 5
    //  актуалность - 0.6 рейтинг - 0.2 цена - 0.2
    // цена * 0,2 -> min актуальность*0,6 -> min рейтинг*0,2 -> max
    //   11*0,6
    // z - какое-то число
    // 100 = 32 24 10 14 11
    // рандом
    final String TAG = "DBInf";
    final String FILE_NAME = "Info";
    final String PHONE = "phone";

    DBHelper dbHelper;

    SharedPreferences sPref;

    LinearLayout resultLayout;

    private foundElement fElement;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        resultLayout = findViewById(R.id.resultsLayout);

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
        "SELECT " + DBHelper.TABLE_CONTACTS_SERVICES + ".*"
               + " FROM " + DBHelper.TABLE_CONTACTS_USERS + " , " + DBHelper.TABLE_CONTACTS_SERVICES
               + " WHERE " + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_USER_ID + " = "
               + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_USER_ID
               + " AND LOWER(" + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_CITY_USERS + ") = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[] {userCity.toLowerCase()});
        if(cursor.moveToFirst()){
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);
            int indexDescription = cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION_SERVICES);

            int countOfFoundServices = 0;
            do{
                //  формирую сообщения, в будущем тут будем формировать объект
                String foundId = cursor.getString(indexId);
                String foundName = cursor.getString(indexName);
                String foundCost = cursor.getString(indexMinCost);
                String foundDescription = cursor.getString(indexDescription);

                addToScreen(foundId, foundName, foundCost, foundDescription);
                countOfFoundServices++;
            }while (cursor.moveToNext() && countOfFoundServices < limitOfService);
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
    }

    private void addToScreen(String id, String name, String cost, String description) {

        resultLayout.removeAllViews();

        fElement = new foundElement(id, name, cost, description);

        transaction = manager.beginTransaction();
        transaction.add(R.id.resultsLayout, fElement);
        transaction.commit();
    }
}