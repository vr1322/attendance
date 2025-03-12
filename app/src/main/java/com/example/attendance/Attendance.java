package com.example.attendance;

public class Attendance {

    private String employeeId;
    private String employeeName;
    private String branch;
    private String inTime;
    private String outTime;
    private String attendanceStatus;
    private String geofencedStatus;
    private String date;

    // Constructor
    public Attendance(String employeeId, String employeeName, String inTime, String outTime,
                      String attendanceStatus, String geofencedStatus, String date) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.inTime = inTime;
        this.outTime = outTime;
        this.attendanceStatus = attendanceStatus;
        this.geofencedStatus = geofencedStatus;
        this.date = date;
    }

    // Getters
    public String getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getBranch() {
        return branch;
    }

    public String getInTime() {
        return inTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public String getGeofencedStatus() {
        return geofencedStatus;
    }

    public String getDate() {
        return date;
    }

    // Setters (if needed)
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public void setGeofencedStatus(String geofencedStatus) {
        this.geofencedStatus = geofencedStatus;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
