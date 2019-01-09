package com.example.ideal.myapplication.other;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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
import com.example.ideal.myapplication.fragments.foundServiceElement;

import java.util.ArrayList;
import java.util.Iterator;

public class SearchService extends FragmentActivity implements View.OnClickListener {
    
    // сначала идут константы
    final String FILE_NAME = "Info";
    final String PHONE = "phone";

    String city = "Город";

    Button findBtn;

    EditText searchLineSearchServiceInput;

    //Выпадающее меню
    Spinner citySpinner;

    //Вертикальный лэйаут
    LinearLayout resultLayout;

    DBHelper dbHelper;
    SharedPreferences sPref;

    private foundServiceElement fElement;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_service);

        findBtn = findViewById(R.id.findServiceSearchServiceBtn);

        //создаём выпадающее меню на основе массива городов
        citySpinner = findViewById(R.id.citySearchServiceSpinner);
        citySpinner.setPrompt("Город");
        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.cities, android.R.layout.simple_spinner_item);
        citySpinner.setAdapter(adapter);

        searchLineSearchServiceInput = findViewById(R.id.searchLineSearchServiceInput);

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.findServiceSearchServiceBtn:
                if(!search()) {
                    resultLayout.removeAllViews();
                    Toast.makeText(this, "Ничего не найдено", Toast.LENGTH_SHORT).show();
                }
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
        getServicesInThisCity(database, userCity);
    }

    // Получает id пользователя
    private String getUserId() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String userId = sPref.getString(PHONE, "-");

        return userId;
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

    private void getServicesInThisCity(SQLiteDatabase database, String userCity) {
        //максимальное количество выводимых предложений
        int limitOfService = 10;

        //вернуть имя юзера, фамилию, город, имя сервиса, цену сервиса, id сервиса
        //таблицы: Services & Users
        //Условия: связь таблиц по id юзера; уточняем по городу
        String sqlQuery =
                "SELECT " + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_NAME_USERS + ", "
                        + DBHelper.KEY_SURNAME_USERS + ", "
                        + DBHelper.KEY_CITY_USERS+ ", "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_NAME_SERVICES + ", "
                        + DBHelper.KEY_MIN_COST_SERVICES + ", "
                        + DBHelper.KEY_ID
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_SERVICES + ", "
                        + DBHelper.TABLE_CONTACTS_USERS
                        + " WHERE "
                        + "LOWER(" + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_CITY_USERS + ") = ?"
                        + " AND "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_USER_ID +
                        " = "
                        + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_USER_ID;

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{userCity.toLowerCase()});

        if (cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            int indexNameUser = cursor.getColumnIndex(DBHelper.KEY_NAME_USERS);
            int indexSurname = cursor.getColumnIndex(DBHelper.KEY_SURNAME_USERS);
            int indexCity = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);

            int indexNameService = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);
            int countOfFoundServices = 0;
            do {
                String foundId = cursor.getString(indexId);
                String foundNameUser = cursor.getString(indexNameUser);
                String foundSurname = cursor.getString(indexSurname);
                String foundCity = cursor.getString(indexCity);
                String foundNameService = cursor.getString(indexNameService);
                String foundCost = cursor.getString(indexMinCost);

                addToScreen(foundId, foundNameUser, foundSurname, foundCity, foundNameService, foundCost);

                countOfFoundServices++;
            } while (cursor.moveToNext() && countOfFoundServices < limitOfService);
        }
        cursor.close();
    }

    private boolean search() {
        // Массив введёных поисковых слов
        String[] enteredWords = searchLineSearchServiceInput.getText().toString().toLowerCase().split(" ");

        // Массив id сервисов которые являются результатом всего поиска
        ArrayList<Long> resultId = new ArrayList<>();

        // Массив id сервисов которые являются результатом поиска по текущему слову
        ArrayList<Long> foundId = new ArrayList<>();

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor;

        String cityCondition = "";
        boolean isCitySelected = !city.equals("Город");
        //Проверка выбран ли город
        if(isCitySelected) {
            //Город выбран
            //Добавляем ещё одно условие
            cityCondition = " AND " + DBHelper.KEY_CITY_USERS + " = ?";
        }

        // Вернуть id сервиса
        // Таблицы: Services & Users
        // Условие: связываем таблицы по id юзера; дополнительное условие на город; уточняет по имени сервиса ИЛИ имени юзера ИЛИ фамилии
        String sqlQuery =
                "SELECT "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_ID
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_SERVICES + ", "
                        + DBHelper.TABLE_CONTACTS_USERS
                        + " WHERE "
                        + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_USER_ID + " = "
                        + DBHelper.TABLE_CONTACTS_SERVICES +"." + DBHelper.KEY_USER_ID
                        + cityCondition
                        + " AND ("
                        + DBHelper.KEY_NAME_SERVICES + " = ?"
                        + " OR "
                        + DBHelper.KEY_NAME_USERS + " = ?"
                        + " OR "
                        + DBHelper.KEY_SURNAME_USERS + " = ?)";

        //Проход по всем введённым поисковым словам
        for(String word : enteredWords) {
            // Проверка выбран ли город
            if(isCitySelected) {
                // Город выбран
                // Вводим параметр для дополнительного условия
                cursor = database.rawQuery(sqlQuery,new String[] {city, word, word, word});
            } else {
                cursor = database.rawQuery(sqlQuery,new String[] {word, word, word});
            }


            if(cursor.moveToFirst()) {
                int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);

                // Заполняем массив найденными id сервисов
                do {
                    long id = cursor.getInt(indexId);
                    if(!foundId.contains(id)) {
                        foundId.add(id);
                    }
                } while (cursor.moveToNext());
            }


            // Проверка найдено ли что-то
            if(foundId.isEmpty()) {
                // Не найдено
                return false;
            } else {
                // Найдено
                // Проверка это первое поисковое слово
                if(resultId.isEmpty()) {
                    // Данное поисковое слово первое
                    for(long id : foundId) {
                        // Добавляем все найденные id в конечный массив
                        resultId.add(id);
                    }
                } else {
                    // Данное поисковое слово не первое
                    // Проверяем все id из конечного массива
                    for(Iterator<Long> it = resultId.iterator(); it.hasNext(); ) {
                        long id = it.next();
                        // Проверка найден ли данный id по новому слову
                        if(!foundId.contains(id)) {
                            // Не найден
                            // Удаляем данный id из конечного массива
                            it.remove();

                            // Проверка конечный массив пуст?
                            if(resultId.isEmpty()) {
                                //Массив пуст
                                return false;
                            }
                        }
                    }
                }
                // Очищаем массив найденных id для следующего слова
                foundId.clear();
            }
        }

        //вернуть имя и фамилию юзера, город, имя сервиса, цену
        //таблицы Services, Users
        //Условия связь таблиц по id юзера; уточняем id сервиса
        sqlQuery =
                "SELECT "
                        + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_NAME_USERS + ", "
                        + DBHelper.KEY_SURNAME_USERS + ", "
                        + DBHelper.KEY_CITY_USERS+ ", "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_NAME_SERVICES + ", "
                        + DBHelper.KEY_MIN_COST_SERVICES
                        + " FROM "
                        + DBHelper.TABLE_CONTACTS_SERVICES + ", "
                        + DBHelper.TABLE_CONTACTS_USERS
                        + " WHERE "
                        + DBHelper.KEY_ID + " = ?"
                        + " AND "
                        + DBHelper.TABLE_CONTACTS_SERVICES + "." + DBHelper.KEY_USER_ID + " = " + DBHelper.TABLE_CONTACTS_USERS + "." + DBHelper.KEY_USER_ID;

        int indexNameUser, indexSurname, indexCity, indexNameService, indexMinCost;
        // Обработка конечного массива с id сервисов
        for(long id : resultId) {
            cursor = database.rawQuery(sqlQuery, new String[] {String.valueOf(id)});

            if (cursor.moveToFirst()) {
                indexNameUser = cursor.getColumnIndex(DBHelper.KEY_NAME_USERS);
                indexSurname = cursor.getColumnIndex(DBHelper.KEY_SURNAME_USERS);
                indexCity = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);

                indexNameService = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
                indexMinCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);

                String foundNameUser = cursor.getString(indexNameUser);
                String foundSurname = cursor.getString(indexSurname);
                String foundCity = cursor.getString(indexCity);
                String foundNameService = cursor.getString(indexNameService);
                String foundCost = cursor.getString(indexMinCost);

                addToScreen(String.valueOf(id), foundNameUser, foundSurname, foundCity, foundNameService, foundCost);
            }
        }
        return true;
    }

    // Вывод фрагмента сервиса на экран
    private void addToScreen(String id, String foundNameUser, String foundSurname, String foundCity,
                             String foundNameService, String foundCost ) {
        resultLayout.removeAllViews();

        fElement = new foundServiceElement(id, foundNameUser, foundSurname, foundCity, foundNameService, foundCost);

        transaction = manager.beginTransaction();
        transaction.add(R.id.resultSearchServiceLayout, fElement);
        transaction.commit();
    }
}