package com.damn.polito.damneatrestaurant.beans;

public class Dish {
    private String name;
    private String description;
    private float price;
    private int disponibility;
    private int photo = 0;


    public Dish(String name, String description, float price, int disponibility) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.disponibility = disponibility;
    }
    public Dish(String name, String description, float price, int disponibility, int photo) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.disponibility = disponibility;
        this.photo = photo;
    }

    public void setPhoto(int photo){
        this.photo = photo;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setDisponibility(int disponibility) {
        this.disponibility = disponibility;
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

    public int getDisponibility() {
        return disponibility;
    }

    public int getPhoto(){
        return photo;
    }
}
