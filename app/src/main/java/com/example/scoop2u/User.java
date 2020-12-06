package com.example.scoop2u;

public class User {
    public String username,accountType,email;

    public User(){

    }

    public User(String username, String accountType, String email) {
        this.accountType = accountType;
        this.username = username;
        this.email = email;
    }

}
