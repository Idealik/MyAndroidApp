package com.example.ideal.myapplication.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    final String FILE_NAME = "Info";
    final String ID = "id";

    TextView nameText;
    TextView costText;
    TextView descriptionText;

    String idString;
    String nameString;
    String costString;
    String descriptionString;

    SharedPreferences sPref;

    @SuppressLint("ValidFragment")
    public foundElement(String id, String name, String cost, String description) {
        idString = id;
        nameString = name;
        costString = cost;
        descriptionString = description;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.found_element, null);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);

        nameText = view.findViewById(R.id.nameText);
        costText = view.findViewById(R.id.costText);
        descriptionText = view.findViewById(R.id.descriptionText);

        sPref = getContext().getSharedPreferences(FILE_NAME, MODE_PRIVATE);

        nameText.setOnClickListener(this);
        costText.setOnClickListener(this);
        descriptionText.setOnClickListener(this);
        setData();
    }

    private void saveId() {
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(ID, idString);
        editor.apply();
    }

    private void setData() {
        nameText.setText(nameString + " ");
        costText.setText(costString + " ");
        descriptionText.setText(descriptionString + " ");
    }


    @Override
    public void onClick(View v) {
        saveId();
        goToGuestService();
        Toast.makeText(this.getContext(), sPref.getString(ID, "-"), Toast.LENGTH_SHORT).show();
    }

    private void goToGuestService(){
        Intent intent = new Intent(this.getContext(), guestService.class);
        startActivity(intent);
    }
}