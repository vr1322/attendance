package com.example.attendance;

import java.io.Serializable;

public class Employee implements Serializable {
    private String name;
    private String designation;
    private String id;
    private boolean isParkingAvailable;
    private boolean isParkingAssigned;
    private String phoneNumber;
    private String profile_pic;  // ✅ Field for storing profile picture URI or URL

    // Constructor
    public Employee(String name, String designation, String id, boolean isParkingAvailable, boolean isParkingAssigned, String phoneNumber, String profile_pic) {
        this.name = name;
        this.designation = designation;
        this.id = id;
        this.isParkingAvailable = isParkingAvailable;
        this.isParkingAssigned = isParkingAssigned;
        this.phoneNumber = phoneNumber;
        this.profile_pic = profile_pic;
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

    // ✅ Corrected Getter and Setter for profile_pic
    public String getProfilePic() {
        return profile_pic;
    }

    public void setProfilePic(String profile_pic) {
        this.profile_pic = profile_pic;
    }
}
