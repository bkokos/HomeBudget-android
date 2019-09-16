package com.myapp.homebudget.auth;

public class User {
    private String userName;
    private String email;
    private String password;


    public static User of(String email){
        String userName = email.split("@")[0];
        return new User(userName, email);
    }

    private User(String userName, String email) {
        this.userName = userName;
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

}
