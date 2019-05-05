package com.damn.polito.damneat.beans;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {

    public enum PriceRange{Cheap, Medium, Expensive}

    private String name;
    private String address;
    private String opening;
    private String phone;
    private String description;
    private String mail;

    private List<String> categories = new ArrayList<>();
    private double priceShip;
    private String image;
    private int reviews;
    private int totalRate;

    private String fbKey;

    public Restaurant(){}

    public Restaurant(String name, String address, String opening, String phone, String description, String mail) {
        this.name = name;
        this.address = address;
        this.opening = opening;
        this.phone = phone;
        this.description = description;
        this.mail = mail;

        priceShip = 0.0;
        image = "none";
        reviews = 0;
        totalRate = 0;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getCategories() {
        StringBuilder sb = new StringBuilder();
        if(categories == null || categories.size() == 0) return "";

        for(String s : categories)
            sb.append(", ").append(s);
        sb.delete(0,2);
        return sb.toString();
    }

    public void setCategories(String categories) {
        if(categories == null) return;
        String[] cat = categories.split(",?\\s+");
        this.categories.clear();
        for(String s : cat)
            this.categories.add(s.trim());
    }

    public double getPriceShip() { return priceShip; }

    public void setPriceShip(double priceShip) { this.priceShip = priceShip; }

    public String getImage() { return image; }

    public void setImage(String image) { this.image = image; }

    public int getReviews() { return reviews; }

    public void setReviews(int reviews) { this.reviews = reviews; }

    public int getTotalRate() { return totalRate; }

    public void setTotalRate(int totalRate) { this.totalRate = totalRate; }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public String getOpening() { return opening; }

    public void setOpening(String opening) { this.opening = opening; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public String getFbKey() { return fbKey; }

    public void setFbKey(@NonNull String fbKey) { this.fbKey = fbKey; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getMail() { return mail; }

    public void setMail(String mail) { this.mail = mail; }

    public int rate(){
        double rate = 100*(double)totalRate/reviews;
        return (int)rate;
    }

    public PriceRange priceRange(){
        String range = categories.get(categories.size()-1);

        switch (range){
            case "(€€)":
                return PriceRange.Medium;
            case "(€€€)":
                return PriceRange.Expensive;
            default:
                return PriceRange.Cheap;
        }
    }


    public boolean contains(String filterPattern) {
        if(name.toLowerCase().contains(filterPattern))
            return true;

        if(getCategories().toLowerCase().contains(filterPattern))
            return true;
        return getCategories().contains(filterPattern);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant)) return false;

        Restaurant that = (Restaurant) o;
        return fbKey.equals(that.fbKey);
    }

    @Override
    public int hashCode() { return fbKey.hashCode(); }
}
