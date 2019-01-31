package com.example.ideal.myapplication.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ideal.myapplication.R;

public class notification_no_canceled_element extends Fragment {

    TextView messageText;
    String text;
    public notification_no_canceled_element() {}

    @SuppressLint("ValidFragment")
    public notification_no_canceled_element (Message message) {
        text = "  Вы успешно записались к " + message.getUserName()
                + ". Запись на "
                + message.getDate()
                + " в " + message.getOrderTime()
                + " на услугу " + message.getServiceName();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notification_no_canceled_element, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        messageText = view.findViewById(R.id.notification_no_canceled_elementText);
        setData();
    }

    private void setData() {
        messageText.setText(text);
    }



}
