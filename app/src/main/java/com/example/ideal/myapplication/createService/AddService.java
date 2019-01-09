package com.example.ideal.myapplication.createService;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ideal.myapplication.other.DBHelper;
import com.example.ideal.myapplication.R;

public class AddService extends AppCompatActivity implements View.OnClickListener {

    final String FILE_NAME = "Info";
    final String PHONE = "phone";
    final String SERVICE_ID = "service id";
    final String STATUS_USER_BY_SERVICE = "status User";

    Button addServicesBtn;

    EditText nameServiceInput;
    EditText costAddServiceInput;
    EditText descriptonServiceInput;
    
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_service);

        addServicesBtn = findViewById(R.id.addServiceAddServiceBtn);

        nameServiceInput = findViewById(R.id.nameAddServiceInput);
        costAddServiceInput = findViewById(R.id.costAddServiceInput);
        descriptonServiceInput = findViewById(R.id.descriptionAddServiceInput);
        
        dbHelper = new DBHelper(this);

        addServicesBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        switch (v.getId()){
            case R.id.addServiceAddServiceBtn:
                addService(database);
                break;

            default:
                break;
        }
    }

    private boolean addService(SQLiteDatabase database){
        String name = nameServiceInput.getText().toString();
        String cost = costAddServiceInput.getText().toString();
        String description = descriptonServiceInput.getText().toString();

        //Проверка на заполненность полей
        if(isFullInputs(name,cost,description)){
            String userId = getUserId();

            //добавление в БД
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.KEY_NAME_SERVICES, name.toLowerCase());
            contentValues.put(DBHelper.KEY_MIN_COST_SERVICES, cost);
            contentValues.put(DBHelper.KEY_DESCRIPTION_SERVICES, description.toLowerCase());
            contentValues.put(DBHelper.KEY_USER_ID, userId);

            long serviceId = database.insert(DBHelper.TABLE_CONTACTS_SERVICES,null,contentValues);
            goToMyCalendar(getString(R.string.status_worker),serviceId);

            return  true;
        }
        else {
            Toast.makeText(this, getString(R.string.empty_field), Toast.LENGTH_SHORT).show();
            return  false;
        }
    }

    protected Boolean isFullInputs(String name, String cost, String description){
        if(name.trim().equals("")) return false;
        if(cost.trim().equals("")) return false;
        if(description.trim().equals("")) return false;

        return  true;
    }


    private String getUserId(){
        SharedPreferences sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        String userId = sPref.getString(PHONE, getString(R.string.defult_value));

        return userId;
    }

    private void goToMyCalendar(String status, Long serviceId) {
        Intent intent = new Intent(this, MyCalendar.class);
        intent.putExtra(SERVICE_ID, serviceId);
        intent.putExtra(STATUS_USER_BY_SERVICE, status);

        startActivity(intent);
        finish();
    }
}

