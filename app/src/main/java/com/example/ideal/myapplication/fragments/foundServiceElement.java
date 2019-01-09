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
import com.example.ideal.myapplication.GuestService;

@SuppressLint("ValidFragment")
public class foundServiceElement extends Fragment implements View.OnClickListener {

    final String SERVICE_ID = "service id";

    TextView nameUserText;
    TextView surname;
    TextView city;
    TextView nameServiceText;
    TextView costText;

    String idString;
    String nameUserString;
    String surnameString;
    String cityString;
    String nameServiceString;
    String costString;

    @SuppressLint("ValidFragment")
    public foundServiceElement(String id, String nameUser, String surname, String city, String nameService, String cost) {
        idString = id;
        nameUserString = nameUser;
        surnameString = surname;
        cityString = city;
        nameServiceString = nameService;
        costString = cost;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.found_service_element, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        nameUserText = view.findViewById(R.id.userNameFoundServiceElementText);
        surname = view.findViewById(R.id.surnameFoundServiceElementText);
        city = view.findViewById(R.id.cityFoundServiceElementText);
        nameServiceText = view.findViewById(R.id.serviceNameFoundServiceElementText);
        costText = view.findViewById(R.id.costFoundServiceElementText);

        nameUserText.setOnClickListener(this);
        surname.setOnClickListener(this);
        city.setOnClickListener(this);
        nameServiceText.setOnClickListener(this);
        costText.setOnClickListener(this);
        setData();
    }

    private void setData() {
        nameUserText.setText(nameUserString + " ");
        surname.setText(surnameString + " ");
        city.setText(cityString + " ");
        surname.setText(surnameString + " ");
        nameServiceText.setText(nameServiceString + " ");
        costText.setText(costString + " ");
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