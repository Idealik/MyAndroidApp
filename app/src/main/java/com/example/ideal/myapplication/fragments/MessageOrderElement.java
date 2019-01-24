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
    private static final String MESSAGE_ORDERS = "message orders";
    private static final String MESSAGE_REVIEWS = "message reviews";
    private static final String MESSAGE_TIME = "message time";

    String messageId;
    String messageDateOfDay;
    String messageTimeOfDay;
    String messageDialogId;
    String messageTimeId;
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
        messageDateOfDay = message.getDate();
        messageTimeOfDay = message.getOrderTime();
        messageTimeId = message.getTimeId();
        messageIsCanceled = message.getIsCanceled();
        messageDialogId = message.getDialogId();

        text = "Добрый день, на " + messageDateOfDay + " в " + messageTimeOfDay
                + " к вам записался пользователь " + message.getUserName() + " на услугу "
                + message.getServiceName() + ". Вы можете отказаться, указав причину.";
    }

    private boolean isRelevance() {
        String commonDate = messageDateOfDay + " " + messageTimeOfDay;

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
        canceledBtn = view.findViewById(R.id.canceledMessageOrderElementBtn);
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

        //если разница между заказом и временем, которое сейчас меньше часа, отмена без review
        //isRelevance нужен, чтобы пользователь, как прошло время, не смог отменить заказ,
        // будучи на активити

        String commonDate = messageDateOfDay + " " + messageTimeOfDay;
        Long sysdateLong = workWithTimeApi.getSysdateLong();
        Long orderDateLong = workWithTimeApi.getMillisecondsStringDate(commonDate);
        if(isRelevance()) {
            if (orderDateLong - sysdateLong > 3600000) {
                cancel();
            } else {
                cancel();
                //отправляем возможность написать ревью
                // заполняю firebase тут, а в мессаджах обрабатываю уже ,есть ли ривью
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                DatabaseReference myRef = database.getReference(MESSAGE_REVIEWS);
                Map<String,Object> items = new HashMap<>();

                String dateNow = workWithTimeApi.getCurDateInFormatHMS();

                items.put("dialog id", messageDialogId);
                items.put(MESSAGE_TIME, dateNow);
                items.put("time id", messageTimeId);
                items.put("is rate", false);

                String messageId =  myRef.push().getKey();
                myRef = database.getReference(MESSAGE_REVIEWS).child(messageId);
                myRef.updateChildren(items);
            }
        }
    }

    private  void cancel(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message orders/" + messageId);
        Map<String, Object> items = new HashMap<>();
        items.put("is canceled", true);
        myRef.updateChildren(items);
        clearPhone();
    }

    private void clearPhone() {
        //получить id message
        //получить date (id working days)
        //сделать query по date в working time и получить id времени
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database
                .getReference(WORKING_TIME)
                .child(messageTimeId);

        Map<String, Object> items = new HashMap<>();
        items.put("user id", "0");
        myRef.updateChildren(items);

        canceledBtn.setEnabled(false);
    }
}