package com.damn.polito.commonresources.beans;

import android.graphics.Bitmap;

import com.damn.polito.commonresources.R;
import com.damn.polito.commonresources.Utility;

import java.io.Serializable;

import static com.damn.polito.commonresources.Utility.StringToBitMap;

public class Dish implements Serializable, Comparable<Dish>{

    public static int[] tier = new int[]{R.drawable.tier1, R.drawable.tier2, R.drawable.tier3};

    private String name;
    private String id;
    private String description;
    private float price;
    private int availability;
    private String photo_str;
    private boolean dish_otd = false;
    private boolean edit_mode = false;
    private static final String NO_PHOTO = "NO_PHOTO";
    private int quantity;
    private int nOrders;
    public Dish(){}

    public Dish(String name, String description, float price, int availability, String photo_str){
        this.name=name;
        this.description=description;
        this.price=price;
        this.availability=availability;
        this.photo_str=photo_str;
        quantity = 0;
    }

    public Dish(String name, String description, float price, int availability) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.availability = availability;
        this.photo_str="NO_PHOTO";
        quantity = 0;

    }
    public Dish(String name, String description, float price, int availability, Bitmap photo) {
        this(name, description, price, availability);
        this.photo_str = Utility.BitMapToString(photo);
    }

    public Dish(String name, int quantity, double price, String id){
        this.name = name;
        this.quantity = quantity;
        this.price = (float) price;
        this.id = id;
    }

    public void  setName(String name){
        this.name = name;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setPhotoBmp(Bitmap photo){
        this.photo_str = Utility.BitMapToString(photo);
    }

    public void setPhoto(String photo){
        this.photo_str = photo;
        if(photo == null)
            this.photo_str = "NO_PHOTO";
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

    public boolean isDishOtd(){
        return dish_otd;
    }

    public void setDishOtd(boolean dish_otd){
        this.dish_otd = dish_otd;
    }

    public String getPhoto(){
        if(photo_str== null)
            return "NO_PHOTO";
        return photo_str;
    }

    public void changeDishOtd(){
        this.dish_otd = !this.dish_otd;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public void increaseQuantity() {
        this.quantity++;
    }
    public void decreaseQuantity() {
        if(this.quantity > 0)
            this.quantity--;
    }
    public void setEditMode(boolean edit_mode){
        this.edit_mode = edit_mode;
    }

    public boolean EditMode(){
        return edit_mode;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public int getnOrders() {
        return nOrders;
    }

    public void setnOrders(int nOrders) {
        this.nOrders = nOrders;
    }


    @Override
    public int compareTo(Dish other) {
        return other.nOrders - this.nOrders;
    }
}

