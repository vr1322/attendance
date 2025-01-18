package com.example.attendance;

import java.io.Serializable;

public class Employee implements Serializable {
    private String name;
    private String designation;
    private String id;
    private boolean isParkingAvailable;
    private boolean isParkingAssigned;
    private String phoneNumber;
    private String photoUri;  // You can use URI if needed for images

    // Constructor
    public Employee(String name, String designation, String id, boolean isParkingAvailable, boolean isParkingAssigned, String phoneNumber, String photoUri) {
        this.name = name;
        this.designation = designation;
        this.id = id;
        this.isParkingAvailable = isParkingAvailable;
        this.isParkingAssigned = isParkingAssigned;
        this.phoneNumber = phoneNumber;
        this.photoUri = photoUri;
    }

    // Getter and Setter methods for all fields
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isParkingAvailable() {
        return isParkingAvailable;
    }

    public void setParkingAvailable(boolean parkingAvailable) {
        isParkingAvailable = parkingAvailable;
    }

    public boolean isParkingAssigned() {
        return isParkingAssigned;
    }

    public void setParkingAssigned(boolean parkingAssigned) {
        isParkingAssigned = parkingAssigned;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }
}
