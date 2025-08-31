package com.example.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GeoFenceAttendanceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;

    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyDlh0EQcVa3F-y-KfjzDMDwutn6BG2lG7g"; // Replace with your key

    private TextView currentTimeText, branchStatus, attendanceStatus, screenTitle;
    private Button btnInTime, btnOutTime, btnCaptureSelfie;
    private ImageView selfiePreview;
    private MapView mapView;
    private GoogleMap gMap;

    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler = new Handler();

    private double branchLat, branchLng;
    private float branchRadius;
    private boolean locationReady = false;
    private boolean selfieCaptured = false;

    private String employeeId, companyCode, employeeName, branchName;
    private LatLng employeeLatLng;

    private Bitmap capturedSelfieBitmap;

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
        btnCaptureSelfie = findViewById(R.id.btnCaptureSelfie);
        selfiePreview = findViewById(R.id.selfiePreview);
        screenTitle = findViewById(R.id.emp_list_txt);
        mapView = findViewById(R.id.employeeMapView);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

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

        btnInTime.setOnClickListener(v -> {
            if (selfieCaptured) {
                markAttendance("in");
            } else {
                Toast.makeText(this, "Please capture selfie first", Toast.LENGTH_SHORT).show();
            }
        });

        btnOutTime.setOnClickListener(v -> {
            if (selfieCaptured) {
                markAttendance("out");
            } else {
                Toast.makeText(this, "Please capture selfie first", Toast.LENGTH_SHORT).show();
            }
        });

        btnCaptureSelfie.setOnClickListener(v -> checkCameraPermission());

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

    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            capturedSelfieBitmap = (Bitmap) extras.get("data");
            if (capturedSelfieBitmap != null) {
                selfiePreview.setImageBitmap(capturedSelfieBitmap);
                selfieCaptured = true;
                Toast.makeText(this, "Selfie captured", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
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
                Log.e("BranchFetchError", e.getMessage());
            }
        }, error -> Log.e("BranchNetworkError", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("user_id", employeeId);
                map.put("user_role", "Employee");
                map.put("company_code", companyCode);
                return map;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                employeeLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                float[] distance = new float[1];
                Location.distanceBetween(
                        employeeLatLng.latitude, employeeLatLng.longitude,
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

                updateMap();
                checkAttendanceStatus();
            } else {
                Toast.makeText(this, "Turn on GPS to mark attendance", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
    }

    private void updateMap() {
        if (gMap == null || employeeLatLng == null) return;

        LatLng branchLatLng = new LatLng(branchLat, branchLng);
        gMap.clear();

        gMap.addMarker(new MarkerOptions().position(employeeLatLng).title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        gMap.addMarker(new MarkerOptions().position(branchLatLng).title(branchName)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(employeeLatLng, 14));
        gMap.getUiSettings().setZoomControlsEnabled(true);

        // Fetch and draw route
        // Fetch and draw route
        new Thread(() -> {
            try {
                String urlStr = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                        employeeLatLng.latitude + "," + employeeLatLng.longitude +
                        "&destination=" + branchLat + "," + branchLng +
                        "&key=" + GOOGLE_MAPS_API_KEY;

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);

                JSONObject json = new JSONObject(sb.toString());
                JSONArray routes = json.getJSONArray("routes");
                if (routes.length() > 0) {
                    String polyline = routes.getJSONObject(0)
                            .getJSONObject("overview_polyline")
                            .getString("points");

                    final java.util.List<LatLng> points = decodePolyline(polyline);

                    runOnUiThread(() -> {
                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(points)
                                .width(8)
                                .color(android.graphics.Color.parseColor("#1976D2"))
                                .geodesic(true);

                        gMap.addPolyline(polylineOptions);
                    });
                }
            } catch (Exception e) {
                Log.e("RouteError", e.getMessage());
            }
        }).start();
    }

    private void markAttendance(String type) {
        String timeNow = getCurrentTime();
        String dateToday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String selfieBase64 = capturedSelfieBitmap != null ? convertBitmapToBase64(capturedSelfieBitmap) : "";

        StringRequest request = new StringRequest(Request.Method.POST, MARK_ATTENDANCE_URL, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                Toast.makeText(this, obj.optString("message", "Unknown response"), Toast.LENGTH_SHORT).show();
                checkAttendanceStatus();
            } catch (Exception e) {
                Log.e("MarkAttendance", e.getMessage());
            }
        }, error -> Log.e("VolleyError", error.toString())) {
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
                map.put("attendance_image", selfieBase64); // ✅ fixed key
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
                Log.e("CheckAttendance", e.getMessage());
            }
        }, error -> Log.e("CheckAttendance", error.toString())) {
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

    private java.util.List<LatLng> decodePolyline(String encoded) {
        java.util.List<LatLng> poly = new java.util.ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(
                    lat / 1E5, lng / 1E5
            );
            poly.add(p);
        }
        return poly;
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.gMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        handler.postDelayed(() -> currentTimeText.setText("Current Time: " + getCurrentTime()), 1000);
        getCurrentLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length == 0) {
            Toast.makeText(this, "Permission request cancelled", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (allPermissionsGranted(grantResults)) {
                    fetchBranchLocation();
                } else {
                    Toast.makeText(this, "Location permission is required to mark attendance", Toast.LENGTH_SHORT).show();
                }
                break;

            case CAMERA_PERMISSION_REQUEST_CODE:
                if (allPermissionsGranted(grantResults)) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required to capture selfie", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Utility method to check all permissions in result
     */
    private boolean allPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}