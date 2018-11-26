package com.example.ideal.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class profile extends AppCompatActivity implements View.OnClickListener {

    final String FILE_NAME = "Info";
    final String STATUS = "status"; //
    SharedPreferences sPref;

    Button logOutBT;
    Button findServices;
    Button addServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        logOutBT = (Button) findViewById(R.id.logOutProfileBtn);
        findServices = (Button) findViewById(R.id.FindServicesProfileBtn);
        addServices = (Button) findViewById(R.id.addServicesProfileBtn);

        logOutBT.setOnClickListener(this);
        findServices.setOnClickListener(this);
        addServices.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addServicesProfileBtn:
                goToAddService();
                break;
            case R.id.FindServicesProfileBtn:
                break;
            case R.id.logOutProfileBtn:
                annulStatus();
                goToLogIn();
            default:
                break;
        }
    }

    private void goToLogIn() {
        Intent intent = new Intent(this, authorization.class);
        startActivity(intent);
        finish();
    }
    private void goToAddService() {
        Intent intent = new Intent(this, addService.class);
        startActivity(intent);
        finish();
    }

    private void annulStatus() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.remove(STATUS);
        editor.apply();
    }
}
