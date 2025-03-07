package com.example.attendance;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class CreateAdminActivity extends AppCompatActivity {

    private EditText etName, cp_code, etEmail, etPassword, etConfirmPassword;
    private CheckBox checkShowPassword;
    private ImageButton btnSubmit;
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;

    private static final String REGISTER_URL = "http://192.168.168.239/ems_api/register_admin.php";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_admin);

        // Initialize UI elements
        etName = findViewById(R.id.ca_name);
        cp_code = findViewById(R.id.cp_code);
        etEmail = findViewById(R.id.ca_email);
        etPassword = findViewById(R.id.ca_password);
        etConfirmPassword = findViewById(R.id.ca_cfpassword);
        checkShowPassword = findViewById(R.id.check_show_password);
        btnSubmit = findViewById(R.id.btn_submit);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering Company...");
        requestQueue = Volley.newRequestQueue(this);

        // Show/Hide Password Logic
        checkShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                etConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPassword.setSelection(etPassword.length());
            etConfirmPassword.setSelection(etConfirmPassword.length());
        });

        // Submit button click listener
        btnSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void validateAndSubmit() {
        String name = etName.getText().toString().trim();
        String cpcode = cp_code.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || cpcode.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        registerCompany(cpcode, name, email, password);
    }

    private void registerCompany(String cpcode, String name, String email, String password) {
        progressDialog.show();

        Map<String, String> params = new HashMap<>();
        params.put("company_code", cpcode);
        params.put("company_name", name);
        params.put("admin_email", email);
        params.put("password", password);

        JSONObject jsonParams = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, REGISTER_URL, jsonParams,
                response -> {
                    progressDialog.dismiss();
                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");

                        if (status.equals("success")) {
                            Toast.makeText(CreateAdminActivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(CreateAdminActivity.this, main_home.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(CreateAdminActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON_ERROR", "Parsing Error: " + e.getMessage());
                        Toast.makeText(CreateAdminActivity.this, "Invalid JSON Response!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    handleVolleyError(error);
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void handleVolleyError(VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                String responseBody = new String(error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
                JSONObject errorResponse = new JSONObject(responseBody);

                String message = errorResponse.optString("message", "Unknown error occurred");
                Toast.makeText(CreateAdminActivity.this, message, Toast.LENGTH_LONG).show();
                Log.e("SERVER_ERROR", message);
            } catch (JSONException | UnsupportedEncodingException e) {
                Log.e("PARSE_ERROR", "Error parsing server response: " + e.getMessage());
                Toast.makeText(CreateAdminActivity.this, "Server error occurred!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(CreateAdminActivity.this, "Failed to connect to server!", Toast.LENGTH_SHORT).show();
            Log.e("VOLLEY_ERROR", "Error: " + error.toString());
        }
    }
}
