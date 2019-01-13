package com.example.ideal.myapplication.fragments;

public class Service {

    private String id;
    private String name;
    private String description;
    private Integer cost;
    private Float rating;
    private String userId;
    private Long countOfRates;

    public boolean setName(String _name){
        if(isCorrectData(_name)) {
            name = _name.trim();
            return true;
        }
        return false;
    }
    public void setId(String _id){
        id = _id;
    }
    public void setDescription(String _description){
        description = _description;
    }
    public void setCost(String _cost){
            cost = Integer.valueOf(_cost);
    }
    public void setRating(String _rating){
        rating = Float.valueOf(_rating);
    }
    public void setUserId(String _userId){
        userId = _userId;
    }
    public void setCountOfRates(String _countOfRates){
        countOfRates = Long.valueOf(_countOfRates);
    }

    public String getId(){return id;}
    public String getName(){return name;}
    public String getDescription(){return description;}
    public Integer getCost(){return cost;}
    public Float getRating(){return rating;}
    public String getUserId(){return userId;}
    public Long getCountOfRates(){return countOfRates;}

    protected boolean isCorrectData(String data){

        if(!data.matches("[a-zA-ZА-Яа-я\\s\\-]+")) return false;
        return true;
    }

}
