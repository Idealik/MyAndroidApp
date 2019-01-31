package com.example.ideal.myapplication.editing;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.fragments.objects.Service;
import com.example.ideal.myapplication.other.DBHelper;
import com.example.ideal.myapplication.other.GuestService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditService extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = "DBInf";
    private final String SERVICE_ID = "service id";

    private final String SERVICES = "services";
    private final String NAME = "name";
    private final String COST = "cost";
    private final String DESCRIPTION = "description";

    private String serviseId;

    Button editServicesBtn;

    EditText nameServiceInput;
    EditText costServiceInput;
    EditText descriptonServiceInput;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_service);

        editServicesBtn = findViewById(R.id.editServiceEditServiceBtn);

        nameServiceInput = findViewById(R.id.nameEditServiceInput);
        costServiceInput = findViewById(R.id.costEditServiceInput);
        descriptonServiceInput = findViewById(R.id.descriptionEditServiceInput);

        // Получаем id сервиса
        serviseId = getIntent().getStringExtra(SERVICE_ID);

        dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String sqlQuery = "SELECT "
                + DBHelper.KEY_NAME_SERVICES + ", "
                + DBHelper.KEY_DESCRIPTION_SERVICES + ", "
                + DBHelper.KEY_MIN_COST_SERVICES
                + " FROM " + DBHelper.TABLE_CONTACTS_SERVICES
                + " WHERE " + DBHelper.KEY_ID + " = ?";
        Cursor cursor = database.rawQuery(sqlQuery, new String[] {serviseId});

        if(cursor.moveToFirst()) {
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_SERVICES);
            int indexCost = cursor.getColumnIndex(DBHelper.KEY_MIN_COST_SERVICES);
            int indexDescription = cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION_SERVICES);

            nameServiceInput.setText(cursor.getString(indexName));
            costServiceInput.setText(cursor.getString(indexCost));
            descriptonServiceInput.setText(cursor.getString(indexDescription));
        }

        editServicesBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.editServiceEditServiceBtn:
                String cost = costServiceInput.getText().toString();
                String description = descriptonServiceInput.getText().toString();

                // Создание сервса и заполнение информации о нём
                Service service = new Service();
                service.setId(serviseId);

                if (!service.setName(nameServiceInput.getText().toString())) {
                    Toast.makeText(
                            this,
                            "Имя сервиса должно содержать только буквы",
                            Toast.LENGTH_SHORT).show();
                    break;
                }

                if(cost.length()!=0) {
                    service.setCost(cost);
                }

                if(description.length()!=0) {
                    service.setDescription(description);
                }

                editServiceInLocalStorage(service);
                editServiceInFirebase(service);
                goToService();
                break;

            default:
                break;
        }
    }

    private void editServiceInLocalStorage(Service service) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        if(service.getName()!=null) contentValues.put(DBHelper.KEY_NAME_SERVICES, service.getName());
        if(service.getCost()!=null) contentValues.put(DBHelper.KEY_MIN_COST_SERVICES, service.getCost());
        if(service.getDescription()!=null) contentValues.put(DBHelper.KEY_DESCRIPTION_SERVICES, service.getDescription());
        if(contentValues.size()>0) {
            database.update(DBHelper.TABLE_CONTACTS_SERVICES, contentValues,
                    DBHelper.KEY_ID + " = ?",
                    new String[]{service.getId()});
        }
    }

    private void editServiceInFirebase(Service service) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(SERVICES+"/"+service.getId());

        Map<String,Object> items = new HashMap<>();
        if(service.getName()!=null) items.put(NAME,service.getName());
        if(service.getCost()!=null) items.put(COST,service.getCost());
        if(service.getDescription()!=null) items.put(DESCRIPTION,service.getDescription());
        reference.updateChildren(items);
    }


    private void goToService() {
        Intent intent = new Intent(this, GuestService.class);
        intent.putExtra(SERVICE_ID, serviseId);
        startActivity(intent);
        finish();
    }
}
