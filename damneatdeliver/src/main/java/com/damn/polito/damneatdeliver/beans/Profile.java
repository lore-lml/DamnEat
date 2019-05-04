package com.damn.polito.damneatdeliver.beans;

public class Profile {

    private String name;
    private String mail;
    private String phone;
    private String description;
    private String address;
    private String bitmapProf;

    public Profile(String name, String mail, String phone, String description, String bitmapProf) {
        this.name = name;
        this.mail = mail;
        this.phone = phone;
        this.description = description;
        this.bitmapProf = bitmapProf;
    }

    public Profile(Profile p) {
        this(p.name, p.mail, p.phone, p.description, p.bitmapProf);
    }
    public Profile() {}

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

    public String getBitmapProf() {
        return bitmapProf;
    }

    public void setBitmapProf(String bitmapProf) {
        this.bitmapProf = bitmapProf;
    }
}
