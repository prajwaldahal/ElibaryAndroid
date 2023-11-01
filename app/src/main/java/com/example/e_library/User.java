package com.example.e_library;

public class User {
    private String id;
    private String name;

    public User(String id, String userName) {
        this.id = id;
        name = userName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return name;
    }

    public void setUserName(String userName) {
name = userName;
    }
}
