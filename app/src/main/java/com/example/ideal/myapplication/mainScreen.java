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

    // выводим 100 объявлений,
    final String TAG = "DBInf";
    final String FILE_NAME = "Info";
    final String PHONE = "phone";

    //Вертикальный лэйаут
    LinearLayout resultLayout;

    DBHelper dbHelper;

    SharedPreferences sPref;

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
        String cityUser = getCityUser(database,userId);
        //получаем всех пользователей из этого города ЖЕЛАТЕЛЬНО ТОЛЬКО ТЕХ У КОГО
        // ЕСТЬ ЧТО-ТО ИЗ УСЛУГ НО ДЛЯ ЭТОГО НАДО БЫ НАУЧИТЬСЯ ПИСАТЬ ЗАПРОС

        String [] users =  findAllWorkersInTheUserCity(database,cityUser);
        int count = users.length;
        Log.d(TAG, ""+count);

        Log.d(TAG, cityUser);
       // getData(database);
    }

    private  void getData(SQLiteDatabase database){
        Cursor cursor = database.query(DBHelper.TABLE_CONTACTS_SERVICES,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        if(cursor.moveToFirst()){
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);
            int indexDescription = cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION_SERVICES);

            // есть только имя
            do{
                    //  формирую сообщения, в будущем тут будем формировать объект
                    String foundId = cursor.getString(indexId);
                    String foundName = cursor.getString(indexName);
                    String foundCost = cursor.getString(indexMinCost);
                    String foundDescription = cursor.getString(indexDescription);

                    addToScreen(foundId, foundName, foundCost, foundDescription);
            }while (cursor.moveToNext());

            Log.d(TAG, "Full msg = ");
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



    private  String getUserId(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        String userId = sPref.getString(PHONE, "-");

        return userId;
    }

    /*private String getCityUser(SQLiteDatabase database,String userId){ тут в цикле курсор

        Cursor cursor = database.query(DBHelper.TABLE_CONTACTS_USERS,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        if(cursor.moveToFirst()){
            int indexIdUser= cursor.getColumnIndex(DBHelper.KEY_USER_ID);
            int indexCity = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);
            // есть только имя
            do{
                //  совпадает айди? наш юзер
                if(userId.equals(cursor.getString(indexIdUser))){
                    return cursor.getString(indexCity);
                }
            }while (cursor.moveToNext());
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
        return "";
    }*/

    private String getCityUser(SQLiteDatabase database,String userId){

        String query = "SELECT city FROM users WHERE _id = 123 ";

        Cursor cursor = database.rawQuery(query,null);

        Log.d(TAG,cursor.toString());

        int indexCity= cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);
        String city =  cursor.getString(indexCity);

        Log.d(TAG,city);

        cursor.close();
        return city;
    }

     private  String [] findAllWorkersInTheUserCity(SQLiteDatabase database,String cityUser){
        // тут в массив стринг добавляем юузеров
         String [] users = new String[100];
         int index=0;

         Cursor cursor = database.query(DBHelper.TABLE_CONTACTS_USERS,
                 null,
                 null,
                 null,
                 null,
                 null,
                 null,
                 null);

         if(cursor.moveToFirst()){
             int indexIdUser= cursor.getColumnIndex(DBHelper.KEY_USER_ID);
             int indexCity = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);
             // есть только имя
             do{
                 //  если город совпадает это тот юзер, что на нужен
                 if(cityUser.equals(cursor.getString(indexCity))){
                     users[index] = cursor.getString(indexIdUser);
                     index++;
                 }
             }while (cursor.moveToNext());

             return users;
         }
         else {
             Log.d(TAG, "DB is empty");
         }
         cursor.close();
         return null;

     }



}
