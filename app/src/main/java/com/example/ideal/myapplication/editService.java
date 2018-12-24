package com.example.ideal.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class editService extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = "DBInf";
    private final String SERVICE_ID = "service id";
    private final String STATUS_USER_BY_SERVICE = "status user";

    long serviceId;

    Button editServicesBtn;

    EditText nameServiceInput;
    EditText costServiceInput;
    EditText descriptonServiceInput;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_service);
        Log.d(TAG, "Hi EDITSERVICE");

        serviceId = getIntent().getLongExtra(SERVICE_ID, -1);

        editServicesBtn = (Button) findViewById(R.id.editServiceEditServiceBtn);

        nameServiceInput = (EditText) findViewById(R.id.nameEditServiceInput);
        costServiceInput = (EditText) findViewById(R.id.costEditServiceInput);
        descriptonServiceInput = (EditText) findViewById(R.id.descriptionEditServiceInput);

        dbHelper = new DBHelper(this);

        editServicesBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.editServiceEditServiceBtn:
                editServiceInDataBase();
                goToMyCalendar();
                break;

                default:
                    break;
        }
    }

    private void editServiceInDataBase() {

        String name = nameServiceInput.getText().toString();
        String cost = costServiceInput.getText().toString();
        String description = descriptonServiceInput.getText().toString();

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        if(name.length()!=0) contentValues.put(DBHelper.KEY_NAME_SERVICES, name);
        if(cost.length()!=0) contentValues.put(DBHelper.KEY_MIN_COST_SERVICES, cost);
        if(description.length()!=0) contentValues.put(DBHelper.KEY_DESCRIPTION_SERVICES, description);

        database.update(DBHelper.TABLE_CONTACTS_SERVICES, contentValues,
                DBHelper.KEY_ID + " = ?",
                new String []{String.valueOf(serviceId)});
    }

    private void goToMyCalendar() {
        String statusUser = getIntent().getStringExtra(STATUS_USER_BY_SERVICE);

        Log.d(TAG, serviceId + " ");
        Log.d(TAG, statusUser + " ");

        Intent intent = new Intent(this, myCalendar.class);
        intent.putExtra(SERVICE_ID, serviceId);
        intent.putExtra(STATUS_USER_BY_SERVICE, statusUser);

        startActivity(intent);
    }
}
