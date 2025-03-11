package com.example.attendance;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import de.hdodenhof.circleimageview.CircleImageView;

public class add_emp extends AppCompatActivity {
    private EditText etEmployeeName, etEmployeeId, etDateOfBirth, etJoiningDate, etDesignation, etPhone, etAddress, etEmail, etPassword, etBasicPay, etOvertimeAllowance;
    private ImageView backButton, icCalendarDob, icCalendarJoining;
    private CircleImageView profilePic;
    private TextView addemp_text;
    private AutoCompleteTextView etBranch;
    private RadioGroup paymentTypeGroup;
    private RadioButton perDay, monthly;
    private Button saveButton;
    private Bitmap bitmap;
    private ProgressDialog progressDialog;

    private static final String ADD_EMPLOYEE_URL = "http://192.168.168.239/ems_api/add_employee.php";
    private static final String BASE_URL = "http://192.168.168.239/ems_api/";
    private static final String GET_BRANCHES_URL = BASE_URL + "get_branches.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emp);

        initializeViews();
        setClickListeners();
        loadBranches();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back);
        addemp_text = findViewById(R.id.add_emp_text);
        profilePic = findViewById(R.id.profilePic);
        etEmployeeName = findViewById(R.id.et_employee_name);
        etEmployeeId = findViewById(R.id.et_employee_id);
        etDateOfBirth = findViewById(R.id.et_date_of_birth);
        etJoiningDate = findViewById(R.id.et_joining_date);
        icCalendarDob = findViewById(R.id.ic_calendar_dob);
        icCalendarJoining = findViewById(R.id.ic_calendar_joining);
        etDesignation = findViewById(R.id.ad_emp_design);
        etPhone = findViewById(R.id.add_emp_number);
        etAddress = findViewById(R.id.add_emp_address);
        etBranch = findViewById(R.id.add_emp_branch);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.pass);
        etBasicPay = findViewById(R.id.basicPay);
        etOvertimeAllowance = findViewById(R.id.overtimeAllowance);
        paymentTypeGroup = findViewById(R.id.paymentTypeGroup);
        perDay = findViewById(R.id.radio_per_day);
        monthly = findViewById(R.id.radio_monthly);
        saveButton = findViewById(R.id.save_btn);
        progressDialog = new ProgressDialog(this);
    }

    private void setClickListeners() {
        backButton.setOnClickListener(view -> startActivity(new Intent(add_emp.this, emp_list.class)));
        addemp_text.setOnClickListener(view -> startActivity(new Intent(add_emp.this, emp_list.class)));

        icCalendarDob.setOnClickListener(v -> showDatePickerDialog(etDateOfBirth));
        icCalendarJoining.setOnClickListener(v -> showDatePickerDialog(etJoiningDate));
        etDateOfBirth.setOnClickListener(v -> showDatePickerDialog(etDateOfBirth));
        etJoiningDate.setOnClickListener(v -> showDatePickerDialog(etJoiningDate));


        profilePic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);
        });

        saveButton.setOnClickListener(v -> addEmployee());
    }


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                profilePic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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



    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
    private void loadBranches() {
        progressDialog.setMessage("Loading branches...");
        progressDialog.show();

        String companyCode = getSharedPreferences("AdminPrefs", MODE_PRIVATE).getString("company_code", "");

        String url = GET_BRANCHES_URL + "?company_code=" + companyCode;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONArray branchesArray = jsonObject.getJSONArray("branches");
                            List<String> branchList = new ArrayList<>();

                            for (int i = 0; i < branchesArray.length(); i++) {
                                JSONObject branch = branchesArray.getJSONObject(i);
                                branchList.add(branch.getString("branch_name"));
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, branchList);
                            etBranch.setAdapter(adapter);
                            etBranch.setThreshold(1); // Start suggesting after 1 character
                        } else {
                            Toast.makeText(this, "No branches found!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing branches!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Network error!", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }

    // Function to convert "DD/MM/YYYY" to "YYYY-MM-DD"
    private String convertDateFormat(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date parsedDate = inputFormat.parse(date);
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }


    private void addEmployee() {
        // Retrieve company_code from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        String companyCode = sharedPreferences.getString("company_code", "");

        if (companyCode.isEmpty()) {
            Toast.makeText(this, "Company code is missing. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String employeeId = etEmployeeId.getText().toString().trim();
        String name = etEmployeeName.getText().toString().trim();
        String designation = etDesignation.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String branch = etBranch.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String dob = etDateOfBirth.getText().toString().trim();
        String joiningDate = etJoiningDate.getText().toString().trim();
        String basicPay = etBasicPay.getText().toString().trim();
        String overtimeAllowance = etOvertimeAllowance.getText().toString().trim();
        String formattedDob = convertDateFormat(dob);
        String formattedJoiningDate = convertDateFormat(joiningDate);

        // Get selected payment type
        String paymentType = perDay.isChecked() ? "Per Day" : "Monthly";

          // Check if dates are valid
        if (formattedDob.isEmpty() || formattedJoiningDate.isEmpty()) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }
        // Validate required fields
        if (employeeId.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty() || branch.isEmpty()) {
            Toast.makeText(this, "All required fields must be filled!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert profile image to Base64
        String profileImageBase64 = "";
        if (bitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            profileImageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        }

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding Employee...");
        progressDialog.show();

        // Make API request
        String finalProfileImageBase6 = profileImageBase64;
        StringRequest request = new StringRequest(Request.Method.POST, "http://192.168.168.239/ems_api/add_employee.php",
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            Toast.makeText(this, "Employee added successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        Log.e("AddEmployee", "JSON Parsing Error", e);
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    Log.e("AddEmployee", "Volley Error", error);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("company_code", companyCode);
                params.put("employee_id", employeeId);
                params.put("employee_name", name);
                params.put("designation", designation);
                params.put("phone", phone);
                params.put("address", address);
                params.put("branch", branch);
                params.put("email", email);
                params.put("password", password);
                params.put("dob", dob);
                params.put("joining_date", joiningDate);
                params.put("basic_pay", basicPay);
                params.put("overtime_allowance", overtimeAllowance);
                params.put("payment_type", paymentType);
                params.put("profile_image", finalProfileImageBase6);
                params.put("dob", formattedDob);
                params.put("joining_date", formattedJoiningDate);
                return params;
            }

        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

}
