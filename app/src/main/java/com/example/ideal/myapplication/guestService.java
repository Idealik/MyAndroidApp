package com.example.ideal.myapplication;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ResourceBundle;

import static android.os.Build.ID;

public class guestService extends AppCompatActivity {

    final String TAG = "DBInf";
    final String SERVICE_ID = "service id";

    TextView nameText;
    TextView costText;
    TextView descriptionText;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_service);

        nameText = findViewById(R.id.nameGuestServiceText);
        costText = findViewById(R.id.costGuestServiceText);
        descriptionText = findViewById(R.id.descriptionGuestServiceText);

        dbHelper = new DBHelper(this);
        String serviceId =getIntent().getStringExtra(SERVICE_ID);

        getData(serviceId);
    }

    private void getData(String serviceId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        // получаем сервис с указанным ID
        String sqlQuery =
                "SELECT " + DBHelper.TABLE_CONTACTS_SERVICES + ".*"
                        + " FROM " + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE " + DBHelper.KEY_ID +" = ? ";
        Cursor cursor = database.rawQuery(sqlQuery, new String[] {serviceId});

        if(cursor.moveToFirst()) {
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);
            int indexDescription = cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION_SERVICES);

            nameText.setText(cursor.getString(indexName));
            costText.setText(cursor.getString(indexMinCost));
            descriptionText.setText(cursor.getString(indexDescription));
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
    }
}