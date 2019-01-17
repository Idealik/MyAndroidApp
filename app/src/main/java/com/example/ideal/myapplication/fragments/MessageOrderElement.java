package com.example.ideal.myapplication.fragments;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.other.GuestService;


public class MessageOrderElement extends Fragment implements View.OnClickListener {

    private static final String TAG = "DBInf";

    TextView messageText;
    Button canceledBtn;

    String text;
    public MessageOrderElement() { }

    @SuppressLint("ValidFragment")
    public MessageOrderElement(Message message) {

        text = "Добрый день, на " + message.getDate() + " в " + message.getTime()
                +  " к вам записался пользователь " + message.getUserName() + " на услугу "
                + message.getServiceName() + ". Вы можете отказаться, указав причину.";
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.message_order_element, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        messageText = view.findViewById(R.id.messageMessageOrderElementText);
        canceledBtn = view.findViewById(R.id.canceledMessageOrderBtn);
        canceledBtn.setOnClickListener(this);
        setData();
    }

    private void setData() {
        messageText.setText(text);
    }

    @Override
    public void onClick(View v) {

        Log.d(TAG, "Вы отказались: ");

    }

}
