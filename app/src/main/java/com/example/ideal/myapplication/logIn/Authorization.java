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

import com.example.ideal.myapplication.fragments.Service;
import com.example.ideal.myapplication.fragments.User;
import com.example.ideal.myapplication.other.DBHelper;
import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.other.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Authorization extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DBInf";
    private static final String STATUS = "status";

    private static final String FILE_NAME = "Info";
    private static final String PHONE_NUMBER = "Phone number";
    private static final String PASS = "password";

    private static final String USERS = "users";
    private static final String NAME = "name";
    private static final String CITY = "city";

    private static final String SERVICES = "services";
    private static final String USER_ID = "user id";
    private static final String DESCRIPTION = "description";
    private static final String COST = "cost";
    private static final String RATING = "rating";
    private static final String COUNT_OF_RATES = "count of rates";

    private static final String WORKING_TIME = "working time";
    private static final String WORKING_DAY_ID = "working day id";
    private static final String TIME = "time";

    private static final String WORKING_DAYS = "working days";
    private static final String SERVICE_ID = "service id";
    private static final String DATE = "data";

    boolean status;
    boolean logIn;
    String truePassword;
    long counter;

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
        logIn = false;
        // если он уже считается вошедшим
        truePassword = "";
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
                if(isFullInputs()) {
                    logInBtn.setClickable(false);
                    String myPhoneNumber = convertPhoneToNormalView(String.valueOf(phoneInput.getText()));
                    String myPassword = String.valueOf(passInput.getText());
                    // Хэшируем пароль (для правильного сравнения)
                    myPassword = encryptThisStringSHA512(myPassword);
                    // Авторизируем пользователя
                    isAuthorizedUser(myPhoneNumber, myPassword);
                }
                break;
            case R.id.registrationAuthorizationBtn:
                goToConfirmation();
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
        saveStatus(true);
        //сохраяем номер пользователя и пароль
        saveIdAndPass(phone,pass);
        //переходим в профиль
        goToProfile();
    }

    private void isAuthorizedUser(final String myPhoneNumber, final String myPassword) {

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Получаем пароль из Firebase
                Object passObj = dataSnapshot.child(USERS).child(myPhoneNumber).child(PASS).getValue();

                if (passObj != null) {
                    truePassword = passObj.toString();
                    // Проверка на правильность пароля

                    if (myPassword.equals(truePassword)) {
                        registrateBtn.setVisibility(View.INVISIBLE);
                        logInBtn.setVisibility(View.INVISIBLE);
                        passInput.setVisibility(View.INVISIBLE);
                        phoneInput.setVisibility(View.INVISIBLE);

                        // Пароль правильный
                        // Получаем остальные данные о пользователе
                        Object name = dataSnapshot.child(USERS)
                                .child(myPhoneNumber)
                                .child(NAME)
                                .getValue();

                        if(name == null) {
                            goToRegistration(myPhoneNumber);
                            logIn = true;
                            return;
                        }

                        String city = String.valueOf(dataSnapshot.child(USERS)
                                .child(myPhoneNumber)
                                .child(CITY)
                                .getValue());

                        User user = new User();
                        user.setName(String.valueOf(name));
                        user.setCity(city);

                        // Добавляем все данные в SQLite
                        updateUserInfoInLocalStorage(myPhoneNumber, user);

                        loadSeviceByUserPhone(myPhoneNumber, myPassword);
                    } else{
                        logInBtn.setClickable(true);
                        // Пароль - неверный
                        // Показываем все вью
                        addViewOnScreen();
                        // Проверяем поля ввода
                        checkInputs();
                        // Обновляем статус
                        saveStatus(false);
                    }
                } else {
                    logInBtn.setClickable(true);
                    // Такого пользователя вообще нет в Firebase
                    // Показываем все вью
                    addViewOnScreen();
                    // Проверяем поля ввода
                    checkInputs();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled");
            }
        });
    }

    private void loadSeviceByUserPhone(final String myPhoneNumber, final String myPassword) {
        Query query = FirebaseDatabase.getInstance().getReference(SERVICES).
                orderByChild(USER_ID).
                equalTo(myPhoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                clearSQLite();
                final long serviceCount = dataSnapshot.getChildrenCount();

                if(serviceCount==0){
                    logIn(myPhoneNumber, myPassword);
                    logIn = true;
                    return;
                }

                for (DataSnapshot service : dataSnapshot.getChildren()) {
                    String serviceId = String.valueOf(service.getKey());
                    String serviceName = String.valueOf(service.child(NAME).getValue());
                    String serviceDescription = String.valueOf(service.child(DESCRIPTION).getValue());
                    String serviceCost = String.valueOf(service.child(COST).getValue());
                    String serviceRating = String.valueOf(service.child(RATING).getValue());
                    String serviceCountOfRates = String.valueOf(service.child(COUNT_OF_RATES).getValue());

                    Service newService = new Service();
                    newService.setId(serviceId);
                    newService.setName(serviceName);
                    newService.setDescription(serviceDescription);
                    newService.setCost(serviceCost);
                    newService.setRating(serviceRating);
                    newService.setCountOfRates(serviceCountOfRates);
                    newService.setUserId(myPhoneNumber);

                    addUserServicesInLocalStorage(newService);
                }

                loadTimeByUserPhone(myPhoneNumber, myPassword);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled");
            }
        });
    }

    private void loadTimeByUserPhone(final String myPhoneNumber, final String myPassword) {
        Query timeQuery = FirebaseDatabase.getInstance().getReference(WORKING_TIME)
                .orderByChild(USER_ID)
                .equalTo(myPhoneNumber);
        timeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final long ordersCount = dataSnapshot.getChildrenCount();
                if((ordersCount==0)&&(!logIn)){
                    logIn(myPhoneNumber, myPassword);
                    logIn = true;
                    return;
                }
                counter = 0;
                for(DataSnapshot time:dataSnapshot.getChildren()){
                    String timeId = String.valueOf(time.getKey());
                    String timeTime = String.valueOf(time.child(TIME).getValue());
                    final String timeUserId = myPhoneNumber;
                    String timeWorkingDayId = String.valueOf(time.child(WORKING_DAY_ID).getValue());

                    addTimeInLocalStorage(timeId, timeTime,timeUserId,timeWorkingDayId);

                    loadWorkingDayById(timeWorkingDayId, ordersCount, myPhoneNumber, myPassword);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void loadWorkingDayById(String workingDayId, final long ordersCount,
                                    final String myPhoneNumber, final String myPassword) {
        DatabaseReference dayReference = FirebaseDatabase.getInstance().getReference(WORKING_DAYS).child(workingDayId);
        dayReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot day) {
                String dayServiceId = String.valueOf(day.child(SERVICE_ID).getValue());

                String dayId = String.valueOf(day.getKey());
                String dayDate = String.valueOf(day.child(DATE).getValue());

                addScheduleInLocalStorage(dayId, dayDate, dayServiceId);

                loadServiceById(dayServiceId, ordersCount, myPhoneNumber, myPassword);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void loadServiceById(String serviceId, final long ordersCount,
                                final String myPhoneNumber, final String myPassword) {
        DatabaseReference serviceReference = FirebaseDatabase.getInstance().getReference(SERVICES).child(serviceId);
        serviceReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot service) {
                String serviceId = String.valueOf(service.getKey());
                String serviceName = String.valueOf(service.child(NAME).getValue());
                String serviceDescription = String.valueOf(service.child(DESCRIPTION).getValue());
                String serviceCost = String.valueOf(service.child(COST).getValue());
                String serviceRating = String.valueOf(service.child(RATING).getValue());
                String serviceCountOfRates = String.valueOf(service.child(COUNT_OF_RATES).getValue());

                Service newService = new Service();
                newService.setId(serviceId);
                newService.setName(serviceName);
                newService.setDescription(serviceDescription);
                newService.setCost(serviceCost);
                newService.setRating(serviceRating);
                newService.setCountOfRates(serviceCountOfRates);
                newService.setUserId(null);

                addUserServicesInLocalStorage(newService);
                counter++;

                if((counter == ordersCount) && (!logIn)) {
                    // Выполняем вход
                    logIn(myPhoneNumber, myPassword);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // Обновляет информацию о текущем пользователе в SQLite
    private void updateUserInfoInLocalStorage(String phoneNumber, User user) {
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
        contentValues.put(DBHelper.KEY_NAME_USERS, user.getName());
        contentValues.put(DBHelper.KEY_CITY_USERS, user.getCity());

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
        cursor.close();
    }

    // Удаляет все сервисы из SQLite
    private void clearSQLite() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(DBHelper.TABLE_CONTACTS_SERVICES, null, null);
        database.delete(DBHelper.TABLE_WORKING_DAYS,null,null);
        database.delete(DBHelper.TABLE_WORKING_TIME,null,null);

    }

    // Добавляет информацию о сервисах данного пользователя в SQLite
    private void addUserServicesInLocalStorage(Service service) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // Заполняем contentValues информацией о данном сервисе
        contentValues.put(DBHelper.KEY_ID, service.getId());
        contentValues.put(DBHelper.KEY_NAME_SERVICES, service.getName());
        contentValues.put(DBHelper.KEY_USER_ID, service.getUserId());
        contentValues.put(DBHelper.KEY_DESCRIPTION_SERVICES, service.getDescription());
        contentValues.put(DBHelper.KEY_MIN_COST_SERVICES, service.getCost());
        contentValues.put(DBHelper.KEY_RATING_SERVICES, service.getRating());
        contentValues.put(DBHelper.KEY_COUNT_OF_RATES_SERVICES, service.getCountOfRates());

        // Добавляем данный сервис в SQLite
        database.insert(DBHelper.TABLE_CONTACTS_SERVICES, null, contentValues);
    }

    private void addTimeInLocalStorage(String timeId, String timeDate,
                                       String timeUserId, String timeWorkingDayId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.KEY_ID, timeId);
        contentValues.put(DBHelper.KEY_TIME_WORKING_TIME, timeDate);
        contentValues.put(DBHelper.KEY_USER_ID,timeUserId);
        contentValues.put(DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME, timeWorkingDayId);

        database.insert(DBHelper.TABLE_WORKING_TIME,null,contentValues);
    }

    private void addScheduleInLocalStorage(String dayId, String dayDate, String serviceId) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_ID, dayId);
        contentValues.put(DBHelper.KEY_DATE_WORKING_DAYS, dayDate);
        contentValues.put(DBHelper.KEY_SERVICE_ID_WORKING_DAYS, serviceId);

        database.insert(DBHelper.TABLE_WORKING_DAYS, null, contentValues);

    }

    private String convertPhoneToNormalView(String phone) {
        if(phone.charAt(0)=='8'){
            phone = "+7" + phone.substring(1);
        }
        return phone;
    }

    protected Boolean isFullInputs(){
        if(phoneInput.getText().toString().isEmpty()) return false;
        if(passInput.getText().toString().isEmpty()) return false;
        return  true;
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
        String userId = sPref.getString(PHONE_NUMBER, "-");

        return  userId;
    }

    //получить пароль для проверки
    private String getUserPass() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String pass = sPref.getString(PASS, "-");
        return  pass;
    }

    private void saveStatus(boolean statusValue) {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean(STATUS, statusValue);
        editor.apply();
    }

    private void saveIdAndPass(String phone, String pass) {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(PHONE_NUMBER, phone);
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

    private void goToRegistration(String phone) {
        Intent intent = new Intent(this, Registration.class);
        intent.putExtra(PHONE_NUMBER,phone);
        startActivity(intent);
        finish();
    }
    private void goToConfirmation() {
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