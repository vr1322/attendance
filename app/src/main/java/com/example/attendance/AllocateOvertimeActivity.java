package com.example.attendance;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.attendance.R;

import java.util.Calendar;

public class AllocateOvertimeActivity extends AppCompatActivity {

    EditText editTextDate, editTextInTime, editTextOutTime, etOvertimeHours;
    Button btnSelectDate, btnInTime, btnOutTime, btnSubmit;
    Spinner spinnerStatus;

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
                    selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    editTextDate.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void showTimePicker(EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                AllocateOvertimeActivity.this,
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    targetEditText.setText(time);
                },
                hour, minute, true
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

        // You can now use these values to send to your backend or database
        // Example output:
        String message = "Date: " + selectedDate +
                "\nIn-Time: " + inTime +
                "\nOut-Time: " + outTime +
                "\nOvertime Hours: " + overtimeHours +
                "\nStatus: " + status;

        Toast.makeText(this, "Submitted:\n" + message, Toast.LENGTH_LONG).show();

        // TODO: Add API call or database logic here
    }
}
