package com.example.ideal.myapplication.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.helpApi.WorkWithTimeApi;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class MessageReviewElement extends Fragment implements View.OnClickListener {

    private static final String TAG = "DBInf";

    String text;
    String messageId;
    String messageName;
    String messageServiceName;
    String messageDateOfDay;
    String messageTime;
    Boolean messageIsCanceled;
    WorkWithTimeApi workWithTimeApi;

    TextView messageText;
    Button reviewBtn;

    public MessageReviewElement() {
    }

    @SuppressLint("ValidFragment")
    public MessageReviewElement(Message message) {
        messageId = message.getId();
        messageName = message.getUserName();
        messageServiceName = message.getServiceName();
        messageDateOfDay = message.getDate();
        messageTime = message.getTime();
        messageIsCanceled = message.getIsCanceled();

        text = "Работник " + messageName
                + " отказался предоставлять вам услугу "
                +  messageServiceName + " на "
                + messageDateOfDay
                + ".\nОднако он сделал это менее чем за час до сеанса, поэтому мы считаем будет честно дать Вам возможность оставить оценку и комментарий.";
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.message_order_element, null);
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
    }
}