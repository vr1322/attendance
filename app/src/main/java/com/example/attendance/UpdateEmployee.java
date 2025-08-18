package com.example.attendance;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateEmployee extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int UCROP_REQUEST_CODE = 2;

    private CircleImageView profilePic;
    private EditText employeeName, employeeEmail, employeeMobileNo, employeeAddress;
    private TextView companyName, companyRegNo, companyCode, logoutTv, updateText, designation;
    private ImageView back, logout_iv;
    private Button updateBtn;

    private String companyCodeStr, employeeIdStr, base64Image = "";
    private Uri imageUri;

    private final String apiUrlFetch = "https://devonix.io/ems_api/fetch_employee_profile.php";
    private final String apiUrlUpdate = "https://devonix.io/ems_api/update_employee_profile.php";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_employee);

        profilePic = findViewById(R.id.profilePic_employee);
        companyName = findViewById(R.id.companyName_employee);
        companyRegNo = findViewById(R.id.companyRegNo_employee);
        companyCode = findViewById(R.id.companyCode_employee);

        employeeName = findViewById(R.id.employeeName);
        designation = findViewById(R.id.designation_employee);
        employeeEmail = findViewById(R.id.employeeEmail);
        employeeMobileNo = findViewById(R.id.employeeMobileNo);
        employeeAddress = findViewById(R.id.employeeAddress);

        updateBtn = findViewById(R.id.updateBtn_employee);
        updateText = findViewById(R.id.updttext_employee);
        logoutTv = findViewById(R.id.logout_employee);
        back = findViewById(R.id.back_employee);
        logout_iv = findViewById(R.id.logout_iv_employee);

        SharedPreferences sharedPreferences = getSharedPreferences("EmployeeSession", MODE_PRIVATE);
        companyCodeStr = sharedPreferences.getString("company_code", "").trim();
        employeeIdStr = sharedPreferences.getString("employee_id", "").trim();

        Log.d("UpdateEmployee", "Company Code: " + companyCodeStr + " | Employee ID: " + employeeIdStr);

        back.setOnClickListener(view -> startActivity(new Intent(UpdateEmployee.this, EmployeeHomeActivity.class)));
        updateText.setOnClickListener(view -> startActivity(new Intent(UpdateEmployee.this, EmployeeHomeActivity.class)));
        logout_iv.setOnClickListener(view -> showLogoutDialog());
        logoutTv.setOnClickListener(view -> showLogoutDialog());

        if (companyCodeStr.isEmpty()) {
            Toast.makeText(this, "Company code missing!", Toast.LENGTH_SHORT).show();
        } else {
            companyCode.setText(companyCodeStr);
            fetchEmployeeDetails();
        }

        profilePic.setOnClickListener(view -> openFileChooser());
        updateBtn.setOnClickListener(view -> updateEmployeeDetails());
    }

    private void fetchEmployeeDetails() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching details...");
        progressDialog.show();

        String url = apiUrlFetch + "?company_code=" + companyCodeStr + "&employee_id=" + employeeIdStr;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressDialog.dismiss();
                    Log.d("FetchEmployeeResponse", response);

                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            JSONObject data = json.getJSONObject("data");

                            companyName.setText(data.getString("company_name"));
                            companyRegNo.setText(data.getString("company_reg_no"));
                            companyCode.setText(data.getString("company_code"));

                            employeeName.setText(data.getString("employee_name"));
                            designation.setText(data.getString("designation")); // ✅ fixed key
                            employeeEmail.setText(data.getString("employee_email"));
                            employeeMobileNo.setText(data.getString("employee_phone"));
                            employeeAddress.setText(data.getString("employee_address"));

                            String profileUrl = data.getString("profile_pic");
                            if (!profileUrl.isEmpty()) {
                                Glide.with(this).load(profileUrl).into(profilePic);
                            }

                        } else {
                            Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parse error!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Fetch failed!", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void updateEmployeeDetails() {
        String name = employeeName.getText().toString().trim();
        String email = employeeEmail.getText().toString().trim();
        String phone = employeeMobileNo.getText().toString().trim();
        String addressStr = employeeAddress.getText().toString().trim();

        if (name.isEmpty()) { employeeName.setError("Enter employee name"); employeeName.requestFocus(); return; }
        if (email.isEmpty()) { employeeEmail.setError("Enter email"); employeeEmail.requestFocus(); return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { employeeEmail.setError("Enter valid email"); employeeEmail.requestFocus(); return; }
        if (phone.isEmpty()) { employeeMobileNo.setError("Enter mobile number"); employeeMobileNo.requestFocus(); return; }
        if (!phone.matches("\\d{10}")) { employeeMobileNo.setError("Enter valid 10-digit mobile number"); employeeMobileNo.requestFocus(); return; }
        if (addressStr.isEmpty()) { employeeAddress.setError("Enter address"); employeeAddress.requestFocus(); return; }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, apiUrlUpdate,
                response -> {
                    progressDialog.dismiss();
                    Log.d("UpdateEmployeeResponse", response);
                    try {
                        JSONObject json = new JSONObject(response);
                        String status = json.getString("status");
                        String msg = json.getString("message");
                        if (status.equals("success")) {
                            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Update failed: " + msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Update error!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error updating profile!", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("company_code", companyCodeStr);
                params.put("employee_id", employeeIdStr);
                params.put("employee_name", name);
                params.put("designation", designation.getText().toString());
                params.put("employee_email", email);
                params.put("mobile_no", phone);
                params.put("address", addressStr);
                if (!base64Image.isEmpty()) { params.put("profile_pic", base64Image); }
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri sourceUri = data.getData();
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "croppedImage.jpg"));

            UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(800, 800)
                    .start(this, UCROP_REQUEST_CODE);

        } else if (requestCode == UCROP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    profilePic.setImageBitmap(bitmap);
                    base64Image = getCompressedBase64(bitmap);
                } catch (IOException e) { e.printStackTrace(); }
            }
        } else if (requestCode == UCROP_REQUEST_CODE && resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Crop error: " + cropError, Toast.LENGTH_SHORT).show();
        }
    }

    private String getCompressedBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Do you really want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SharedPreferences.Editor editor = getSharedPreferences("EmployeeSession", MODE_PRIVATE).edit();
                    editor.clear();
                    editor.apply();
                    Intent intent = new Intent(UpdateEmployee.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
