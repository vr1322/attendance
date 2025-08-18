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

    private static final String LOGIN_URL = "https://devonix.io/ems_api/login.php";

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
        companyNameTextView = findViewById(R.id.companyName);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        // Show stored company name if available
        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        String storedCompanyName = sharedPreferences.getString("company_name", "Company Name Not Set");
        companyNameTextView.setText(storedCompanyName);

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
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
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

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("email", email);
            jsonParams.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, LOGIN_URL, jsonParams,
                response -> {
                    progressDialog.dismiss();
                    Log.d("LoginResponse", "Response received: " + response);

                    try {
                        String status = response.getString("status");

                        if (status.equals("success")) {
                            JSONObject user = response.getJSONObject("user");
                            String role = user.getString("role");
                            String companyCode = user.optString("company_code", "");
                            String companyNameStr = user.optString("company_name", "Unknown Company");
                            String employeeName = user.optString("employee_name", user.optString("name", "User")); // Flexible field
                            String employeeId = user.optString("employee_id", "");

                            // Update company name UI
                            companyNameTextView.setText(companyNameStr);

                            SharedPreferences sharedPreferences;
                            SharedPreferences.Editor editor;

                            switch (role.toLowerCase()) {
                                case "employee":
                                    sharedPreferences = getSharedPreferences("EmployeeSession", MODE_PRIVATE);
                                    editor = sharedPreferences.edit();
                                    editor.putString("employee_id", employeeId);
                                    editor.putString("employee_name", employeeName);
                                    editor.putString("email", email);
                                    editor.putString("role", role);
                                    editor.putString("company_code", companyCode);
                                    editor.putString("company_name", companyNameStr);
                                    editor.apply();

                                    Toast.makeText(MainActivity.this, "Welcome " + employeeName, Toast.LENGTH_SHORT).show();
                                    Intent empIntent = new Intent(MainActivity.this, EmployeeHomeActivity.class);
                                    empIntent.putExtra("role", role);
                                    empIntent.putExtra("email", email);
                                    empIntent.putExtra("company_code", companyCode);
                                    empIntent.putExtra("employee_id", employeeId);
                                    empIntent.putExtra("employee_name", employeeName);
                                    startActivity(empIntent);
                                    break;

                                case "admin":
                                    sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
                                    editor = sharedPreferences.edit();
                                    editor.putString("company_code", companyCode);
                                    editor.putString("company_name", companyNameStr);
                                    editor.putString("email", email);
                                    editor.putString("role", role);
                                    editor.apply();

                                    Intent adminIntent = new Intent(MainActivity.this, home.class);
                                    adminIntent.putExtra("role", role);
                                    adminIntent.putExtra("email", email);
                                    adminIntent.putExtra("company_code", companyCode);
                                    startActivity(adminIntent);
                                    break;

                                case "manager":
                                    sharedPreferences = getSharedPreferences("ManagerSession", MODE_PRIVATE);
                                    editor = sharedPreferences.edit();
                                    editor.putString("manager_id", employeeId);
                                    editor.putString("manager_name", employeeName);
                                    editor.putString("email", email);
                                    editor.putString("role", role);
                                    editor.putString("company_code", companyCode);
                                    editor.putString("company_name", companyNameStr);
                                    editor.apply();

                                    Toast.makeText(MainActivity.this, "Welcome " + employeeName, Toast.LENGTH_SHORT).show();
                                    Intent mgrIntent = new Intent(MainActivity.this, ManagerHomeActivity.class);
                                    mgrIntent.putExtra("role", role);
                                    mgrIntent.putExtra("email", email);
                                    mgrIntent.putExtra("company_code", companyCode);
                                    mgrIntent.putExtra("manager_name", employeeName);
                                    startActivity(mgrIntent);
                                    break;

                                case "supervisor":
                                    sharedPreferences = getSharedPreferences("SupervisorSession", MODE_PRIVATE);
                                    editor = sharedPreferences.edit();
                                    editor.putString("supervisor_id", employeeId);
                                    editor.putString("supervisor_name", employeeName);
                                    editor.putString("email", email);
                                    editor.putString("role", role);
                                    editor.putString("company_code", companyCode);
                                    editor.putString("company_name", companyNameStr);
                                    editor.apply();

                                    Toast.makeText(MainActivity.this, "Welcome " + employeeName, Toast.LENGTH_SHORT).show();
                                    Intent supIntent = new Intent(MainActivity.this, SupervisorHomeActivity.class);
                                    supIntent.putExtra("role", role);
                                    supIntent.putExtra("email", email);
                                    supIntent.putExtra("company_code", companyCode);
                                    supIntent.putExtra("supervisor_name", employeeName);
                                    startActivity(supIntent);
                                    break;


                                default:
                                    Toast.makeText(MainActivity.this, "Unknown role", Toast.LENGTH_SHORT).show();
                                    return;
                            }

                            finish(); // Close login screen
                        } else {
                            String message = response.optString("message", "Invalid credentials");
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Invalid server response", Toast.LENGTH_SHORT).show();
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
