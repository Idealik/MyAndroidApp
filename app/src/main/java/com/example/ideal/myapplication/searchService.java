package com.example.ideal.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class searchService extends AppCompatActivity implements View.OnClickListener {
    // нужно сохранять номер, чтобы потом делать запрос в бд по нему и узнавать местоположение
    // сначала идут константы
    final String TAG = "DBInf";

    //переменные стринг или др

    // кнопки
    Button findBtn;
    //editTEXT
    EditText nameInput;
    EditText answerInput;
    //бд
    DBHelper dbHelper;

    // работа с файлами

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_service);

        findBtn = findViewById(R.id.findServiceSearchServiceBtn);

        nameInput = findViewById(R.id.nameSearchServiceInput);
        answerInput = findViewById(R.id.answerSearchServiceInput);

        dbHelper = new DBHelper(this);

        findBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String name = nameInput.getText().toString();
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        switch (v.getId()){
            case R.id.findServiceSearchServiceBtn:
                search(database, name);
                break;
            default:
                break;
        }
    }


    private  void search(SQLiteDatabase database, String name){
        String msg= "";
        Cursor cursor = database.query(DBHelper.TABLE_CONTACTS_SERVICES,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        if(cursor.moveToFirst()){
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);
            int indexDescription = cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION_SERVICES);

            // есть только имя
            do{
                boolean isName  = name.equals(cursor.getString(indexName));
                if(isName){
                    //  формирую сообщения, в будущем тут будем формировать объект
                    msg +=  " Name = " + cursor.getString(indexName)
                            + " Cost = " + cursor.getString(indexMinCost)
                            + " Descr = " + cursor.getString(indexDescription)
                            + " ";
                }
            }while (cursor.moveToNext());
            answerInput.setText(String.valueOf(msg));
            
            Log.d(TAG, "Full msg = " + msg);
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
    }

}