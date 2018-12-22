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

import com.example.ideal.myapplication.fragments.foundElement;

public class searchService extends FragmentActivity implements View.OnClickListener {
    // сначала идут константы
    final String TAG = "DBInf";
    final String FILE_NAME = "Info";
    final String PHONE = "phone";


    // кнопки
    Button findBtn;

    //editTEXT
    EditText nameInput;

    //Вертикальный лэйаут
    LinearLayout resultLayout;

    //бд
    DBHelper dbHelper;

    SharedPreferences sPref;


    private foundElement fElement;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_service);

        findBtn = findViewById(R.id.findServiceSearchServiceBtn);

        nameInput = findViewById(R.id.nameSearchServiceInput);

        resultLayout = findViewById(R.id.resultSearchServiceLayout);

        dbHelper = new DBHelper(this);
        manager = getSupportFragmentManager();

        createMainScreen();


        findBtn.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        switch (v.getId()){
            case R.id.findServiceSearchServiceBtn:
                search(database);
                break;
            default:
                break;
        }
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

    // после нажатия на кнопку
    private  void search(SQLiteDatabase database){
        String name = nameInput.getText().toString().toLowerCase();
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

            do{
                // проверяем только по имени причем в нижнем регистре
                boolean isName  = name.equals(cursor.getString(indexName).toLowerCase());
                if(isName){

                    String foundId = cursor.getString(indexId);
                    String foundName = cursor.getString(indexName);
                    String foundCost = cursor.getString(indexMinCost);
                    String foundDescription = cursor.getString(indexDescription);
                    //  формируем объект layout c некоторыми элементами
                    addToScreen(foundId, foundName, foundCost, foundDescription);
                }
            }while (cursor.moveToNext());
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
        transaction.add(R.id.resultSearchServiceLayout, fElement);
        transaction.commit();
    }

}