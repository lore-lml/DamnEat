package com.damn.polito.commonresources.beans;

public class Customer {
    private String customerName;
    private String customerID;
    private String customerMail;
    private String customerPhone;
    private String customerAddress;
    private String customerPhoto;

    public Customer(String customerName, String customerAddress){
        this.customerAddress = customerAddress;
        this.customerName = customerName;
    }

    public Customer() {
    }

    public Customer(String customerName, String customerID, String customerMail, String customerPhone, String customerAddress, String customerPhoto) {
        this.customerName = customerName;
        this.customerID = customerID;
        this.customerMail = customerMail;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.customerPhoto = customerPhoto;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getCustomerMail() {
        return customerMail;
    }

    public void setCustomerMail(String customerMail) {
        this.customerMail = customerMail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerPhoto() {
        if(customerPhoto == null)
            return "NO_PHOTO";
        return customerPhoto;
    }

    public void setCustomerPhoto(String customerPhoto) {
        this.customerPhoto = customerPhoto;
    }
}
