package com.example.ideal.myapplication.createService;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.fragments.Service;
import com.example.ideal.myapplication.other.DBHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddService extends AppCompatActivity implements View.OnClickListener {

    private static final String FILE_NAME = "Info";
    private static final String PHONE_NUMBER = "Phone number";


    private static final String SERVICE_ID = "service id";
    private static final String STATUS_USER_BY_SERVICE = "status User";

    private static final String SERVICES = "services";
    private static final String NAME = "name";
    private static final String COST = "cost";
    private static final String DESCRIPTION = "description";
    private static final String USER_ID = "user id";
    private static final String COUNT_OF_RATES = "count of rates";
    private static final String RATING = "rating";

    Button addServicesBtn;

    EditText nameServiceInput;
    EditText costAddServiceInput;
    EditText descriptionServiceInput;

    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_service);

        addServicesBtn = findViewById(R.id.addServiceAddServiceBtn);

        nameServiceInput = findViewById(R.id.nameAddServiceInput);
        costAddServiceInput = findViewById(R.id.costAddServiceInput);
        descriptionServiceInput = findViewById(R.id.descriptionAddServiceInput);

        dbHelper = new DBHelper(this);

        addServicesBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.addServiceAddServiceBtn:
                if(isFullInputs()) {
                    Service service = new Service();
                    if (!service.setName(nameServiceInput.getText().toString())) {
                        Toast.makeText(
                                this,
                                "Имя сервиса должно содержать только буквы",
                                Toast.LENGTH_SHORT).show();
                        break;
                    }

                    service.setDescription(descriptionServiceInput.getText().toString());
                    service.setCost(costAddServiceInput.getText().toString());

                    addService(service);
                }
                else {
                    Toast.makeText(this, getString(R.string.empty_field), Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    private void addService(Service service) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(SERVICES);
        String userId = getUserId();

        Map<String,Object> items = new HashMap<>();
        items.put(NAME,service.getName().toLowerCase());
        items.put(COST,service.getCost());
        items.put(DESCRIPTION,service.getDescription());
        items.put(USER_ID,userId);
        items.put(COUNT_OF_RATES, 0);
        items.put(RATING, 5);
        String serviceId =  myRef.push().getKey();
        myRef = database.getReference(SERVICES).child(serviceId);
        myRef.updateChildren(items);

        service.setId(serviceId);
        addServiceInLocalStorage(service);
    }

    private void addServiceInLocalStorage(Service service){

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        String userId = getUserId();
        //добавление в БД
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_ID, service.getId());
        contentValues.put(DBHelper.KEY_NAME_SERVICES, service.getName().toLowerCase());
        contentValues.put(DBHelper.KEY_MIN_COST_SERVICES, service.getCost());
        contentValues.put(DBHelper.KEY_DESCRIPTION_SERVICES, service.getDescription());
        contentValues.put(DBHelper.KEY_COUNT_OF_RATES_SERVICES,0);
        contentValues.put(DBHelper.KEY_RATING_SERVICES,5);
        contentValues.put(DBHelper.KEY_USER_ID, userId);

        database.insert(DBHelper.TABLE_CONTACTS_SERVICES,null,contentValues);
        goToMyCalendar(getString(R.string.status_worker),service.getId());

    }

    protected Boolean isFullInputs(){
        if(nameServiceInput.getText().toString().isEmpty()) return false;
        if(descriptionServiceInput.getText().toString().isEmpty()) return false;
        if(costAddServiceInput.getText().toString().isEmpty()) return false;

        return  true;
    }

    private String getUserId(){
        SharedPreferences sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);

        return sPref.getString(PHONE_NUMBER, getString(R.string.defult_value));
    }

    private void goToMyCalendar(String status, String serviceId) {
        Intent intent = new Intent(this, MyCalendar.class);
        intent.putExtra(SERVICE_ID, serviceId);
        intent.putExtra(STATUS_USER_BY_SERVICE, status);

        startActivity(intent);
        finish();
    }
}