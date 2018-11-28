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
import android.widget.Toast;

public class registration extends AppCompatActivity implements View.OnClickListener {

    final String TAG = "DBInf";
    final String STATUS = "status";
    final String PHONE = "phone";
    final String PASS = "pass";
    final String FILE_NAME = "Info";   

    boolean status;
    
    Button registrateBtn;
    Button readBtn;
    Button deleteBtn;
    Button loginBtn;
    
    EditText phoneInput;
    EditText passInput;
    
    DBHelper dbHelper;          //База Данных
    SharedPreferences sPref;    //класс для работы с записью в файлы
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        registrateBtn = (Button) findViewById(R.id.registrateRegistrationBtn);
        readBtn = (Button) findViewById(R.id.readRegistrationBtn);
        deleteBtn = (Button) findViewById(R.id.deleteRegistrationBtn);
        loginBtn = (Button) findViewById(R.id.loginRegistrationBtn);
        
        phoneInput = (EditText) findViewById(R.id.phoneRegistrationInput);
        passInput = (EditText) findViewById(R.id.passRegistrationInput);
        
        dbHelper = new DBHelper(this);

        registrateBtn.setOnClickListener(this);
        readBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        switch (v.getId()){
            case R.id.registrateRegistrationBtn:
                String myPhone = phoneInput.getText().toString();
                String myPass = passInput.getText().toString();

                if(isStrongPassword(myPass)) {
                    if(isFreePhone(database, myPhone)) {
                        registration(database, myPhone, myPass);
                        goToProfile();
                    } else {
                        Log.d(TAG, "reg has failed!");
                        Toast.makeText(
                                this,
                                "Пользователь с данным номером телефона уже зарегистрирован.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "reg has failed!");
                    Toast.makeText(
                            this,
                            "Пароль недостаточно надёжен, попробуй другой.",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.readRegistrationBtn:
                readDB(database);
                break;

            case R.id.deleteRegistrationBtn:
                deleteDB(database);
                break;

            case R.id.loginRegistrationBtn:
                goToAuthorization();
                break;

            default:
                break;
        }
    }

    private void registration(SQLiteDatabase database, String myPhone, String myPass){
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.KEY_PHONE, myPhone);
            contentValues.put(DBHelper.KEY_PASS, myPass);

            database.insert(DBHelper.TABLE_CONTACTS_USERS, null, contentValues);
            savePhoneAndPass(myPhone, myPass);
            saveStatus();

            Log.d(TAG, "reg was successfull");
    }

    protected boolean isStrongPassword(String myPass) {
        if(!myPass.matches(".*[A-Z].*")) return  false;
        if(!myPass.matches(".*[0-9].*")) return  false;
        if(myPass.length()<=5) return false;
        return true;
    }

    private boolean isFreePhone(SQLiteDatabase database, String phone){
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
            int indexPhone = cursor.getColumnIndex(DBHelper.KEY_PHONE);
            do{
                if(phone.equals(cursor.getString(indexPhone))){
                    return  false;
                }
            }while (cursor.moveToNext());

            Log.d(TAG, "Full msg = " + msg);
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
        return  true;
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
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexPhone = cursor.getColumnIndex(DBHelper.KEY_PHONE);
            int indexPass = cursor.getColumnIndex(DBHelper.KEY_PASS);

            do{
                msg +=
                        "Index = " + cursor.getString(indexId)
                        + "\t Number = " + cursor.getString(indexPhone)
                        + "\t Pass = " + cursor.getString(indexPass) 
                        + " \n";
                Log.d(TAG, cursor.getString(indexId) 
                        + " \t" + cursor.getString(indexPhone) 
                        + " \t" + cursor.getString(indexPass)
                        + " ");
            }while (cursor.moveToNext());

            Log.d(TAG, " \nFull msg \n" + msg);
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
    }

    private  void deleteDB(SQLiteDatabase database){
        database.delete(DBHelper.TABLE_CONTACTS_USERS,null,null);
        Log.d(TAG, "DB was deleted");
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
    
    private  void  goToAuthorization(){
        Intent intent = new Intent(registration.this, authorization.class);
        startActivity(intent);
        finish();
    }
}






