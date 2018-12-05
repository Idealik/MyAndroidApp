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

import java.net.PasswordAuthentication;

public class authorization extends AppCompatActivity implements View.OnClickListener {

    final String TAG = "DBInf";
    final String STATUS = "status";
    final String PHONE = "phone";
    final String PASS = "pass";
    final String FILE_NAME = "Info";

    boolean status;

    Button logInBtn;
    Button registrateBtn;

    EditText phoneInput;
    EditText passInput;

    DBHelper dbHelper;
    SharedPreferences sPref; //класс для работы с записью в файлы

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authorization);

        dbHelper = new DBHelper(this);
        status = getStatus(); // если он уже считается вошедшим, то ничего не создаем.

        if(status) {
            //проверяем БД
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            String myPhone = getUserId();
            String myPass = getUserPass();
            boolean confirmed  = checkData(database,myPhone,myPass);

            // Входим в профиль
            if (confirmed)
                goToProfile();
            else
                Log.d(TAG, myPhone + " " + myPass);
        }

        logInBtn = (Button) findViewById(R.id.logInAuthorizationBtn);
        registrateBtn = (Button) findViewById(R.id.registrationAuthorizationBtn);

        phoneInput = (EditText) findViewById(R.id.phoneAuthorizationInput);
        passInput = (EditText) findViewById(R.id.passAuthorizationInput);

        logInBtn.setOnClickListener(this);
        registrateBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String myPhone = phoneInput.getText().toString();
        String myPass = passInput.getText().toString();

        switch (v.getId()){
            case R.id.logInAuthorizationBtn:
                readDB(database);
                //Проверка пароля и логина
                boolean confirmed  = checkData(database,myPhone,myPass);
                logIn(confirmed,myPhone,myPass); // сохраняем статус,получаем, переходим в профиль
                break;
            case R.id.registrationAuthorizationBtn:
                goToRegegistration();
            default:
                break;
        }
    }

    private void logIn(boolean confirmed, String phone, String pass){
        if(confirmed){
            Log.d(TAG, "You are ");
            saveStatus(); // сохраняем статус
            saveIdAndPass(phone,pass);  //сохраяем пользователя и пароль
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
        Cursor cursor = database.query(
                DBHelper.TABLE_CONTACTS_USERS,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        if(cursor.moveToFirst()){
            int indexPhone = cursor.getColumnIndex(DBHelper.KEY_USER_ID);
            int indexPass = cursor.getColumnIndex(DBHelper.KEY_PASS_USERS);
            boolean isConfirmed;
            do{
                isConfirmed=
                        myPhone.equals(cursor.getString(indexPhone))
                                && myPass.equals(cursor.getString(indexPass));
                if(isConfirmed){
                    cursor.close();
                    return true;
                }
            }while (cursor.moveToNext());
        }
        cursor.close();
        return  false;
    }

    private void readDB(SQLiteDatabase database){
        String msg = "";
        Cursor cursor = database.query(
                DBHelper.TABLE_CONTACTS_USERS,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        if(cursor.moveToFirst()){
            int indexPhone = cursor.getColumnIndex(DBHelper.KEY_USER_ID);
            int indexPass = cursor.getColumnIndex(DBHelper.KEY_PASS_USERS);

            do{
                msg += " Number = " + cursor.getString(indexPhone)
                                + " Pass =" + cursor.getString(indexPass)
                                + " ";
                Log.d(TAG," " + cursor.getString(indexPhone)
                        + " " + cursor.getString(indexPass)
                        + " ");

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

    //получить номер телефона для проверки
    private String getUserId() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String userId = sPref.getString(PHONE, "-");

        return  userId;
    }

    //получить пароль для проверки
    private String getUserPass() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String pass = sPref.getString(PASS, "-");

        return  pass;
    }

    private void saveStatus() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean(STATUS, true);
        editor.apply();
    }
    private void saveIdAndPass(String phone, String pass) {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(PHONE, phone);
        editor.putString(PASS, pass);
        editor.apply();
    }

    private void goToRegegistration() {
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