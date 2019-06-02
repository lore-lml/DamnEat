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
    private Deliverer deliverer;

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
