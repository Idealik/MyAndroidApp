package com.example.ideal.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class profile extends AppCompatActivity implements View.OnClickListener {

    final String FILE_NAME = "Info";
    final String STATUS = "status";
    
    Button logOutBtn;
    Button findServicesBtn;
    Button addServicesBtn;
    
    SharedPreferences sPref;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        logOutBtn = (Button) findViewById(R.id.logOutProfileBtn);
        findServicesBtn = (Button) findViewById(R.id.findServicesProfileBtn);
        addServicesBtn = (Button) findViewById(R.id.addServicesProfileBtn);

        logOutBtn.setOnClickListener(this);
        findServicesBtn.setOnClickListener(this);
        addServicesBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addServicesProfileBtn:
                goToAddService();
                break;
            case R.id.findServicesProfileBtn:
                goToSearchService();
                break;
            case R.id.logOutProfileBtn:
                annulStatus();
                goToLogIn();
            default:
                break;
        }
    }
    
    //Анулировать статус
    private void annulStatus() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.remove(STATUS);
        editor.apply();
    }
    
    private void goToLogIn() {
        Intent intent = new Intent(this, authorization.class);
        startActivity(intent);
        finish();
    }
    private void goToAddService() {
        Intent intent = new Intent(this, addService.class);
        startActivity(intent);
    }
    
    private void goToSearchService() {
        Intent intent = new Intent(this, searchService.class);
        startActivity(intent);
    }
}
