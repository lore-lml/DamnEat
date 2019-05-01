package com.damn.polito.damneatdeliver.beans;

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

    public List<Dish> getDishes() { return dishes;
    }

    /*Visualizzazione compatta*/
    public List<Dish> getCumulatedDishes(){
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
            out.get(i).setNumber(outTmp.get(out.get(i).getName()));
        }

        return out;
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
