package com.example.ideal.myapplication.editing;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ideal.myapplication.fragments.User;
import com.example.ideal.myapplication.logIn.Authorization;
import com.example.ideal.myapplication.logIn.Registration;
import com.example.ideal.myapplication.other.DBHelper;
import com.example.ideal.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EditProfile extends AppCompatActivity implements View.OnClickListener {
    //изменяет номер телефона во всех таблицах, где используется
    private final String TAG = "DBInf";

    private static final String USER_NAME = "name";
    private static final String PASS = "password";
    private static final String USER_CITY = "city";

    private static final String USERS = "users/";
    private static final String WORKING_TIME = "working time/";
    private static final String SERVICE = "services/";

    private static final String USER_ID = "user id";

    private static final String PHONE_NUMBER = "Phone number";
    private static final String FILE_NAME = "Info";

    String oldPhone;

    Button editBtn;
    Button verifyButton;
    Button resendButton;

    EditText nameInput;
    EditText cityInput;
    EditText phoneInput;
    EditText codeInput;

    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    DBHelper dbHelper;
    SharedPreferences sPref;
    User user;

    private FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        nameInput = findViewById(R.id.nameEditProfileInput);
        cityInput = findViewById(R.id.cityEditProfileInput);
        phoneInput = findViewById(R.id.phoneEditProfileInput);
        codeInput = findViewById(R.id.codeEditProfileInput);

        editBtn = findViewById(R.id.editProfileEditProfileBtn);
        resendButton = findViewById(R.id.resendProfileEditProfileBtn);
        verifyButton = findViewById(R.id.verifyProfileEditProfileBtn);

        fbAuth = FirebaseAuth.getInstance();
        user = new User();
        dbHelper = new DBHelper(this);

        oldPhone = getUserPhone();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String sqlQuery = "SELECT "
                + DBHelper.KEY_NAME_USERS + ", "
                + DBHelper.KEY_CITY_USERS + ", "
                + DBHelper.KEY_USER_ID
                + " FROM " + DBHelper.TABLE_CONTACTS_USERS
                + " WHERE " + DBHelper.KEY_USER_ID + " = ?";
        Cursor cursor = database.rawQuery(sqlQuery, new String[]{oldPhone});

        if (cursor.moveToFirst()) {
            int indexName = cursor.getColumnIndex(DBHelper.KEY_NAME_USERS);
            int indexCity = cursor.getColumnIndex(DBHelper.KEY_CITY_USERS);
            int indexPhone = cursor.getColumnIndex(DBHelper.KEY_USER_ID);

            nameInput.setText(cursor.getString(indexName));
            cityInput.setText(cursor.getString(indexCity));
            phoneInput.setText(cursor.getString(indexPhone));
        }
        editBtn.setOnClickListener(this);
        resendButton.setOnClickListener(this);
        verifyButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.editProfileEditProfileBtn:

                String phone = convertPhoneToNormalView(phoneInput.getText().toString());

                user.setName(nameInput.getText().toString().toLowerCase());
                user.setCity(cityInput.getText().toString().toLowerCase());
                //Проверка изменённого номеа
                if (phone.length() > 0) {

                    updateInfoInFireBase(user, phone);

                } else {
                    Toast.makeText(this, getString(R.string.empty_phone_field), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.verifyProfileEditProfileBtn:

                String code = codeInput.getText().toString();
                Log.d(TAG, "onClick: " + code);
                if (!code.trim().equals("")) {
                    // подтверждаем код и если все хорошо, создаем юзера
                    verifyCode(code);
                }
                break;

            case R.id.resendConfirmationBtn:
                resendCode();
                break;

            default:
                break;
        }
    }

    private void updateInfoInFireBase(User user, final String phone) {
        //можно объеденить ссылки?
        DatabaseReference reference = FirebaseDatabase
                .getInstance()
                .getReference("users/" + getUserPhone());

        Map<String, Object> items = new HashMap<>();
        if (user.getName() != null) items.put(USER_NAME, user.getName());
        if (user.getCity() != null) items.put(USER_CITY, user.getCity());
        reference.updateChildren(items);

        updateInfoInLocalDataBase(user);

        if(!phone.equals(getUserPhone())) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(USERS).child(phone);

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //такого номера нет
                    if (dataSnapshot.getChildrenCount() == 0) {
                        sendCode(phone);
                    } else {
                        attentionThisUserAlreadyReg();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            goToAuthorization();
        }
    }

    private void sendCode(String phoneNumber) {

        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                verificationCallbacks);
    }

    public void verifyCode(String code) {
        //получаем ответ гугл
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneVerificationId, code);
        //заходим с айфоном и токеном
        signInWithPhoneAuthCredential(credential);
    }

    private void setUpVerificationCallbacks() {

        verificationCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        //вызывается, если номер подтвержден
                        codeInput.setText("");
                        //выводит соообщение о том, что пользователь уже зарегестрирован
                        //пользователь уже проверен, значит зарегестрирован
                        attentionThisUserAlreadyReg();
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Log.d(TAG, "Invalid credential: "
                                    + e.getLocalizedMessage());
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // SMS quota exceeded
                            Log.d(TAG, "SMS Quota exceeded.");
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        //происходит, когда отослали код
                        phoneVerificationId = verificationId;
                        resendToken = token;

                        codeInput.setVisibility(View.VISIBLE);
                        resendButton.setVisibility(View.VISIBLE);
                        verifyButton.setVisibility(View.VISIBLE);
                    }
                };
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        //входим
        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "onComplete: ");
                        //если введенный код совпадает с присланным кодом
                        if (task.isSuccessful()) {
                            String phone = phoneInput.getText().toString();
                            phone = convertPhoneToNormalView(phone);
                            updatePhone(phone);
                            //сохраняем его в лок данные
                            savePhone(phone);
                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                attentionThisCodeWasWrong();
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void updatePhone(String phone) {

        //я не знаю, как сделать update id, поэтому я ксоздаю нового пользователя и удаляю старого
        deleteOldPhoneNumber(phone);
        createNewPhoneNumber(phone);
        savePhone(phone);
        updateOtherPlaceWithPhone(phone);
        //добавить обновление локальной бд
        //не буду менять локальнгый бд, просто выкину его с финишом на авторизацию
        //там перезайдет и подгрузим все данные
    }

    private void deleteOldPhoneNumber(String phone){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/" + oldPhone );
        //удаляем старый телефон в таблице юзер
        Map<String,Object> items = new HashMap<>();
        items.put("name", null);
        items.put("city", null);
        items.put("password",null);
        myRef.updateChildren(items);
    }

    private void createNewPhoneNumber(String phone) {
        //создаем новый номер и данные с ним в таблице юзер
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference  myRef = database.getReference("users/"+phone);
        Map<String,Object> items = new HashMap<>();
        items.put("name", user.getName());
        items.put("city", user.getCity());
        items.put("password", getUserPass());
        myRef.updateChildren(items);
    }
    private void updateOtherPlaceWithPhone(final String phone) {
        //делаем update номера в service, и working time, если есть запись
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        //update в working time
        final Query query = database.getReference(WORKING_TIME)
                .orderByChild(USER_ID)
                .equalTo(oldPhone);
        //находим id времени по телефону и меняем в нем телефон
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot time: dataSnapshot.getChildren()){

                    DatabaseReference myRef = database.getReference(WORKING_TIME+time.getKey());
                    Map<String,Object> items = new HashMap<>();
                    items.put(USER_ID, phone);
                    myRef.updateChildren(items);
                }
                //update в service
                updateService(phone);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                //стоит обрабатывать исключения?
            }
        });
    }

    private void updateService(final String phone) {
        //аналогично с working days
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        final Query query = database.getReference(SERVICE)
                .orderByChild(USER_ID)
                .equalTo(oldPhone);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot service: dataSnapshot.getChildren()){

                    DatabaseReference myRef = database.getReference(SERVICE+service.getKey());
                    Map<String,Object> items = new HashMap<>();
                    items.put(USER_ID, phone);
                    myRef.updateChildren(items);
                }
                goToAuthorization();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //стоит обрабатывать исключения?
            }
        });
    }

    //Обновление информации в БД
    private void updateInfoInLocalDataBase(User user) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        if(user.getName()!=null) contentValues.put(DBHelper.KEY_NAME_USERS, user.getName());
        if(user.getCity()!=null) contentValues.put(DBHelper.KEY_CITY_USERS, user.getCity());

        if(contentValues.size()>0) {
            database.update(DBHelper.TABLE_CONTACTS_USERS, contentValues,
                    DBHelper.KEY_USER_ID + " = ?",
                    new String[]{String.valueOf(oldPhone)});
        }
    }

    public void resendCode() {

        String phoneNumber = phoneInput.getText().toString();

        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendToken);
    }

    private String getUserPhone() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String userId = sPref.getString(PHONE_NUMBER, "-");

        return  userId;
    }

    private String convertPhoneToNormalView(String phone) {
        if(phone.charAt(0)=='8'){
            phone = "+7" + phone.substring(1);
        }
        return phone;
    }

    private void attentionThisUserAlreadyReg(){
        Toast.makeText(
                this,
                "Данный пользователь уже зарегестрирован.",
                Toast.LENGTH_SHORT).show();
    }

    private void attentionThisCodeWasWrong(){
        Toast.makeText(
                this,
                "Код введен неверно",
                Toast.LENGTH_SHORT).show();
    }

    private void savePhone(String phone) {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(PHONE_NUMBER, phone);
        editor.apply();
    }

    private String getUserPass() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String pass = sPref.getString(PASS, "-");
        return  pass;
    }

    private void goToAuthorization(){
        Intent intent = new Intent(EditProfile.this, Authorization.class);
        startActivity(intent);
        finish();
    }
}