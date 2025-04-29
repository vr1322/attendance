package com.example.attendance;

public class OvertimeRequest {
    private int id;
    private String employeeName;
    private double overtimeHours;
    private double overtimeRate;
    private double totalAmount;

    public OvertimeRequest(int id, String employeeName, double overtimeHours, double overtimeRate, double totalAmount) {
        this.id = id;
        this.employeeName = employeeName;
        this.overtimeHours = overtimeHours;
        this.overtimeRate = overtimeRate;
        this.totalAmount = totalAmount;
    }

    public int getId() { return id; }
    public String getEmployeeName() { return employeeName; }
    public double getOvertimeHours() { return overtimeHours; }
    public double getOvertimeRate() { return overtimeRate; }
    public double getTotalAmount() { return totalAmount; }
}