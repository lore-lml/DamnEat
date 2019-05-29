package com.damn.polito.commonresources.beans;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RateObject implements Comparable<RateObject>{

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
        int month = calendar.get(Calendar.MONTH)+1;
        int year = calendar.get(Calendar.YEAR);
        this.date = String.format(Locale.getDefault(),"%02d/%02d/%d", day, month, year);
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

    @Override
    public int compareTo(RateObject other) {
        if(this == other)
            return 0;
        try {
            Calendar a = Calendar.getInstance();
            DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALY);
            Date date1 = format.parse(this.date);
            a.setTime(date1);

            Calendar b = Calendar.getInstance();
            Date date2 = format.parse(other.date);
            b.setTime(date2);

            if(!a.equals(b))
                return b.compareTo(a);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        //Se sono di tipo Service allora ordinali in base al rate
        if(this.type == RateObject.RateType.Service && this.type == other.type)
            return other.rate - this.rate;
        //Se solo uno è di tipo service metti sempre dopo, il tipo service
        if(this.type == RateObject.RateType.Service)
            return 1;
        if(other.type == RateObject.RateType.Service)
            return -1;

        //Altrimenti ordina in base al nome del ristorante
        if(this.restaurant.getRestaurantName().equals(other.restaurant.getRestaurantName()))
            return other.rate - this.rate;
        return this.restaurant.getRestaurantName().compareTo(other.restaurant.getRestaurantName());
    }
}
