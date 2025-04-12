package com.example.attendance;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etOtp, etNewPassword;
    private Button btnSendOtp, btnVerifyOtp, btnResetPassword;
    private Spinner spinnerRole;
    private LinearLayout layoutEmail, layoutOtp, layoutNewPassword;
    private ProgressDialog progressDialog;

    private static final String SEND_OTP_URL = "https://devonix.io/ems_api/send_otp.php";
    private static final String VERIFY_OTP_AND_RESET_URL = "https://devonix.io/ems_api/verify_otp_and_reset.php";

    private String emailGlobal = "", selectedRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        etEmail = findViewById(R.id.et_email_forgot);
        etOtp = findViewById(R.id.et_otp);
        etNewPassword = findViewById(R.id.et_new_password);
        spinnerRole = findViewById(R.id.spinner_role);
        btnSendOtp = findViewById(R.id.btn_send_otp);
        btnVerifyOtp = findViewById(R.id.btn_verify_otp);
        btnResetPassword = findViewById(R.id.btn_reset_password);

        layoutEmail = findViewById(R.id.layout_email);
        layoutOtp = findViewById(R.id.layout_otp);
        layoutNewPassword = findViewById(R.id.layout_new_password);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);

        // Setup role spinner
        String[] roles = {"admin", "employee", "manager", "supervisor"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // SEND OTP
        btnSendOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            selectedRole = spinnerRole.getSelectedItem().toString();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.show();
            JSONObject params = new JSONObject();
            try {
                params.put("email", email);
                params.put("role", selectedRole);
            } catch (Exception e) {
                e.printStackTrace();
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, SEND_OTP_URL, params,
                    response -> {
                        progressDialog.dismiss();
                        boolean success = response.optString("status", "").equals("success");
                        String message = response.optString("message", "Something went wrong");
                        if (success) {
                            emailGlobal = email;
                            layoutEmail.setVisibility(View.GONE);
                            layoutOtp.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "OTP sent to your email", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    });

            Volley.newRequestQueue(this).add(request);
        });

        // VERIFY OTP
        btnVerifyOtp.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();
            if (otp.isEmpty()) {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            layoutOtp.setVisibility(View.GONE);
            layoutNewPassword.setVisibility(View.VISIBLE);
        });

        // RESET PASSWORD
        btnResetPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String otp = etOtp.getText().toString().trim();

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.show();
            JSONObject params = new JSONObject();
            try {
                params.put("email", emailGlobal);
                params.put("otp", otp);
                params.put("new_password", newPassword);
                params.put("role", selectedRole);
            } catch (Exception e) {
                e.printStackTrace();
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, VERIFY_OTP_AND_RESET_URL, params,
                    response -> {
                        progressDialog.dismiss();
                        boolean success = response.optString("status", "").equals("success");
                        String message = response.optString("message", "Try again");
                        if (success) {
                            Toast.makeText(this, "Password reset successful", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    });

            Volley.newRequestQueue(this).add(request);
        });
    }
}
