package com.damn.polito.commonresources.beans;

import java.io.Serializable;

public class Restaurant implements Serializable {
    private String restaurantName;
    private String restaurantPhone;
    private String restaurantID;
    private String restaurantAddress;
    private Double restaurant_price_ship;
    private String photo;
    private String notificationId;

    public Restaurant() {}

    public Restaurant(String restaurantName, String restaurantPhone, String restaurantID, String restaurantAddress, Double restaurant_price_ship, String photo) {
        this.restaurantName = restaurantName;
        this.restaurantPhone = restaurantPhone;
        this.restaurantID = restaurantID;
        this.restaurantAddress = restaurantAddress;
        this.restaurant_price_ship = restaurant_price_ship;
        this.photo = photo;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantPhone() {
        return restaurantPhone;
    }

    public void setRestaurantPhone(String restaurantPhone) {
        this.restaurantPhone = restaurantPhone;
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    public String getPhoto() {
        if(photo == null)
            return "NO_PHOTO";
        if(photo.equals(""))
            return "NO_PHOTO";
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Double getRestaurant_price_ship() {
        return restaurant_price_ship;
    }

    public void setRestaurant_price_ship(Double restaurant_price_ship) {
        this.restaurant_price_ship = restaurant_price_ship;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }
}
