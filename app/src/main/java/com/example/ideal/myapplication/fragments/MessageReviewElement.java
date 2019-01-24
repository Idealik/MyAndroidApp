package com.example.ideal.myapplication.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.helpApi.WorkWithTimeApi;
import com.example.ideal.myapplication.other.GuestService;
import com.example.ideal.myapplication.other.Review;


public class MessageReviewElement extends Fragment implements View.OnClickListener {

    private static final String TAG = "DBInf";

    private static final String PHONE_NUMBER = "Phone number";
    private static final String SERVICE_ID = "service id";

    String text;
    String messageId;
    String messageName;
    String messageServiceName;
    String messageDateOfDay;
    String messageTime;
    String serviceId;
    String phoneNumber;
    boolean messageIsRate;
    WorkWithTimeApi workWithTimeApi;

    TextView messageText;
    Button reviewBtn;

    public MessageReviewElement() {
    }

    @SuppressLint("ValidFragment")
    public MessageReviewElement(Message message, String _serviceId, String _phone) {
        messageId = message.getId();
        messageName = message.getUserName();
        messageServiceName = message.getServiceName();
        messageDateOfDay = message.getDate();
        messageTime = message.getMessageTime();
        messageIsRate = message.getIsRate();
        serviceId = _serviceId;
        phoneNumber = _phone;

        text = "Работник " + messageName
                + " отказался предоставлять вам услугу "
                +  messageServiceName + " на "
                + messageDateOfDay
                + ".\nОднако он сделал это менее чем за час до сеанса, поэтому мы считаем будет честно дать Вам возможность оставить оценку и комментарий.";
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.message_review_element, null);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        messageText = view.findViewById(R.id.messageMessageReviewElementText);
        reviewBtn = view.findViewById(R.id.reviewMessageReviewElementBtn);
        reviewBtn.setOnClickListener(this);

        setData();
    }

    private void setData() {
        messageText.setText(text);
    }

    @Override
    public void onClick(View v) {
        goToReview();
    }

    private void goToReview() {
        Intent intent = new Intent(this.getContext(), Review.class);
        intent.putExtra(PHONE_NUMBER, phoneNumber);
        intent.putExtra(SERVICE_ID, serviceId);
        startActivity(intent);
    }
}