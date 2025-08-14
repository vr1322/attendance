package com.example.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SupervisorAttendanceActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    private TextView currentTimeText, branchStatus, attendanceStatus, screenTitle;
    private Button btnInTime, btnOutTime;
    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler = new Handler();

    private double branchLat, branchLng;
    private float branchRadius;
    private boolean locationReady = false;

    private String supervisorId, companyCode, supervisorName, branchName;

    private final String GET_BRANCH_URL = "https://devonix.io/ems_api/get_geo_branch_location.php";
    private final String MARK_ATTENDANCE_URL = "https://devonix.io/ems_api/mark_geo_attendance.php";
    private final String CHECK_ATTENDANCE_URL = "https://devonix.io/ems_api/check_attendance_status.php";

    private final ActivityResultLauncher<Intent> locationSettingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> checkLocationEnabled());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_attendance);

        currentTimeText = findViewById(R.id.currentTime);
        branchStatus = findViewById(R.id.branchStatus);
        attendanceStatus = findViewById(R.id.attendanceStatus);
        btnInTime = findViewById(R.id.btnSupervisorInTime);
        btnOutTime = findViewById(R.id.btnSupervisorOutTime);
        screenTitle = findViewById(R.id.supervisor_attendance_title);
        screenTitle.setText("Supervisor Attendance");

        ImageView backBtn = findViewById(R.id.back);
        backBtn.setOnClickListener(v -> onBackPressed());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        loadSession();

        if (supervisorId == null || supervisorId.isEmpty() || companyCode == null || companyCode.isEmpty()) {
            Toast.makeText(this, "Invalid supervisor session. Please login again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Toast.makeText(this, "Welcome, " + supervisorName, Toast.LENGTH_SHORT).show();

        btnInTime.setEnabled(false);
        btnOutTime.setEnabled(false);

        btnInTime.setOnClickListener(v -> markAttendance("in"));
        btnOutTime.setOnClickListener(v -> markAttendance("out"));

        checkLocationPermission();
    }

    private void loadSession() {
        Intent intent = getIntent();
        supervisorId = intent.getStringExtra("supervisor_id");
        companyCode = intent.getStringExtra("company_code");
        supervisorName = intent.getStringExtra("supervisor_name");

        if (supervisorId == null || supervisorId.isEmpty()) {
            SharedPreferences sp = getSharedPreferences("SupervisorSession", MODE_PRIVATE);
            supervisorId = sp.getString("supervisor_id", "");
            companyCode = sp.getString("company_code", "");
            supervisorName = sp.getString("supervisor_name", "Supervisor");
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchBranchLocation();
        }
    }

    private void fetchBranchLocation() {
        StringRequest request = new StringRequest(Request.Method.POST, GET_BRANCH_URL, response -> {
            Log.d("BranchAPIResponse", response);
            try {
                JSONObject obj = new JSONObject(response);
                if (obj.getString("status").equals("success")) {
                    branchLat = obj.getDouble("latitude");
                    branchLng = obj.getDouble("longitude");
                    branchRadius = (float) obj.getDouble("radius");
                    branchName = obj.getString("branch_name");

                    getCurrentLocation();
                } else {
                    branchName = null;
                    Toast.makeText(this, "Branch not found: " + obj.optString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                branchName = null;
                Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("BranchFetchError", e.getMessage(), e);
            }
        }, error -> {
            branchName = null;
            Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("BranchNetworkError", error.toString());
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("user_id", supervisorId);
                map.put("user_role", "Supervisor");
                map.put("company_code", companyCode);
                return map;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && branchName != null) {
                float[] distance = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        branchLat, branchLng, distance);

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

                checkAttendanceStatus();
            } else {
                Toast.makeText(this, "Turn on GPS and make sure branch is loaded", Toast.LENGTH_SHORT).show();
                openLocationSettings();
            }
        });
    }

    private void openLocationSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        locationSettingsLauncher.launch(intent);
    }

    private void checkLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = false;
        try { gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); } catch (Exception ignored) {}
        if (gpsEnabled) getCurrentLocation();
        else Toast.makeText(this, "Please enable GPS to mark attendance", Toast.LENGTH_SHORT).show();
    }

    private void markAttendance(String type) {
        if (branchName == null || branchName.trim().isEmpty()) {
            Toast.makeText(this, "Branch not loaded. Cannot mark attendance.", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeNow = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String dateToday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        StringRequest request = new StringRequest(Request.Method.POST, MARK_ATTENDANCE_URL, response -> {
            Log.d("MarkAttendanceResponse", response);
            try {
                JSONObject obj = new JSONObject(response);
                Toast.makeText(this, obj.optString("message", "Unknown response"), Toast.LENGTH_SHORT).show();
                checkAttendanceStatus();
            } catch (Exception e) {
                Toast.makeText(this, "Response parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("MarkAttendance", "Parse error: " + e.getMessage(), e);
            }
        }, error -> {
            Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("VolleyError", error.toString());
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("employee_id", supervisorId);
                map.put("employee_name", supervisorName);
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
            Log.d("CheckAttendanceResponse", response);
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
                Log.e("CheckAttendance", "Parse error: " + e.getMessage(), e);
            }
        }, error -> Log.e("CheckAttendance", "Volley error: " + error.getMessage())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("employee_id", supervisorId);
                map.put("company_code", companyCode);
                map.put("date", dateToday);
                return map;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private final Runnable timeUpdater = new Runnable() {
        @Override
        public void run() {
            currentTimeText.setText("Current Time: " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
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
