package com.damn.polito.commonresources.beans;

import com.damn.polito.commonresources.beans.Dish;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Order {


    private int id;
    private List<Dish> dishes;
    private Date date;
    private double price;
    private String customerAddress;
    private String customerName;
    private String delivererName;



    private String state;
    private boolean expanded;

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
        this.expanded = false;
    }

    public Order(List<Dish> dishes, Date date, String customerAddress, String customerName, double price){
        this.dishes = dishes;
        this.date = date;
        this.customerAddress = customerAddress;
        this.customerName = customerName;
        this.price = price;
        this.state = "ordered";
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    public int Id() {
        return id;
    }

    public List<Dish> getDishes() { return dishes;
    }

    /*Visualizzazione compatta*/
    public List<Dish> CumulatedDishes(){
        List<Dish> out = new ArrayList<>();
        Map<String,Integer> outTmp = new HashMap<>();
        int i,j,contiene;
        for(i=0;i<dishes.size();i++) {
            contiene = 0;
            for (j = 0; j < out.size() && !out.isEmpty(); j++){
                if (dishes.get(i).getName()!=null && out.get(j).getName()!=null) {
                    if(dishes.get(i).getName().equalsIgnoreCase(out.get(j).getName()))
                        contiene=1;
                }
            }
            if (contiene == 0) {
                out.add(dishes.get(i));
                outTmp.put(dishes.get(i).getName(), 1);

            } else {
                outTmp.put(dishes.get(i).getName(), outTmp.get(dishes.get(i).getName()) + 1);
            }
        }
        for(i=0;i<out.size();i++){
            out.get(i).setQuantity(outTmp.get(out.get(i).getName()));
        }

        return out;
    }

    public int DishesNumber() {
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

    public boolean Expanded() { return expanded; }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

}
