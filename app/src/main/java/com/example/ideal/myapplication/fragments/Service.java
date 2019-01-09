package com.example.ideal.myapplication.fragments;

public class Service {

    String name;
    String description;
    Integer cost;
    Float rating;
    Long userId;
    Long countOfRates;

    public boolean setName(String _name){
        if(isCorrectData(_name)) {
            name = _name;
            return true;
        }
        return false;
    }
    public void setDescription(String _description){
        description = _description;
    }
    public void setCost(Integer _cost){
            cost = _cost;
    }
    public void setRating(Float _rating){
            rating = _rating;
    }
    public void setUserId(Long _userId){
            userId = _userId;
    }
    public void setCountOfRates(Long _countOfRates){
        countOfRates = _countOfRates;
    }

    public String getName(){return name;}
    public String getDescription(){return description;}
    public Integer getCost(){return cost;}
    public Float getRating(){return rating;}
    public Long getUserId(){return userId;}
    public Long getCountOfRates(){return countOfRates;}

    protected boolean isCorrectData(String data){

        if(!data.matches("[a-zA-ZА-Яа-я\\-]+")) return false;
        if(data.length()<0) return false;

        return true;
    }

}
