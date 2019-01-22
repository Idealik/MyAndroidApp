package com.example.ideal.myapplication.logIn;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.fragments.Service;
import com.example.ideal.myapplication.fragments.User;
import com.example.ideal.myapplication.helpApi.WorkWithViewApi;
import com.example.ideal.myapplication.other.DBHelper;
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

    private static final String FILE_NAME = "Info";
    private static final String STATUS = "status";
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

    private boolean logIn;
    private String truePassword;
    private long counter;
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
        boolean status = getStatus();
        logIn = false;
        truePassword = "";

        logInBtn = findViewById(R.id.logInAuthorizationBtn);
        registrateBtn = findViewById(R.id.registrationAuthorizationBtn);

        phoneInput = findViewById(R.id.phoneAuthorizationInput);
        passInput = findViewById(R.id.passAuthorizationInput);

        logInBtn.setOnClickListener(this);
        registrateBtn.setOnClickListener(this);

        if(status) {
            // получаем с локальных записей логин и пароль
            String myPhoneNumber = getUserPhone();
            String myPassword = getUserPass();

            isAuthorizedUser(myPhoneNumber, myPassword);
        } else {
            showViewsOnScreen();
        }
        WorkWithViewApi.hideKeyboard(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case  R.id.logInAuthorizationBtn:

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

    private void isAuthorizedUser(final String myPhoneNumber, final String myPassword) {

        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(USERS).child(myPhoneNumber);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                // Получаем пароль из Firebase
                Object passObj = userSnapshot.child(PASS).getValue();
                if (passObj != null) {
                    truePassword = passObj.toString();

                    // Проверка на правильность пароля
                    if (myPassword.equals(truePassword)) {
                        // Пароль правильный

                        // скарываем Views и запукаем прогресс бар
                        hideViewsOfScreen();

                        // Получаем остальные данные о пользователе
                        Object name = userSnapshot.child(NAME).getValue();
                        if(name == null) {
                            // Имя в БД отсутствует, значит пользователь не до конца зарегистрировался
                            goToRegistration(myPhoneNumber);
                            logIn = true;
                        } else {
                            String city = String.valueOf(userSnapshot.child(CITY).getValue());

                            User user = new User();
                            user.setPhone(String.valueOf(myPhoneNumber));
                            user.setName(String.valueOf(name));
                            user.setCity(city);

                            // Очищаем LocalStorage
                            clearSQLite();

                            // Добавляем все данные в SQLite
                            addUserInfoInLocalStorage(user);

                            //loadDialogs();

                            loadServiceByUserPhone(myPhoneNumber, myPassword);
                        }
                    } else{
                        logInBtn.setClickable(true);
                        // Пароль - неверный
                        // Показываем все вью
                        showViewsOnScreen();
                        // Проверяем поля ввода
                        checkInputs();
                        // Обновляем статус
                        saveStatus(false);
                    }
                } else {
                    logInBtn.setClickable(true);
                    // Такого пользователя вообще нет в Firebase
                    // Показываем все вью
                    showViewsOnScreen();
                    // Проверяем поля ввода
                    checkInputs();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    private void loadServiceByUserPhone(final String myPhoneNumber, final String myPassword) {
        Query query = FirebaseDatabase.getInstance().getReference(SERVICES).
                orderByChild(USER_ID).
                equalTo(myPhoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

                    addWorkingDaysInLocalStorage(serviceId);
                    addUserServicesInLocalStorage(newService);
                }

                loadTimeByUserPhone(myPhoneNumber, myPassword);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    private void addWorkingDaysInLocalStorage(final String serviceId) {
        Query query = FirebaseDatabase.getInstance().getReference(WORKING_DAYS)
                .orderByChild(SERVICE_ID)
                .equalTo(serviceId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot days) {
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                for(DataSnapshot day:days.getChildren()) {
                    String dayId = day.getKey();
                    String date = String.valueOf(day.child(DATE).getValue());

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DBHelper.KEY_ID, dayId);
                    contentValues.put(DBHelper.KEY_DATE_WORKING_DAYS, date);
                    contentValues.put(DBHelper.KEY_SERVICE_ID_WORKING_DAYS, serviceId);

                    database.insert(DBHelper.TABLE_WORKING_DAYS, null, contentValues);

                    addWorkingTimeInLocalStorage(dayId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    private void addWorkingTimeInLocalStorage(final String dayId) {
        Query query = FirebaseDatabase.getInstance().getReference(WORKING_TIME)
                .orderByChild(WORKING_DAY_ID)
                .equalTo(dayId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot timePieces) {
                SQLiteDatabase database = dbHelper.getWritableDatabase();
                for(DataSnapshot timeSnapshot:timePieces.getChildren()) {
                    String timeId = timeSnapshot.getKey();
                    String time = String.valueOf(timeSnapshot.child(TIME).getValue());
                    String userId = String.valueOf(timeSnapshot.child(USER_ID).getValue());

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DBHelper.KEY_ID, timeId);
                    contentValues.put(DBHelper.KEY_TIME_WORKING_TIME, time);
                    contentValues.put(DBHelper.KEY_USER_ID, userId);
                    contentValues.put(DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME, dayId);

                    database.insert(DBHelper.TABLE_WORKING_TIME, null, contentValues);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
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
                    String timeUserId = myPhoneNumber;
                    String timeWorkingDayId = String.valueOf(time.child(WORKING_DAY_ID).getValue());

                    addTimeInLocalStorage(timeId, timeTime, timeUserId, timeWorkingDayId);

                    loadWorkingDayById(timeWorkingDayId, ordersCount, myPhoneNumber, myPassword);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
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
                attentionBadConnection();
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
                String serviceUserId = String.valueOf(service.child(USER_ID).getValue());

                Service newService = new Service();
                newService.setId(serviceId);
                newService.setName(serviceName);
                newService.setDescription(serviceDescription);
                newService.setCost(serviceCost);
                newService.setRating(serviceRating);
                newService.setCountOfRates(serviceCountOfRates);
                newService.setUserId(serviceUserId);

                addUserServicesInLocalStorage(newService);
                counter++;

                if((counter == ordersCount) && (!logIn)) {
                    // Выполняем вход
                    logIn(myPhoneNumber, myPassword);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    // Обновляет информацию о текущем пользователе в SQLite
    private void addUserInfoInLocalStorage(User user) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // Заполняем contentValues информацией о данном пользователе
        contentValues.put(DBHelper.KEY_NAME_USERS, user.getName());
        contentValues.put(DBHelper.KEY_CITY_USERS, user.getCity());
        contentValues.put(DBHelper.KEY_USER_ID, user.getPhone());

        // Добавляем данного пользователя в SQLite
        database.insert(DBHelper.TABLE_CONTACTS_USERS, null, contentValues);
    }

    // Удаляет все данные о пользователях, сервисах, рабочих днях и рабочем времени из SQLite
    private void clearSQLite() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(DBHelper.TABLE_CONTACTS_USERS,null,null);
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

    private void showViewsOnScreen(){
        logInBtn.setVisibility(View.VISIBLE);
        registrateBtn.setVisibility(View.VISIBLE);
        phoneInput.setVisibility(View.VISIBLE);
        passInput.setVisibility(View.VISIBLE);
    }

    private void hideViewsOfScreen(){
        registrateBtn.setVisibility(View.INVISIBLE);
        logInBtn.setVisibility(View.INVISIBLE);
        passInput.setVisibility(View.INVISIBLE);
        phoneInput.setVisibility(View.INVISIBLE);
    }

    private void logIn(String phone, String pass){
        //сохраняем статус
        saveStatus(true);
        //сохраяем номер пользователя и пароль
        saveIdAndPass(phone,pass);
        //переходим в профиль
        goToProfile();
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

        return  sPref.getBoolean(STATUS, false);
    }

    //получить номер телефона для проверки
    private String getUserPhone() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);

        return  sPref.getString(PHONE_NUMBER, "-");
    }

    //получить пароль для проверки
    private String getUserPass() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        return  sPref.getString(PASS, "-");
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

    private void goToProfile(){
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
        finish();
    }

    private void attentionBadConnection() {
        Toast.makeText(this,"Плохое соединение",Toast.LENGTH_SHORT).show();
    }
}