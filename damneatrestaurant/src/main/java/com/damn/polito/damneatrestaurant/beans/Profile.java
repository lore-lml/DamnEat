package com.damn.polito.damneatrestaurant.beans;

import android.graphics.Bitmap;

public class Profile {

    String name;
    String mail;
    String phone;
    String description;
    String address;
    String opening;
    String bitmapProf;
    //Bitmap bitmap;

    public Profile(String name, String mail, String phone, String description, String address, String opening, String bitmapProf) {
        this.name = name;
        this.mail = mail;
        this.phone = phone;
        this.description = description;
        this.address = address;
        this.opening = opening;
        this.bitmapProf = bitmapProf;
    }
/*
    public Profile(String name, String mail, String phone, String description, String address, String opening, Bitmap bitmap) {
        this.name = name;
        this.mail = mail;
        this.phone = phone;
        this.description = description;
        this.address = address;
        this.opening = opening;
        this.bitmap = bitmap;
    }*/

    public Profile() {

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

    public String getOpening() {
        return opening;
    }

    public void setOpening(String opening) {
        this.opening = opening;
    }

    public String getBitmapProf() {
        return bitmapProf;
    }

    public void setBitmapProf(String bitmapProf) {
        this.bitmapProf = bitmapProf;
    }

   /* public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }*/

    public void setProfile(Profile p) {
        name=p.getName();
        mail= p.getMail();
        phone=p.getPhone();
        description=p.getDescription();
        address=p.getAddress();
        opening=p.getOpening();
        bitmapProf=p.getBitmapProf();
    }
}

