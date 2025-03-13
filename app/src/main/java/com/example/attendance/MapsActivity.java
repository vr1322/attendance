package com.example.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SeekBar radiusSeekBar;
    private TextView radiusValue, branchAddress;
    private Button saveButton;
    private SearchView searchView;
    private ImageView back;
    private LatLng selectedLocation = null;
    private Marker tempMarker = null;
    private Circle tempCircle = null;
    private float selectedRadius = 50; // Default radius

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        back = findViewById(R.id.back);
        radiusSeekBar = findViewById(R.id.radiusSeekBar);
        radiusValue = findViewById(R.id.radiusValue);
        branchAddress = findViewById(R.id.branchAddress);
        saveButton = findViewById(R.id.saveButton);
        searchView = findViewById(R.id.searchView);

        // Back button
        back.setOnClickListener(v -> finish());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Handle radius selection
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (selectedLocation != null) {
                    selectedRadius = Math.max(progress, 50);
                    radiusValue.setText(selectedRadius + " meters");
                    updateTempCircle();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Save selected location
        saveButton.setOnClickListener(v -> saveLocation());

        // Handle search query
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(true);

        // Enable location if permission granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Load existing branches
        loadStoredBranches();

        // Handle user tap to select a location
        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            selectedRadius = 50;
            updateTempMarker(latLng);
            fetchAddress(latLng);
            updateSeekBar();
        });
    }

    private void loadStoredBranches() {
        SharedPreferences prefs = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        String companyCode = prefs.getString("company_code", "").trim();

        if (companyCode.isEmpty()) {
            Toast.makeText(this, "Company code not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.144.102/ems_api/get_branches.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (!jsonResponse.getString("status").equals("success")) {
                            return;
                        }

                        JSONArray branches = jsonResponse.getJSONArray("branches");
                        for (int i = 0; i < branches.length(); i++) {
                            JSONObject branch = branches.getJSONObject(i);
                            double lat = branch.getDouble("latitude");
                            double lng = branch.getDouble("longitude");
                            int radius = branch.getInt("radius");
                            String name = branch.getString("branch_name");

                            LatLng branchLocation = new LatLng(lat, lng);
                            mMap.addMarker(new MarkerOptions()
                                    .position(branchLocation)
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))); // Green marker

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(branchLocation, 15));

                            mMap.addCircle(new CircleOptions()
                                    .center(branchLocation)
                                    .radius(radius)
                                    .strokeWidth(3f)
                                    .strokeColor(0xFF00FF00)  // Green border
                                    .fillColor(0x2200FF00));  // Transparent green fill
                            ;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("MapsActivity", "Network error: " + error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("company_code", companyCode);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void searchLocation(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng searchedLocation = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, 15));
                updateTempMarker(searchedLocation);
                fetchAddress(searchedLocation);
            } else {
                Toast.makeText(this, "Location not found!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateTempMarker(LatLng latLng) {
        if (tempMarker != null) tempMarker.remove();
        tempMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title("Selected Location"));
        updateTempCircle();
    }

    private void updateTempCircle() {
        if (tempCircle != null) tempCircle.remove();
        if (selectedLocation != null) {
            tempCircle = mMap.addCircle(new CircleOptions()
                    .center(selectedLocation)
                    .radius(selectedRadius)
                    .strokeWidth(3f)
                    .strokeColor(0xFF0000FF)
                    .fillColor(0x220000FF));
        }
    }

    private void fetchAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            branchAddress.setText(addresses.get(0).getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void updateSeekBar() {
        if (radiusSeekBar != null) {
            radiusSeekBar.setProgress((int) selectedRadius);
            radiusValue.setText(selectedRadius + " meters");
        }
    }
    private void saveLocation() {
        Intent intent = new Intent();
        intent.putExtra("selected_address", branchAddress.getText().toString());
        intent.putExtra("selected_radius", (int) selectedRadius);
        intent.putExtra("selected_latitude", selectedLocation.latitude);
        intent.putExtra("selected_longitude", selectedLocation.longitude);
        setResult(RESULT_OK, intent);
        finish();
    }
}
