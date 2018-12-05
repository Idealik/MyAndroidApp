package com.example.ideal.myapplication;

import android.content.ContentValues;
import android.content.SharedPreferences;
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
    final String FILE_NAME = "Info";
    final String PHONE = "phone";

    Button addServicesBtn;
    Button readBtn;

    EditText nameServiceInput;
    EditText costAddServiceTB;
    EditText descriptonServiceInput;
    
    DBHelper dbHelper;

    SharedPreferences sPref;


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

        switch (v.getId()){
            case R.id.addServiceAddServiceBtn:
                addService(database);
                break;
            case R.id.readAddServiceBtn:
                readBtn(database);
            default:
                break;
        }
    }

    private boolean addService(SQLiteDatabase database){
        String name = nameServiceInput.getText().toString();
        String cost = costAddServiceTB.getText().toString();
        String description = descriptonServiceInput.getText().toString();
        String userId = getUserId();

        ContentValues contentValues = new ContentValues();
        //добавление в сервис данных
        contentValues.put(DBHelper.KEY_NAME_SERVICES, name);
        contentValues.put(DBHelper.KEY_MIN_COST_SERVICES, cost);
        contentValues.put(DBHelper.KEY_DESCRIPTION_SERVICES, description);
        // добавление id пользователя в таблицу сервисов, чтобы потом использовать в mainScreen
        contentValues.put(DBHelper.KEY_USER_ID, userId);

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
            int indexIdUser= cursor.getColumnIndex(DBHelper.KEY_USER_ID);

            do{
                msg += " Index = " + cursor.getString(indexId)
                        + " Name = " + cursor.getString(indexName)
                        + " Cost = " + cursor.getString(indexMinCost)
                        + " Descr = " + cursor.getString(indexDescription)
                        + " Id user = " + cursor.getString(indexIdUser)
                        + " ";
                Log.d(TAG, cursor.getString(indexId)
                        + " "
                        + cursor.getString(indexName)
                        + " "
                        + cursor.getString(indexMinCost)
                        + " "
                        + cursor.getString(indexDescription)
                        + " "
                        + cursor.getString(indexIdUser)
                        + " ");
            } while (cursor.moveToNext());

            Log.d(TAG, "Full msg = " + msg);
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
    }

    private  void deleteDB(SQLiteDatabase database){
        database.delete(DBHelper.TABLE_CONTACTS_SERVICES,null,null);
        Log.d(TAG, "DB was deleted");
    }

    private  String getUserId(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        String userId = sPref.getString(PHONE, "-");

        return userId;
    }


}

