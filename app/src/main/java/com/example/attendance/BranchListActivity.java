package com.example.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BranchListActivity extends AppCompatActivity implements BranchAdapter.OnBranchActionListener {
    private RecyclerView branchRecyclerView;
    private BranchAdapter branchAdapter;
    private List<Branch> branchList = new ArrayList<>();
    private FloatingActionButton fabAddBranch;
    private ImageView backButton;

    private static final String GET_BRANCHES_URL = "https://devonix.io/ems_api/get_branches.php";
    private static final String DELETE_BRANCH_URL = "https://devonix.io/ems_api/delete_branch.php";
    private static final String TAG = "BranchListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_list);

        // Initialize Views
        branchRecyclerView = findViewById(R.id.branchRecyclerView);
        fabAddBranch = findViewById(R.id.fab_add_branch);
        backButton = findViewById(R.id.back);

        // Set up RecyclerView
        branchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        branchAdapter = new BranchAdapter(this, this);
        branchRecyclerView.setAdapter(branchAdapter);

        // Load branches from API
        loadBranches();

        // Floating Button Click - Open Add Branch Activity
        fabAddBranch.setOnClickListener(v -> {
            Intent intent = new Intent(BranchListActivity.this, AddBranchActivity.class);
            startActivityForResult(intent, 100);
        });

        // Back Button Click
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBranches(); // Reload branches when returning to this activity
    }

    @Override
    public void onEditBranch(Branch branch) {
        Intent intent = new Intent(this, EditBranchActivity.class);
        intent.putExtra("branch_id", branch.getId());
        intent.putExtra("branch_name", branch.getName());
        intent.putExtra("latitude", branch.getLatitude());
        intent.putExtra("longitude", branch.getLongitude());
        intent.putExtra("radius", branch.getRadius());
        intent.putExtra("address", branch.getAddress());
        startActivity(intent);
    }

    private void loadBranches() {
        SharedPreferences prefs = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        String companyCode = prefs.getString("company_code", "").trim();

        if (companyCode.isEmpty()) {
            Toast.makeText(this, "Company code not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        new FetchBranchesTask().execute(companyCode);
    }

    private class FetchBranchesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String companyCode = params[0];
            try {
                // Ensure we're using a GET request
                URL url = new URL(GET_BRANCHES_URL + "?company_code=" + companyCode);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            } catch (Exception e) {
                Log.e("BranchAPI", "Error: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getString("status").equals("success")) {
                        JSONArray branchesArray = jsonObject.getJSONArray("branches");

                        branchList.clear(); // Ensure no old data remains
                        for (int i = 0; i < branchesArray.length(); i++) {
                            JSONObject branchObj = branchesArray.getJSONObject(i);
                            Branch branch = new Branch(
                                    branchObj.getInt("id"),
                                    branchObj.getString("branch_name"),
                                    branchObj.getString("address"),
                                    branchObj.getDouble("latitude"),
                                    branchObj.getDouble("longitude"),
                                    branchObj.getInt("radius")
                            );
                            branchList.add(branch);
                        }

                        Log.d("BranchAPI", "Branches Loaded: " + branchList.size());

                        // Update UI
                        runOnUiThread(() -> branchAdapter.updateBranches(branchList));

                    } else {
                        Log.e("BranchAPI", "Error: " + jsonObject.getString("message"));
                        Toast.makeText(BranchListActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("BranchAPI", "Error parsing JSON: " + e.getMessage());
                }
            } else {
                Toast.makeText(BranchListActivity.this, "Server connection failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDeleteBranch(int position, int branchId) {
        if (branchList.isEmpty()) {
            Toast.makeText(this, "No branches available to delete!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Branch")
                .setMessage("Do you really want to delete this branch?")
                .setPositiveButton("Yes", (dialog, which) -> deleteBranchFromAPI(position, branchId))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteBranchFromAPI(int position, int branchId) {
        String deleteUrl = DELETE_BRANCH_URL; // Ensure correct API URL

        StringRequest stringRequest = new StringRequest(Request.Method.POST, deleteUrl,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if (status.equals("success")) {
                            Toast.makeText(BranchListActivity.this, "Branch deleted successfully", Toast.LENGTH_SHORT).show();

                            // ✅ Reload branches from API to reflect changes
                            loadBranches();
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(BranchListActivity.this, "Failed to delete: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(BranchListActivity.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("DeleteBranch", "JSON Parsing Error", e);
                    }
                },
                error -> {
                    String errorMsg = error.getMessage() != null ? error.getMessage() : "Unknown error";
                    Toast.makeText(BranchListActivity.this, "Request Failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e("DeleteBranch", "Volley Error: " + errorMsg, error);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("branch_id", String.valueOf(branchId));
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(BranchListActivity.this);
        queue.add(stringRequest);
    }

}
