package com.example.ideal.myapplication.other;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.fragments.objects.Service;
import com.example.ideal.myapplication.fragments.objects.User;
import com.example.ideal.myapplication.fragments.foundElements.foundServiceElement;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SearchService extends FragmentActivity implements View.OnClickListener {

    // сначала идут константы
    private static final String FILE_NAME = "Info";
    private static final String PHONE = "phone";
    private static final String TAG = "DBInf";

    private static final String USERS = "users";
    private static final String NAME = "name";
    private static final String CITY = "city";

    private static final String SERVICES = "services";
    private static final String USER_ID = "user id";
    private static final String DESCRIPTION = "description";
    private static final String COST = "cost";
    private static final String RATING = "rating";
    private static final String COUNT_OF_RATES = "count of rates";

    String city = "Город";
    String searchBy = "название сервиса";
    int countOfService = 0;
    long numberOfService;
    long numberOfUser;

    Button findBtn;

    EditText searchLineInput;

    //Выпадающее меню
    Spinner citySpinner;
    Spinner searchBySpinner;

    //Вертикальный лэйаут
    LinearLayout resultLayout;

    DBHelper dbHelper;
    SharedPreferences sPref;

    private FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_service);

        findBtn = findViewById(R.id.findServiceSearchServiceBtn);

        //создаём выпадающее меню на основе массива городов
        citySpinner = findViewById(R.id.citySearchServiceSpinner);
        citySpinner.setPrompt("Город");
        ArrayAdapter<?> cityAdapter = ArrayAdapter.createFromResource(this, R.array.cities, android.R.layout.simple_spinner_item);
        citySpinner.setAdapter(cityAdapter);

        searchBySpinner = findViewById(R.id.searchBySearchServiceSpinner);
        searchBySpinner.setPrompt("название сервиса");
        ArrayAdapter<?> searchByAdapter = ArrayAdapter.createFromResource(this, R.array.searchBy, android.R.layout.simple_spinner_item);
        searchBySpinner.setAdapter(searchByAdapter);

        searchLineInput = findViewById(R.id.searchLineSearchServiceInput);

        resultLayout = findViewById(R.id.resultSearchServiceLayout);

        dbHelper = new DBHelper(this);
        manager = getSupportFragmentManager();

        showServicesInHomeTown();

        findBtn.setOnClickListener(this);

        //отслеживаем смену городов в выпадающем меню
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                TextView cityText = (TextView)itemSelected;
                city = cityText.getText().toString();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        searchBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                TextView searchByText = (TextView)itemSelected;
                searchBy = searchByText.getText().toString();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.findServiceSearchServiceBtn:
                resultLayout.removeAllViews();
                search();
                break;
            default:
                break;
        }
    }

    private void showServicesInHomeTown() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        //получаем id пользователя
        String userId = getUserId();

        //получаем город юзера
        String userCity = getUserCity(database, userId);

        //получаем все сервисы, которые находятся в городе юзера
        getServicesInThisCity(userCity);
    }

    // Получает id пользователя
    private String getUserId() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);

        return sPref.getString(PHONE, "-");
    }

    //Получает город пользователя
    private String getUserCity(SQLiteDatabase database, String userId) {

        // Получить город юзера
        // Таблица Users
        // уточняем по id юзера
        String sqlQuery =
                "SELECT " + DBHelper.KEY_CITY_USERS
                        + " FROM " + DBHelper.TABLE_CONTACTS_USERS
                        + " WHERE " + DBHelper.KEY_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[] {userId});

        int indexCity = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);

        String city = "dubna"; // дефолтное значение
        if (cursor.moveToFirst()) {
            city = cursor.getString(indexCity);
        }
        cursor.close();
        return city;
    }

    private void getServicesInThisCity(final String userCity) {
        final int limitOfService = 3;

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

                    Query serviseQuery = FirebaseDatabase.getInstance().getReference(SERVICES)
                            .orderByChild(USER_ID)
                            .equalTo(userId);

                    serviseQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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
                            attentionBadConnection();
                        }
                    });

                    if(countOfService == limitOfService) {
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });

        countOfService = 0;
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

    private void search() {

        switch (searchBy) {
            case "название сервиса":
                searchByNameService();
                break;

            case "имя и фамилия":
                searchByWorkerName();
                break;
        }

    }

    private void searchByNameService() {
        final String enteredText = searchLineInput.getText().toString().toLowerCase();
        Query serviceQuery = FirebaseDatabase.getInstance().getReference(SERVICES)
                .orderByChild(NAME)
                .equalTo(enteredText);
        serviceQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final long servicesCount;
                if(dataSnapshot.getValue() == null) {
                    attentionNothingFound();
                    return;
                } else {
                    servicesCount = dataSnapshot.getChildrenCount();
                    numberOfService = 0;
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String serviceId = snapshot.getKey();
                    String serviceUserId = String.valueOf(snapshot.child(USER_ID).getValue());
                    String serviceCost = String.valueOf(snapshot.child(COST).getValue());
                    String serviceDescription = String.valueOf(snapshot.child(DESCRIPTION).getValue());
                    String serviceRating = String.valueOf(snapshot.child(RATING).getValue());
                    String serviceCountOfRates = String.valueOf(snapshot.child(COUNT_OF_RATES).getValue());

                    final Service service = new Service();
                    service.setId(serviceId);
                    service.setName(enteredText);
                    service.setUserId(serviceUserId);
                    service.setCost(serviceCost);
                    service.setDescription(serviceDescription);
                    service.setRating(serviceRating);
                    service.setCountOfRates(serviceCountOfRates);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USERS).child(serviceUserId);
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String userCity = String.valueOf(dataSnapshot.child(CITY).getValue());
                            if (!city.equals("Город")) {
                                if (!userCity.equals(city)) {
                                    numberOfService++;
                                    if(numberOfService == servicesCount) {
                                        attentionNothingFound();
                                    }
                                    return;
                                }
                            }

                            String userName = String.valueOf(dataSnapshot.child(NAME).getValue());

                            User user = new User();
                            user.setName(userName);
                            user.setCity(userCity);

                            updateServicesInLocalStorage(service);
                            addToScreen(service, user);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            attentionBadConnection();
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    private void searchByWorkerName() {
        String enteredText = searchLineInput.getText().toString().toLowerCase();
        Query userQuery = FirebaseDatabase.getInstance().getReference(USERS)
                .orderByChild(NAME)
                .equalTo(enteredText);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final long usersCount;
                if(dataSnapshot.getValue() == null) {
                    attentionNothingFound();
                    return;
                } else {
                    usersCount = dataSnapshot.getChildrenCount();
                    numberOfUser = 0;
                }

                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userCity = String.valueOf(snapshot.child(CITY).getValue());
                    if (!city.equals("Город")) {
                        if (!userCity.equals(city)) {
                            numberOfUser++;
                            if(numberOfUser == usersCount) {
                                attentionNothingFound();
                            }
                            return;
                        }
                    }

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
                            if(dataSnapshot.getValue() == null) {
                                numberOfUser++;
                                if(numberOfUser == usersCount) {
                                    attentionNothingFound();
                                }
                            }

                            for(DataSnapshot serviceSnapshot:dataSnapshot.getChildren()) {
                                String serviceId = serviceSnapshot.getKey();
                                String serviceName = String.valueOf(serviceSnapshot.child(NAME).getValue());
                                String serviceCost = String.valueOf(serviceSnapshot.child(COST).getValue());
                                String serviceDescription = String.valueOf(serviceSnapshot.child(DESCRIPTION).getValue());
                                String serviceRating = String.valueOf(serviceSnapshot.child(RATING).getValue());
                                String serviceCountOfRates = String.valueOf(serviceSnapshot.child(COUNT_OF_RATES).getValue());

                                final Service service = new Service();
                                service.setId(serviceId);
                                service.setName(serviceName);
                                service.setUserId(userId);
                                service.setCost(serviceCost);
                                service.setDescription(serviceDescription);
                                service.setRating(serviceRating);
                                service.setCountOfRates(serviceCountOfRates);

                                updateServicesInLocalStorage(service);
                                addToScreen(service, user);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            attentionBadConnection();
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                attentionBadConnection();
            }
        });
    }

    // Вывод фрагмента сервиса на экран
    private void addToScreen(Service service, User user) {
        foundServiceElement fElement = new foundServiceElement(service, user);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.resultSearchServiceLayout, fElement);
        transaction.commit();
    }

    private void attentionNothingFound() {
        Toast.makeText(this, "Ничего не найдено", Toast.LENGTH_SHORT).show();
    }

    private void attentionBadConnection() {
        Toast.makeText(this,"Плохое соединение",Toast.LENGTH_SHORT).show();
    }
}