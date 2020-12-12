package com.example.scoop2u;

public class User {

    public String username,accountType,email,currentDriverID;
    public double longitude,latitude;
    public User(){

    }
    public User(String username, String accountType, String email, String currentDriverID,double longitude, double latitude) {
        this.accountType = accountType;
        this.username = username;
        this.email = email;
        this.currentDriverID = currentDriverID;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String at) {
        accountType = at;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double l) {
        longitude = l;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double l) {
        latitude = l;
    }
}
