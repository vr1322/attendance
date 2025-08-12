package com.example.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GeoFenceAttendanceActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private TextView currentTimeText, branchStatus, attendanceStatus, screenTitle;
    private Button btnInTime, btnOutTime;

    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler = new Handler();

    private double branchLat, branchLng;
    private float branchRadius;
    private boolean locationReady = false;

    private String employeeId, companyCode, employeeName, branchName;

    private final String GET_BRANCH_URL = "https://devonix.io/ems_api/get_geo_branch_location.php";
    private final String MARK_ATTENDANCE_URL = "https://devonix.io/ems_api/mark_geo_attendance.php";
    private final String CHECK_ATTENDANCE_URL = "https://devonix.io/ems_api/check_attendance_status.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fence_attendance);

        currentTimeText = findViewById(R.id.currentTime);
        branchStatus = findViewById(R.id.branchStatus);
        attendanceStatus = findViewById(R.id.attendanceStatus);
        btnInTime = findViewById(R.id.btnInTime);
        btnOutTime = findViewById(R.id.btnOutTime);
        screenTitle = findViewById(R.id.emp_list_txt);

        screenTitle.setText("Mark Attendance");

        ImageView backBtn = findViewById(R.id.back);
        backBtn.setOnClickListener(v -> onBackPressed());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        loadSession();

        if (employeeId == null || employeeId.isEmpty() || companyCode == null || companyCode.isEmpty()) {
            Toast.makeText(this, "Invalid employee session. Please login again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Toast.makeText(this, "Welcome, " + employeeName, Toast.LENGTH_SHORT).show();

        btnInTime.setOnClickListener(v -> markAttendance("in"));
        btnOutTime.setOnClickListener(v -> markAttendance("out"));

        checkLocationPermission();
    }

    private void loadSession() {
        Intent intent = getIntent();
        employeeId = intent.getStringExtra("employee_id");
        companyCode = intent.getStringExtra("company_code");
        employeeName = intent.getStringExtra("employee_name");

        if (employeeId == null || employeeId.isEmpty()) {
            SharedPreferences sharedPreferences = getSharedPreferences("EmployeeSession", MODE_PRIVATE);
            employeeId = sharedPreferences.getString("employee_id", "");
            companyCode = sharedPreferences.getString("company_code", "");
            employeeName = sharedPreferences.getString("employee_name", "Employee");
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchBranchLocation();
        }
    }

    private void fetchBranchLocation() {
        StringRequest request = new StringRequest(Request.Method.POST, GET_BRANCH_URL, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                if (obj.getString("status").equals("success")) {
                    branchLat = obj.getDouble("latitude");
                    branchLng = obj.getDouble("longitude");
                    branchRadius = (float) obj.getDouble("radius");
                    branchName = obj.getString("branch_name");

                    getCurrentLocation();
                } else {
                    Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("BranchFetchError", e.getMessage());
            }
        }, error -> {
            Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("BranchNetworkError", error.toString());
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("employee_id", employeeId);
                map.put("company_code", companyCode);
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                float[] distance = new float[1];
                Location.distanceBetween(
                        location.getLatitude(), location.getLongitude(),
                        branchLat, branchLng, distance
                );

                if (distance[0] <= branchRadius) {
                    locationReady = true;
                    branchStatus.setText("✅ Inside branch area");
                    btnInTime.setEnabled(true);
                    btnOutTime.setEnabled(true);
                } else {
                    locationReady = false;
                    branchStatus.setText("❌ Outside branch area");
                    btnInTime.setEnabled(false);
                    btnOutTime.setEnabled(false);
                }

                // Now check today's attendance for this employee
                checkAttendanceStatus();
            } else {
                Toast.makeText(this, "Turn on GPS to mark attendance", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
    }

    private void markAttendance(String type) {
        String timeNow = getCurrentTime();
        String dateToday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        StringRequest request = new StringRequest(Request.Method.POST, MARK_ATTENDANCE_URL, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                String message = obj.optString("message", "Unknown response");
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                checkAttendanceStatus(); // Refresh attendance after marking

            } catch (Exception e) {
                Toast.makeText(this, "Response parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("MarkAttendance", "Parse error: " + e.getMessage());
            }
        }, error -> {
            Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("VolleyError", error.toString());
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("employee_id", employeeId);
                map.put("employee_name", employeeName);
                map.put("company_code", companyCode);
                map.put("branch", branchName);
                map.put("attendance_status", "Present");
                map.put("geofenced_status", locationReady ? "1" : "0");
                map.put("date", dateToday);
                map.put("in_time", type.equals("in") ? timeNow : "");
                map.put("out_time", type.equals("out") ? timeNow : "");
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void checkAttendanceStatus() {
        String dateToday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        StringRequest request = new StringRequest(Request.Method.POST, CHECK_ATTENDANCE_URL, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                boolean status = obj.optBoolean("status", false);

                if (status) {
                    String inTime = obj.optString("in_time", "--");
                    String outTime = obj.optString("out_time", "--");
                    attendanceStatus.setText("In-Time: " + inTime + " | Out-Time: " + outTime);

                    if (!inTime.equals("--") && outTime.equals("--")) {
                        btnInTime.setEnabled(false);
                        btnOutTime.setEnabled(true);
                    } else if (!inTime.equals("--") && !outTime.equals("--")) {
                        btnInTime.setEnabled(false);
                        btnOutTime.setEnabled(false);
                    }
                } else {
                    attendanceStatus.setText("Attendance not marked today");
                    btnInTime.setEnabled(true);
                    btnOutTime.setEnabled(false);
                }

            } catch (Exception e) {
                Log.e("CheckAttendance", "Parse error: " + e.getMessage());
            }
        }, error -> {
            Log.e("CheckAttendance", "Volley error: " + error.getMessage());
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("employee_id", employeeId);
                map.put("company_code", companyCode);
                map.put("date", dateToday);
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private final Runnable timeUpdater = new Runnable() {
        @Override
        public void run() {
            currentTimeText.setText("Current Time: " + getCurrentTime());
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(timeUpdater);
        getCurrentLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(timeUpdater);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchBranchLocation();
        } else {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
        }
    }
}
