package com.example.ideal.myapplication.fragments.objects;

public class RatingReview {

    private int countOfRates;
    private float avgRating;
    private String id;
    private String review;
    private String serviceId;
    private String rating;
    private String valuingPhone;
    private String messageTime;

    public void setCountOfRates(String _countOfRates) {
        countOfRates = Integer.valueOf(_countOfRates);
    }
    public void setAvgRating(String _avgRating) {
        avgRating = Float.valueOf(_avgRating);
    }
    public void setReview(String _review){
        review=_review;
    }
    public void setId(String _id){
        id=_id;
    }
    public void setServiceId(String _serviceId){
        serviceId = _serviceId;
    }
    public void setValuingPhone(String _valuingPhone){
        valuingPhone=_valuingPhone;
    }
    public void setMessageTime(String _messageTime){
        messageTime=_messageTime;
    }
    public void setRating(String _rating){
        rating=_rating;
    }

    public int getCountOfRates() {
        return countOfRates;
    }

    public float getAvgRating() {
        return avgRating;
    }

    public String getReview() {
        return review;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getValuingPhone() {
        return valuingPhone;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public String getRating() {
        return rating;
    }

    public String getId() {
        return id;
    }
}
