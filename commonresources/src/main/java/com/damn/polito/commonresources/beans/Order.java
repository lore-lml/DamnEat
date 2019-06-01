package com.damn.polito.commonresources.beans;

import com.damn.polito.commonresources.beans.Dish;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {

    private String id;
    private List<Dish> dishes;
    private Date date;
    private double price;
    private String delivererName = "NOT_ASSIGNED_YET";
    private String delivererPhoto = "NO_PHOTO";
    private String delivererID = "NOT_ASSIGNED_YET";
    private String note;
    private String deliveryTime;
    private boolean rated = false;
    private Double deliveryCost = .0;
    private Double latitude, longitude;

    private Customer customer;
    private Restaurant restaurant;
    //private Deliverer deliverer;

    private String state;
    private boolean expanded;

    public  Order(){
        this.customer = new Customer();
        this.restaurant = new Restaurant();
    }

    public Order(List<Dish> dishes, Date date, double price, String delivererName, String note, String deliveryTime, Customer customer, Restaurant restaurant) {
        this.dishes = dishes;
        this.date = date;
        this.price = price;
        this.delivererName = delivererName;
        this.note = note;
        this.deliveryTime = deliveryTime;
        this.customer = customer;
        this.restaurant = restaurant;
        this.state = "ordered";

    }

    public Order(String id, List<Dish> dishes, Date date, String customerAddress, String customerName, String delivererName, double price) {
        this.id = id;
        this.dishes = dishes;
        this.date = date;
        this.customer = new Customer(customerName, customerAddress);
        this.restaurant = new Restaurant();
        this.delivererName = delivererName;
        this.price=price;
        this.expanded = false;
    }

    public Order(List<Dish> cart_dishes, Date date, Restaurant restaurant, Customer customer, Double price, String note, String deliveryTime) {
        this.dishes = cart_dishes;
        this.date = date;
        this.customer = customer;
        this.restaurant = restaurant;
        this.price = price;
        this.note = note;
        this.deliveryTime = deliveryTime;
        this.state = "ordered";
    }

//    public Order(List<Dish> dishes, Date date, String customerAddress, String customerName, double price){
//        this.dishes = dishes;
//        this.date = date;
//        this.customer = new Customer(customerName, customerAddress);
//        this.price = price;
//        this.state = "ordered";
//    }



    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    public String Id() {
        return id;
    }

    public List<Dish> getDishes() { return dishes;
    }

//    /*Visualizzazione compatta*/
//    public List<Dish> CumulatedDishes(){
//        List<Dish> out = new ArrayList<>();
//        Map<String,Integer> outTmp = new HashMap<>();
//        int i,j,contiene;
//        for(i=0;i<dishes.size();i++) {
//            contiene = 0;
//            for (j = 0; j < out.size() && !out.isEmpty(); j++){
//                if (dishes.get(i).getName()!=null && out.get(j).getName()!=null) {
//                    if(dishes.get(i).getName().equalsIgnoreCase(out.get(j).getName()))
//                        contiene=1;
//                }
//            }
//            if (contiene == 0) {
//                out.add(dishes.get(i));
//                outTmp.put(dishes.get(i).getName(), 1);
//
//            } else {
//                outTmp.put(dishes.get(i).getName(), outTmp.get(dishes.get(i).getName()) + 1);
//            }
//        }
//        for(i=0;i<out.size();i++){
//            out.get(i).setQuantity(outTmp.get(out.get(i).getName()));
//        }
//
//        return out;
//    }

    public int DishesNumber() {
        int number = 0;
        for(Dish d: dishes){
            number += d.getQuantity();
        }

        return number;
    }

    public Date getDate() {
        return date;
    }

    public String getCustomerAddress() {
        return customer.getCustomerAddress();
    }

    public String getCustomerName() {
        return customer.getCustomerName();
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
        this.customer.setCustomerAddress(customerAddress);
    }

    public void setCustomerName(String customerName) {
        this.customer.setCustomerName(customerName);
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDelivererName(String delivererName) {
        this.delivererName = delivererName;
    }

    public String getDelivererPhoto() {
        return delivererPhoto;
    }

    public void setDelivererPhoto(String delivererPhoto) {
        this.delivererPhoto = delivererPhoto;
    }

    public boolean isRated() {
        return rated;
    }

    public void setRated(boolean rated) {
        this.rated = rated;
    }

    public String getDelivererID() {
        return delivererID;
    }

    public void setDelivererID(String delivererID) {
        this.delivererID = delivererID;
    }

    public Double getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(Double deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return id.equals(order.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
