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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class registration extends AppCompatActivity implements View.OnClickListener {

    final String TAG = "DBInf";
    final String STATUS = "status";
    final String PHONE = "phone";
    final String PASS = "pass";
    final String FILE_NAME = "Info";

    Button registrateBtn;
    Button loginBtn;

    EditText nameInput;
    EditText surnameInput;
    EditText cityInput;
    EditText phoneInput;
    EditText passInput;

    DBHelper dbHelper;          //База Данных
    SharedPreferences sPref;    //класс для работы с записью в файлы

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        registrateBtn = (Button) findViewById(R.id.registrateRegistrationBtn);
        loginBtn = (Button) findViewById(R.id.loginRegistrationBtn);

        nameInput = (EditText) findViewById(R.id.nameRegistrationInput);
        surnameInput = (EditText) findViewById(R.id.surnameRegistrationInput);
        phoneInput = (EditText) findViewById(R.id.phoneRegistrationInput);
        passInput = (EditText) findViewById(R.id.passRegistrationInput);
        cityInput = (EditText) findViewById(R.id.cityRegistrationInput);

        dbHelper = new DBHelper(this);

        registrateBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        switch (v.getId()){
            case R.id.registrateRegistrationBtn:
                String myName = nameInput.getText().toString();
                String mySurname = surnameInput.getText().toString();
                String myCity = cityInput.getText().toString();
                String myPhone = phoneInput.getText().toString();
                String myPass = passInput.getText().toString();

                if(isFullInputs(myPhone,myPass,myCity)){
                    if(isStrongPassword(myPass)) {
                        if(isFreePhone(database, myPhone)) {
                            registration(database, myName, mySurname, myPhone, myPass, myCity);
                            goToProfile();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Пользователь с данным номером телефона уже зарегистрирован.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(
                                this,
                                "Пароль недостаточно надёжен, попробуй другой.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(
                            this,
                            "Не все поля заполнены",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.loginRegistrationBtn:
                goToAuthorization();
                break;

            default:
                break;
        }
    }

    private void registration(SQLiteDatabase database, String myName, String mySurname,
                              String myPhone, String myPass, String myCity){

        ContentValues contentValues = new ContentValues();
         myPass =  encryptThisStringSHA512(myPass);
        // добавить проверку на непустые поля
        contentValues.put(DBHelper.KEY_NAME_USERS, myName);
        contentValues.put(DBHelper.KEY_SURNAME_USERS, mySurname);
        contentValues.put(DBHelper.KEY_USER_ID, myPhone);
        contentValues.put(DBHelper.KEY_PASS_USERS, myPass);
        contentValues.put(DBHelper.KEY_CITY_USERS, myCity);

        database.insert(DBHelper.TABLE_CONTACTS_USERS, null, contentValues);
        saveIdAndPass(myPhone, myPass);
        saveStatus();

        readDB(database);
        Log.d(TAG, "reg was successful");
    }

    protected boolean isStrongPassword(String myPass) {
      //  if(!myPass.matches(".*[A-Z].*")) return  false;
        if(!myPass.matches(".*[0-9].*")) return  false;
        if(myPass.length()<=5) return false;
        return true;
    }

    private boolean isFreePhone(SQLiteDatabase database, String phone){
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
            do{
                if(phone.equals(cursor.getString(indexPhone))){
                    return  false;
                }
            }while (cursor.moveToNext());
        }
        cursor.close();
        return  true;
    }
    protected Boolean isFullInputs(String phone, String pass, String city){

        if(phone.trim().equals("")) return false;
        if(pass.trim().equals("")) return false;
        if(city.trim().equals("") ) return false;

        return  true;
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

    private static String encryptThisStringSHA512(String input)
    {
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void readDB(SQLiteDatabase database){
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
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_USERS);
            int indexSurname = cursor.getColumnIndex(DBHelper.KEY_SURNAME_USERS);
            int indexPass = cursor.getColumnIndex(DBHelper.KEY_PASS_USERS);
            int indexCity = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);

            do{
                Log.d(TAG, " \t" + cursor.getString(indexPhone)
                        + " \t" + cursor.getString(indexPass)
                        + " \t" + cursor.getString(indexCity)
                        + " \t" + cursor.getString(indexName)
                        + " \t" + cursor.getString(indexSurname)
                        + " ");
            }while (cursor.moveToNext());
        }
        else {
            Log.d(TAG, "DB is empty");
        }
        cursor.close();
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