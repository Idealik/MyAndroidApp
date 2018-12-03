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

    final String FILE_NAME = "Info";
    final String TAG = "DBInf";
    final String ID = "id";

    TextView nameText;
    TextView costText;
    TextView descriptionText;

    DBHelper dbHelper;
    SharedPreferences sPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_service);

        nameText = findViewById(R.id.nameGuestServiceText);
        costText = findViewById(R.id.costGuestServiceText);
        descriptionText = findViewById(R.id.descriptionGuestServiceText);

        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);

        dbHelper = new DBHelper(this);
        String id = sPref.getString(ID, "-");
        getData(id);
    }

    private void getData(String id) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query(DBHelper.TABLE_CONTACTS_SERVICES,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        if(cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);
            int indexDescription = cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION_SERVICES);

            // есть только имя
            do {
                boolean isId = id.equals(cursor.getString(indexId));
                if (isId) {
                    //  формирую сообщения, в будущем тут будем формировать объект
                    nameText.setText(cursor.getString(indexName));
                    costText.setText(cursor.getString(indexMinCost));
                    descriptionText.setText(cursor.getString(indexDescription));

                    cursor.close();
                    return;
                }
            } while (cursor.moveToNext());
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
    }
}
