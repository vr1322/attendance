package com.example.attendance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

public class EmpMarkAttendanceActivity extends AppCompatActivity {

    private TextView tvEmployeeLocation, tvOfficeLocation, tvAttendanceStatus;
    private Button btnMarkAttendance;

    private FusedLocationProviderClient fusedLocationClient;
    private double officeLat, officeLon;
    private float allowedRadius = 100; // Radius in meters

    private static final String OFFICE_LOCATION_URL = "https://yourdomain.com/get_office_location.php?company_code=1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_mark_attendance);

        tvEmployeeLocation = findViewById(R.id.tv_employee_location);
        tvOfficeLocation = findViewById(R.id.tv_office_location);
        tvAttendanceStatus = findViewById(R.id.tv_attendance_status);
        btnMarkAttendance = findViewById(R.id.btn_mark_attendance);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fetchOfficeLocation(); // Step 1: Fetch office location from database

        btnMarkAttendance.setOnClickListener(v -> markAttendance());
    }

    // Step 1: Fetch Office Location from API
    private void fetchOfficeLocation() {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, OFFICE_LOCATION_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            officeLat = jsonObject.getDouble("latitude");
                            officeLon = jsonObject.getDouble("longitude");
                            tvOfficeLocation.setText("Office Location: " + officeLat + ", " + officeLon);
                            getEmployeeLocation();
                        } else {
                            tvAttendanceStatus.setText("Office location not found.");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        tvAttendanceStatus.setText("Error parsing office location.");
                    }
                },
                error -> {
                    tvAttendanceStatus.setText("Failed to fetch office location.");
                    Log.e("VolleyError", error.toString());
                });

        queue.add(request);
    }

    // Step 2: Fetch Employee Location using GPS
    @SuppressLint("MissingPermission")
    private void getEmployeeLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    double empLat = location.getLatitude();
                    double empLon = location.getLongitude();
                    tvEmployeeLocation.setText("Current Location: " + empLat + ", " + empLon);

                    // Step 3: Compare with Office Location
                    float[] results = new float[1];
                    Location.distanceBetween(empLat, empLon, officeLat, officeLon, results);
                    float distance = results[0];

                    if (distance <= allowedRadius) {
                        tvAttendanceStatus.setText("You are within office area. Attendance can be marked.");
                        btnMarkAttendance.setEnabled(true);
                    } else {
                        tvAttendanceStatus.setText("You are outside the office area.");
                        btnMarkAttendance.setEnabled(false);
                    }
                } else {
                    tvAttendanceStatus.setText("Unable to fetch location.");
                }
            }
        });
    }

    private void markAttendance() {
        Toast.makeText(this, "Attendance Marked Successfully!", Toast.LENGTH_SHORT).show();
        // Here, add API call or database insertion code for attendance marking
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getEmployeeLocation();
            } else {
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
