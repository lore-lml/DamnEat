package com.damn.polito.damneatrestaurant.beans;

import android.graphics.Bitmap;

import com.damn.polito.commonresources.Utility;

public class DishFirebaseWrapper {
    private String name;
    private String description;
    private float price;
    private int availability;
    private String photo;
    private static final String NO_PHOTO = "NO_PHOTO";


    public DishFirebaseWrapper(String name, String description, float price, int availability) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.availability = availability;
        this.photo = NO_PHOTO;
    }

    public DishFirebaseWrapper(String name, String description, float price, int availability, String photo) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.availability = availability;
        this.photo = photo;
    }

    public DishFirebaseWrapper(Dish dish){
        this.name = dish.getName();
        this.description = dish.getDescription();
        this.availability = dish.getAvailability();
        this.price = dish.getPrice();
    }

    public Dish getDish(){
        if (photo.equals(NO_PHOTO))
            return new Dish(name, description, price, availability);
        Bitmap bmp = Utility.StringToBitMap(photo);
        return new Dish(name, description, price, availability, bmp);
    }


}
