package com.example.attendance;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.attendance.databinding.ActivityEmpMarkAttendanceBinding;
import com.google.android.gms.location.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmpMarkAttendanceActivity extends AppCompatActivity {

    private ActivityEmpMarkAttendanceBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final String ATTENDANCE_URL = "https://devonix.io/ems_api/emp_mark_attendance.php";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmpMarkAttendanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("EmployeeSession", Context.MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check session data before allowing attendance marking
        if (!isSessionValid()) {
            showSessionErrorDialog();
            return;
        }

        // Button click event to mark attendance
        binding.markAttendanceBtn.setOnClickListener(v -> requestLocationPermission());
    }

    // Check if SharedPreferences contains valid session data
    private boolean isSessionValid() {
        String employeeId = sharedPreferences.getString("employee_id", "");
        String employeeName = sharedPreferences.getString("employee_name", "");
        String companyCode = sharedPreferences.getString("company_code", "");
        String branchName = sharedPreferences.getString("branch_name", "");

        // Log session data for debugging
        Log.d("EmployeeSession", "ID: " + employeeId + ", Name: " + employeeName + ", Company Code: " + companyCode + ", Branch: " + branchName);

        return !employeeId.isEmpty() && !employeeName.isEmpty() && !companyCode.isEmpty() && !branchName.isEmpty();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            fetchCurrentLocation();
        }
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            binding.locationText.setText("Lat: " + latitude + ", Lng: " + longitude);
                            sendAttendanceToServer(latitude, longitude);
                        } else {
                            Toast.makeText(this, "Unable to fetch location. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendAttendanceToServer(final double latitude, final double longitude) {
        RequestQueue queue = Volley.newRequestQueue(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        String currentTime = timeFormat.format(new Date());

        // Fetch session data again to ensure accuracy
        String employeeId = sharedPreferences.getString("employee_id", "");
        String employeeName = sharedPreferences.getString("employee_name", "");
        String companyCode = sharedPreferences.getString("company_code", "");
        String branchName = sharedPreferences.getString("branch_name", "");

        if (employeeId.isEmpty() || employeeName.isEmpty() || companyCode.isEmpty() || branchName.isEmpty()) {
            showSessionErrorDialog();
            return;
        }

        Log.d("Attendance", "Sending Data: EmployeeID=" + employeeId + ", CompanyCode=" + companyCode + ", Branch=" + branchName);

        StringRequest request = new StringRequest(Request.Method.POST, ATTENDANCE_URL,
                response -> {
                    Log.d("AttendanceResponse", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if ("success".equals(status)) {
                            double distance = jsonResponse.optDouble("distance", 0.0);
                            int geofencedStatus = jsonResponse.optInt("geofenced_status", 0);

                            Toast.makeText(this, "Attendance Marked!\nDistance: " + distance + "m\nGeofenced: " + (geofencedStatus == 1 ? "Yes" : "No"), Toast.LENGTH_LONG).show();
                        } else {
                            showErrorDialog("Attendance Failed", message);
                        }
                    } catch (JSONException e) {
                        showErrorDialog("Server Error", "Invalid response from server.");
                        Log.e("AttendanceError", "JSON Parsing Error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("VolleyError", "Server Error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"));
                    showErrorDialog("Server Error", "Failed to mark attendance. Please try again.");
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("employee_id", employeeId);
                params.put("employee_name", employeeName);
                params.put("company_code", companyCode);
                params.put("branch", branchName);
                params.put("in_time", currentTime);
                params.put("date", currentDate);
                params.put("attendance_status", "Present");
                params.put("latitude", String.valueOf(latitude));
                params.put("longitude", String.valueOf(longitude));
                return params;
            }
        };

        queue.add(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(this, "Permission denied! Unable to mark attendance.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showSessionErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Session Error")
                .setMessage("Employee session data is missing. Please log in again.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
