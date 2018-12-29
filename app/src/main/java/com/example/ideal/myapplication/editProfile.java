package com.example.ideal.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class editProfile extends AppCompatActivity implements View.OnClickListener{

    Button editBtn;

    EditText nameInput;
    EditText surnamenameInput;
    EditText cityInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);


    }

    @Override
    public void onClick(View v) {

     //
    }
}
