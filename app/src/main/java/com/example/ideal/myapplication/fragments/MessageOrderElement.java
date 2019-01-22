package com.example.ideal.myapplication.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.helpApi.WorkWithTimeApi;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class MessageOrderElement extends Fragment implements View.OnClickListener {

    private static final String TAG = "DBInf";
    private static final String WORKING_TIME = "working time/";
    private static final String WORKING_DAYS_ID = "working day id";
    private static final String MESSAGE_ORDERS = "message orders";

    String messageId;
    String messageDateDay;
    String messageTimeDay;
    Boolean messageIsCanceled;
    WorkWithTimeApi workWithTimeApi;

    TextView messageText;
    Button canceledBtn;

    String text;

    public MessageOrderElement() {
    }

    @SuppressLint("ValidFragment")
    public MessageOrderElement(Message message) {
        messageId = message.getId();
        messageDateDay = message.getDate();
        messageTimeDay = message.getOrderTime();
        messageIsCanceled = message.getIsCanceled();

        text = "Добрый день, на " + message.getDate() + " в " + message.getOrderTime()
                + " к вам записался пользователь " + message.getUserName() + " на услугу "
                + message.getServiceName() + ". Вы можете отказаться, указав причину.";
    }

    private boolean isRelevance() {
        String commonDate = messageDateDay + " " + messageTimeDay;

        Long orderDateLong = workWithTimeApi.getMillisecondsStringDate(commonDate);
        Long sysdateLong = workWithTimeApi.getSysdateLong();

        return orderDateLong - sysdateLong > 0;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.message_order_element, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        messageText = view.findViewById(R.id.messageMessageOrderElementText);
        canceledBtn = view.findViewById(R.id.canceledMessageOrderBtn);
        canceledBtn.setOnClickListener(this);
        workWithTimeApi = new WorkWithTimeApi();

        if(!isRelevance()){
            canceledBtn.setVisibility(View.INVISIBLE);
        }

        if(messageIsCanceled) {
            canceledBtn.setEnabled(false);
        }

        setData();
    }

    private void setData() {
        messageText.setText(text);
    }

    @Override
    public void onClick(View v) {
        setIsCanceled();
    }

    private void setIsCanceled() {
        //Отказываем юзеру в услуге за ЧАС до ее исполнения
        //Иначе даем возможность написать ревью
        String commonDate = messageDateDay + " " + messageTimeDay;
        Long sysdateLong = workWithTimeApi.getSysdateLong();
        Log.d(TAG, "setIsCanceled: ");
        Long orderDateLong = workWithTimeApi.getMillisecondsStringDate(commonDate);
        //если разница между заказом и временем, которое сейчас меньше часа, отмена без review
        //isRelevance нужен, чтобы пользователь, как прошло время, не смог отменить заказ,
        // будучи на активити
        if(isRelevance()) {
            if (orderDateLong - sysdateLong > 3600000) {
                Log.d(TAG, "setIsCanceled: " + (orderDateLong - sysdateLong));
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("message orders/" + messageId);
                Map<String, Object> items = new HashMap<>();
                items.put("is canceled", true);
                myRef.updateChildren(items);
                clearPhone();
            } else {
                //отправляем возможность написать ревью
                Log.d(TAG, "АЯ-ЯЙ Я ЩАС РАЗРЕШУ РЕВЬЮ ПЛОХОЕ НАПИСАТЬ! ");
            }
        }
    }

    private void clearPhone() {
        //получить id message
        //получить date (id working days)
        //сделать query по date в working time и получить id времени
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(MESSAGE_ORDERS).child(messageId).child("date");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dateId) {
                String date = String.valueOf(dateId.getValue());

                Query query = database.getReference(WORKING_TIME)
                        .orderByChild(WORKING_DAYS_ID)
                        .equalTo(date);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for(DataSnapshot time: dataSnapshot.getChildren()) {
                            String timeId = String.valueOf(time.getKey());
                            Log.d(TAG, "onDataChange: " + timeId);

                            DatabaseReference myRef = database.getReference(WORKING_TIME).child(timeId);

                            Map<String, Object> items = new HashMap<>();
                            items.put("user id", "0");
                            myRef.updateChildren(items);

                        }
                        canceledBtn.setEnabled(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
}