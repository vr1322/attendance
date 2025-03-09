package com.example.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddBranchActivity extends AppCompatActivity {

    private EditText branchName, branchAddress, radiusMeters;
    private ImageView searchLocation, back;
    private TextView addBranchText;
    private ImageButton submitBranch;
    private String companyCode = "";
    private double latitude = 0.0, longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_branch);

        branchName = findViewById(R.id.branchName);
        branchAddress = findViewById(R.id.branchAddress);
        radiusMeters = findViewById(R.id.radiusMeters);
        searchLocation = findViewById(R.id.searchLocation);
        submitBranch = findViewById(R.id.submitBranch);
        back = findViewById(R.id.back);
        addBranchText = findViewById(R.id.add_branch_text);

        back.setOnClickListener(view -> startActivity(new Intent(AddBranchActivity.this, home.class)));
        addBranchText.setOnClickListener(view -> startActivity(new Intent(AddBranchActivity.this, home.class)));
        SharedPreferences prefs = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        companyCode = prefs.getString("company_code", "");

        if (companyCode.isEmpty()) {
            Toast.makeText(this, "Company code not found!", Toast.LENGTH_SHORT).show();
        }

        // Handle location selection from MapsActivity
        ActivityResultLauncher<Intent> mapActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            String selectedAddress = data.getStringExtra("selected_address");
                            int selectedRadius = data.getIntExtra("selected_radius", 50);
                            latitude = data.getDoubleExtra("selected_latitude", 0.0);
                            longitude = data.getDoubleExtra("selected_longitude", 0.0);

                            if (selectedAddress != null) {
                                branchAddress.setText(selectedAddress);
                            } else {
                                Log.e("AddBranchActivity", "Error: Address data is null");
                            }
                            radiusMeters.setText(String.valueOf(selectedRadius));
                        } else {
                            Log.e("AddBranchActivity", "MapsActivity result was not OK or data was null");
                        }
                    }
                }
        );

        searchLocation.setOnClickListener(v -> {
            Intent intent = new Intent(AddBranchActivity.this, MapsActivity.class);
            mapActivityLauncher.launch(intent);
        });

        submitBranch.setOnClickListener(v -> {
            String name = branchName.getText().toString().trim();
            String address = branchAddress.getText().toString().trim();
            String radius = radiusMeters.getText().toString().trim();

            if (name.isEmpty()) {
                branchName.setError("Please enter a branch name");
                return;
            }
            if (address.isEmpty()) {
                branchAddress.setError("Please select a location");
                return;
            }
            if (radius.isEmpty()) {
                radiusMeters.setError("Please enter a valid radius");
                return;
            }

            saveBranchToDatabase(name, address, Integer.parseInt(radius));
        });
    }

    private void saveBranchToDatabase(String name, String address, int radius) {
        String apiUrl = "http://192.168.168.239/ems_api/save_branch.php";

        StringRequest request = new StringRequest(Request.Method.POST, apiUrl,
                response -> {
                    Log.d("AddBranchActivity", "Save Branch Response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            Toast.makeText(this, "Branch Saved Successfully!", Toast.LENGTH_SHORT).show();
                            finish(); // Close activity after success
                        } else {
                            Toast.makeText(this, "Failed to save branch!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error processing server response!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("AddBranchActivity", "Network error: " + error.toString());
                    Toast.makeText(this, "Network error! Check connection.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("company_code", companyCode);
                params.put("branch_name", name);
                params.put("latitude", String.valueOf(latitude));
                params.put("longitude", String.valueOf(longitude));
                params.put("radius", String.valueOf(radius));
                params.put("address", address);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
