package com.example.attendance;

public class Branch {
    private int id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private int radius;

    public Branch(int id, String name, String address, double latitude, double longitude, int radius) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getRadius() {
        return radius;
    }
}
