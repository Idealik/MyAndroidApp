package com.example.ideal.myapplication;

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

    // кнопки
    Button findBtn;

    //editTEXT
    EditText nameInput;

    //Вертикальный лэйаут
    LinearLayout resultLayout;

    //бд
    DBHelper dbHelper;

    private foundElement fElement;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_service);

        findBtn = findViewById(R.id.findServiceSearchServiceBtn);

        nameInput = findViewById(R.id.nameSearchServiceInput);

        resultLayout = findViewById(R.id.resultsLayout);

        dbHelper = new DBHelper(this);
        manager = getSupportFragmentManager();

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
        transaction.add(R.id.resultsLayout, fElement);
        transaction.commit();
    }

}