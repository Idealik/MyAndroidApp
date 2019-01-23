package com.example.ideal.myapplication.helpApi;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WorkWithTimeApi {

    private static final String TAG = "DBInf";

    public Long getSysdateLong(){
        //3600000*3 для москвы это +3 часа
        Date sysdate = new Date();
        return sysdate.getTime()+3600000*3;
    }

    public Long getMillisecondsStringDate(String date){

        Log.d(TAG, "getMillisecondsStringDate: " + date);
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        try {
            Date orderDate;
            orderDate = formatForDateNow.parse(date);
            return orderDate.getTime();
        }
        catch (Exception e){
            Log.d(TAG, "getMillisecondsStringDate: " + e );
        }
        return 0L;
    }
    //возвращает время в формате HH:mm:ss
    public String getCurDateInFormatHMS() {
        Date dateNow = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("HH:mm:ss");
        formatForDateNow.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        return formatForDateNow.format(dateNow);
    }
}
