package com.example.attendance;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class AttendanceDetailsBottomSheet extends BottomSheetDialogFragment {

    private static final String BASE_URL = "https://devonix.io/ems_api/";
    private static final String GET_ALL_ATTENDANCE_URL = BASE_URL + "get_all_attendance.php";
    private static final String MARK_ATTENDANCE_URL = BASE_URL + "mark_attendance.php";

    private TextView tvName;
    private EditText etDate, etInTime, etOutTime, etOvertime;
    private Spinner spinnerStatus;
    private Button btnEdit;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

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

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(requireContext());

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);
        String empName = sharedPreferences.getString("employee_name", "").trim();
        tvName.setText(empName);

        // Date and Time Pickers
        etDate.setOnClickListener(v -> showDatePickerDialog(etDate));
        etInTime.setOnClickListener(v -> showTimePickerDialog(etInTime));
        etOutTime.setOnClickListener(v -> showTimePickerDialog(etOutTime));
        etOvertime.setOnClickListener(v -> showOvertimePickerDialog());

        // Handle "Edit" button click
        btnEdit.setOnClickListener(v -> markAttendance());

        return view;
    }


    private void markAttendance() {
        String employeeId = sharedPreferences.getString("employee_id", "");
        String companyCode = sharedPreferences.getString("company_code", "");
        String employeeName = sharedPreferences.getString("employee_name", "");
        String branch = sharedPreferences.getString("branch", "");
        String inTime = etInTime.getText().toString().trim();
        String outTime = etOutTime.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();
        String date = etDate.getText().toString().trim();
        int geofencedStatus = 1; // Assuming employee is within geofence
        Log.d("AttendanceDebug", "Employee ID: " + employeeId);
        Log.d("AttendanceDebug", "Company Code: " + companyCode);
        Log.d("AttendanceDebug", "Employee Name: " + employeeName);
        Log.d("AttendanceDebug", "Branch: " + branch);
        Log.d("AttendanceDebug", "Status: " + status);
        Log.d("AttendanceDebug", "In Time: " + inTime);
        Log.d("AttendanceDebug", "Out Time: " + outTime);
        Log.d("AttendanceDebug", "Date: " + date);

        if (employeeId.isEmpty() || companyCode.isEmpty() || employeeName.isEmpty() || branch.isEmpty() || status.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("employee_id", employeeId);
            jsonBody.put("company_code", companyCode);
            jsonBody.put("employee_name", employeeName);
            jsonBody.put("branch", branch);
            jsonBody.put("in_time", inTime);
            jsonBody.put("out_time", outTime);
            jsonBody.put("attendance_status", status);
            jsonBody.put("geofenced_status", geofencedStatus);
            jsonBody.put("date", date);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "JSON Error!", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, MARK_ATTENDANCE_URL, jsonBody,
                response -> {
                    try {
                        boolean success = response.getString("status").equals("success");
                        String message = response.getString("message");
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        if (success) dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Response Error!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "Network Error!", Toast.LENGTH_SHORT).show());

        requestQueue.add(request);
    }

    // Date Picker Dialog
    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH); // 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            // Ensure two-digit format for day and month
            String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            editText.setText(formattedDate);
        }, year, month, day).show();
    }


    // Time Picker Dialog
    private void showTimePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(requireContext(), (view, selectedHour, selectedMinute) -> {
            String amPm = (selectedHour >= 12) ? "PM" : "AM";
            int hourIn12Format = (selectedHour == 0 || selectedHour == 12) ? 12 : selectedHour % 12;
            String selectedTime = String.format("%02d:%02d %s", hourIn12Format, selectedMinute, amPm);
            editText.setText(selectedTime);
        }, hour, minute, false).show();
    }

    // NumberPicker for Overtime (Hours Only)
    private void showOvertimePickerDialog() {
        BottomSheetDialog overtimeDialog = new BottomSheetDialog(requireContext());
        View overtimeView = getLayoutInflater().inflate(R.layout.dialog_overtime_picker, null);
        NumberPicker numberPicker = overtimeView.findViewById(R.id.numberPicker);
        Button btnConfirm = overtimeView.findViewById(R.id.btnConfirmOvertime);

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(12);
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
