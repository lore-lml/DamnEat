package com.damn.polito.damneatrestaurant.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {

    private int id;
    private List<String> dishes;
    private Date date;
    private double price;
    private String customerAddress;
    private String customerName;
    private String delivererName;

    /*FOR FUTURE USE*/
    //private Customer customer;
    //private Deliverer deliverer;

    /*COSTRUTTORE TEMPORANEO, CAMBIARE ARRAYLIST DI STRINGHE CON DISH*/
    public Order(int id, List<String> dishes, Date date, String customerAddress, String customerName, String delivererName, double price) {
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

    public List<String> getDishes() {
        return dishes;
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
}
