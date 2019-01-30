package com.example.ideal.myapplication.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.ideal.myapplication.R;
import com.example.ideal.myapplication.helpApi.WorkWithTimeApi;
import com.example.ideal.myapplication.other.GuestService;
import com.example.ideal.myapplication.other.Review;


public class MessageReviewElement extends Fragment implements View.OnClickListener {

    private static final String TAG = "DBInf";

    private static final String SERVICE_ID = "service id";
    private static final String MESSAGE_ID = "message id";
    private static final String STATUS_USER_BY_RATE = "user status";

    String text;
    String messageId;
    String messageName;
    String messageServiceName;
    String messageDateOfDay;
    String messageTime;
    String serviceId;
    boolean messageIsRateByUser;
    boolean messageIsRateByWorker;
    boolean isUser;
    String status;

    TextView messageText;
    Button reviewBtn;

    public MessageReviewElement() {
    }

    @SuppressLint("ValidFragment")
    public MessageReviewElement(Message message, String _serviceId, String _status) {
        messageId = message.getId();
        messageName = message.getUserName();
        messageServiceName = message.getServiceName();
        messageDateOfDay = message.getDate();
        messageTime = message.getOrderTime();
        messageIsRateByUser = message.getIsRateByUser();
        messageIsRateByWorker = message.getIsRateByWorker();
        isUser = _status.equals("user");
        serviceId = _serviceId;

        //письмо для юзера
        if(isUser) {

              text = "Работник " + messageName
                    + " предоставлял услугу "
                    + messageServiceName
                    + " на "
                    + messageDateOfDay
                      + ".\nПожалуйста, оставьте отзыв о нем, чтобы улучшить качество сервиса."
                      + " Вы также сможете увидеть отзыв, о себе,"
                      + " как только пользователь оставит его или пройдет 72 часа.";

           /* text = "Работник " + messageName
                    + " отказался предоставлять вам услугу "
                    + messageServiceName
                    + " на "
                    + messageDateOfDay
                    + ".\nОднако он сделал это менее чем за час до сеанса," +
                    " поэтому мы считаем будет честно дать Вам возможность оставить оценку" +
                    " и комментарий.";*/
        }
        else {
            //письмо для воркера
            text = "Вы оказывали услугу " + messageServiceName
                    + " пользователю "
                    + messageName
                    + " на "
                    + messageDateOfDay
                    + " в "
                    + messageTime
                    + ".\nПожалуйста, оставьте отзыв о нем, чтобы улучшить качество сервиса."
                    + " Вы также сможете увидеть отзыв, о себе,"
                    + " как только пользователь оставит его или пройдет 72 часа.";
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.message_review_element, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        messageText = view.findViewById(R.id.messageMessageReviewElementText);
        reviewBtn = view.findViewById(R.id.reviewMessageReviewElementBtn);
        // Проверяем стоит ли оценка и это юзер? Чтобы устанвоить статус и в ревью отметить поле
        if(isUser) {
            if (!messageIsRateByUser) {
                reviewBtn.setOnClickListener(this);
                status = "user";
            } else {
                reviewBtn.setEnabled(false);
            }
        }
        else {
            if (!messageIsRateByWorker) {
                reviewBtn.setOnClickListener(this);
                status = "worker";
            } else {
                reviewBtn.setEnabled(false);
            }
        }
        setData();
    }

    private void setData() {
        messageText.setText(text);
    }

    @Override
    public void onClick(View v) {
        goToReview();
    }

    private void goToReview() {
        Intent intent = new Intent(this.getContext(), Review.class);
        intent.putExtra(SERVICE_ID, serviceId);
        intent.putExtra(MESSAGE_ID, messageId);
        intent.putExtra(STATUS_USER_BY_RATE, status);
        startActivity(intent);
    }
}