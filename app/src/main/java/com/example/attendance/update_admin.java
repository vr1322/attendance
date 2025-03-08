package com.example.attendance;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class update_admin extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView back, logout_iv, profilePic;
    private EditText companyCodeField, companyName, companyRegNo, adminName, designation, adminEmail, mobileNo, address;
    private Button updateBtn;
    private String companyCode;
    private TextView updttext, logut;
    private Uri imageUri;
    private String apiUrlFetch = "http://192.168.168.239/ems_api/get_admin_profile.php";
    private String apiUrlUpdate = "http://192.168.168.239/ems_api/update_admin_profile.php";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_admin);

        back = findViewById(R.id.back);
        updttext = findViewById(R.id.updttext);
        logut = findViewById(R.id.logout);
        profilePic = findViewById(R.id.profilePic);
        logout_iv = findViewById(R.id.logout_iv);
        companyCodeField = findViewById(R.id.companyCode);
        companyName = findViewById(R.id.companyName);
        companyRegNo = findViewById(R.id.companyRegNo);
        adminName = findViewById(R.id.adminName);
        designation = findViewById(R.id.designation);
        adminEmail = findViewById(R.id.adminEmail);
        mobileNo = findViewById(R.id.mobileNo);
        address = findViewById(R.id.address);
        updateBtn = findViewById(R.id.updateBtn);

        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        companyCode = sharedPreferences.getString("company_code", "").trim();  // Trim to remove unwanted spaces

        if (companyCode.isEmpty()) {
            Log.e("CompanyCode", "Company code is missing! Trying to re-fetch...");
            Toast.makeText(this, "Company code is missing!", Toast.LENGTH_SHORT).show();
        } else {
            companyCodeField.setText(companyCode);
            Log.d("CompanyCode", "Retrieved from SharedPreferences: " + companyCode);
            fetchAdminDetails();  // ✅ Call fetchAdminDetails() AFTER setting companyCode
        }


        back.setOnClickListener(view -> startActivity(new Intent(update_admin.this, settings.class)));
        updttext.setOnClickListener(view -> startActivity(new Intent(update_admin.this, settings.class)));
        logout_iv.setOnClickListener(view -> startActivity(new Intent(update_admin.this, MainActivity.class)));
        logut.setOnClickListener(view -> startActivity(new Intent(update_admin.this, MainActivity.class)));
        // Make profilePic act as the choose button
        profilePic.setOnClickListener(view -> openFileChooser());

        updateBtn.setOnClickListener(view -> updateAdminDetails());
    }

    private void fetchAdminDetails() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching details...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrlFetch + "?company_code=" + companyCode,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONObject data = jsonObject.getJSONObject("data");
                            companyCodeField.setText(data.getString("company_code"));
                            companyName.setText(data.getString("company_name"));
                            companyRegNo.setText(data.getString("company_reg_no"));
                            adminName.setText(data.getString("admin_name"));
                            designation.setText(data.getString("designation"));
                            adminEmail.setText(data.getString("admin_email"));
                            mobileNo.setText(data.getString("mobile_no"));
                            address.setText(data.getString("address"));

                            String profileUrl = data.getString("profile_picture");
                            if (!profileUrl.isEmpty()) {
                                Glide.with(this).load(profileUrl).into(profilePic);
                            }
                        } else {
                            Toast.makeText(update_admin.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(update_admin.this, "Error fetching details!", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            progressDialog.dismiss();
            Toast.makeText(update_admin.this, "Failed to fetch details!", Toast.LENGTH_SHORT).show();
        });

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void updateAdminDetails() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating details...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrlUpdate,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Toast.makeText(update_admin.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(update_admin.this, "Update failed!", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            progressDialog.dismiss();
            Toast.makeText(update_admin.this, "Error updating details!", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("company_code", companyCodeField.getText().toString().trim());
                params.put("company_name", companyName.getText().toString());
                params.put("company_reg_no", companyRegNo.getText().toString());
                params.put("admin_name", adminName.getText().toString());
                params.put("designation", designation.getText().toString());
                params.put("admin_email", adminEmail.getText().toString());
                params.put("mobile_no", mobileNo.getText().toString());
                params.put("address", address.getText().toString());
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilePic.setImageURI(imageUri);
        }
    }
}