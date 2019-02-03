package com.example.ideal.myapplication.reviews;

import android.annotation.SuppressLint;
import android.content.Intent;
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

public class RatingBarForServiceElement extends Fragment implements View.OnClickListener {

    //возможно стоит размести поверх этого еще один layout на который можно будет кликнуть и перейти в комментарии
    private static final String SERVICE_ID = "service id";

    private TextView countOfRatesText;
    private TextView avgRatesText;
    private RatingBar ratingBar;

    private int countOfRates;
    private float avgRates;
    private String serviceId;

    public RatingBarForServiceElement() { }

    @SuppressLint("ValidFragment")
    public RatingBarForServiceElement(RatingReview ratingReview) {
        countOfRates = ratingReview.getCountOfRates();
        avgRates = ratingReview.getAvgRating();
        serviceId = ratingReview.getServiceId();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.rating_bar_for_service_element, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        countOfRatesText = view.findViewById(R.id.countOfRatesRatingBarElementText);
        avgRatesText = view.findViewById(R.id.avgRatingRatingBarElementText);
        ratingBar = view.findViewById(R.id.ratingBarRatingBarForService);

        avgRatesText.setOnClickListener(this);
        ratingBar.setOnClickListener(this);
        countOfRatesText.setOnClickListener(this);

        setData();
    }

    @Override
    public void onClick(View v) {
        goToReviewForService();
    }
    private void setData(){
        if(countOfRates>0) {
            countOfRatesText.setText(String.valueOf(countOfRates));
            avgRatesText.setText(String.valueOf(avgRates));
            ratingBar.setRating(avgRates);
        }
        else {
            countOfRatesText.setText("Оценок еще нет.");
        }
    }

    private void goToReviewForService() {
        Intent intent = new Intent(this.getContext(), ReviewForService.class);
        intent.putExtra(SERVICE_ID, serviceId);

        startActivity(intent);
    }
}
