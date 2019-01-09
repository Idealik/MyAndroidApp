package com.example.ideal.myapplication.fragments;

import android.util.Log;

public class User {

    private String name;
    private String surname;
    private String city;

    public boolean setName(String _name){
        Log.d("111", "setNAme: " +_name);

        if(isCorrectData(_name)) {
            name = _name;
            return true;
        }
        return false;
    }

    public boolean setSurname(String _surname){
        if(isCorrectData(_surname)) {
            surname = _surname;
            return true;
        }
        return false;
    }

    public boolean setCity(String _city){
        if(isCorrectData(_city)) {
            city = _city;
            return true;
        }
        return false;
    }

    public String getName(){return name;}
    public String getSurname(){return surname;}
    public String getCity(){ return city; }

    protected boolean isCorrectData(String data){

        if(!data.matches("[a-zA-ZА-Яа-я\\-]+")) return false;
        if(data.length()<0) return false;

        return true;
    }

}
