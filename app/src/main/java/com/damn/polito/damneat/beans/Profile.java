package com.damn.polito.damneat.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Profile {

    private String name;
    private String mail;
    private String phone;
    private String description;
    private String address;
    private String bitmapProf;
    private Set<String> favoriteRestaurants;

    public Profile(String name, String mail, String phone, String description, String address, String bitmapProf) {
        this();
        this.name = name;
        this.mail = mail;
        this.phone = phone;
        this.description = description;
        this.address = address;
        this.bitmapProf = bitmapProf;
    }

    public Profile(Profile p) {
        this(p.name, p.mail, p.phone, p.description, p.address, p.bitmapProf);
    }
    public Profile() {
        favoriteRestaurants = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBitmapProf() {
        return bitmapProf;
    }

    public void setBitmapProf(String bitmapProf) {
        this.bitmapProf = bitmapProf;
    }

    public Set<String> favouriteRestaurantsSet() {
        return favoriteRestaurants;
    }

    public void sFavoriteRestaurantsSet(Set<String> favoriteRestaurants) {
        this.favoriteRestaurants = favoriteRestaurants;
    }

    public String getFavoriteRestaurants() {
        if(favoriteRestaurants.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();

        for(String key : favoriteRestaurants)
            sb.append(key).append(",");

        sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }

    public void setFavoriteRestaurants(String favoriteRestaurants) {
        String[] s = favoriteRestaurants.split(",");
        this.favoriteRestaurants.addAll(Arrays.asList(s));
    }

    public void addFavoriteRestaurant(String restaurantKey){
        if(restaurantKey == null || restaurantKey.isEmpty()) return;
        this.favoriteRestaurants.add(restaurantKey);
    }

    public void removeFavoriteRestaurant(String restaurantKey) {
        if(restaurantKey == null || restaurantKey.isEmpty()) return;
        this.favoriteRestaurants.remove(restaurantKey);
    }
}
