package com.example.attendance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


public class EditBranchActivity extends AppCompatActivity {
    private EditText branchName, branchAddress, radiusMeters;
    private ImageButton submitBranch;
    private ImageView backButton, search;
    private TextView editbranch_text;
    private ProgressDialog progressDialog;

    private int branchId;
    private String companyCode;

    private static final String BASE_URL = "https://devonix.io/ems_api/";
    private static final String GET_BRANCH_URL = BASE_URL + "get_branch_by_id.php";  // New API
    private static final String UPDATE_BRANCH_URL = BASE_URL + "edit_branch.php";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_branch);

        // Initialize views
        branchName = findViewById(R.id.branchName);
        search = findViewById(R.id.searchLocation);
        branchAddress = findViewById(R.id.branchAddress);
        radiusMeters = findViewById(R.id.radiusMeters);
        submitBranch = findViewById(R.id.submitBranch);
        backButton = findViewById(R.id.back);
        editbranch_text = findViewById(R.id.edit_branch_text);

        // Get branchId from intent
        branchId = getIntent().getIntExtra("branch_id", -1);
        if (branchId == -1) {
            Toast.makeText(this, "Invalid Branch ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch company_code from SharedPreferences
        companyCode = getSharedPreferences("AdminPrefs", MODE_PRIVATE).getString("company_code", "");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        // Load branch details
        loadBranchDetails();

        // Back button
        backButton.setOnClickListener(v -> finish());
        editbranch_text.setOnClickListener(v -> finish());
        search.setOnClickListener(v -> {
            android.content.Intent intent = new Intent(EditBranchActivity.this, MapsActivity.class);
            mapActivityLauncher.launch(intent);
        });



        // Submit button
        submitBranch.setOnClickListener(v -> updateBranch());
    }

    private final ActivityResultLauncher<Intent> mapActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String selectedAddress = result.getData().getStringExtra("selected_address");
                    String selectedRadius = result.getData().getStringExtra("selected_radius");

                    if (selectedAddress != null) {
                        branchAddress.setText(selectedAddress);
                    }
                    if (selectedRadius != null) {
                        radiusMeters.setText(selectedRadius);
                    }
                }
            });


    private void loadBranchDetails() {
        progressDialog.show();
        String url = GET_BRANCH_URL + "?branch_id=" + branchId + "&company_code=" + companyCode;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONObject branchData = jsonObject.getJSONObject("branch");

                            branchName.setText(branchData.getString("branch_name"));
                            branchAddress.setText(branchData.getString("address"));
                            radiusMeters.setText(branchData.getString("radius"));
                        } else {
                            Toast.makeText(this, "Failed to load branch data", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        Log.e("EditBranch", "JSON Parsing Error", e);
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    Log.e("EditBranch", "Volley Error", error);
                });

        queue.add(request);
    }

    private void updateBranch() {
        String name = branchName.getText().toString().trim();
        String address = branchAddress.getText().toString().trim();
        String radius = radiusMeters.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || radius.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Updating...");
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, UPDATE_BRANCH_URL,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            Toast.makeText(this, "Branch updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        Log.e("EditBranch", "JSON Parsing Error", e);
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    Log.e("EditBranch", "Volley Error", error);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("branch_id", String.valueOf(branchId));
                params.put("company_code", companyCode);
                params.put("branch_name", name);
                params.put("address", address);
                params.put("radius", radius);
                return params;
            }
        };

        queue.add(request);
    }
}
