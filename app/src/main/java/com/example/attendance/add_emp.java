package com.example.attendance;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class add_emp extends AppCompatActivity {

    private EditText etEmployeeName, etEmployeeId, etDateOfBirth, etJoiningDate, etDesignation, etPhone, etAddress, etEmail, etPassword, etBasicPay, etOvertimeAllowance;
    private ImageView backButton, icCalendarDob, icCalendarJoining;
    private RadioGroup paymentTypeGroup;
    private RadioButton perDay, monthly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emp);

        initializeViews();
        setClickListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        etEmployeeName = findViewById(R.id.et_employee_name);
        etEmployeeId = findViewById(R.id.et_employee_id);
        etDateOfBirth = findViewById(R.id.et_date_of_birth);
        etJoiningDate = findViewById(R.id.et_joining_date);
        icCalendarDob = findViewById(R.id.ic_calendar_dob);
        icCalendarJoining = findViewById(R.id.ic_calendar_joining);
        etDesignation = findViewById(R.id.ad_emp_design);
        etPhone = findViewById(R.id.add_emp_number);
        etAddress = findViewById(R.id.addd_emp_address);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.pass);
        etBasicPay = findViewById(R.id.basicPay);
        etOvertimeAllowance = findViewById(R.id.overtimeAllowance);
        paymentTypeGroup = findViewById(R.id.paymentTypeGroup);
        perDay = findViewById(R.id.radio_per_day);
        monthly = findViewById(R.id.radio_monthly);
    }

    private void setClickListeners() {
        backButton.setOnClickListener(v -> finish());

        icCalendarDob.setOnClickListener(v -> showDatePickerDialog(etDateOfBirth));
        icCalendarJoining.setOnClickListener(v -> showDatePickerDialog(etJoiningDate));
    }

    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            editText.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }
}
