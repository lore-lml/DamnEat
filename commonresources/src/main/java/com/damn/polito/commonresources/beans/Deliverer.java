package com.damn.polito.commonresources.beans;

import java.io.Serializable;

public class Deliverer implements Serializable {

    private String name;
    private String mail;
    private String phone;
    private String description;
    private Double latitude, longitude;
    private String bitmapProf;
    private boolean state, expanded = false;
    private int distance;
    private Long positionTime;

    private String key;

    public Deliverer(){}

    public Deliverer(String name, String mail, String phone, String description, String bitmapProf, Double latitude, Double longitude) {
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

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int distance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
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

    public void changeExpanded() { expanded = !expanded; }

    public boolean Expanded() { return expanded; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Deliverer)) return false;

        Deliverer deliverer = (Deliverer) o;

        return key.equals(deliverer.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    public Long getPositionTime() {
        return positionTime;
    }

    public void setPositionTime(Long positionTime) {
        this.positionTime = positionTime;
    }
}

