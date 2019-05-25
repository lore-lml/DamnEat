package com.damn.polito.commonresources.beans;

public class RateObject {
    public enum RateType{Service, Meal, Restaurant}

    private int rate;
    private String note;
    private Customer customer;
    private Restaurant restaurant;
    private RateType type;

    public RateObject(){}

    public RateObject(int rate, String note, RateType type, Customer customer) {
        this.rate = rate;
        this.note = note;
        this.type = type;
        this.customer = customer;
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
}
