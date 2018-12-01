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
import android.widget.TextView;

import com.example.ideal.myapplication.R;

@SuppressLint("ValidFragment")
public class foundElement extends Fragment {

    TextView nameText;
    TextView costText;
    TextView descriptionText;

    String nameString;
    String costString;
    String descriptionString;

    @SuppressLint("ValidFragment")
    public foundElement(String name, String cost, String description) {
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

        nameText.setText(nameString);
        costText.setText(costString);
        descriptionText.setText(descriptionString);
    }

    public void setData(String name, String cost, String description) {

//        if(nameText!=null && costText!=null && descriptionText!=null) {
            nameText.setText(name);
            costText.setText(cost);
            descriptionText.setText(description);
    }


}
