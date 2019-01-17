package com.example.ideal.myapplication.other;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.fragments.DialogElement;
import com.example.ideal.myapplication.fragments.Service;
import com.example.ideal.myapplication.fragments.User;
import com.example.ideal.myapplication.fragments.foundServiceElement;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Dialogs extends AppCompatActivity {

    private static final String TAG = "DBInf";

    private static final String FILE_NAME = "Info";
    private static final String PHONE_NUMBER = "Phone number";

    private static final String DIALOGS = "dialogs";
    private static final String FIRST_PHONE = "first phone";
    private static final String SECOND_PHONE = "second phone";

    private static final String USERS = "users";
    private static final String NAME = "name";

    private static final String MESSAGE_ORDERS = "message orders";
    private static final String DIALOG_ID = "dialog id";
    private static final String DATE = "date";
    private static final String IS_CANCELED = "is canceled";
    private static final String SERVICE_ID = "service id";
    private static final String TIME = "time";


    SharedPreferences sPref;
    DBHelper dbHelper;

    private DialogElement dElement;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialogs);

        manager = getSupportFragmentManager();
        dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(DBHelper.TABLE_MESSAGES, null, null);
        database.delete(DBHelper.TABLE_DIALOGS, null, null);

        loadDialogs();
    }

    private void loadDialogs() {
        final String myPhone = getUserPhone();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        Query query1 = database.getReference(DIALOGS)
                .orderByChild(FIRST_PHONE)
                .equalTo(myPhone);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dialogs) {
                for(DataSnapshot dialog:dialogs.getChildren()) {
                    final String otherPhone = String.valueOf(dialog.child(SECOND_PHONE).getValue());
                    final String dialogId = dialog.getKey();
                    String secondPhone = String.valueOf(dialog.child(SECOND_PHONE).getValue());
                    addDialogInLocalStorage(dialogId,myPhone, secondPhone);

                    DatabaseReference reference = database.getReference(USERS).child(otherPhone);
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot user) {

                            String name = String.valueOf(user.child(NAME).getValue());
                            addUserInLocalStorage(name,otherPhone);

                            addMessagesInLocalStorage(dialogId);
                            addToScreen(dialogId, name);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        Query query2 = database.getReference(DIALOGS)
                .orderByChild(SECOND_PHONE)
                .equalTo(myPhone);
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dialogs) {
                for(DataSnapshot dialog:dialogs.getChildren()){
                     final String otherPhone = String.valueOf(dialog.child(FIRST_PHONE).getValue());
                    final String dialogId = dialog.getKey();

                    String firstPhone = String.valueOf(dialog.child(FIRST_PHONE).getValue());
                    addDialogInLocalStorage(dialogId,firstPhone, myPhone);

                    DatabaseReference reference = database.getReference(USERS).child(otherPhone);
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot user) {
                            String name = String.valueOf(user.child(NAME).getValue());

                            addUserInLocalStorage(name,otherPhone);
                            addMessagesInLocalStorage(dialogId);
                            addToScreen(dialogId, name);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void addUserInLocalStorage(String name, String otherPhone) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_NAME_USERS, name);
        contentValues.put(DBHelper.KEY_USER_ID, otherPhone);

        database.insert(DBHelper.TABLE_CONTACTS_USERS, null, contentValues);

    }

    private void addDialogInLocalStorage(String dialogId, String firstPhone, String secondPhone) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_ID, dialogId);
        contentValues.put(DBHelper.KEY_FIRST_USER_ID_DIALOGS, firstPhone);
        contentValues.put(DBHelper.KEY_SECOND_USER_ID_DIALOGS, secondPhone);

        database.insert(DBHelper.TABLE_DIALOGS, null, contentValues);

    }
    private void addMessagesInLocalStorage(final String dialogId) {
        Query messagesQuery = FirebaseDatabase.getInstance().getReference(MESSAGE_ORDERS)
                .orderByChild(DIALOG_ID)
                .equalTo(dialogId);
        messagesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot messages) {
                SQLiteDatabase database = dbHelper.getWritableDatabase();

                for(DataSnapshot message:messages.getChildren()){
                    String messageId = message.getKey();
                    String date = String.valueOf(message.child(DATE).getValue());
                    String isCanceled = String.valueOf(message.child(IS_CANCELED).getValue());
                    String time = String.valueOf(message.child(TIME).getValue());

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DBHelper.KEY_ID, messageId);
                    contentValues.put(DBHelper.KEY_DIALOG_ID_MESSAGES, dialogId);
                    contentValues.put(DBHelper.KEY_DAY_ID_MESSAGES, date);
                    contentValues.put(DBHelper.KEY_IS_CANCELED_MESSAGES, isCanceled);
                    contentValues.put(DBHelper.KEY_TIME_MESSAGES, time);

                    database.insert(DBHelper.TABLE_MESSAGES,null,contentValues);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private String getUserPhone() {
        sPref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String userId = sPref.getString(PHONE_NUMBER, "-");

        return  userId;
    }

    private void addToScreen(String dialogId, String name) {
        dElement = new DialogElement(dialogId, name);
        transaction = manager.beginTransaction();
        transaction.add(R.id.mainDialogsLayout, dElement);
        transaction.commit();
    }
}
