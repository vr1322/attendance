package com.example.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class EmployeeHomeActivity extends AppCompatActivity {

    private String companyCode, companyName, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_home);

        // ✅ Retrieve SharedPreferences data
        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        companyCode = sharedPreferences.getString("company_code", "");
        companyName = sharedPreferences.getString("company_name", "");
        email = sharedPreferences.getString("email", "");

        // Optional: show a toast or log to confirm retrieval
        Toast.makeText(this, "Welcome: " + email, Toast.LENGTH_SHORT).show();

        // Initializing buttons and card views
        CardView atView = findViewById(R.id.at_View);
        CardView lmView = findViewById(R.id.lm_View);
        CardView maView = findViewById(R.id.ma_View);
        CardView salView = findViewById(R.id.sal_View);

        ImageButton btnMarkAttendance = findViewById(R.id.btn_mark_attendance);
        ImageButton btnAttendTrack = findViewById(R.id.btn_attend_track);
        ImageButton btnSalDetail = findViewById(R.id.btn_sal_detail);
        ImageButton btnLeaveManage = findViewById(R.id.btn_leave_manage);

        Button otReq = findViewById(R.id.ot_request);

        // Setting up onClickListeners for navigation
        atView.setOnClickListener(v -> navigateTo(AttendanceTrackingActivity.class));
        lmView.setOnClickListener(v -> navigateTo(LeaveManagementActivity.class));
        maView.setOnClickListener(v -> navigateTo(EmpMarkAttendanceActivity.class));
        salView.setOnClickListener(v -> navigateTo(SalaryDetailsActivity.class));

        btnMarkAttendance.setOnClickListener(v -> navigateTo(EmpMarkAttendanceActivity.class));
        btnAttendTrack.setOnClickListener(v -> navigateTo(AttendanceTrackingActivity.class));
        btnSalDetail.setOnClickListener(v -> navigateTo(SalaryDetailsActivity.class));
        btnLeaveManage.setOnClickListener(v -> navigateTo(LeaveManagementActivity.class));

        otReq.setOnClickListener(v -> navigateTo(EmpOtbtn.class));
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(EmployeeHomeActivity.this, targetActivity);
        startActivity(intent);
    }
}
