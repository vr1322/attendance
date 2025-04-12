package com.example.attendance;

import android.content.Intent;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class ComtactEmpInfo extends AppCompatActivity {

    ImageView back;
    TextView tvEmpName, tvEmpId, tvBranch, tvPhone, tvEmail;
    Button btnCall, btnMessage, btnWhatsApp, btnEmail;

    String empName = "", empId = "", branch = "", phone = "", email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comtact_emp_info);

        back = findViewById(R.id.);
        tvEmpName = findViewById(R.id.employee_name);
        tvEmpId = findViewById(R.id.employee_id);
        tvBranch = findViewById(R.id.branch_name);
        tvPhone = findViewById(R.id.phone_number);
        tvEmail = findViewById(R.id.email_id);

        btnCall = findViewById(R.id.btn_call);
        btnMessage = findViewById(R.id.btn_message);
        btnWhatsApp = findViewById(R.id.btn_whatsapp);
        btnEmail = findViewById(R.id.btn_email);

        // Get employee ID from intent
        empId = getIntent().getStringExtra("emp_id");
        if (empId != null) {
            fetchEmployeeDetails(empId);
        } else {
            Toast.makeText(this, "Employee ID not found", Toast.LENGTH_SHORT).show();
        }

        back.setOnClickListener(v -> finish());

        btnCall.setOnClickListener(v -> {
            if (!phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                startActivity(intent);
            }
        });

        btnMessage.setOnClickListener(v -> {
            if (!phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("sms:" + phone));
                startActivity(intent);
            }
        });

        btnWhatsApp.setOnClickListener(v -> {
            if (!phone.isEmpty()) {
                String url = "https://wa.me/" + phone;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        btnEmail.setOnClickListener(v -> {
            if (!email.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + email));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Regarding...");
                intent.putExtra(Intent.EXTRA_TEXT, "Hello " + empName + ",");
                startActivity(intent);
            }
        });
    }

    private void fetchEmployeeDetails(String empId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String apiUrl = "https://devonix.io/ems_api/get_employee_contact.php?employee_id=" + empId;
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONObject json = new JSONObject(response.toString());
                if (json.getBoolean("status")) {
                    JSONObject data = json.getJSONObject("data");

                    empName = data.getString("name");
                    branch = data.getString("branch");
                    phone = data.getString("phone");
                    email = data.getString("email");

                    new Handler(Looper.getMainLooper()).post(() -> {
                        tvEmpName.setText(empName);
                        tvEmpId.setText(empId);
                        tvBranch.setText(branch);
                        tvPhone.setText(phone);
                        tvEmail.setText(email);
                    });
                } else {
                    String message = json.getString("message");
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    );
                }

            } catch (Exception e) {
                Log.e("FetchEmp", "Error: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(this, "Failed to fetch employee data", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
