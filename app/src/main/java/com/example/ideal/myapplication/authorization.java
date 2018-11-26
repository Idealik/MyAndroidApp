package com.example.ideal.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class authorization extends AppCompatActivity implements View.OnClickListener {

    final String TAG = "DBInf";
    final String STATUS = "status";
    final String PHONE = "phone";
    final String PASS = "pass";
    final String FILE_NAME = "Info";

    SharedPreferences sPref; //класс для работы с записью в файлы
    boolean status;

    DBHelper dbHelper;
    Button autorizationBT;
    Button registrationBT;

    EditText phoneTB;
    EditText passTB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authorization);

        dbHelper = new DBHelper(this);

        status = getStatus(); // если он уже считается вошедшим, то ничего не создаем.
        if(status) {
            //проверяем БД
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            String myPhone = getPhone();
            String myPass = getPass();
            boolean confirmed  = checkData(database,myPhone,myPass);

            if (confirmed) // Входим в профиль
                goToProfile();
        }

        autorizationBT = (Button) findViewById(R.id.authorizationBtn);
        registrationBT = (Button) findViewById(R.id.registrationAuthBtn);

        phoneTB = (EditText) findViewById(R.id.phoneAutTB);
        passTB = (EditText) findViewById(R.id.passAutTB);

        autorizationBT.setOnClickListener(this);
        registrationBT.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        String myPhone = phoneTB.getText().toString();
        String myPass = passTB.getText().toString();

        switch (v.getId()){
            case R.id.authorizationBtn:
                ReadDB(database);
                boolean confirmed  = checkData(database,myPhone,myPass);
                logIn(confirmed); // сохраняем статус,получаем, переходим в профиль
                break;
            case R.id.registrationAuthBtn:
                goToReg();
            default:
                break;
        }
    }

    private void logIn(boolean confirmed){

        if(confirmed){
            Log.d(TAG, "You are ");
            saveStatus(); // сохраняем статус
            status = getStatus(); // если true то пользователь вошел иначе не вошел

            if(status){
                goToProfile(); // переходим в профиль
            }
        }
        else {
            Log.d(TAG, "You are NOT!");
        }
    }

    private boolean checkData(SQLiteDatabase database,String myPhone, String myPass){

        Cursor cursor = database.query(DBHelper.TABLE_CONTACTS_USERS,null,null,null,null,null,null,null);

        if(cursor.moveToFirst()){
            int indexPhone = cursor.getColumnIndex(DBHelper.KEY_PHONE);
            int indexPass = cursor.getColumnIndex(DBHelper.KEY_PASS);

            do{
               if(myPhone.equals(cursor.getString(indexPhone)) && myPass.equals(cursor.getString(indexPass))){
                   cursor.close();
                   return true;
               }
            }while (cursor.moveToNext());
        }
        cursor.close();
        return  false;
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

    private boolean getStatus() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        boolean result = sPref.getBoolean(STATUS, false);

        return  result;
    }

    private String getPhone() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String phone = sPref.getString(PHONE, "");

        return  phone;
    }

    private String getPass() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String pass = sPref.getString(PASS, "");

        return  pass;
    }

    private void saveStatus() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean(STATUS, true);
        editor.apply();
    }

    private void goToReg() {
        Intent intent = new Intent(this, registration.class);
        startActivity(intent);
        finish();
    }

    private  void goToProfile(){
        Intent intent = new Intent(this, profile.class);
        startActivity(intent);
        finish();
    }

}




