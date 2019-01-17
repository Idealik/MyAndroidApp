package com.example.ideal.myapplication.other;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.createService.AddService;
import com.example.ideal.myapplication.editing.EditProfile;
import com.example.ideal.myapplication.fragments.Service;
import com.example.ideal.myapplication.fragments.foundOrderElement;
import com.example.ideal.myapplication.fragments.foundServiceProfileElement;
import com.example.ideal.myapplication.logIn.Authorization;
import com.google.firebase.database.FirebaseDatabase;

public class Profile extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DBInf";
    private static final String PHONE_NUMBER = "Phone number";
    private  final String FILE_NAME = "Info";
    private  final String STATUS = "status";
    private  static final String USER_NAME = "my name";
    private  static final String USER_CITY = "my city";
    private  final String SERVICE_ID = "service id";

    private  Button logOutBtn;
    private  Button findServicesBtn;
    private  Button addServicesBtn;
    private  Button mainScreenBtn;
    private  Button editProfileBtn;
    private  Button dialogsBtn;

    private  TextView nameText;
    private  TextView cityText;

    private  ScrollView servicesScroll;
    private  ScrollView ordersScroll;
    private  LinearLayout servicesLayout;
    private  LinearLayout ordersLayout;

    private  SwitchCompat servicesOrOrdersSwitch;

    private  SharedPreferences sPref;
    private  DBHelper dbHelper;

    private foundServiceProfileElement fServiceElement;
    private foundOrderElement fOrderElement;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        logOutBtn = findViewById(R.id.logOutProfileBtn);
        findServicesBtn = findViewById(R.id.findServicesProfileBtn);
        addServicesBtn = findViewById(R.id.addServicesProfileBtn);
        mainScreenBtn = findViewById(R.id.mainScreenProfileBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        dialogsBtn = findViewById(R.id.dialogsProfileBtn);

        servicesOrOrdersSwitch = findViewById(R.id.servicesOrOrdersProfileSwitch);

        servicesScroll = findViewById(R.id.servicesProfileScroll);
        ordersScroll = findViewById(R.id.orderProfileScroll);
        servicesLayout = findViewById(R.id.servicesProfileLayout);
        ordersLayout = findViewById(R.id.ordersProfileLayout);

        nameText = findViewById(R.id.nameProfileText);
        cityText = findViewById(R.id.cityProfileText);

        dbHelper = new DBHelper(this);

        manager = getSupportFragmentManager();


        //получаем id пользователя
        String userId = getUserId();
        // получаем сервис пользователя, если он заходит к себе в профиль, то 0
        String serviceId = getIntent().getStringExtra(SERVICE_ID);

        // идет проверка, относится ли этот сервис к пользователю,
        // чтобы дать соответствующий функционал
        // если сервис не находится в бд, то считаем, что это пользователь без сервисов
        if(isMyService(serviceId,userId)){

            // если это мой сервис - значит мой профиль
            // создаем свои сервисы
            createServicesList(userId);
            // создаем список записей
            createOrdersList(userId);
            // добавляем данные о пользователе
            createProfileData(userId);
            servicesLayout.setVisibility(View.INVISIBLE);
            servicesScroll.setVisibility(View.INVISIBLE);


            servicesOrOrdersSwitch.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        buttonView.setText("My services");
                        ordersLayout.setVisibility(View.INVISIBLE);
                        ordersScroll.setVisibility(View.INVISIBLE);
                        servicesLayout.setVisibility(View.VISIBLE);
                        servicesScroll.setVisibility(View.VISIBLE);
                    } else {
                        buttonView.setText("My orders");
                        servicesLayout.setVisibility(View.INVISIBLE);
                        servicesScroll.setVisibility(View.INVISIBLE);
                        ordersLayout.setVisibility(View.VISIBLE);
                        ordersScroll.setVisibility(View.VISIBLE);
                    }
                }
            });
            addServicesBtn.setOnClickListener(this);
            editProfileBtn.setOnClickListener(this);

        }else {
            //прячем функционал, мы же на чужом профиле
            servicesOrOrdersSwitch.setVisibility(View.INVISIBLE);
            addServicesBtn.setVisibility(View.INVISIBLE);
            editProfileBtn.setVisibility(View.INVISIBLE);
            // получаем id User, которому принадлжеит сервис
            userId = getUserId(serviceId);

            // подгружаем данные о пользователе
            createProfileData(userId);
            // подргужаем его сервисы
            createServicesList(userId);

            // отображаем все сервисы пользователя
            ordersLayout.setVisibility(View.INVISIBLE);
            ordersScroll.setVisibility(View.INVISIBLE);
            servicesLayout.setVisibility(View.VISIBLE);
            servicesScroll.setVisibility(View.VISIBLE);
        }
        logOutBtn.setOnClickListener(this);
        findServicesBtn.setOnClickListener(this);
        mainScreenBtn.setOnClickListener(this);
        dialogsBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addServicesProfileBtn:
                goToAddService();
                break;
            case R.id.findServicesProfileBtn:
                goToSearchService();
                break;
            case R.id.logOutProfileBtn:
                annulStatus();
                goToLogIn();
                break;
            case R.id.mainScreenProfileBtn:
                goToMainScreen();
                break;
            case R.id.editProfileBtn:
                goToEditProfile();
                break;
            case R.id.dialogsProfileBtn:
                goToDialogs();
                break;
            default:
                break;
        }
    }

    private boolean isMyService(String serviceId, String userId) {
        //значит мы зашли сразу в профиль свой и это владелец
        if(serviceId == null) return true;

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //Картеж этого сервиса с id текущего пользователя
        Cursor cursor = database.query(
                DBHelper.TABLE_CONTACTS_SERVICES,
                new String[]{DBHelper.KEY_NAME_SERVICES},
                DBHelper.KEY_ID + " = ? AND " + DBHelper.KEY_USER_ID + " = ? ",
                new String[]{serviceId, userId},
                null,
                null,
                null,
                null);

        // такой существует ?
        if(cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }
    //получаем все заказы, которые принадлежат юзеру
    private void createOrdersList(String userId){
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // получаем id сервиса, имя сервиса, дату и время всех записей
        // Из 3-х таблиц: сервисы, рабочие дни, рабочие время
        // Условие: связка таблиц по id сервиса и id рабочего дня; уточняем пользователя по id
        String sqlQuery =
                "SELECT "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_ID + ", "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_NAME_SERVICES + ", "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_DATE_WORKING_DAYS + ", "
                        + DBHelper.TABLE_WORKING_TIME + "." + DBHelper.KEY_TIME_WORKING_TIME
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_SERVICES + ", "
                        + DBHelper.TABLE_WORKING_DAYS + ", "
                        + DBHelper.TABLE_WORKING_TIME
                        + " WHERE "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_ID + " = " + DBHelper.KEY_SERVICE_ID_WORKING_DAYS
                        + " AND "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_ID + " = " + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME
                        + " AND "
                        + DBHelper.TABLE_WORKING_TIME + "." + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[] {userId});

        if(cursor.moveToFirst()){
            int indexServiceId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexServiceName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexDate = cursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);
            int indexTime = cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);

            do{
                String foundId = cursor.getString(indexServiceId);
                String foundName = cursor.getString(indexServiceName);
                String foundDate = cursor.getString(indexDate);
                String foundTime = cursor.getString(indexTime);

                addOrderToScreen(foundId, foundName, foundDate, foundTime);
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    //получаем все сервисы, которые пренадлежат юзеру
    private void createServicesList(String userId){
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Возвращает id, название, рэйтинг и количество оценивших
        // используем таблицу сервисы
        // уточняем юзера по его id
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_ID + ", "
                        + DBHelper.KEY_NAME_SERVICES + ", "
                        + DBHelper.KEY_RATING_SERVICES + ", "
                        + DBHelper.KEY_COUNT_OF_RATES_SERVICES
                        + " FROM " + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE " + DBHelper.KEY_USER_ID + " = ?";
        Cursor cursor = database.rawQuery(sqlQuery, new String[] {userId});

        if(cursor.moveToFirst()){
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexNameService = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexRatingService = cursor.getColumnIndex(DBHelper.KEY_RATING_SERVICES);
            int indexCountOfRatesService = cursor.getColumnIndex(DBHelper.KEY_COUNT_OF_RATES_SERVICES);
            do{
                String foundId = cursor.getString(indexId);
                String foundNameService = cursor.getString(indexNameService);
                String foundRatingService = cursor.getString(indexRatingService);
                String foundCountOfRatesService = cursor.getString(indexCountOfRatesService);

                Service service = new Service();
                service.setId(foundId);
                service.setName(foundNameService);
                service.setRating(foundRatingService);
                service.setCountOfRates(foundCountOfRatesService);

                addServiceToScreen(service);
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    //получить id-phone пользователя
    private String getUserId(){
        // возваращает id текущего пользователя
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        String userId = String.valueOf(sPref.getString(PHONE_NUMBER, "0"));

        return userId;
    }

    //возвращает id юзера указанного сервиса
    private String getUserId(String serviceId){
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        // нужно вернуть: userID из Users
        // используем таблицы: Services and Users
        // Условие: по заданному serviceID
        String sqlQuery =
                "SELECT "
                        + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_USER_ID
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_USERS + ", "
                        + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_ID + " = ?"
                        + " AND "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_USER_ID +
                        " = "
                        + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_USER_ID;

        Cursor cursor = database.rawQuery(sqlQuery,new String[] {serviceId});

        String userId;
        if(cursor.moveToFirst()){
            int indexUserId = cursor.getColumnIndex(DBHelper.KEY_USER_ID);
            userId = cursor.getString(indexUserId);
        }
        else {
            userId = "-"; // но такого быть не может
        }

        return userId;
    }

    // получаем данные о пользователе и отображаем в прфоиле
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createProfileData(String userId){

        SQLiteDatabase database = dbHelper.getReadableDatabase();

        //получаем имя, фамилию и город пользователя по его id
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_NAME_USERS + ", "
                        + DBHelper.KEY_CITY_USERS
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_USERS
                        + " WHERE "
                        + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery,new String[] {userId});

        if(cursor.moveToFirst()){
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_USERS);
            int indexCity = cursor . getColumnIndex(DBHelper.KEY_CITY_USERS);
            String[] names = cursor.getString(indexName).split(" ");
            for (int i=0; i<names.length; i++) {
                names[i] = names[i].substring(0, 1).toUpperCase() + names[i].substring(1);
            }
            String name = String.join(" ", names);

            String city = cursor.getString(indexCity).substring(0, 1).toUpperCase()
                    + cursor.getString(indexCity).substring(1);
            nameText.setText(name);
            cityText.setText(city);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();

        String userId = getUserId();
        String serviceId = getIntent().getStringExtra(SERVICE_ID);

        if(isMyService(serviceId,userId)){
            // если это мой сервис
            addNewOrders(userId);
            addNewServices(userId);
            createProfileData(userId);
        }
    }

    //добавляет вновь добавленный сервис (обновление serviceList)
    private void addNewServices(String userId) {
        //количество сервисов отображаемых на данный момент(старых)
        int visibleCount = servicesLayout.getChildCount();

        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // Возвращает id, название, рэйтинг и количество оценивших
        // используем таблицу сервисы
        // уточняем юзера по его id
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_ID + ", "
                        + DBHelper.KEY_NAME_SERVICES + ", "
                        + DBHelper.KEY_RATING_SERVICES + ", "
                        + DBHelper.KEY_COUNT_OF_RATES_SERVICES
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE "
                        + DBHelper.KEY_USER_ID + " = ? ";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{userId});

        //Проверка на наличие вновь добавленных сервисов
        if(cursor.getCount() > visibleCount) {
            //Идём с конца
            if(cursor.moveToLast()){
                int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
                int indexNameService = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
                int indexRatingService = cursor.getColumnIndex(DBHelper.KEY_RATING_SERVICES);
                int indexCountOfRatesService = cursor.getColumnIndex(DBHelper.KEY_COUNT_OF_RATES_SERVICES);
                int countOfNewOrders = 0;

                do{
                    String foundId = cursor.getString(indexId);
                    String foundNameService = cursor.getString(indexNameService);
                    String foundRatingService = cursor.getString(indexRatingService);
                    String foundCountOfRatesService = cursor.getString(indexCountOfRatesService);

                    Service service = new Service();
                    service.setId(foundId);
                    service.setName(foundNameService);
                    service.setRating(foundRatingService);
                    service.setCountOfRates(foundCountOfRatesService);

                    addServiceToScreen(service);

                    countOfNewOrders++;
                    //пока в курсоре есть строки и есть новые сервисы
                }while (cursor.moveToPrevious() && countOfNewOrders<(cursor.getCount() - visibleCount));
            }
        }
        cursor.close();
    }

    //добавляет вновь добавленные записи (обновление ordersList)
    private void addNewOrders(String userId) {
        // количство записей отображаемых на данный момент(старых)
        int visibleCount = ordersLayout.getChildCount();

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        // получаем id сервиса, имя сервиса, дату и время всех записей
        // Из 3-х таблиц: сервисы, рабочие дни, рабочие время
        // Условие: связка таблиц по id сервиса и id рабочего дня; уточняем пользователя по id
        String sqlQuery =
                "SELECT "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_ID + ", "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_NAME_SERVICES + ", "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_DATE_WORKING_DAYS + ", "
                        + DBHelper.TABLE_WORKING_TIME + "." + DBHelper.KEY_TIME_WORKING_TIME
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_SERVICES + ", "
                        + DBHelper.TABLE_WORKING_DAYS + ", "
                        + DBHelper.TABLE_WORKING_TIME
                        + " WHERE "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_ID + " = " + DBHelper.KEY_SERVICE_ID_WORKING_DAYS
                        + " AND "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_ID + " = " + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME
                        + " AND "
                        + DBHelper.TABLE_WORKING_TIME + "." + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[] {userId});

        //если есть новые записи
        if(cursor.getCount() > visibleCount) {
            //Идём с конца
            if(cursor.moveToLast()){
                int indexServiceId = cursor.getColumnIndex(DBHelper.KEY_ID);
                int indexServiceName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
                int indexDate = cursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);
                int indexTime = cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);

                int countOfNewOrders = 0;

                do{
                    String foundId = cursor.getString(indexServiceId);
                    String foundName = cursor.getString(indexServiceName);
                    String foundDate = cursor.getString(indexDate);
                    String foundTime = cursor.getString(indexTime);

                    addOrderToScreen(foundId, foundName, foundDate, foundTime);

                    countOfNewOrders++;
                    //пока в курсоре есть строки и есть новые записи
                }while (cursor.moveToPrevious() && countOfNewOrders<(cursor.getCount() - visibleCount));
            }
        }
        cursor.close();
    }

    private void addServiceToScreen(Service service) {
        fServiceElement = new foundServiceProfileElement(service);

        transaction = manager.beginTransaction();
        transaction.add(R.id.servicesProfileLayout, fServiceElement);
        transaction.commit();
    }

    private void addOrderToScreen(String id, String name, String date, String time) {
        fOrderElement = new foundOrderElement(id, name, date, time);

        transaction = manager.beginTransaction();
        transaction.add(R.id.ordersProfileLayout, fOrderElement);
        transaction.commit();
    }

    //Анулировать статус при выходе
    private void annulStatus() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.remove(STATUS);
        editor.apply();
    }

    private void goToLogIn() {
        Intent intent = new Intent(this, Authorization.class);
        startActivity(intent);
        finish();
    }
    private void goToAddService() {
        Intent intent = new Intent(this, AddService.class);
        startActivity(intent);
    }

    private void goToSearchService() {
        Intent intent = new Intent(this, SearchService.class);
        startActivity(intent);
    }

    private void goToMainScreen() {
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
    }

    private void goToEditProfile() {
        Intent intent = new Intent(this, EditProfile.class);
        intent.putExtra(USER_NAME, nameText.getText());
        intent.putExtra(USER_CITY, cityText.getText());
        startActivity(intent);
    }

    private void goToDialogs() {
        Intent intent = new Intent(this, Dialogs.class);
        startActivity(intent);
    }

}