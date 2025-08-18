package com.example.attendance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class EmployeeHomeActivity extends AppCompatActivity {

    private String companyCode, companyName, email, employeeId, employeeName;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_home);

        context = this;

        // Load session
        SharedPreferences sharedPreferences = getSharedPreferences("EmployeeSession", MODE_PRIVATE);
        companyCode = sharedPreferences.getString("company_code", null);
        companyName = sharedPreferences.getString("company_name", null);
        email = sharedPreferences.getString("email", null);
        employeeId = sharedPreferences.getString("employee_id", null);
        employeeName = sharedPreferences.getString("employee_name", null);

        // Check session validity
        if (companyCode == null || employeeId == null || employeeName == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Log session
        Log.d("SessionDebug", "Company Code: " + companyCode);
        Log.d("SessionDebug", "Company Name: " + companyName);
        Log.d("SessionDebug", "Email: " + email);
        Log.d("SessionDebug", "Emp ID: " + employeeId);
        Log.d("SessionDebug", "Emp Name: " + employeeName);

        // Welcome Toast
        Toast.makeText(context, "Welcome: " + employeeName + "\nEmail: " + email, Toast.LENGTH_LONG).show();

        // UI Elements
        CardView atView = findViewById(R.id.at_View);
        CardView lmView = findViewById(R.id.lm_View);
        CardView maView = findViewById(R.id.ma_View);
        CardView salView = findViewById(R.id.sal_View);

        ImageButton btnMarkAttendance = findViewById(R.id.btn_mark_attendance);
        ImageButton btnAttendTrack = findViewById(R.id.btn_attend_track);
        ImageButton btnSalDetail = findViewById(R.id.btn_sal_detail);
        ImageButton btnLeaveManage = findViewById(R.id.btn_leave_manage);

        Button otReq = findViewById(R.id.ot_request);

        // Profile ImageView
        ImageView empProfile = findViewById(R.id.emp_profile);
        empProfile.setOnClickListener(v -> openUpdateEmployee());

        // Click events
        atView.setOnClickListener(v -> navigateTo(AttendanceTrackingActivity.class));
        lmView.setOnClickListener(v -> navigateTo(LeaveManagementActivity.class));
        maView.setOnClickListener(v -> handleMarkAttendance());
        salView.setOnClickListener(v -> navigateTo(SalaryDetailsActivity.class));

        btnMarkAttendance.setOnClickListener(v -> handleMarkAttendance());
        btnAttendTrack.setOnClickListener(v -> navigateTo(AttendanceTrackingActivity.class));
        btnSalDetail.setOnClickListener(v -> navigateTo(SalaryDetailsActivity.class));
        btnLeaveManage.setOnClickListener(v -> navigateTo(LeaveManagementActivity.class));

        otReq.setOnClickListener(v -> navigateTo(EmpOtbtn.class));
    }

    // Open UpdateEmployee activity
    private void openUpdateEmployee() {
        Intent intent = new Intent(EmployeeHomeActivity.this, UpdateEmployee.class);
        intent.putExtra("employee_id", employeeId);
        intent.putExtra("company_code", companyCode);
        startActivity(intent);
    }

    // Handle mark attendance
    private void handleMarkAttendance() {
        if (employeeId == null || employeeId.isEmpty() || companyCode == null || companyCode.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        Intent intent = new Intent(EmployeeHomeActivity.this, GeoFenceAttendanceActivity.class);
        intent.putExtra("employee_id", employeeId);
        intent.putExtra("company_code", companyCode);
        intent.putExtra("employee_name", employeeName);
        startActivity(intent);
    }

    // Generic navigation method
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(EmployeeHomeActivity.this, targetActivity);
        startActivity(intent);
    }
}
