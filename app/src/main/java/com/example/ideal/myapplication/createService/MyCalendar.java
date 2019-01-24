package com.example.ideal.myapplication.createService;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.helpApi.WorkWithTimeApi;
import com.example.ideal.myapplication.other.DBHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MyCalendar extends AppCompatActivity implements View.OnClickListener {

    private static final String FILE_NAME = "Info";
    private static final String PHONE_NUMBER = "Phone number";
    private static final String REF = "working days";

    private static final String SERVICE_ID = "service id";
    private static final String WORKING_DAYS_ID = "working day id";
    private static final String STATUS_USER_BY_SERVICE = "status User";
    private static final String DATE = "data";
    private static final String USER = "User";
    private static final String WORKER = "worker";

    private static final int WEEKS_COUNT = 4;
    private static final int DAYS_COUNT = 7;



    String statusUser;
    String date;
    String serviceId;

    Button[][] dayBtns;
    Button nextBtn;
    WorkWithTimeApi workWithTimeApi;

    RelativeLayout mainLayout;

    DBHelper dbHelper;
    SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_calendar);
        mainLayout = findViewById(R.id.mainMyCalendarLayout);
        nextBtn = findViewById(R.id.continueMyCalendarBtn2);
        dayBtns = new Button[WEEKS_COUNT][DAYS_COUNT];

        dbHelper = new DBHelper(this);
        workWithTimeApi = new WorkWithTimeApi();

        // получаем статус, чтобы определить, кто зашел, worker or User
        statusUser = getIntent().getStringExtra(STATUS_USER_BY_SERVICE);
        serviceId = getIntent().getStringExtra(SERVICE_ID);

        // создаём календарь
        createCalendar();

        // проверяем имеется ли у данного пользователя запись на данную услугу
        if(statusUser.equals(USER)){
            checkOrder();
        }
        else {
            //если worker
            selectWorkingDayWithTime();
        }
        nextBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.continueMyCalendarBtn2:
                if(statusUser.equals(WORKER)){
                    if(isDaySelected()) {
                        addWorkingDay();
                    } else {
                        Toast.makeText(this, "Выбирите дату, на которую хотите настроить расписание", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    if(isDaySelected()) {
                        goToMyTime(checkCurrentDay(date), statusUser);
                    } else {
                        Toast.makeText(this, "Выбирите дату, на которую хотите записаться", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            default:
                Button btn = (Button) v;
                // Проверка былв ли кнопка выбрана
                if (!Boolean.valueOf(btn.getTag(R.string.selectedId).toString())) {
                    // На была выбрана - фокусируемся на ней
                    btn.setBackgroundResource(R.drawable.selected_day_button);
                    for (int i = 0; i < WEEKS_COUNT; i++) {
                        for (int j = 0; j < DAYS_COUNT; j++) {
                            if(Boolean.valueOf(dayBtns[i][j].getTag(R.string.selectedId).toString())) {
                                dayBtns[i][j].setTag(R.string.selectedId, false);
                                dayBtns[i][j].setBackgroundResource(R.drawable.day_button);
                                break;
                            }
                        }
                    }
                    date = convertDate(btn.getText().toString(), btn.getTag(R.string.yearId).toString());
                    btn.setTag(R.string.selectedId, true);
                } else {
                    // Была выбрана - снимаем выделение
                    btn.setTag(R.string.selectedId, false);
                    btn.setBackgroundResource(R.drawable.day_button);
                }
                break;
        }
    }

    //Выделяет рабочие дни
    private void selectWorkingDayWithTime() {
        String dayAndMonth, year;
        String dayId;
        int dayWithTimesColor = ContextCompat.getColor(this, R.color.dayWithTimes);
        int dayWithoutTimesColor = ContextCompat.getColor(this, R.color.black);
        for (int i = 0; i < WEEKS_COUNT; i++) {
            for (int j = 0; j < DAYS_COUNT; j++) {
                dayAndMonth = dayBtns[i][j].getText().toString();
                year = dayBtns[i][j].getTag(R.string.yearId).toString();

                dayId = checkCurrentDay(convertDate(dayAndMonth, year));
                if (!dayId.equals("0") ) {
                    if (hasSomeWork(dayId)) {
                        dayBtns[i][j].setTextColor(dayWithTimesColor);
                    }
                    else {
                        dayBtns[i][j].setTextColor(dayWithoutTimesColor);
                    }
                }
            }
        }
    }

    private boolean hasSomeWork(String dayId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // Получает id рабочего дня
        // Таблицы: рабочие время
        // Условия: уточняем id рабочего дня
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_ID
                        + " FROM "
                        + DBHelper.TABLE_WORKING_TIME
                        + " WHERE "
                        + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + " = ? ";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{dayId});

        if(cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    // проверяет имеется ли у данного пользователя запись на данную услугу
    private void checkOrder(){
        //Если пользователь записан на какой-то день выделить только его
        date = getOrderDate();
        if(!date.equals("")) {
            String[] arrDate = date.split("-");
            String orderDate = arrDate[0] + " " + monthToString(Integer.valueOf(arrDate[1]));

            for (int i = 0; i < WEEKS_COUNT; i++) {
                for (int j = 0; j < DAYS_COUNT; j++) {
                    if(orderDate.equals(dayBtns[i][j].getText().toString()) && arrDate[2].equals(dayBtns[i][j].getTag(R.string.yearId).toString())) {
                        dayBtns[i][j].setBackgroundResource(R.drawable.selected_day_button);
                        dayBtns[i][j].setTag(R.string.selectedId, true);
                    } else {
                        dayBtns[i][j].setTag(R.string.selectedId, false);
                        dayBtns[i][j].setEnabled(false);
                        dayBtns[i][j].setBackgroundResource(R.drawable.disabled_button);
                    }
                }
            }
        } else {
            // Если записи на данный сервис нет, отключаем всё нерабочие дни
            String dayAndMonth, year;
            String dayId;

            for (int i = 0; i < WEEKS_COUNT; i++) {
                for (int j = 0; j < DAYS_COUNT; j++) {
                    dayAndMonth = dayBtns[i][j].getText().toString();
                    year = dayBtns[i][j].getTag(R.string.yearId).toString();

                    dayId = checkCurrentDay(convertDate(dayAndMonth, year));
                    if (dayId.equals("0") ) {
                        dayBtns[i][j].setEnabled(false);
                        dayBtns[i][j].setBackgroundResource(R.drawable.disabled_button);
                    } else {
                        if (!hasSomeTime(dayId)) {
                            dayBtns[i][j].setEnabled(false);
                            dayBtns[i][j].setBackgroundResource(R.drawable.disabled_button);
                        }
                    }
                }
            }
        }
    }

    //Возвращает дату записи
    private String getOrderDate() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String userId = getUserId();
        // Получает дату записи
        // Таблицы: рабочии дни, рабочие время
        // Условия: связываем таблицы по id рабочего дня; уточняем id сервиса и id пользователя
        String sqlQuery =
                "SELECT " + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_DATE_WORKING_DAYS
                        + " FROM " + DBHelper.TABLE_WORKING_TIME + ", " + DBHelper.TABLE_WORKING_DAYS
                        + " WHERE " + DBHelper.KEY_SERVICE_ID_WORKING_DAYS + " = ? AND "
                        + DBHelper.KEY_USER_ID + " = ? "
                        + " AND "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_ID + " = " + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME;

        Cursor cursor = database.rawQuery(sqlQuery, new String[] {String.valueOf(serviceId), userId});

        if(cursor.moveToFirst()) {
            int indexDate = cursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);
            String orderDate = cursor.getString(indexDate);
            cursor.close();
            return orderDate;
        } else {
            cursor.close();
            return "";
        }
    }

    private void createCalendar() {
        Calendar calendar = Calendar.getInstance();

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        int dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK)+5)%DAYS_COUNT;
        int dayOfMonth, month, year;
        String stringMonth;

        //Создание календаря
        calendar.add(Calendar.DATE, -dayOfWeek);
        for(int i = 0; i < WEEKS_COUNT; i++) {
            for (int j = 0; j < DAYS_COUNT; j++) {
                dayBtns[i][j] = new Button(this);

                //положение, бэкграунд, размеры
                dayBtns[i][j].setX(j * width / DAYS_COUNT);
                dayBtns[i][j].setY(i * height / (2*WEEKS_COUNT));
                dayBtns[i][j].setBackgroundResource(R.drawable.day_button);
                dayBtns[i][j].setLayoutParams(new ViewGroup.LayoutParams(width / DAYS_COUNT-5, height / (2*WEEKS_COUNT)-35));

                //тэги
                dayBtns[i][j].setTag(R.string.selectedId, false);
                year = calendar.get(Calendar.YEAR);
                dayBtns[i][j].setTag(R.string.yearId, year);

                //надпись
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                month = calendar.get(Calendar.MONTH)+1;
                stringMonth = monthToString(month);
                dayBtns[i][j].setTextSize(11);
                dayBtns[i][j].setText(dayOfMonth + " " + stringMonth);


                //отрисовываем прошедше дни
                if ((j < dayOfWeek) && (i == 0)) {
                    dayBtns[i][j].setEnabled(false);
                    dayBtns[i][j].setBackgroundResource(R.drawable.disabled_button);
                } else {
                    dayBtns[i][j].setOnClickListener(this);
                }

                if (dayBtns[i][j].getParent() != null) {
                    ((ViewGroup) dayBtns[i][j].getParent()).removeView(dayBtns[i][j]);
                }
                mainLayout.addView(dayBtns[i][j]);
                calendar.add(Calendar.DATE, 1);
            }
        }
    }

    // Возвращает выбран ли како-либо день
    private boolean isDaySelected() {
        for (int i = 0; i < WEEKS_COUNT; i++) {
            for (int j = 0; j < DAYS_COUNT; j++) {
                if(dayBtns[i][j].isEnabled()) {
                    if(Boolean.valueOf(dayBtns[i][j].getTag(R.string.selectedId).toString())) {
                        date = convertDate(dayBtns[i][j].getText().toString(), dayBtns[i][j].getTag(R.string.yearId).toString());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Возвращает есть ли в рабочем дне рабочие часы
    private boolean hasSomeTime(String dayId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // Получает id рабочего дня
        // Таблицы: рабочие время
        // Условия: уточняем id рабочего дня
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_TIME_WORKING_TIME + ", "
                        + DBHelper.KEY_DATE_WORKING_DAYS
                        + " FROM "
                        + DBHelper.TABLE_WORKING_TIME + ", "
                        + DBHelper.TABLE_WORKING_DAYS
                        + " WHERE "
                        + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + " = "
                        + DBHelper.TABLE_WORKING_DAYS + "." + DBHelper.KEY_ID
                        + " AND "
                        + DBHelper.KEY_WORKING_DAYS_ID_WORKING_TIME + " = ? "
                        + " AND "
                        + DBHelper.KEY_USER_ID + " = 0";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{dayId});

        if(cursor.moveToFirst()) {
            int indexDate = cursor.getColumnIndex(DBHelper.KEY_DATE_WORKING_DAYS);
            int indexTime = cursor.getColumnIndex(DBHelper.KEY_TIME_WORKING_TIME);
            String date, time;

            do {
                date = cursor.getString(indexDate);
                time = cursor.getString(indexTime);
                if(hasMoreThenTwoHours(date, time)) {
                    cursor.close();
                    return true;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return false;
    }

    private boolean hasMoreThenTwoHours(String date, String time) {
        long twoHours = 2*60*60*1000;

        long sysdateLong = workWithTimeApi.getSysdateLong() ;
        long currentLong = workWithTimeApi.getMillisecondsStringDate(date + " " + time);

        return currentLong - sysdateLong >= twoHours;
    }

    // Преобразует дату в формат БД
    private String convertDate(String dayAndMonth, String year) {
        String[] arrDate = dayAndMonth.split(" ");
        int month = monthToInt(arrDate[1]);

        String convertedDate = arrDate[0] + "-" + month + "-" + year;

        return convertedDate;
    }

    // Добавляе рабочий день в БД
    private void addWorkingDay() {
        String id = checkCurrentDay(date);

        // Проверка на существование такого дня
        if(!id.equals("0") ){
            goToMyTime(id,statusUser);
        } else {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(REF);

            Map<String,Object> items = new HashMap<>();
            items.put(DATE,date);
            items.put(SERVICE_ID, serviceId);

            Object dayId =  myRef.push().getKey();
            myRef = database.getReference(REF).child(String.valueOf(dayId));
            myRef.updateChildren(items);

            putDataInLocalStorage(serviceId, String.valueOf(dayId));

        }
    }

    private void putDataInLocalStorage(String serviceId, String dayId) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_ID, dayId);
        contentValues.put(DBHelper.KEY_DATE_WORKING_DAYS, date);
        contentValues.put(DBHelper.KEY_SERVICE_ID_WORKING_DAYS, serviceId);

        database.insert(DBHelper.TABLE_WORKING_DAYS, null, contentValues);

        goToMyTime(dayId,statusUser);
    }

    //Возвращает id дня по id данного сервиса и дате
    private String checkCurrentDay(String day) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Получает id рабочего дня
        // Таблицы: рабочии дни
        // Условия: уточняем id сервиса и дату
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_ID
                        + " FROM "
                        + DBHelper.TABLE_WORKING_DAYS
                        + " WHERE "
                        + DBHelper.KEY_SERVICE_ID_WORKING_DAYS + " = ? AND "
                        + DBHelper.KEY_DATE_WORKING_DAYS + " = ? ";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{String.valueOf(serviceId), day});

        if(cursor.moveToFirst()) {
            int indexId = cursor.getColumnIndex(DBHelper.KEY_ID);
            return String.valueOf(cursor.getString(indexId));
        }
        cursor.close();
        return "0";
    }

    private  String getUserId(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        return sPref.getString(PHONE_NUMBER, "-");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(statusUser.equals(USER)){
            checkOrder();
        }
        else {
            selectWorkingDayWithTime();
        }
    }

    private void goToMyTime(String dayId, String statusUser){

        Intent intent = new Intent(this, MyTime.class);
        intent.putExtra(WORKING_DAYS_ID, dayId);
        intent.putExtra(STATUS_USER_BY_SERVICE, statusUser);

        startActivity(intent);
    }

    private String monthToString(int month) {
        switch (month) {
            case 1:
                return "янв";
            case 2:
                return "фев";
            case 3:
                return "мар";
            case 4:
                return "апр";
            case 5:
                return "май";
            case 6:
                return "июнь";
            case 7:
                return "июль";
            case 8:
                return "авг";
            case 9:
                return "сен";
            case 10:
                return "окт";
            case 11:
                return "ноя";
            case 12:
                return "дек";
        }

        return "";
    }

    private int monthToInt(String month) {
        switch (month) {
            case "янв":
                return 1;
            case "фев":
                return 2;
            case "мар":
                return 3;
            case "апр":
                return 4;
            case "май":
                return 5;
            case "июнь":
                return 6;
            case "июль":
                return 7;
            case "авг":
                return 8;
            case "сен":
                return 9;
            case "окт":
                return 10;
            case "ноя":
                return 11;
            case "дек":
                return 12;
        }
        return -1;
    }
}
