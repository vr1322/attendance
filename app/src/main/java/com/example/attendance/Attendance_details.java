package com.example.attendance;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class Attendance_details extends AppCompatActivity {

    private TextView tvName;
    private EditText etDate, etInTime, etOutTime, etOvertime;
    private Spinner spinnerStatus;
    private Button btnEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_details);

        // Initialize UI Elements
        tvName = findViewById(R.id.tv_name);
        etDate = findViewById(R.id.et_date);
        etInTime = findViewById(R.id.et_intime);
        etOutTime = findViewById(R.id.et_outtime);
        spinnerStatus = findViewById(R.id.spinner_status);
        btnEdit = findViewById(R.id.btn_edit);

        // Fetch Data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);
        String employeeName = sharedPreferences.getString("employee_name", "N/A");

        tvName.setText(employeeName);

        // Date & Time Picker Handlers
        etDate.setOnClickListener(v -> showDatePickerDialog(etDate));
        etInTime.setOnClickListener(v -> showTimePickerDialog(etInTime));
        etOutTime.setOnClickListener(v -> showTimePickerDialog(etOutTime));
        etOvertime.setOnClickListener(v -> showTimePickerDialog(etOvertime));

        // Save Button Click
        btnEdit.setOnClickListener(v -> saveAttendanceDetails());
    }

    // Show Date Picker
    private void showDatePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String selectedDate = String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
            editText.setText(selectedDate);
        }, year, month, day).show();
    }

    // Show Time Picker
    private void showTimePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String selectedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour);
            editText.setText(selectedTime);
        }, hour, minute, true).show();
    }

    // Save Data Logic
    private void saveAttendanceDetails() {
        String date = etDate.getText().toString().trim();
        String inTime = etInTime.getText().toString().trim();
        String outTime = etOutTime.getText().toString().trim();
        String overtime = etOvertime.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();

        if (date.isEmpty() || inTime.isEmpty() || outTime.isEmpty() || overtime.isEmpty()) {
            Toast.makeText(this, "Please fill all details before saving.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sample Data Submission (Add API call logic here)
        Toast.makeText(this, "Attendance Details Updated Successfully!", Toast.LENGTH_LONG).show();
    }
}
