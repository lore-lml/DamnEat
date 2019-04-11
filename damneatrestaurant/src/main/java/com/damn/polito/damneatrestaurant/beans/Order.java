package com.damn.polito.damneatrestaurant.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {

    private int id;
    private List<Dish> dishes;
    private Date date;
    private double price;
    private String customerAddress;
    private String customerName;
    private String delivererName;
    private boolean expanded = false;

    /*FOR FUTURE USE*/
    //private Customer customer;
    //private Deliverer deliverer;

    public Order(int id, List<Dish> dishes, Date date, String customerAddress, String customerName, String delivererName, double price) {
        this.id = id;
        this.dishes = dishes;
        this.date = date;
        this.customerAddress = customerAddress;
        this.customerName = customerName;
        this.delivererName = delivererName;
        this.price=price;
    }

    public int getId() {
        return id;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public int getDishesNumber() {
        return dishes.size();
    }

    public Date getDate() {
        return date;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDelivererName() {
        return delivererName;
    }

    public Double getPrice(){
        return price;
    }

    public void changeExpanded() {expanded = !expanded; }

    public boolean isExpanded() { return expanded; }
}
