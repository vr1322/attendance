package com.example.attendance;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class AttendanceDetailsBottomSheet extends BottomSheetDialogFragment {

    private static final String BASE_URL = "https://devonix.io/ems_api/";
    private static final String MARK_ATTENDANCE_URL = BASE_URL + "mark_attendance.php";

    private TextView tvName;
    private EditText etDate, etInTime, etOutTime;
    private Spinner spinnerStatus;
    private Button btnEdit, btnCapture;
    private ImageView imgPreview;

    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    private Bitmap capturedImageBitmap = null; // Store captured image

    // Camera launcher
    private final ActivityResultLauncher<Intent> captureImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    capturedImageBitmap = (Bitmap) extras.get("data");
                    if (capturedImageBitmap != null) {
                        imgPreview.setImageBitmap(capturedImageBitmap);
                    }
                } else {
                    Toast.makeText(requireContext(), "Capture cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_attendance_details, container, false);

        // Initialize Views
        tvName = view.findViewById(R.id.tv_name);
        etDate = view.findViewById(R.id.et_date);
        etInTime = view.findViewById(R.id.et_intime);
        etOutTime = view.findViewById(R.id.et_outtime);
        spinnerStatus = view.findViewById(R.id.spinner_status);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnCapture = view.findViewById(R.id.btnCapture);
        imgPreview = view.findViewById(R.id.imgPreview);

        // Volley
        requestQueue = Volley.newRequestQueue(requireContext());

        // SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);
        String empName = sharedPreferences.getString("employee_name", "").trim();
        tvName.setText(empName);

        // Pickers
        etDate.setOnClickListener(v -> showDatePickerDialog(etDate));
        etInTime.setOnClickListener(v -> showTimePickerDialog(etInTime));
        etOutTime.setOnClickListener(v -> showTimePickerDialog(etOutTime));

        // Capture Button
        btnCapture.setOnClickListener(v -> openCamera());

        // Save Button
        btnEdit.setOnClickListener(v -> markAttendance());

        return view;
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            captureImageLauncher.launch(intent);
        } else {
            Toast.makeText(requireContext(), "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void markAttendance() {
        String employeeId = sharedPreferences.getString("employee_id", "");
        String companyCode = sharedPreferences.getString("company_code", "");
        String employeeName = sharedPreferences.getString("employee_name", "");
        String branch = sharedPreferences.getString("branch", "");
        String role = sharedPreferences.getString("role", "");
        String status = spinnerStatus.getSelectedItem().toString();
        String date = etDate.getText().toString().trim();

        // ✅ Get in_time & out_time
        String inTime = etInTime.getText().toString().trim();
        String outTime = etOutTime.getText().toString().trim();

        int geofencedStatus = 1;

        if (employeeId.isEmpty() || companyCode.isEmpty() || employeeName.isEmpty() || branch.isEmpty() || status.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Ensure date is mandatory
        if (date.isEmpty()) {
            Toast.makeText(requireContext(), "Date is required for marking attendance!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Role-based restrictions
        if (role.equalsIgnoreCase("Supervisor") || role.equalsIgnoreCase("Manager")) {
            if (!status.equalsIgnoreCase("Absent")) {
                Toast.makeText(requireContext(), "Supervisors/Managers can only mark employees as Absent!", Toast.LENGTH_SHORT).show();
                return;
            }
            // For Absent → no photo required
        } else if (role.equalsIgnoreCase("Admin")) {
            // Admin → photo optional
        } else {
            // Regular Employee → photo required
            if (capturedImageBitmap == null) {
                Toast.makeText(requireContext(), "Photo capture is required for employees.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Encode image if present
        String encodedImage = "";
        if (capturedImageBitmap != null) {
            encodedImage = compressAndEncodeImage(capturedImageBitmap);
        }

        // Build JSON
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("employee_id", employeeId);
            jsonBody.put("company_code", companyCode);
            jsonBody.put("employee_name", employeeName);
            jsonBody.put("branch", branch);
            jsonBody.put("attendance_status", status);
            jsonBody.put("geofenced_status", geofencedStatus);
            jsonBody.put("date", date);

            // ✅ Add times
            jsonBody.put("in_time", inTime);
            jsonBody.put("out_time", outTime);

            jsonBody.put("attendance_image", encodedImage);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "JSON Error!", Toast.LENGTH_SHORT).show();
            return;
        }

        // API request
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

    // Compress and encode image
    private String compressAndEncodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    // Date Picker
    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, y, m, d) -> {
            String formattedDate = String.format("%04d-%02d-%02d", y, m + 1, d);
            editText.setText(formattedDate);
        }, year, month, day).show();
    }

    // Time Picker
    private void showTimePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(requireContext(), (view, h, m) -> {
            String amPm = (h >= 12) ? "PM" : "AM";
            int hour12 = (h == 0 || h == 12) ? 12 : h % 12;
            String selectedTime = String.format("%02d:%02d %s", hour12, m, amPm);
            editText.setText(selectedTime);
        }, hour, minute, false).show();
    }
}
