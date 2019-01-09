package com.example.ideal.myapplication.editing;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ideal.myapplication.other.DBHelper;
import com.example.ideal.myapplication.R;

public class EditProfile extends AppCompatActivity implements View.OnClickListener{

    private static final String USER_NAME = "my name";
    private static final String USER_SURNAME = "my surname";
    private static final String USER_CITY = "my city";
    private static final String PHONE = "phone";
    private static final String FILE_NAME = "Info";

    String oldPhone;

    Button editBtn;

    EditText nameInput;
    EditText surnameInput;
    EditText cityInput;
    EditText phoneInput;

    DBHelper dbHelper;
    SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        nameInput = findViewById(R.id.nameEditProfileInput);
        surnameInput = findViewById(R.id.surnameEditProfileInput);
        cityInput = findViewById(R.id.cityEditProfileInput);
        phoneInput = findViewById(R.id.phoneEditProfileInput);
        editBtn = findViewById(R.id.editProfileEditProfileBtn);

        nameInput.setText(getIntent().getStringExtra(USER_NAME));
        surnameInput.setText(getIntent().getStringExtra(USER_SURNAME));
        cityInput.setText(getIntent().getStringExtra(USER_CITY));
        oldPhone = getUserPhone();
        phoneInput.setText(oldPhone);

        dbHelper = new DBHelper(this);

        editBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.editProfileEditProfileBtn:
                SQLiteDatabase database = dbHelper.getReadableDatabase();
                String phone = phoneInput.getText().toString();

                //Проверка изменённого номеа
                if(phone.length() > 0) {
                    //Этот номер никем не используется или не изменён?
                    if (isFreePhone(database, phone) || phone.equals(oldPhone)) {
                        updateInfoInDataBase();
                        savePhone(phone);
                        finish();
                    } else {
                        Toast.makeText(this, getString(R.string.this_phone_is_already_usead), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.empty_phone_field), Toast.LENGTH_SHORT).show();
                }
            break;
        }
    }

    //Обновление информации в БД
    private void updateInfoInDataBase() {
        String name = nameInput.getText().toString();
        String surname = surnameInput.getText().toString();
        String city = cityInput.getText().toString();
        String phone = phoneInput.getText().toString();

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        if(name.length()!=0) contentValues.put(DBHelper.KEY_NAME_USERS, name);
        if(surname.length()!=0) contentValues.put(DBHelper.KEY_SURNAME_USERS, surname);
        if(city.length()!=0) contentValues.put(DBHelper.KEY_CITY_USERS, city);
        if(phone.length()!=0) contentValues.put(DBHelper.KEY_USER_ID, phone);
        if(contentValues.size()>0) {
            database.update(DBHelper.TABLE_CONTACTS_USERS, contentValues,
                    DBHelper.KEY_USER_ID + " = ?",
                    new String[]{String.valueOf(oldPhone)});
        }
    }

    private String getUserPhone() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String userId = sPref.getString(PHONE, "-");

        return  userId;
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

    private void savePhone(String phone) {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(PHONE, phone);
        editor.apply();
    }
}
