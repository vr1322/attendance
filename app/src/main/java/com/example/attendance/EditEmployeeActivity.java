package com.example.attendance;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditEmployeeActivity extends AppCompatActivity {
    private EditText etEmployeeName, etEmployeeId, etDateOfBirth, etJoiningDate, etDesignation, etPhone, etAddress, etEmail, etPassword, etBasicPay, etOvertimeAllowance;
    private ImageView backButton, icCalendarDob, icCalendarJoining;
    private CircleImageView profilePic;
    private TextView editEmpText;
    private AutoCompleteTextView etBranch;
    private RadioGroup paymentTypeGroup;
    private RadioButton perDay, monthly;
    private Button updateButton;
    private Bitmap bitmap;
    private ProgressDialog progressDialog;

    private static final String BASE_URL = "https://devonix.io/ems_api/";
    private static final String EDIT_EMPLOYEE_URL = BASE_URL + "edit_employee.php";
    private static final String GET_BRANCHES_URL = BASE_URL + "get_branches.php";
    private static final String GET_EMPLOYEE_DETAILS_URL = BASE_URL + "get_employee_details.php";

    private String employeeId, companyCode, profileImageBase64 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_employee);

        initializeViews();
        setClickListeners();

        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        companyCode = sharedPreferences.getString("company_code", "");

        Intent intent = getIntent();
        employeeId = intent.getStringExtra("employee_id");
        if (intent.hasExtra("company_code")) {
            companyCode = intent.getStringExtra("company_code");
        }

        if (employeeId != null && !employeeId.isEmpty()) {
            getEmployeeDetails();
        } else {
            Toast.makeText(this, "Employee ID is missing!", Toast.LENGTH_SHORT).show();
            finish();
        }

        loadBranches();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back);
        editEmpText = findViewById(R.id.updt_emp_text);
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
        updateButton = findViewById(R.id.save_btn);
        progressDialog = new ProgressDialog(this);
    }

    private void setClickListeners() {
        backButton.setOnClickListener(view -> finish());
        editEmpText.setOnClickListener(view -> finish());

        icCalendarDob.setOnClickListener(v -> showDatePickerDialog(etDateOfBirth));
        icCalendarJoining.setOnClickListener(v -> showDatePickerDialog(etJoiningDate));
        etDateOfBirth.setOnClickListener(v -> showDatePickerDialog(etDateOfBirth));
        etJoiningDate.setOnClickListener(v -> showDatePickerDialog(etJoiningDate));

        profilePic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);
        });

        updateButton.setOnClickListener(v -> updateEmployee());
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

    private void getEmployeeDetails() {
        progressDialog.setMessage("Loading Employee Details...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, GET_EMPLOYEE_DETAILS_URL,
                response -> {
                    progressDialog.dismiss();
                    Log.d("API_RESPONSE", "Response: " + response); // Print the full response

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONObject employee = jsonObject.getJSONObject("employee");

                            // Print employee details to debug
                            Log.d("EMPLOYEE_DETAILS", employee.toString());

                            etEmployeeName.setText(employee.optString("employee_name", ""));
                            etEmployeeId.setText(employee.optString("employee_id", ""));
                            etDesignation.setText(employee.optString("designation", ""));
                            etPhone.setText(employee.optString("phone", ""));
                            etAddress.setText(employee.optString("address", ""));
                            etBranch.setText(employee.optString("branch_name", ""));
                            etEmail.setText(employee.optString("email", ""));
                            etPassword.setText(employee.optString("password", ""));
                            etDateOfBirth.setText(convertToDisplayFormat(employee.optString("date_of_birth", "")));
                            etJoiningDate.setText(convertToDisplayFormat(employee.optString("joining_date", "")));
                            etBasicPay.setText(employee.optString("basic_pay", ""));
                            etOvertimeAllowance.setText(employee.optString("overtime_allowance", ""));

                            // Set payment type
                            if (employee.optString("payment_type", "").equals("Per Day")) {
                                perDay.setChecked(true);
                            } else {
                                monthly.setChecked(true);
                            }

                            // ✅ CORRECT: Use Glide to load the image from URL
                            String profileImagePath = employee.optString("profile_pic", "").trim();
                            if (!profileImagePath.isEmpty()) {
                                String imageUrl = "https://devonix.io/ems_api/" + profileImagePath;
                                Glide.with(this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_profile) // Set a default image
                                        .error(R.drawable.ic_profile) // Show error image if loading fails
                                        .into(profilePic);
                            }


                        } else {
                            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("JSON_ERROR", "Error parsing response: " + e.getMessage());
                        Toast.makeText(this, "Error parsing response!", Toast.LENGTH_SHORT).show();
                    }

    },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Network error!", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("employee_id", employeeId);
                params.put("company_code", companyCode);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private String convertToDisplayFormat(String dateStr) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateStr;  // Return original if parsing fails
        }
    }

    private String convertDateFormat(String dateStr) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";  // Return empty string if parsing fails
        }
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                profilePic.setImageBitmap(bitmap);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                profileImageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateEmployee() {
        String name = etEmployeeName.getText().toString().trim();
        String designation = etDesignation.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String branch = etBranch.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String dob = convertDateFormat(etDateOfBirth.getText().toString().trim());
        String joiningDate = convertDateFormat(etJoiningDate.getText().toString().trim());
        String basicPay = etBasicPay.getText().toString().trim();
        String overtimeAllowance = etOvertimeAllowance.getText().toString().trim();
        String paymentType = perDay.isChecked() ? "Per Day" : "Monthly";

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Employee...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, EDIT_EMPLOYEE_URL,
                response -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Employee updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Network error!", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("employee_id", employeeId);
                params.put("company_code", companyCode);
                params.put("employee_name", name);
                params.put("designation", designation);
                params.put("phone", phone);
                params.put("address", address);
                params.put("branch", branch);
                params.put("email", email);
                params.put("password", password);
                params.put("basic_pay", basicPay);
                params.put("overtime_allowance", overtimeAllowance);
                params.put("payment_type", paymentType);
                params.put("dob", dob);
                params.put("joining_date", joiningDate);
                params.put("profile_image", profileImageBase64);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
