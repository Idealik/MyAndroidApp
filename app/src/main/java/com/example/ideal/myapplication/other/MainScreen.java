package com.example.ideal.myapplication.other;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.fragments.Service;
import com.example.ideal.myapplication.fragments.User;
import com.example.ideal.myapplication.fragments.foundServiceElement;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainScreen extends AppCompatActivity {

    // добавить, чтобы не было видно своих сервисов
    // например номер юзера, возвращаемого сервиса не должен быть равен локальному
    private static final String TAG = "DBInf";
    private static final String FILE_NAME = "Info";
    private static final String PHONE_NUMBER = "Phone number";

    private static final String USERS = "users";
    private static final String NAME = "name";
    private static final String CITY = "city";

    private static final String SERVICES = "services";
    private static final String USER_ID = "user id";
    private static final String DESCRIPTION = "description";
    private static final String COST = "cost";
    private static final String RATING = "rating";
    private static final String COUNT_OF_RATES = "count of rates";

    int countOfService = 0;

    DBHelper dbHelper;
    SharedPreferences sPref;

    LinearLayout resultLayout;

    private foundServiceElement fElement;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        resultLayout = findViewById(R.id.resultsMainScreenLayout);

        manager = getSupportFragmentManager();

        dbHelper = new DBHelper(this);

        createMainScreen();
    }

    private void createMainScreen(){
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //получаем id пользователя
        String userId = getUserId();

        //получаем город юзера
        String userCity = getUserCity(database,userId);

        //получаем все сервисы, которые находятся в городе юзера
        getServicesInThisCity(userCity);
    }

    private  String getUserId(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        String userId = sPref.getString(PHONE_NUMBER, "-");

        return userId;
    }

    private String getUserCity(SQLiteDatabase database,String userId){
        // Получить город юзера
        // Таблица Users
        // с фиксированным userId
        String sqlQuery =
                "SELECT " + DBHelper.KEY_CITY_USERS
                        + " FROM " + DBHelper.TABLE_CONTACTS_USERS
                        + " WHERE " + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[] {userId});

        int indexCity = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);
        // дефолтное значение
        String city="Dubna";

        if(cursor.moveToFirst()) {
            city = cursor.getString(indexCity);
        }
        cursor.close();
        return city;
    }

    private  void getServicesInThisCity(final String userCity){

        final int limitOfService = 6;
        //возвращение всех пользователей из контретного города
        Query userQuery = FirebaseDatabase.getInstance().getReference(USERS)
                .orderByChild(CITY)
                .equalTo(userCity);

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()) {
                    String userName = String.valueOf(snapshot.child(NAME).getValue());
                    final String userId = snapshot.getKey();

                    final User user = new User();
                    user.setName(userName);
                    user.setCity(userCity);

                    Query serviceQuery = FirebaseDatabase.getInstance().getReference(SERVICES)
                            .orderByChild(USER_ID)
                            .equalTo(userId);

                    serviceQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String serviceId = snapshot.getKey();
                                String serviceName = String.valueOf(snapshot.child(NAME).getValue());
                                String serviceCost = String.valueOf(snapshot.child(COST).getValue());
                                String serviceDescription = String.valueOf(snapshot.child(DESCRIPTION).getValue());
                                String serviceRating = String.valueOf(snapshot.child(RATING).getValue());
                                String serviceCountOfRates = String.valueOf(snapshot.child(COUNT_OF_RATES).getValue());

                                Service service = new Service();
                                service.setId(serviceId);
                                service.setName(serviceName);
                                service.setUserId(userId);
                                service.setCost(serviceCost);
                                service.setDescription(serviceDescription);
                                service.setRating(serviceRating);
                                service.setCountOfRates(serviceCountOfRates);

                                updateServicesInLocalStorage(service);
                                addToScreen(service, user);
                                countOfService++;
                                if(countOfService == limitOfService) {
                                    return;
                                }
                            }
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d(TAG, "onCancelled");
                        }
                    });
                    if(countOfService == limitOfService) {
                        return;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled");
            }
        });

        countOfService = 0;
    }

    private void addToScreen(Service service, User user) {
        fElement = new foundServiceElement(service, user);

        transaction = manager.beginTransaction();
        transaction.add(R.id.resultsMainScreenLayout, fElement);
        transaction.commit();
    }

    // Добавляет информацию о сервисах в SQLite
    private void updateServicesInLocalStorage(Service service) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String serviceId = service.getId();

        // Данные из тыблицы Service
        // По номеру id
        String sqlQuery =
                "SELECT * "
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_SERVICES
                        + " WHERE "
                        + DBHelper.KEY_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{serviceId});

        // Заполняем contentValues информацией о данном сервисе
        contentValues.put(DBHelper.KEY_NAME_SERVICES, service.getName());
        contentValues.put(DBHelper.KEY_USER_ID, service.getUserId());
        contentValues.put(DBHelper.KEY_DESCRIPTION_SERVICES, service.getDescription());
        contentValues.put(DBHelper.KEY_MIN_COST_SERVICES, service.getCost());
        contentValues.put(DBHelper.KEY_RATING_SERVICES, service.getRating());
        contentValues.put(DBHelper.KEY_COUNT_OF_RATES_SERVICES, service.getCountOfRates());

        // Проверка есть ли такой сервис в SQLite
        if(cursor.moveToFirst()) {
            // Данный сервис уже есть
            // Обновляем информацию о нём
            database.update(
                    DBHelper.TABLE_CONTACTS_SERVICES,
                    contentValues,
                    DBHelper.KEY_ID + " = ?",
                    new String[]{serviceId});
        } else {
            // Данного сервиса нет
            // Добавляем serviceId в contentValues
            contentValues.put(DBHelper.KEY_ID, serviceId);

            // Добавляем данный сервис в SQLite
            database.insert(DBHelper.TABLE_CONTACTS_SERVICES, null, contentValues);
        }
        cursor.close();
    }

}