package com.damn.polito.commonresources.beans;

import java.util.Calendar;
import java.util.Date;

public class RateObject {
    public enum RateType{Service, Meal, Restaurant}

    private int rate;
    private String note;
    private Customer customer;
    private Restaurant restaurant;
    private RateType type;
    private String date;

    public RateObject(){}

    public RateObject(int rate, String note, RateType type, Customer customer) {
        this.rate = rate;
        this.note = note;
        this.type = type;
        this.customer = customer;
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        this.date = "" + day + "/" + month + "/" + year;

    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public RateType getType() {
        return type;
    }

    public void setType(RateType type) {
        this.type = type;
    }

    public String getDate(){
        return this.date;
    }
    public void setDate(String date){
        this.date = date;
    }
}
