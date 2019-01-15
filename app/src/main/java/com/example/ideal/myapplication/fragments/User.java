package com.example.ideal.myapplication.fragments;

import android.util.Log;

public class User {

    private String name;
    private String city;

    public boolean setName(String _name){
        if(isCorrectName(_name)) {
            name = _name;
            return true;
        }
        return false;
    }

    public boolean setCity(String _city){
        if(isCorrectCity(_city)) {
            city = _city;
            return true;
        }
        return false;
    }

    public String getName(){return name;}
    public String getCity(){ return city; }

    public boolean isCorrectName(String name){

        if(!name.matches("[a-zA-ZА-Яа-я\\-]+\\s[a-zA-ZА-Яа-я\\-]+")) return false;

        return true;
    }

    public boolean isCorrectCity(String city){

        if(!city.matches("[a-zA-ZА-Яа-я\\-]+")) return false;

        return true;
    }
}
