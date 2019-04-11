package com.damn.polito.damneatrestaurant.beans;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.damn.polito.commonresources.Utility;

public class Dish {

    private String name;
    private String description;
    private float price;
    private int availability;
    private Bitmap photo;
    private boolean dish_otd = false;


    private int number;
    public Dish(){}

    public Dish(String name, String description, float price, int availability) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.availability = availability;
        this.photo = null;
        number=0;

    }
    public Dish(String name, String description, float price, int availability, Bitmap photo) {
        this(name, description, price, availability);
        this.photo = photo;
    }

    public void setPhoto(Bitmap photo){
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
    public Bitmap getPhoto(){
        return photo;
    }

    public boolean isDishOtd(){
        return dish_otd;
    }

    public void setDishOtd(boolean dish_otd){
        this.dish_otd = dish_otd;
    }

    public String getPhotoStr(){
        if (photo == null)
            return "NO_PHOTO";
        return Utility.BitMapToString(photo);
    }

    public void changeDishOtd(){
        this.dish_otd = !this.dish_otd;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
