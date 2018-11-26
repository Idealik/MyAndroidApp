package com.example.ideal.myapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class addService extends AppCompatActivity implements View.OnClickListener {

    final String TAG = "DBInf";

    EditText nameAddServiceTB;
    EditText costAddServiceTB;
    EditText descriptonAddServiceTB;
    DBHelper dbHelper;

    Button addServicesProfileBT;
    Button readDB;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        nameAddServiceTB = (EditText) findViewById(R.id.nameAddServiceTB);
        costAddServiceTB = (EditText) findViewById(R.id.costAddServiceTB);
        descriptonAddServiceTB = (EditText) findViewById(R.id.descriptonAddServiceTB);

        addServicesProfileBT = (Button) findViewById(R.id.addServiceAddServiceBtn);
        readDB = (Button) findViewById(R.id.readAddServiceBtn);
        dbHelper = new DBHelper(this);

        addServicesProfileBT.setOnClickListener(this);
        readDB.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        String name = nameAddServiceTB.getText().toString();
        String cost = costAddServiceTB.getText().toString();
        String description = descriptonAddServiceTB.getText().toString();

        switch (v.getId()){
            case R.id.addServiceAddServiceBtn:
             registration(database,contentValues,name,cost,description);
                break;
            case R.id.readAddServiceBtn:
                readDB(database);
            default:
                break;
        }
    }

    private boolean registration(SQLiteDatabase database, ContentValues contentValues, String name,String cost,String description){

        contentValues.put(DBHelper.KEY_NAME_SERVICES, name);
        contentValues.put(DBHelper.KEY_MIN_COST_SERVICES, cost);
        contentValues.put(DBHelper.KEY_DESCRIPTION_SERVICES, description);

        database.insert(DBHelper.TABLE_CONTACTS_SERVICES,null,contentValues); //попробуй изменить имя таблицы
        Log.d(TAG, "reg was successfull");

        return  true;
    }

    private  void readDB(SQLiteDatabase database){
        String msg= "";
        Cursor cursor = database.query(DBHelper.TABLE_CONTACTS_SERVICES,null,null,null,null,null,null,null);

        if(cursor.moveToFirst()){
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);
            int indexDescription = cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION_SERVICES);

            do{
                msg += " Index = " + cursor.getString(indexId) + " Name = " + cursor.getString(indexName) + " Cost = " + cursor.getString(indexMinCost) +
                        " Descr = " + cursor.getString(indexDescription) + " ";
                Log.d(TAG, cursor.getString(indexId) + " " + cursor.getString(indexName) + " " + cursor.getString(indexMinCost) + " " + cursor.getString(indexDescription )+ " ");

            }while (cursor.moveToNext());

            Log.d(TAG, "Full msg = " + msg);
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
    }


}

