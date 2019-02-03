package com.example.ideal.myapplication.reviews;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.fragments.objects.RatingReview;
import com.example.ideal.myapplication.fragments.objects.User;
import com.example.ideal.myapplication.other.DBHelper;

public class ReviewForService extends AppCompatActivity {

    private static final String TAG = "DBInf";

    private static final String SERVICE_ID = "service id";

    private String serviceId;

    private DBHelper dbHelper;

    private FragmentManager manager;

    private LinearLayout ratingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_for_service);

        serviceId = getIntent().getStringExtra(SERVICE_ID);
        dbHelper = new DBHelper(this);

        manager = getSupportFragmentManager();
        ratingLayout = findViewById(R.id.resultReviewForService);

        createReviews();
    }


    private void createReviews() {
        //получаем данные и добавляем их на экран

        //получить: все о пользователе и об его review for service
        // таблицы: users, review for service
        // связь таблиц по номеру телефона, находим reviews по serviceId

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        String sqlQuery = "SELECT * FROM "
                + DBHelper.TABLE_CONTACTS_USERS + ", " + DBHelper.TABLE_REVIEWS_FOR_SERVICE
                + " WHERE "
                + DBHelper.TABLE_CONTACTS_USERS + " . " +DBHelper.KEY_USER_ID + " = " + DBHelper.KEY_VALUING_PHONE_REVIEWS_FOR_SERVICE
                + " AND "
                + DBHelper.KEY_SERVICE_ID_REVIEWS_FOR_SERVICE + " = ?";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{serviceId});


        Log.d(TAG, "createReviews: " + serviceId);
        if (cursor.moveToFirst()) {
            do {
                //получаем все о юзере
                int indexNameUser = cursor.getColumnIndex(DBHelper.KEY_NAME_USERS);
                int indexCityUser = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);
                int indexPhoneUser = cursor.getColumnIndex(DBHelper.KEY_USER_ID);
                User user = new User();
                user.setName(cursor.getString(indexNameUser));
                user.setCity(cursor.getString(indexCityUser));
                user.setPhone(cursor.getString(indexPhoneUser));
                Log.d(TAG, "createReviews: " + user.getPhone());
                Log.d(TAG, "createReviews: " + user.getName());

                int indexReviewReview = cursor.getColumnIndex(DBHelper.KEY_REVIEW_REVIEWS_FOR_SERVICE);
                int indexRatingReview = cursor.getColumnIndex(DBHelper.KEY_RATING_REVIEWS_FOR_SERVICE);
                int indexMessageTimeReview = cursor.getColumnIndex(DBHelper.KEY_MESSAGE_TIME_MESSAGES);
                RatingReview ratingReview = new RatingReview();
                ratingReview.setReview(cursor.getString(indexReviewReview));
                ratingReview.setRating(cursor.getString(indexRatingReview));
                ratingReview.setMessageTime(cursor.getString(indexMessageTimeReview));

                // получаем все о самом отзыве
                addToScreen(user, ratingReview);
            } while (cursor.moveToNext());
        }
        cursor.close();

    }

    private void addToScreen(User user, RatingReview review) {
        Log.d(TAG, "addToScreen: ");
        ReviewForServiceElement fElement = new ReviewForServiceElement(user,review);

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.resultReviewForService, fElement);
        transaction.commit();
    }
}
