package com.example.aplicacionintermediaprueba;

public class ContactModel {
    private int id;
    private String phoneNo;
    private String name;

    // Constructor
    public ContactModel(int id, String name, String phoneNo) {
        this.id = id;
        this.name = name;
        this.phoneNo = phoneNo;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getName() {
        return name;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }
}

