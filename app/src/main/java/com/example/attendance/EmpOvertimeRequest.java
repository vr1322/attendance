package com.example.attendance;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class EmpOvertimeRequest extends AppCompatActivity {

    private Button btnSelectDate, btnInTime, btnOutTime, btnSubmit;
    private EditText etOvertimeHours;
    private Spinner spinnerStatus;

    private String selectedDate = "", inTime = "", outTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_overtime_request);

        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnInTime = findViewById(R.id.btnInTime);
        btnOutTime = findViewById(R.id.btnOutTime);
        btnSubmit = findViewById(R.id.btnSubmit);
        etOvertimeHours = findViewById(R.id.etOvertimeHours);

        // Spinner data
        String[] statusOptions = {"Overtime", "Normal"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statusOptions);
        spinnerStatus.setAdapter(adapter);

        btnSelectDate.setOnClickListener(v -> openDatePicker());
        btnInTime.setOnClickListener(v -> openTimePicker(true));
        btnOutTime.setOnClickListener(v -> openTimePicker(false));

        btnSubmit.setOnClickListener(v -> {
            String hours = etOvertimeHours.getText().toString().trim();
            String status = spinnerStatus.getSelectedItem().toString();

            if (selectedDate.isEmpty() || inTime.isEmpty() || outTime.isEmpty() || hours.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // You can send this data to the server using an API call (e.g., Retrofit or Volley)
            Toast.makeText(this, "Overtime Request Submitted", Toast.LENGTH_SHORT).show();
        });
    }

    private void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    btnSelectDate.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void openTimePicker(boolean isInTime) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    String formattedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                    if (isInTime) {
                        inTime = formattedTime;
                        btnInTime.setText(inTime);
                    } else {
                        outTime = formattedTime;
                        btnOutTime.setText(outTime);
                    }
                }, hour, minute, true);
        timePickerDialog.show();
    }
}
