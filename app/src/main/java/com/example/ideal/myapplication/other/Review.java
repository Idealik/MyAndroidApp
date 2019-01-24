package com.example.ideal.myapplication.other;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.ideal.myapplication.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Review extends AppCompatActivity implements View.OnClickListener {

    private static final String PHONE_NUMBER = "Phone number";

    private static final String REVIEWS_FOR_SERVICE = "reviews for service";
    private static final String SERVICE_ID = "service id";
    private static final String USER_ID = "user id";
    private static final String REVIEW = "review";
    private static final String RATING = "rating";

    private float myRating;
    private String serviceId;
    private String userId;

    private Button rateReviewBtn;

    private EditText reviewInput;

    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review);

        rateReviewBtn = findViewById(R.id.rateReviewBtn);
        reviewInput = findViewById(R.id.reviewReviewInput);
        ratingBar = findViewById(R.id.ratingBarReview);

        myRating = 0;
        serviceId = getIntent().getStringExtra(SERVICE_ID);
        userId = getIntent().getStringExtra(PHONE_NUMBER);
        addListenerOnRatingBar();
        rateReviewBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if(myRating!=0){
            //создаем в бд сам этот ревью

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(REVIEWS_FOR_SERVICE);

            String review = reviewInput.getText().toString();

            Map<String,Object> items = new HashMap<>();
            items.put(SERVICE_ID, serviceId);
            items.put(USER_ID, userId);
            items.put(REVIEW, review);
            items.put(RATING, myRating);

            String reviewId =  myRef.push().getKey();
            myRef = database.getReference(REVIEWS_FOR_SERVICE).child(reviewId);
            myRef.updateChildren(items);

            goToDialogs();
        }
        else {
            attentionRatingIsNull();
        }
    }

    //Описываем работу слушателя изменения состояний Rating Bar:
    public void addListenerOnRatingBar() {

        //При смене значения рейтинга в нашем элементе Rating Bar,
        //это изменение будет сохраняться в myRating
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                myRating = rating;
            }
        });
    }
    private void attentionRatingIsNull() {
        Toast.makeText(this,"Пожалуйста, укажите оценку",Toast.LENGTH_SHORT).show();
    }

    private void goToDialogs() {
        Intent intent = new Intent(this, Dialogs.class);
        startActivity(intent);
    }

}
