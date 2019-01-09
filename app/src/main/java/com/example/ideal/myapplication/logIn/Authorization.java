package com.example.ideal.myapplication.logIn;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ideal.myapplication.other.DBHelper;
import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.other.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Authorization extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "tag";
    final String STATUS = "status";

    final String FILE_NAME = "Info";
    final String PHONE = "phone";
    final String PASS = "password";


    private static final String REF = "users/";
    private static final String NAME = "name";
    private static final String SURNAME = "surname";
    private static final String CITY = "city";


    boolean status;
    String truePassword = "";

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
        status = getStatus();
        // если он уже считается вошедшим

        logInBtn = findViewById(R.id.logInAuthorizationBtn);
        registrateBtn = findViewById(R.id.registrationAuthorizationBtn);

        phoneInput = findViewById(R.id.phoneAuthorizationInput);
        passInput = findViewById(R.id.passAuthorizationInput);

        if(status) {
            // получаем с локальных записей логин и пароль
            String myPhoneNumber = getUserPhone();
            String myPassword = getUserPass();

            isAuthorizedUser(myPhoneNumber, myPassword);
        } else {
            addViewOnScreen();
        }
        logInBtn.setOnClickListener(this);
        registrateBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.logInAuthorizationBtn:
                String myPhoneNumber = convertPhoneToNormalView(phoneInput.getText().toString());
                String myPassword = passInput.getText().toString();

                // Хэшируем пароль (для правильного сравнения)
                myPassword = encryptThisStringSHA512(myPassword);
                // Авторизируем пользователя
                isAuthorizedUser(myPhoneNumber, myPassword);
                break;
            case R.id.registrationAuthorizationBtn:
                goToRegistration();
            default:
                break;
        }
    }

    private void addViewOnScreen(){
        logInBtn.setVisibility(View.VISIBLE);
        registrateBtn.setVisibility(View.VISIBLE);
        phoneInput.setVisibility(View.VISIBLE);
        passInput.setVisibility(View.VISIBLE);
    }

    private void logIn(String phone, String pass){
        //сохраняем статус
        saveStatus();
        //сохраяем номер пользователя и пароль
        saveIdAndPass(phone,pass);
        //переходим в профиль
        goToProfile();
    }

    private void isAuthorizedUser(final String myPhoneNumber, final String myPassword) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference(REF + myPhoneNumber);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Получаем пароль из Firebase
                Object passObj = dataSnapshot.child(PASS).getValue();

                if(passObj != null) {
                    truePassword = passObj.toString();
                    // Проверка на правильность пароля
                    if(myPassword.equals(truePassword)) {
                        // Пароль правильный
                        // Получаем остальные данные о пользователе
                        String name = String.valueOf(dataSnapshot.child(NAME).getValue());
                        String surname = String.valueOf(dataSnapshot.child(SURNAME).getValue());
                        String city = String.valueOf(dataSnapshot.child(CITY).getValue());

                        // Добавляем все данные в SQLite
                        updateSQLite(myPhoneNumber, name, surname, city);
                        // Выполняем вход
                        logIn(myPhoneNumber, myPassword);
                    }
                    else {
                        // Пароль - неверный
                        // Показываем все вью
                        addViewOnScreen();
                        // Проверяем поля ввода
                        checkInputs();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("111", "onCancelled");
            }
        });
    }

    private void updateSQLite(String phoneNumber, String name, String surname, String city) {
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // Данные из тыблицы Users
        // По номеру телефона
        String sqlQuery =
                "SELECT * "
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_USERS
                        + " WHERE "
                        + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{phoneNumber});

        // Заполняем contentValues информацией о данном пользователе
        contentValues.put(DBHelper.KEY_NAME_USERS, name);
        contentValues.put(DBHelper.KEY_SURNAME_USERS, surname);
        contentValues.put(DBHelper.KEY_CITY_USERS, city);

        // Проверка есть ли такой пользователь в SQLite
        if(cursor.moveToFirst()) {
            // Данный пользователь уже есть
            // Обновляем информацию о нём
            database.update(
                    DBHelper.TABLE_CONTACTS_USERS,
                    contentValues,
                    DBHelper.KEY_USER_ID + " = ?",
                    new String[]{phoneNumber});
        } else {
            // Данного пользователя нет
            // Добавляем номер телефона в contentValues
            contentValues.put(DBHelper.KEY_USER_ID, phoneNumber);

            // Добавляем данного пользователя в SQLite
            database.insert(DBHelper.TABLE_CONTACTS_USERS, null, contentValues);
        }
    }

    private String convertPhoneToNormalView(String phone) {
        if(phone.charAt(0)=='8'){
            phone = "+7" + phone.substring(1);
        }
        return phone;
    }

    private void checkInputs() {
        // Проверяем поля ввода
        if(!phoneInput.getText().toString().isEmpty()) {
            // Поля непустые
            // Выводим сообщене
            Toast.makeText(
                    this,
                    "Вы ввели неправильные данные",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean getStatus() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        boolean result = sPref.getBoolean(STATUS, false);

        return  result;
    }

    //получить номер телефона для проверки
    private String getUserPhone() {
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

    private void goToRegistration() {
        Intent intent = new Intent(this, Confirmation.class);
        startActivity(intent);
        finish();
    }

    private  void goToProfile(){
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
        finish();
    }

}