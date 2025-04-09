package com.example.attendance;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AllocateOvertimeActivity extends AppCompatActivity {

    private Button btnSelectDate, btnInTime, btnOutTime, btnSubmit;
    private EditText etOvertimeHours;
    private Spinner spinnerStatus;

    private Calendar selectedDate = Calendar.getInstance();
    private String inTime = "", outTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allocate_overtime); // Make sure this matches your XML filename

        // Initialize views
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnInTime = findViewById(R.id.btnInTime);
        btnOutTime = findViewById(R.id.btnOutTime);
        btnSubmit = findViewById(R.id.btnSubmit);
        etOvertimeHours = findViewById(R.id.etOvertimeHours);
        spinnerStatus = findViewById(R.id.spinnerStatus);

        // Spinner setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Overtime", "Normal"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Date Picker
        btnSelectDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());
                        btnSelectDate.setText(dateString);
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        // Time Pickers
        btnInTime.setOnClickListener(v -> showTimePicker(true));
        btnOutTime.setOnClickListener(v -> showTimePicker(false));

        // Submit Button
        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    private void showTimePicker(boolean isInTime) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    if (isInTime) {
                        inTime = time;
                        btnInTime.setText(time);
                    } else {
                        outTime = time;
                        btnOutTime.setText(time);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        dialog.show();
    }

    private void handleSubmit() {
        String date = btnSelectDate.getText().toString().trim();
        String hours = etOvertimeHours.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();

        if (date.isEmpty() || inTime.isEmpty() || outTime.isEmpty() || hours.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // You can now send these values to your backend or database
        Toast.makeText(this, "Submitted:\nDate: " + date +
                        "\nIn-Time: " + inTime +
                        "\nOut-Time: " + outTime +
                        "\nHours: " + hours +
                        "\nStatus: " + status,
                Toast.LENGTH_LONG).show();
    }
}
