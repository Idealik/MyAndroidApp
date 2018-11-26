package com.example.ideal.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class registration extends AppCompatActivity {

    final String TAG = "DBInf";
    boolean status;
    final String STATUS = "status";
    final String PHONE = "phone";
    final String PASS = "pass";
    final String FILE_NAME = "Info";
    SharedPreferences sPref; //класс для работы с записью в файлы

     EditText phone;
     EditText pass;
     DBHelper dbHelper;

     Button btnReg;
     Button btnRead;
     Button btnDel;
     Button authoriz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        phone = (EditText) findViewById(R.id.phoneRegTB);
        pass = (EditText) findViewById(R.id.passRegTB);

        btnReg = (Button) findViewById(R.id.BTreg);
        btnRead = (Button) findViewById(R.id.BTread);
        btnDel = (Button) findViewById(R.id.BTdel);
        authoriz = (Button) findViewById(R.id.AuthorizationRegistrationBtn);

        final  String s1 = "9999999";
        phone.setText(String.valueOf(s1));

        // беру данные с форм
        dbHelper = new DBHelper(this);

        WorkWithDB();
    }

    private void WorkWithDB(){
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SQLiteDatabase database = dbHelper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                String myphone = phone.getText().toString();
                String mypass = pass.getText().toString();

                switch (v.getId()){
                    case R.id.BTreg:
                        Registration(database,contentValues,myphone,mypass);
                        break;

                    case R.id.BTread:
                        ReadDB(database);
                        break;

                    case R.id.BTdel:
                        DeleteDB(database);
                        break;

                    case R.id.AuthorizationRegistrationBtn:
                        goToAuthoriz();
                    default:
                        break;
                }
            }
        };
        btnReg.setOnClickListener(onClickListener);
        btnRead.setOnClickListener(onClickListener);
        btnDel.setOnClickListener(onClickListener);
        authoriz.setOnClickListener(onClickListener);
    }


    private boolean Registration(SQLiteDatabase database, ContentValues contentValues, String myphone, String mypass){

        contentValues.put(DBHelper.KEY_PHONE, myphone);
        contentValues.put(DBHelper.KEY_PASS, mypass);

        database.insert(DBHelper.TABLE_CONTACTS_USERS,null,contentValues); //попробуй изменить имя таблицы
        savePhoneAndPass(myphone, mypass);
        saveStatus();
        Log.d(TAG, "reg was successfull");


        return  true;
    }

    private void ReadDB(SQLiteDatabase database){
        String msg = "";

        Cursor cursor = database.query(DBHelper.TABLE_CONTACTS_USERS,null,null,null,null,null,null,null);

        if(cursor.moveToFirst()){
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexPhone = cursor.getColumnIndex(DBHelper.KEY_PHONE);
            int indexPass = cursor.getColumnIndex(DBHelper.KEY_PASS);

            do{
                msg += " Index = " + cursor.getString(indexId) + " Number = " + cursor.getString(indexPhone) + " Pass =" + cursor.getString(indexPass) + " ";
                Log.d(TAG, cursor.getString(indexId) + " " + cursor.getString(indexPhone) + " " + cursor.getString(indexPass) + " ");

            }while (cursor.moveToNext());

            Log.d(TAG, "Full msg = " + msg);
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
    }

    private  void DeleteDB(SQLiteDatabase database){
        database.delete(DBHelper.TABLE_CONTACTS_USERS,null,null);
        Log.d(TAG, "DB was deleted");
    }


    private  void  goToAuthoriz(){
        Intent intent = new Intent(registration.this, authorization.class);

        dbHelper.close();

        startActivity(intent);
        finish();
    }

    private boolean getStatus() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        boolean result = sPref.getBoolean(STATUS, false);

        return  result;
    }

    private void saveStatus() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean(STATUS, true);
        editor.apply();
    }

    private void savePhoneAndPass(String phone, String pass) {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(PHONE, phone);
        editor.putString(PASS, pass);
        editor.apply();
    }

    private  void goToProfile(){
        Intent intent = new Intent(this, profile.class);
        startActivity(intent);
        finish();
    }

}






