package com.example.attendance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class ComtactEmpInfo extends AppCompatActivity {

    ImageView back;
    TextView tvEmpName, tvEmpId, tvBranch, tvPhone, tvEmail;
    Button btnCall, btnMessage, btnWhatsApp, btnEmail;

    String empName = "", empId = "", branch = "", phone = "", email = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comtact_emp_info);

        back = findViewById(R.id.back);
        tvEmpName = findViewById(R.id.employee_name);
        tvEmpId = findViewById(R.id.employee_id);
        tvBranch = findViewById(R.id.branch_name);
        tvPhone = findViewById(R.id.phone_number);
        tvEmail = findViewById(R.id.email_id);
        btnCall = findViewById(R.id.btn_call);
        btnMessage = findViewById(R.id.btn_message);
        btnWhatsApp = findViewById(R.id.btn_whatsapp);
        btnEmail = findViewById(R.id.btn_email);

        // Get SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        String empId = sharedPreferences.getString("employee_id", "");
        String empName = sharedPreferences.getString("employee_name", "");
        String branch = sharedPreferences.getString("branch", "");

        // Set Name, ID, Branch
        tvEmpName.setText(empName);
        tvEmpId.setText(empId);
        tvBranch.setText(branch);

        // Fetch Contact Info from Server
        fetchContactDetails(empId);

        // Back Button Click
        back.setOnClickListener(v -> finish());
    }

    private void fetchContactDetails(String employeeId) {
        String url = "https://devonix.io/ems_api/get_employee_contact.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        Log.d("EMP_CONTACT_RESPONSE", response); // Debug log

                        JSONObject jsonObject = new JSONObject(response);

                        // ✅ Get boolean instead of string
                        boolean success = jsonObject.getBoolean("success");

                        if (success) {
                            String phone = jsonObject.getString("phone");
                            String email = jsonObject.getString("email");

                            tvPhone.setText(phone);
                            tvEmail.setText(email);

                            // Action buttons
                            btnCall.setOnClickListener(v -> {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" + phone));
                                startActivity(intent);
                            });

                            btnMessage.setOnClickListener(v -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("sms:" + phone));
                                startActivity(intent);
                            });

                            btnWhatsApp.setOnClickListener(v -> {
                                String urlWhatsapp = "https://api.whatsapp.com/send?phone=" + phone;
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(urlWhatsapp));
                                startActivity(intent);
                            });

                            btnEmail.setOnClickListener(v -> {
                                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                        "mailto", email, null));
                                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                            });

                        } else {
                            Toast.makeText(this, "Failed to fetch contact details", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("employee_id", employeeId);
                params.put("company_code", getSharedPreferences("AdminPrefs", MODE_PRIVATE).getString("company_code", ""));
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

}
