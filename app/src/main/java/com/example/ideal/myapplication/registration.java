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
                //получение данных с инпутов
                String myName = nameInput.getText().toString();
                String mySurname = surnameInput.getText().toString();
                String myCity = cityInput.getText().toString();
                String myPhone = phoneInput.getText().toString();
                String myPass = passInput.getText().toString();
                //проверка на незаполенные поля
                if(isFullInputs(myPhone,myPass,myCity, myName)){
                    //проверка на стойкость пароля
                    if(isStrongPassword(myPass)) {
                        //проверка свободен ли телефон
                        if(isFreePhone(database, myPhone)) {
                            //процесс регистрации
                            registration(database, myName, mySurname, myPhone, myPass, myCity);
                            // идем в профиль
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
                                "Пароль должен содержать буквы и цифры и быть не менее 6 символов.",
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
                // идем в авторизацию
                goToAuthorization();
                break;

            default:
                break;
        }
    }

    private void registration(SQLiteDatabase database, String myName, String mySurname,
                              String myPhone, String myPass, String myCity){
        ContentValues contentValues = new ContentValues();
        //хэшируем пароль
        myPass =  encryptThisStringSHA512(myPass);
        //заносим данные в контент
        contentValues.put(DBHelper.KEY_NAME_USERS, myName.toLowerCase());
        contentValues.put(DBHelper.KEY_SURNAME_USERS, mySurname.toLowerCase());
        contentValues.put(DBHelper.KEY_USER_ID, myPhone);
        contentValues.put(DBHelper.KEY_PASS_USERS, myPass);
        contentValues.put(DBHelper.KEY_CITY_USERS, myCity.toLowerCase());
        //заносим данные в БД
        database.insert(DBHelper.TABLE_CONTACTS_USERS, null, contentValues);
        // локально сохраняем телефон и пароль
        saveIdAndPass(myPhone, myPass);
        // сохраняем статус о том, что пользователь вошел
        saveStatus();
        }

    protected boolean isStrongPassword(String myPass) {
        if(!myPass.matches(".*[a-z].*")) return  false;
        if(!myPass.matches(".*[0-9].*")) return  false;
        if(myPass.length()<=5) return false;
        return true;
    }

    private boolean isFreePhone(SQLiteDatabase database, String phone){
        // вернуть номер телефона
        // используем таблицу Users
        // номер телефона уже должен быть в базе данных
        String sqlQuery =
                "SELECT "
                + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_USER_ID
                + " FROM "
                + DBHelper.TABLE_CONTACTS_USERS
                + " WHERE "
                + DBHelper.TABLE_CONTACTS_USERS +"." + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery,new String[] {String.valueOf(phone)});

        if(cursor.moveToFirst()){
            // если есть такой номер, значит он занят
            cursor.close();
            return  false;
        }
        else {
            // иначе свободен
            cursor.close();
            return true;
        }

    }
    protected Boolean isFullInputs(String phone, String pass, String city, String name){

        if(phone.trim().equals("")) return false;
        if(pass.trim().equals("")) return false;
        if(city.trim().equals("")) return false;
        if(name.trim().equals("")) return false;

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