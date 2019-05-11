package com.damn.polito.commonresources.beans;

public class Deliverer {

    private String name;
    private String mail;
    private String phone;
    private String description;
    private long latitude, longitude;
    private String bitmapProf;
    private boolean state;

    private String key;

    public Deliverer(){}

    public Deliverer(String name, String mail, String phone, String description, String bitmapProf, long latitude, long longitude) {
        this.name = name;
        this.mail = mail;
        this.phone = phone;
        this.description = description;
        this.bitmapProf = bitmapProf;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public Deliverer(String name, String mail, String phone, String description, String bitmapProf) {
        this.name = name;
        this.mail = mail;
        this.phone = phone;
        this.description = description;
        this.bitmapProf = bitmapProf;
    }

    public Deliverer(Deliverer p) {
        this(p.name, p.mail, p.phone, p.description, p.bitmapProf, p.latitude, p.longitude);
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

    public long getLatitude() {
        return latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public String getBitmapProf() {
        return bitmapProf;
    }

    public void setBitmapProf(String bitmapProf) {
        this.bitmapProf = bitmapProf;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}

