package com.example.ideal.myapplication.reviews;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.helpApi.WorkWithTimeApi;
import com.example.ideal.myapplication.other.DBHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Review extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DBInf";

    private static final String PHONE_NUMBER = "Phone number";

    private static final String REVIEWS_FOR_SERVICE = "reviews for service";
    private static final String REVIEWS_FOR_USER = "reviews for user";
    private static final String SERVICE_ID = "service id";
    private static final String VALUING_PHONE = "valuing phone";
    private static final String ESTIMATED_PHONE = "estimated phone";
    private static final String REVIEW = "review";
    private static final String RATING = "rating";
    private static final String MESSAGE_REVIEWS = "message reviews";
    private static final String MESSAGE_ID = "message id";
    private static final String MESSAGE_TIME = "message time";


    private static final String FILE_NAME = "Info";

    private static final String STATUS_USER_BY_RATE = "user status";

    private static final String IS_RATE_BY_USER = "is rate by user" ;
    private static final String IS_RATE_BY_WORKER = "is rate by worker" ;


    private float myRating;
    private String serviceId;
    private boolean isUser;
    String dateNow;

    private EditText reviewInput;

    private RatingBar ratingBar;
    private DBHelper dbHelper;
    String messageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review);

        Button rateReviewBtn = findViewById(R.id.rateReviewBtn);
        reviewInput = findViewById(R.id.reviewReviewInput);
        ratingBar = findViewById(R.id.ratingBarReview);
        messageId = getIntent().getStringExtra(MESSAGE_ID);
        myRating = 0;
        serviceId = getIntent().getStringExtra(SERVICE_ID);
        String status = getIntent().getStringExtra(STATUS_USER_BY_RATE);
        isUser = status.equals("user");

        dbHelper = new DBHelper(this);

        WorkWithTimeApi workWithTimeApi = new WorkWithTimeApi();
        dateNow = workWithTimeApi.getCurDateInFormatHMS();

        addListenerOnRatingBar();
        rateReviewBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        // Проверяем оценку
        if(myRating!=0){
            // Оценка поставлена

            // Берём оценку
            String review = reviewInput.getText().toString();

            // Загружаем оценку в Firebase в зависимости от статуса человека
            if(isUser) {
                //юзер оценивает сервис
                createReviewForService(review);
            }
            else {
                //воркер оценивает юзера
                createReviewForUser(review);
            }
            // Обновляем атрибут "is rate" в таблице "message review"
            updateMessageReview();

            // Возвращаемся в диалоги
            goToMessages();

        }
        else {
            // Оценка не поставлена
            attentionRatingIsNull();
        }
    }

    private void updateMessageReview() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(MESSAGE_REVIEWS).child(messageId);

        Map<String,Object> items = new HashMap<>();
        if(isUser) {
            items.put(IS_RATE_BY_USER, true);
        }
        else {
            items.put(IS_RATE_BY_WORKER, true);
        }
        myRef.updateChildren(items);
    }

    private void createReviewForService(String review) {
        //оценка юзером
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(REVIEWS_FOR_SERVICE);
        String myPhoneNumber = getUserId();


        Map<String,Object> items = new HashMap<>();
        items.put(SERVICE_ID, serviceId);
        items.put(VALUING_PHONE, myPhoneNumber);
        items.put(MESSAGE_TIME, dateNow);
        items.put(REVIEW, review);
        items.put(RATING, myRating);

        String reviewId =  myRef.push().getKey();
        myRef = database.getReference(REVIEWS_FOR_SERVICE).child(reviewId);
        myRef.updateChildren(items);
        updateMessageReviewInLocalStorage();
    }

    private void createReviewForUser(String review) {
        //оценка воркером
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(REVIEWS_FOR_USER);
        String myPhoneNumber = getUserId();

        Map<String,Object> items = new HashMap<>();
        items.put(VALUING_PHONE, myPhoneNumber); // кто оценивает
        items.put(ESTIMATED_PHONE, getEstimatedPhone()); // кого оцениваем
        items.put(MESSAGE_TIME, dateNow);
        items.put(REVIEW, review);
        items.put(RATING, myRating);

        String reviewId =  myRef.push().getKey();
        myRef = database.getReference(REVIEWS_FOR_USER).child(reviewId);
        myRef.updateChildren(items);
        updateMessageReviewInLocalStorage();
    }

    private void updateMessageReviewInLocalStorage() {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        if(isUser) {
            contentValues.put(DBHelper.KEY_IS_RATE_BY_USER_MESSAGE_REVIEWS, "true");
        }
        else {
            contentValues.put(DBHelper.KEY_IS_RATE_BY_WORKER_MESSAGE_REVIEWS, "true");
        }

        database.update(DBHelper.TABLE_MESSAGE_REVIEWS, contentValues,
                DBHelper.KEY_ID + " = ?",
                new String[]{String.valueOf(messageId)});

    }

    private Object getEstimatedPhone() {
        //получить номера из диалога, к которому принадлежит этот messageId

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        // Получает телефоны из диалога
        // Таблицы: dialogs, message_reviews
        // Условия: уточняем id message_reviews и связываем диалоги с message_reviews
        String sqlQuery =
                "SELECT "
                        + DBHelper.KEY_FIRST_USER_ID_DIALOGS + ", "
                        + DBHelper.KEY_SECOND_USER_ID_DIALOGS
                        + " FROM "
                        + DBHelper.TABLE_DIALOGS + ", "
                        + DBHelper.TABLE_MESSAGE_REVIEWS
                        + " WHERE "
                        + DBHelper.TABLE_DIALOGS +"."+DBHelper.KEY_ID
                        + " = "
                        + DBHelper.KEY_DIALOG_ID_MESSAGES
                        + " AND "
                        + DBHelper.TABLE_MESSAGE_REVIEWS +"." + DBHelper.KEY_ID + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{messageId});

        if(cursor.moveToFirst()){

            int indexFirstPhone = cursor.getColumnIndex(DBHelper.KEY_FIRST_USER_ID_DIALOGS);
            String firstPhone = cursor.getString(indexFirstPhone);

            int indexSecondPhone = cursor.getColumnIndex(DBHelper.KEY_SECOND_USER_ID_DIALOGS);
            String secondPhone = cursor.getString(indexSecondPhone);

            if(firstPhone.equals(getUserId())){
                cursor.close();
                return secondPhone;
            }
            else {
                cursor.close();
                return  firstPhone;
            }
        }
        return "0";
    }


    //Описываем работу слушателя изменения состояний RatingReview Bar:
    public void addListenerOnRatingBar() {
        //При смене значения рейтинга в нашем элементе RatingReview Bar,
        //это изменение будет сохраняться в myRating
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                myRating = rating;
            }
        });
    }

    private String getUserId(){
        SharedPreferences sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);

        return sPref.getString(PHONE_NUMBER, "-");
    }

    private void attentionRatingIsNull() {
        Toast.makeText(this,"Пожалуйста, укажите оценку",Toast.LENGTH_SHORT).show();
    }

    private void goToMessages() {
        super.onBackPressed();
    }

}
