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
public class foundServiceElement extends Fragment implements View.OnClickListener {

    final String SERVICE_ID = "service id";

    TextView nameUserText;
    TextView city;
    TextView nameServiceText;
    TextView costText;

    String idString;
    String nameUserString;
    String cityString;
    String nameServiceString;
    String costString;

    @SuppressLint("ValidFragment")
    public foundServiceElement(Service service, User user) {
        idString = service.getId();
        nameUserString = user.getName();
        cityString = user.getCity();
        nameServiceString = service.getName();
        costString = service.getCost().toString();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.found_service_element, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        nameUserText = view.findViewById(R.id.userNameFoundServiceElementText);
        city = view.findViewById(R.id.cityFoundServiceElementText);
        nameServiceText = view.findViewById(R.id.serviceNameFoundServiceElementText);
        costText = view.findViewById(R.id.costFoundServiceElementText);

        nameUserText.setOnClickListener(this);
        city.setOnClickListener(this);
        nameServiceText.setOnClickListener(this);
        costText.setOnClickListener(this);
        setData();
    }

    private void setData() {
        nameUserText.setText(nameUserString);
        city.setText(cityString);
        nameServiceText.setText(nameServiceString);
        costText.setText(costString);
    }

    @Override
    public void onClick(View v) {
        goToGuestService();
    }

    private void goToGuestService(){
        Intent intent = new Intent(this.getContext(), GuestService.class);
        intent.putExtra(SERVICE_ID, idString);
        startActivity(intent);
    }
}