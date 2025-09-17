package com.example.attendance;

import java.util.List;

public class AllReportsModel {
    private String employeeName, employeeId, branchName, title, description, date;
    private List<String> images;

    public AllReportsModel(String employeeName, String employeeId, String branchName,
                           String title, String description, List<String> images, String date) {
        this.employeeName = employeeName;
        this.employeeId = employeeId;
        this.branchName = branchName;
        this.title = title;
        this.description = description;
        this.images = images;
        this.date = date;
    }

    public String getEmployeeName() { return employeeName; }
    public String getEmployeeId() { return employeeId; }
    public String getBranchName() { return branchName; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getImages() { return images; }
    public String getDate() { return date; }
}
