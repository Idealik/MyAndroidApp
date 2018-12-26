package com.example.ideal.myapplication.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.LocaleList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.guestService;

import static android.content.Context.MODE_PRIVATE;

@SuppressLint("ValidFragment")
public class foundElement extends Fragment implements View.OnClickListener {

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
    public foundElement(String id, String nameUser, String surname, String city, String nameService, String cost) {
        idString = id;
        nameUserString = nameUser;
        surnameString = surname;
        cityString = city;
        nameServiceString = nameService;
        costString = cost;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.found_element, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        nameUserText = view.findViewById(R.id.nameUserText);
        surname = view.findViewById(R.id.surnmaeText);
        city = view.findViewById(R.id.cityText);
        nameServiceText = view.findViewById(R.id.nameServiceText);
        costText = view.findViewById(R.id.costText);

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
        Intent intent = new Intent(this.getContext(), guestService.class);
        intent.putExtra(SERVICE_ID, Long.valueOf(idString));
        startActivity(intent);
    }
}