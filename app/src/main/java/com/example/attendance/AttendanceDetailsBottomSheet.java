package com.example.attendance;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;

public class AttendanceDetailsBottomSheet extends BottomSheetDialogFragment {

    private TextView tvName;
    private EditText etDate, etInTime, etOutTime, etOvertime;
    private Spinner spinnerStatus;
    private Button btnEdit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_attendance_details, container, false);

        // Initialize Views
        tvName = view.findViewById(R.id.tv_name);
        etDate = view.findViewById(R.id.et_date);
        etInTime = view.findViewById(R.id.et_intime);
        etOutTime = view.findViewById(R.id.et_outtime);
        etOvertime = view.findViewById(R.id.et_overtime);
        spinnerStatus = view.findViewById(R.id.spinner_status);
        btnEdit = view.findViewById(R.id.btn_edit);

        // Populate data
        loadAttendanceDetails();

        // Date and Time Pickers
        etDate.setOnClickListener(v -> showDatePickerDialog(etDate));
        etInTime.setOnClickListener(v -> showTimePickerDialog(etInTime));
        etOutTime.setOnClickListener(v -> showTimePickerDialog(etOutTime));
        etOvertime.setOnClickListener(v -> showOvertimePickerDialog());

        // Handle "Edit" button click
        btnEdit.setOnClickListener(v -> updateAttendanceDetails());

        return view;
    }

    private void loadAttendanceDetails() {
        tvName.setText("John Smith");
        etDate.setText("15 JAN 2025");
        etInTime.setText("10:00 AM");
        etOutTime.setText("07:00 PM");
        etOvertime.setText("0 hrs");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.attendance_status, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        spinnerStatus.setSelection(0);
    }

    // Date Picker Dialog
    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = selectedDay + " " + getMonthName(selectedMonth) + " " + selectedYear;
            editText.setText(selectedDate);
        }, year, month, day).show();
    }

    // Time Picker Dialog with AM/PM Format
    private void showTimePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(requireContext(), (view, selectedHour, selectedMinute) -> {
            String amPm = (selectedHour >= 12) ? "PM" : "AM";
            int hourIn12Format = (selectedHour == 0 || selectedHour == 12) ? 12 : selectedHour % 12;
            String selectedTime = String.format("%02d:%02d %s", hourIn12Format, selectedMinute, amPm);
            editText.setText(selectedTime);
        }, hour, minute, false).show();  // `false` for 12-hour format
    }

    // NumberPicker for Overtime (Hours Only)
    private void showOvertimePickerDialog() {
        BottomSheetDialog overtimeDialog = new BottomSheetDialog(requireContext());
        View overtimeView = getLayoutInflater().inflate(R.layout.dialog_overtime_picker, null);
        NumberPicker numberPicker = overtimeView.findViewById(R.id.numberPicker);
        Button btnConfirm = overtimeView.findViewById(R.id.btnConfirmOvertime);

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(12);  // Set max overtime limit (adjust if needed)
        numberPicker.setValue(0);

        btnConfirm.setOnClickListener(v -> {
            etOvertime.setText(numberPicker.getValue() + " hrs");
            overtimeDialog.dismiss();
        });

        overtimeDialog.setContentView(overtimeView);
        overtimeDialog.show();
    }

    private String getMonthName(int month) {
        String[] monthNames = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
        return monthNames[month];
    }

    private void updateAttendanceDetails() {
        String selectedStatus = spinnerStatus.getSelectedItem().toString();
        Toast.makeText(requireContext(), "Attendance marked as: " + selectedStatus, Toast.LENGTH_SHORT).show();
    }
}
