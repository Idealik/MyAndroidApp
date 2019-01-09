package com.example.ideal.myapplication.logIn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Confirmation extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DBInf";

    final String PASS = "password";
    final String FILE_NAME = "Info";
    private static final String PHONE_NUMBER = "Phone number";

    private Button registrationButton;
    private Button verifyButton;
    private Button resendButton;

    private EditText phoneText;
    private EditText passwordText;
    private EditText repeatPasswordText;
    private EditText codeText;

    SharedPreferences sPref;

    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    private FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmation);
        //получаем id btns
        registrationButton = findViewById(R.id.registrationConfirmationBtn);
        verifyButton = findViewById(R.id.verifyConfirmationBtn);
        resendButton = findViewById(R.id.resendConfirmationBtn);
        //получаем id inputs
        phoneText = findViewById(R.id.phoneConfirmationInput);
        passwordText = findViewById(R.id.passConfirmationInput);
        repeatPasswordText = findViewById(R.id.repeatPassConfirmationInput);
        codeText = findViewById(R.id.codeConfirmationInput);

        fbAuth = FirebaseAuth.getInstance();

        registrationButton.setOnClickListener(this);
        resendButton.setOnClickListener(this);
        verifyButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //получаем данные
        String phone = phoneText.getText().toString();
        String password = passwordText.getText().toString();
        String repeatPassword = repeatPasswordText.getText().toString();

        switch (v.getId()) {
            case R.id.registrationConfirmationBtn:
                //проверка на заполенность полей
                if(isFullInputs(phone,password,repeatPassword)){
                    phone = convertPhoneToNormalView(phone);
                    // проверка на надежность пароля
                    if(isStrongPassword(password)) {
                        // проверка на совпадение паролей
                        if (password.equals(repeatPassword)) {
                            //отправляем код, если пользователь еще не подтвержден
                            sendCode(phone);
                        }
                        else {
                            Toast.makeText(
                                    this,
                                    "Пароли не совпадают",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(
                                this,
                                "Пароль должен содержать буквы и цифры и быть не менее 6 символов.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.verifyConfirmationBtn:
                String code = codeText.getText().toString();

                if(!code.trim().equals("")) {
                    // подтверждаем код и если все хорошо, создаем юзера
                    verifyCode(code);
                }
                break;
            default:
                break;
        }
    }

    private String convertPhoneToNormalView(String phone) {
        if(phone.charAt(0)=='8'){
            phone = "+7" + phone.substring(1);
        }
        return phone;
    }

    private void createNewUser(String phoneNumber, String pass) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/"+phoneNumber);
        Map<String,Object> items = new HashMap<>();
        items.put("password", pass);
        myRef.updateChildren(items);

        goToRegistration(phoneNumber);
    }

    public void sendCode(String phoneNumber) {

        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                verificationCallbacks);
    }


    private void setUpVerificationCallbacks() {

        verificationCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        //вызывается, если номер подтвержден
                        codeText.setText("");
                        //выводит соообщение о том, что пользователь уже зарегестрирован
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

                        codeText.setVisibility(View.VISIBLE);
                        verifyButton.setVisibility(View.VISIBLE);
                        resendButton.setVisibility(View.VISIBLE);
                    }
                };
    }

    public void verifyCode(String code) {
        //получаем ответ гугл
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneVerificationId, code);
        //заходим с айфоном и токеном
        signInWithPhoneAuthCredential(credential);
    }

    public void resendCode() {

        String phoneNumber = phoneText.getText().toString();

        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendToken);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        //входим
        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //если введенный код совпадает с присланным кодом
                        if (task.isSuccessful()) {

                            String phone = phoneText.getText().toString();
                            String password = passwordText.getText().toString();
                            phone  = convertPhoneToNormalView(phone);

                            password = encryptThisStringSHA512(password);
                            //создаем пользователя
                            createNewUser(phone, password);
                            //сохраняем его в лок данные
                            saveIdAndPass(phone,password);

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

    private static String encryptThisStringSHA512(String input)
    {
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    protected boolean isFullInputs(String phone, String pass, String repeatPassword){

        if(phone.trim().equals("")) return false;
        if(pass.trim().equals("")) return false;
        if(repeatPassword.trim().equals("")) return false;

        return  true;
    }
    protected boolean isStrongPassword(String myPass) {
        if(!myPass.matches(".*[a-z].*")) return  false;
        if(!myPass.matches(".*[0-9].*")) return  false;
        if(myPass.length()<=5) return false;
        return true;
    }
    protected boolean isRealPhone(String phone){
        //^((8|+7))?((?\d{3})?[- ]?)?[\d- ]{7,10}$
        // if(!phone.matches("^((8|\\+7))?((?\\d{3})?[-]?)?[\\d- ]{7,10}$\n"))

        return true;
    }

    private void saveIdAndPass(String phone, String pass) {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(PHONE_NUMBER, phone);
        editor.putString(PASS, pass);
        editor.apply();
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

    private  void goToRegistration(String phoneNumber){
        Intent intent = new Intent(this, Registration.class);
        intent.putExtra(PHONE_NUMBER, phoneNumber);
        startActivity(intent);
        finish();
    }

}