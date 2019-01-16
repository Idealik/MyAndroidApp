package com.example.ideal.myapplication.other;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.ideal.myapplication.R;

public class Messages extends AppCompatActivity {

    final String DIALOG_ID = "dialog id";

    String dialogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages);

        dialogId = getIntent().getStringExtra(DIALOG_ID);


        loadInformation();
    }

    private void loadInformation() {
    }
}
