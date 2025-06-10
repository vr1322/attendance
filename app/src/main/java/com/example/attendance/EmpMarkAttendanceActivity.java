package com.example.attendance;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.*;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmpMarkAttendanceActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private FusedLocationProviderClient fusedLocationClient;

    private Button markButton;

    String employee_id, company_code, employee_name, branch_name;

    double officeLat, officeLng, officeRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_mark_attendance);

        markButton = findViewById(R.id.markButton);

        // Retrieve company_code from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);
        company_code = sharedPref.getString("company_code", null);
        String email = sharedPref.getString("email", null); // Assuming email is stored in SharedPreferences

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Fetch employee details from SharedPreferences
        getEmployeeDetails(email);

        // Set click listener for attendance marking
        markButton.setOnClickListener(v -> checkLocationPermission());

        // Retrieve employee branch from database using email
        getEmployeeBranchFromDatabase(email);
    }

    private void getEmployeeDetails(String email) {
        // Retrieve employee details from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);

        // Assuming employee_id, employee_name, and branch_name are stored in SharedPreferences
        employee_id = sharedPref.getString("employee_id", null);
        employee_name = sharedPref.getString("employee_name", null);
        branch_name = sharedPref.getString("branch_name", null);

        // If the branch_name is null or empty, fetch it from the server
        if (branch_name == null || branch_name.isEmpty()) {
            fetchEmployeeBranchFromServer(email);
        } else {
            // If branch_name is found, fetch branch location from the database
            getBranchLocation();
        }
    }

    private void fetchEmployeeBranchFromServer(String email) {
        String url = "https://devonix.io/ems_api/get_employee_branch.php";
        Log.d("Network Request", "email: " + email + ", company_code: " + company_code);


        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            // Fetch branch_name from response
                            branch_name = obj.getString("branch_name");

                            // Save branch_name in SharedPreferences
                            SharedPreferences sharedPref = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("branch_name", branch_name);
                            editor.apply();

                            // Now fetch branch location
                            getBranchLocation();
                        } else {
                            Toast.makeText(this, "Employee not found or invalid data", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error fetching employee branch data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error fetching employee branch data", Toast.LENGTH_SHORT).show();
                    Log.e("EmployeeBranchError", error.toString());
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("email", email); // Pass email to fetch the branch
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }


    private void getEmployeeBranchFromDatabase(String email) {
        String url = "https://devonix.io/ems_api/get_employee_branch_from_email.php";  // This URL should query the employees table
        Log.d("Network Request", "email: " + email + ", company_code: " + company_code);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            // Get the employee's branch name from the response
                            branch_name = obj.getString("branch_name");

                            // Save the branch_name in SharedPreferences
                            SharedPreferences sharedPref = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("branch_name", branch_name);
                            editor.apply();

                            // Get branch location using the branch name
                            getBranchLocation();
                        } else {
                            Toast.makeText(this, "Employee not found or invalid data", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error fetching employee branch data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error fetching employee branch data", Toast.LENGTH_SHORT).show();
                    Log.e("EmployeeBranchError", error.toString());
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("email", email);
                map.put("company_code", company_code);
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void getBranchLocation() {
        String url = "https://devonix.io/ems_api/get_branch_location.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {
                            JSONObject data = obj.getJSONObject("data");

                            officeLat = data.getDouble("latitude");
                            officeLng = data.getDouble("longitude");
                            officeRadius = data.getDouble("radius");

                            Toast.makeText(this, "Branch location loaded", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Branch location not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing branch location", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error fetching branch location", Toast.LENGTH_SHORT).show();
                    Log.e("BranchLocationError", error.toString());
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("branch_name", branch_name);
                map.put("company_code", company_code);
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void markAttendance() {
        String url = "https://devonix.io/ems_api/geo_emp_mark_attendance.php";

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Attendance marked successfully", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(this, "Failed to mark attendance", Toast.LENGTH_SHORT).show();
                    Log.e("MarkAttendanceError", error.toString());
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("employee_id", employee_id);
                map.put("company_code", company_code);
                map.put("employee_name", employee_name);
                map.put("branch", branch_name);
                map.put("date", currentDate);
                map.put("in_time", currentTime);
                map.put("attendance_status", "Present");
                map.put("geofenced_status", "1");
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void checkLocationPermission() {
        // Check if location permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, proceed with marking attendance
            markAttendance();
        }
    }

    // Handle the result of permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with marking attendance
                markAttendance();
            } else {
                // Permission denied, show a message or handle appropriately
                Toast.makeText(this, "Location permission is required to mark attendance", Toast.LENGTH_SHORT).show();
            }
        }
    }
}