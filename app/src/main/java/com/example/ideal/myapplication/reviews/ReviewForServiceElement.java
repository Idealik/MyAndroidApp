package com.example.ideal.myapplication.reviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.fragments.objects.RatingReview;
import com.example.ideal.myapplication.fragments.objects.User;

public class ReviewForServiceElement extends Fragment {

    //возможно стоит размести поверх этого еще один layout на который можно будет кликнуть и перейти в комментарии
    private static final String TAG = "DBInf";

    private TextView reviewText;
    private TextView userNameText;

    private String review;
    private String userName;

    public ReviewForServiceElement() { }

    @SuppressLint("ValidFragment")
    public ReviewForServiceElement(User user, RatingReview ratingReview) {
        userName = user.getName();
        review = ratingReview.getReview();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.review_for_service_element, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onViewCreated: " + userName);
        Log.d(TAG, "onViewCreated: " + review);

        userNameText = view.findViewById(R.id.nameReviewForServiceElementText);
        reviewText = view.findViewById(R.id.reviewReviewForServiceElementText);

        setData();
    }
    private void setData(){
        userNameText.setText(userName);
        reviewText.setText(review);
    }

}
