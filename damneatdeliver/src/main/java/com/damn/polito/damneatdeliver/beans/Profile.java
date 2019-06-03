package com.damn.polito.damneatdeliver.beans;

public class Profile {

    private String name;
    private String mail;
    private String phone;
    private String description;
    private String bitmapProf;
    private String notificationId;
    private Boolean state;
    private Double latitude, longitude;
    private Long positionTime;

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

    public Long getPositionTime() {
        return positionTime;
    }

    public void setPositionTime(Long positionTime) {
        this.positionTime = positionTime;
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

    public String getBitmapProf() {
        if(bitmapProf==null)
            return "NO_PHOTO";
        return bitmapProf;
    }

    public Boolean getState() {
        if(state==null)
            return false;
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setBitmapProf(String bitmapProf) {
        this.bitmapProf = bitmapProf;
    }

    public void setPosition(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }
}
