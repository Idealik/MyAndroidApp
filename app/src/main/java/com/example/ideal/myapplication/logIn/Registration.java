package com.example.ideal.myapplication.logIn;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ideal.myapplication.other.DBHelper;
import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.other.Profile;
import com.example.ideal.myapplication.fragments.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DBInf";
    private static final String STATUS = "status";
    private static final String FILE_NAME = "Info";

    private static final String PHONE_NUMBER = "Phone number";
    private static final String REF = "users/";

    private static final String NAME = "name";
    private static final String CITY = "city";

    Button registrateBtn;
    Button loginBtn;

    EditText nameInput;
    EditText surnameInput;
    EditText cityInput;
    EditText phoneInput;

    DBHelper dbHelper;

    User user;

    SharedPreferences sPref;    //класс для работы с записью в файлы

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        registrateBtn = findViewById(R.id.registrateRegistrationBtn);
        loginBtn = findViewById(R.id.loginRegistrationBtn);

        nameInput = findViewById(R.id.nameRegistrationInput);
        surnameInput = findViewById(R.id.surnameRegistrationInput);
        cityInput = findViewById(R.id.cityRegistrationInput);
        phoneInput = findViewById(R.id.phoneRegistrationInput);
        //Заполняем поле телефона
        String phoneNumber = getIntent().getStringExtra(PHONE_NUMBER);
        phoneInput.setText(phoneNumber);

        dbHelper = new DBHelper(this);

        registrateBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.registrateRegistrationBtn:
                //получение данных с инпутов
                user = new User();
                //проверка на незаполенные поля
                if(isFullInputs()) {
                    //если имя не устанавлявается, значит выводим тоаст и выходим из кейса
                    String fullName = nameInput.getText().toString().toLowerCase() + " " + surnameInput.getText().toString().toLowerCase();
                    if(!user.setName(fullName)){
                        Toast.makeText(
                                this,
                                "Имя должно содержать только буквы",
                                Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if(!user.setCity(cityInput.getText().toString().toLowerCase())){
                        Toast.makeText(
                                this,
                                "Название города должно содержать только буквы",
                                Toast.LENGTH_SHORT).show();
                        break;
                    }
                    registration(user);
                    //идем в профиль
                    goToProfile();
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

    private void registration(User user) {
        String phoneNumber = phoneInput.getText().toString();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(REF+phoneNumber);

        Map<String,Object> items = new HashMap<>();
        items.put(NAME,user.getName());
        items.put(CITY,user.getCity());
        myRef.updateChildren(items);
        //заносим данные о пользователе в локальную базу данных
        putDataInLocalStorage(user, phoneNumber);
        // сохраняем статус о том, что пользователь вошел
        saveStatus();
    }

    private void putDataInLocalStorage(User user,
                                       String phoneNumber) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        database.delete(DBHelper.TABLE_CONTACTS_USERS, null, null);

        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.KEY_NAME_USERS, user.getName());
        contentValues.put(DBHelper.KEY_CITY_USERS, user.getCity());
        contentValues.put(DBHelper.KEY_USER_ID, phoneNumber);

        database.insert(DBHelper.TABLE_CONTACTS_USERS,null,contentValues);
    }

    protected Boolean isFullInputs(){
        if(nameInput.getText().toString().isEmpty()) return false;
        if(surnameInput.getText().toString().isEmpty()) return false;
        if(cityInput.getText().toString().isEmpty()) return false;

        return  true;
    }

    private void saveStatus() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean(STATUS, true);
        editor.apply();
    }

    private  void goToProfile(){
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
        finish();
    }

    private  void  goToAuthorization(){
        Intent intent = new Intent(Registration.this, Authorization.class);
        startActivity(intent);
        finish();
    }
}