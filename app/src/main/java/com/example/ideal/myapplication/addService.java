package com.example.ideal.myapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class addService extends AppCompatActivity implements View.OnClickListener {

    final String TAG = "DBInf";

    Button addServicesBtn;
    Button readBtn;

    EditText nameServiceInput;
    EditText costAddServiceTB;
    EditText descriptonServiceInput;
    
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_service);
        
        addServicesBtn = (Button) findViewById(R.id.addServiceAddServiceBtn);
        readBtn = (Button) findViewById(R.id.readAddServiceBtn);
        
        nameServiceInput = (EditText) findViewById(R.id.nameAddServiceInput);
        costAddServiceTB = (EditText) findViewById(R.id.costAddServiceInput);
        descriptonServiceInput = (EditText) findViewById(R.id.descriptonAddServiceInput);
        
        dbHelper = new DBHelper(this);

        addServicesBtn.setOnClickListener(this);
        readBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String name = nameServiceInput.getText().toString();
        String cost = costAddServiceTB.getText().toString();
        String description = descriptonServiceInput.getText().toString();

        switch (v.getId()){
            case R.id.addServiceAddServiceBtn:
                addService(database,name,cost,description);
                break;
            case R.id.readAddServiceBtn:
                readBtn(database);
            default:
                break;
        }
    }

    private boolean addService(SQLiteDatabase database, String name,String cost,String description){
        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.KEY_NAME_SERVICES, name);
        contentValues.put(DBHelper.KEY_MIN_COST_SERVICES, cost);
        contentValues.put(DBHelper.KEY_DESCRIPTION_SERVICES, description);

        database.insert(DBHelper.TABLE_CONTACTS_SERVICES,null,contentValues);
        
        Log.d(TAG, "reg was successfull");

        return  true;
    }

    private  void readBtn(SQLiteDatabase database){
        String msg= "";
        Cursor cursor = database.query(
                DBHelper.TABLE_CONTACTS_SERVICES,
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
                msg += " Index = " + cursor.getString(indexId)
                        + " Name = " + cursor.getString(indexName)
                        + " Cost = " + cursor.getString(indexMinCost)
                        + " Descr = " + cursor.getString(indexDescription)
                        + " ";
                Log.d(TAG, cursor.getString(indexId)
                        + " "
                        + cursor.getString(indexName)
                        + " "
                        + cursor.getString(indexMinCost)
                        + " "
                        + cursor.getString(indexDescription )
                        + " ");
            } while (cursor.moveToNext());

            Log.d(TAG, "Full msg = " + msg);
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
    }


}

