package com.example.ideal.myapplication.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.example.ideal.myapplication.other.GuestService;
import com.example.ideal.myapplication.other.Messages;

@SuppressLint("ValidFragment")
public class DialogElement extends Fragment implements View.OnClickListener {

    final String DIALOG_ID = "dialog id";

    TextView nameText;

    String idString;
    String nameString;

    @SuppressLint("ValidFragment")
    public DialogElement(String id, String name) {
        idString = id;
        nameString = name;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_element, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nameText = view.findViewById(R.id.nameDialogElementText);

        nameText.setOnClickListener(this);
        setData();
    }

    private void setData() {
        nameText.setText(nameString);
    }

    @Override
    public void onClick(View v) {
        goToDialog();
    }

    private void goToDialog(){
        //Toast.makeText(this.getContext(), "click", Toast.LENGTH_SHORT);
        Intent intent = new Intent(this.getContext(), Messages.class);
        intent.putExtra(DIALOG_ID, idString);
        startActivity(intent);
    }
}