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
        getMyServices(database, userId);
    }



    //получить id-phone пользователя
    private  long getUserId(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        long userId = Long.valueOf(sPref.getString(PHONE, "0"));

        return userId;
    }

    private void getMyServices(SQLiteDatabase database, long userId) {

        Cursor cursor = database.query(
                DBHelper.TABLE_CONTACTS_SERVICES,
                new String[]{DBHelper.KEY_ID, DBHelper.KEY_NAME_SERVICES, DBHelper.KEY_MIN_COST_SERVICES, DBHelper.KEY_DESCRIPTION_SERVICES},
                DBHelper.KEY_USER_ID + " = ? ",
                new String[]{String.valueOf(userId)},
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
                //  формирую сообщения, в будущем тут будем формировать объект
                String foundId = cursor.getString(indexId);
                String foundName = cursor.getString(indexName);
                String foundCost = cursor.getString(indexMinCost);
                String foundDescription = cursor.getString(indexDescription);

                addToScreen(foundId, foundName, foundCost, foundDescription);
            }while (cursor.moveToNext());
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
    }

    private void addToScreen(String id, String name, String cost, String description) {
        fElement = new foundElement(id, name, cost, description);

        transaction = manager.beginTransaction();
        transaction.add(R.id.resultProfileLayout, fElement);
        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        int visibleCount = resultLayout.getChildCount();
        long userId = Long.valueOf(sPref.getString(PHONE, "0"));

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(
                DBHelper.TABLE_CONTACTS_SERVICES,
                new String[]{DBHelper.KEY_ID, DBHelper.KEY_NAME_SERVICES, DBHelper.KEY_MIN_COST_SERVICES, DBHelper.KEY_DESCRIPTION_SERVICES},
                DBHelper.KEY_USER_ID + " = ? ",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null,
                null);

        if(cursor.getCount() > visibleCount) {
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);
            int indexDescription = cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION_SERVICES);

            if(cursor.moveToLast()) {
                String foundId = cursor.getString(indexId);
                String foundName = cursor.getString(indexName);
                String foundCost = cursor.getString(indexMinCost);
                String foundDescription = cursor.getString(indexDescription);

                addToScreen(foundId, foundName, foundCost, foundDescription);
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