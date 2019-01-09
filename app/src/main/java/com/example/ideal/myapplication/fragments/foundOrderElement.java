package com.example.ideal.myapplication.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.other.GuestService;

@SuppressLint("ValidFragment")
public class foundOrderElement extends Fragment implements View.OnClickListener {

    final String SERVICE_ID = "service id";

    TextView nameText;
    TextView dateText;
    TextView timeText;

    String idString;
    String nameString;
    String dateString;
    String timeString;

    @SuppressLint("ValidFragment")
    public foundOrderElement(String id, String name, String date, String time) {
        idString = id;
        nameString = name;
        dateString = date;
        timeString = time;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.found_order_element, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        nameText = view.findViewById(R.id.nameFoundOrderElementText);
        dateText = view.findViewById(R.id.costFoundOrderElementText);
        timeText = view.findViewById(R.id.descriptionFoundOrderElementText);

        nameText.setOnClickListener(this);
        dateText.setOnClickListener(this);
        timeText.setOnClickListener(this);
        setData();
    }

    private void setData() {
        nameText.setText(nameString);
        dateText.setText(dateString);
        timeText.setText(timeString);
    }

    @Override
    public void onClick(View v) {
        goToGuestService();
    }

    private void goToGuestService(){
        Intent intent = new Intent(this.getContext(), GuestService.class);
        intent.putExtra(SERVICE_ID, Long.valueOf(idString));
        startActivity(intent);
    }
}