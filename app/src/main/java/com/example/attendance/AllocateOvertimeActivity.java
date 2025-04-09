package com.example.attendance;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AllocateOvertimeActivity extends AppCompatActivity {

    EditText editTextDate, editTextInTime, editTextOutTime, etOvertimeHours;
    Button btnSelectDate, btnInTime, btnOutTime, btnSubmit;
    Spinner spinnerStatus;
    ImageButton backBtn; // Back Button

    String selectedDate = "", inTime = "", outTime = "", overtimeHours = "", status = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allocate_overtime);

        // Initialize views
        editTextDate = findViewById(R.id.editTextDate);
        editTextInTime = findViewById(R.id.editTextInTime);
        editTextOutTime = findViewById(R.id.editTextOutTime);
        etOvertimeHours = findViewById(R.id.etOvertimeHours);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnInTime = findViewById(R.id.btnInTime);
        btnOutTime = findViewById(R.id.btnOutTime);
        btnSubmit = findViewById(R.id.btnSubmit);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        backBtn = findViewById(R.id.back_btn); // Initialize back button

        // Back button click
        backBtn.setOnClickListener(v -> finish());

        // Setup spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Overtime", "Normal"}
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Date Picker
        btnSelectDate.setOnClickListener(view -> showDatePicker());
        editTextDate.setOnClickListener(view -> showDatePicker());

        // In Time Picker
        btnInTime.setOnClickListener(view -> showTimePicker(editTextInTime));
        editTextInTime.setOnClickListener(view -> showTimePicker(editTextInTime));

        // Out Time Picker
        btnOutTime.setOnClickListener(view -> showTimePicker(editTextOutTime));
        editTextOutTime.setOnClickListener(view -> showTimePicker(editTextOutTime));

        // Submit Button
        btnSubmit.setOnClickListener(view -> handleSubmit());
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AllocateOvertimeActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    editTextDate.setText(selectedDate); // Update the EditText with the selected date
                },
                year, month, day
        );

        datePickerDialog.show();
    }


    private void showTimePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                AllocateOvertimeActivity.this,
                (view, selectedHour, selectedMinute) -> {
                    String amPm = (selectedHour >= 12) ? "PM" : "AM";
                    int hourIn12Format = (selectedHour == 0 || selectedHour == 12) ? 12 : selectedHour % 12;
                    String selectedTime = String.format("%02d:%02d %s", hourIn12Format, selectedMinute, amPm);
                    targetEditText.setText(selectedTime);
                },
                hour, minute, false // false to show 12-hour format in dialog
        );

        timePickerDialog.show();
    }


    private void handleSubmit() {
        // Get all input values
        selectedDate = editTextDate.getText().toString().trim();
        inTime = editTextInTime.getText().toString().trim();
        outTime = editTextOutTime.getText().toString().trim();
        overtimeHours = etOvertimeHours.getText().toString().trim();
        status = spinnerStatus.getSelectedItem().toString();

        // Basic validation
        if (selectedDate.isEmpty() || inTime.isEmpty() || outTime.isEmpty() || overtimeHours.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Read employee data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        String employeeId = sharedPreferences.getString("employee_id", "");
        String employeeName = sharedPreferences.getString("employee_name", "");
        String branch = sharedPreferences.getString("branch", "");

        // API URL
        String apiUrl = "https://devonix.io/ems_api/employee_overtime_attendance.php";

        // Send POST request using Volley
        StringRequest request = new StringRequest(Request.Method.POST, apiUrl,
                response -> {
                    Toast.makeText(this, "Overtime Submitted Successfully!", Toast.LENGTH_LONG).show();
                    finish(); // Optional: close activity
                },
                error -> {
                    Toast.makeText(this, "Submission failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("employee_id", employeeId);
                params.put("employee_name", employeeName);
                params.put("branch", branch);
                params.put("date", selectedDate);
                params.put("in_time", inTime);
                params.put("out_time", outTime);
                params.put("overtime_hours", overtimeHours);
                params.put("status", status);
                return params;
            }
        };

        // Add to request queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

}
