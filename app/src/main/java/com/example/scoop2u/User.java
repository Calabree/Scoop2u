package com.example.scoop2u;

public class User {
    public String username,accountType,email;
    public double longitude, latitude;
    public User(){

    }

    public User(String username, String accountType, String email) {
        this.accountType = accountType;
        this.username = username;
        this.email = email;
    }

    public User(String username, String accountType, String email, Double lng, Double lat) {
        this.accountType = accountType;
        this.username = username;
        this.email = email;
        this.longitude=lng;
        this.latitude=lat;
    }

}
