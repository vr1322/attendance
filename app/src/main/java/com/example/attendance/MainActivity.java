package com.example.attendance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private CheckBox checkShowPassword;
    private ImageButton btnSubmit;
    private TextView tvForgot;
    private ProgressDialog progressDialog;

    private static final String LOGIN_URL = "http://192.168.168.239/ems_api/login.php"; // Change to your actual API URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        checkShowPassword = findViewById(R.id.check_show_password);
        btnSubmit = findViewById(R.id.btn_submit);
        tvForgot = findViewById(R.id.tv_forgot);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");

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

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, LOGIN_URL, jsonParams,
                response -> {
                    progressDialog.dismiss();
                    try {
                        String status = response.getString("status");

                        if (status.equals("success")) {
                            JSONObject user = response.getJSONObject("user");
                            String role = user.getString("role");

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
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Invalid Server Response!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Failed to connect to server!", Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
