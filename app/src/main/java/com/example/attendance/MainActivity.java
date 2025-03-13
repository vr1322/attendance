package com.example.attendance;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private CheckBox checkShowPassword;
    private ImageButton btnSubmit;
    private TextView tvForgot, companyNameTextView;
    private ProgressDialog progressDialog;

    private static final String LOGIN_URL = "http://192.168.144.102/ems_api/login.php"; // Ensure this URL is correct

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        checkShowPassword = findViewById(R.id.check_show_password);
        btnSubmit = findViewById(R.id.btn_submit);
        tvForgot = findViewById(R.id.tv_forgot);
        companyNameTextView = findViewById(R.id.companyName); // ✅ TextView for company name

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        // ✅ Load stored company name from SharedPreferences (if available)
        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        String storedCompanyName = sharedPreferences.getString("company_name", "Company Name Not Set");
        companyNameTextView.setText(storedCompanyName); // ✅ Display company name on launch

        checkShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                checkShowPassword.setButtonTintList(ContextCompat.getColorStateList(this, R.color.blue));
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPassword.setSelection(etPassword.length());
        });

        btnSubmit.setOnClickListener(v -> loginUser());

        tvForgot.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show();
            // Implement forgot password logic
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(MainActivity.this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        // Create JSON parameters for API request
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("email", email);
            jsonParams.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("LoginRequest", "Sending login request: " + jsonParams.toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, LOGIN_URL, jsonParams,
                response -> {
                    progressDialog.dismiss();
                    Log.d("LoginResponse", "Response received: " + response.toString());

                    try {
                        String status = response.getString("status");

                        if (status.equals("success")) {
                            JSONObject user = response.getJSONObject("user");
                            String role = user.getString("role");
                            String companyCode = user.optString("company_code", "");
                            String companyNameStr = user.optString("company_name", "Unknown Company"); // ✅ Fetch company_name

                            Log.d("CompanyName", "Received from API: " + companyNameStr);

                            // ✅ Set company name in TextView
                            companyNameTextView.setText(companyNameStr);

                            // ✅ Save company name & code to SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("company_code", companyCode);
                            editor.putString("company_name", companyNameStr); // ✅ Save company name
                            editor.apply();

                            // Navigate to the appropriate home screen based on role
                            Intent intent;
                            switch (role) {
                                case "admin":
                                    intent = new Intent(MainActivity.this, home.class);
                                    break;
                                case "employee":
                                    intent = new Intent(MainActivity.this, EmployeeHomeActivity.class);
                                    break;
                                case "manager":
                                    intent = new Intent(MainActivity.this, ManagerHomeActivity.class);
                                    break;
                                case "supervisor":
                                    intent = new Intent(MainActivity.this, SupervisorHomeActivity.class);
                                    break;
                                default:
                                    Toast.makeText(MainActivity.this, "Unknown role", Toast.LENGTH_SHORT).show();
                                    return;
                            }

                            intent.putExtra("email", email);
                            intent.putExtra("company_name", companyNameStr); // ✅ Pass company_name to next activity
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMessage = response.optString("message", "Invalid credentials");
                            Log.e("LoginError", "Server error message: " + errorMessage);
                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("LoginError", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Invalid Server Response!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Log.e("LoginError", "Network error: " + error.toString());
                    Toast.makeText(MainActivity.this, "Failed to connect to server!", Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
