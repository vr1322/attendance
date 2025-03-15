package com.example.attendance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log; // Import for debugging
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class main_home extends AppCompatActivity {

    private Button btn_rg_cp;
    private ImageButton btn_submit;
    private EditText cp_code;
    private ProgressDialog progressDialog;

    private RequestQueue requestQueue;

    // ✅ Change IP for Emulator: Use `10.0.2.2` instead of `192.168.168.239` if testing on Android Emulator.
    private static final String VERIFY_URL = "https://devonix.io/ems_api/verify_company.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);

        btn_submit = findViewById(R.id.btn_submit);
        btn_rg_cp = findViewById(R.id.rg_cp_btn);
        cp_code = findViewById(R.id.cp_code);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Verifying Company Code...");

        // ✅ Instantiate RequestQueue once
        requestQueue = Volley.newRequestQueue(this);

        // Check company code before navigating to MainActivity
        btn_submit.setOnClickListener(v -> verifyCompanyCode());

        // Navigate to CreateAdminActivity for registration
        btn_rg_cp.setOnClickListener(v -> {
            Intent intent = new Intent(main_home.this, CreateAdminActivity.class);
            startActivity(intent);
        });
    }

    private void verifyCompanyCode() {
        String enteredCode = cp_code.getText().toString().trim();

        if (enteredCode.isEmpty()) {
            Toast.makeText(this, "Please enter a Company Code!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        Map<String, String> params = new HashMap<>();
        params.put("company_code", enteredCode);

        JSONObject jsonParams = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, VERIFY_URL, jsonParams,
                response -> {
                    progressDialog.dismiss();
                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");

                        if (status.equals("success")) {
                            String companyNameStr = response.optString("company_name", "Unknown Company"); // ✅ Fetch company_name

                            Toast.makeText(main_home.this, "Company Verified!", Toast.LENGTH_SHORT).show();

                            // ✅ Store company name in SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("company_name", companyNameStr);
                            editor.apply();

                            // ✅ Pass company_name to MainActivity
                            Intent intent = new Intent(main_home.this, MainActivity.class);
                            intent.putExtra("company_name", companyNameStr);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(main_home.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(main_home.this, "Invalid JSON Response!", Toast.LENGTH_SHORT).show();
                        Log.e("API_ERROR", "JSON Parsing Error: " + e.getMessage());
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    if (error.networkResponse != null) {
                        Log.e("API_ERROR", "Server Response Code: " + error.networkResponse.statusCode);
                        Log.e("API_ERROR", "Response Data: " + new String(error.networkResponse.data));
                    } else {
                        Log.e("API_ERROR", "Volley Error: " + error.getMessage());
                    }
                    Toast.makeText(main_home.this, "Server Error! Check Logs", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

}
