package com.damn.polito.damneatrestaurant.beans;

public class Dish {

    private String name;
    private String description;
    private float price;
    private int availability;
    private int photo = 0;

    public Dish(){}

    public Dish(String name, String description, float price, int availability) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.availability = availability;
    }
    public Dish(String name, String description, float price, int availability, int photo) {
        this(name, description, price, availability);
        this.photo = photo;
    }

    public void setPhoto(int photo){
        this.photo = photo;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public float getPrice() {
        return price;
    }

    public int getAvailability() {
        return availability;
    }
    public int getPhoto(){
        return photo;
    }
}
