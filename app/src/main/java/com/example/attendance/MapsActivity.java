package com.example.attendance;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;


import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SeekBar radiusSeekBar;
    private TextView radiusValue, branchAddress;
    private Button saveButton;
    private SearchView searchView; // Added SearchView

    private LatLng selectedLocation = null;
    private Marker tempMarker = null;
    private Circle tempCircle = null;
    private float selectedRadius = 50; // Default radius

    private SharedPreferences sharedPreferences;
    private final List<LatLng> savedLocations = new ArrayList<>();
    private final HashMap<LatLng, Float> radiusMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        radiusSeekBar = findViewById(R.id.radiusSeekBar);
        radiusValue = findViewById(R.id.radiusValue);
        branchAddress = findViewById(R.id.branchAddress);
        saveButton = findViewById(R.id.saveButton);
        searchView = findViewById(R.id.searchView); // Initialize SearchView

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        sharedPreferences = getSharedPreferences("GeofenceData", Context.MODE_PRIVATE);
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Change hint text color
                ((TextView) searchView.findViewById(androidx.appcompat.R.id.search_src_text))
                        .setHintTextColor(getResources().getColor(android.R.color.darker_gray));

                // Change search text color
                ((TextView) searchView.findViewById(androidx.appcompat.R.id.search_src_text))
                        .setTextColor(getResources().getColor(android.R.color.black));
            }
        });

// Change search icon and close (cross) icon color
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setColorFilter(getResources().getColor(android.R.color.black));

        ImageView closeIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeIcon.setColorFilter(getResources().getColor(android.R.color.black));


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

        saveButton.setOnClickListener(v -> saveLocation());

        // Implement SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                searchView.clearFocus(); // Hide keyboard after search
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
        // Move the compass icon below SearchView
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setPadding(0, 1200, 30, 0); // Adjust padding (top, left, right, bottom)
        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            selectedRadius = 50;
            updateTempMarker(latLng);
            fetchAddress(latLng);
            updateSeekBar();
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);
        View locationButton = ((View) findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

        if (locationButton != null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE); // Move to bottom
            params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);    // Move to right
            params.setMargins(0, 0, 30, 0); // Adjust margins (right, bottom)
            locationButton.setLayoutParams(params);
        }
    }

    private void searchLocation(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng searchedLocation = new LatLng(address.getLatitude(), address.getLongitude());

                // Move camera to searched location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, 15));

                // Remove old markers
                if (tempMarker != null) tempMarker.remove();
                if (tempCircle != null) tempCircle.remove();

                // Add new marker
                tempMarker = mMap.addMarker(new MarkerOptions()
                        .position(searchedLocation)
                        .title(locationName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                selectedLocation = searchedLocation;
                fetchAddress(searchedLocation);
            } else {
                Toast.makeText(this, "Location not found!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error searching location!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTempMarker(LatLng latLng) {
        if (tempMarker != null) tempMarker.remove();
        tempMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title(String.valueOf(selectedLocation)));
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
            if (addresses != null && !addresses.isEmpty()) {
                branchAddress.setText(addresses.get(0).getAddressLine(0));
            } else {
                branchAddress.setText("Address not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            branchAddress.setText("Error fetching address");
        }
    }

    private void updateSeekBar() {
        radiusSeekBar.setProgress((int) selectedRadius);
        radiusValue.setText(selectedRadius + " meters");
    }

    private void saveLocation() {
        if (selectedLocation != null) {
            savedLocations.add(selectedLocation);
            radiusMap.put(selectedLocation, selectedRadius);

            if (tempMarker != null) tempMarker.remove();
            if (tempCircle != null) tempCircle.remove();

            mMap.addMarker(new MarkerOptions()
                    .position(selectedLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title("Saved Location"));

            mMap.addCircle(new CircleOptions()
                    .center(selectedLocation)
                    .radius(selectedRadius)
                    .strokeWidth(3f)
                    .strokeColor(0xFF00FF00)
                    .fillColor(0x2200FF00));

            SharedPreferences.Editor editor = sharedPreferences.edit();
            StringBuilder savedData = new StringBuilder();
            for (LatLng loc : savedLocations) {
                savedData.append(loc.latitude)
                        .append(",").append(loc.longitude)
                        .append(",").append(radiusMap.getOrDefault(loc, 50f))
                        .append("; ");
            }
            editor.putString("SavedBranches", savedData.toString());
            editor.apply();

            Toast.makeText(this, "Location Saved!", Toast.LENGTH_SHORT).show();

            selectedLocation = null;
            tempMarker = null;
            tempCircle = null;
            branchAddress.setText("");
            radiusValue.setText("50 meters");
        } else {
            Toast.makeText(this, "Select a location first", Toast.LENGTH_SHORT).show();
        }
    }
}
