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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class update_admin extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private CircleImageView profilePic;
    private EditText companyCodeField, companyName, companyRegNo, adminName, designation, adminEmail, mobileNo, address;
    private Button updateBtn;
    private ImageView back, logout_iv;
    private String companyCode, base64Image = "";
    private TextView updttext, logut;
    private Uri imageUri;
    private String apiUrlFetch = "http://192.168.168.239/ems_api/get_admin_profile.php";
    private String apiUrlUpdate = "http://192.168.168.239/ems_api/update_admin_profile.php";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_admin);

        profilePic = findViewById(R.id.profilePic);
        companyCodeField = findViewById(R.id.companyCode);
        companyName = findViewById(R.id.companyName);
        companyRegNo = findViewById(R.id.companyRegNo);
        adminName = findViewById(R.id.adminName);
        designation = findViewById(R.id.designation);
        adminEmail = findViewById(R.id.adminEmail);
        mobileNo = findViewById(R.id.mobileNo);
        address = findViewById(R.id.address);
        updateBtn = findViewById(R.id.updateBtn);
        updttext = findViewById(R.id.updttext);
        logut = findViewById(R.id.logout);
        back = findViewById(R.id.back);
        logout_iv = findViewById(R.id.logout_iv);

        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        companyCode = sharedPreferences.getString("company_code", "").trim();

        // Set Click Listeners
        back.setOnClickListener(view -> startActivity(new Intent(update_admin.this, settings.class)));
        updttext.setOnClickListener(view -> startActivity(new Intent(update_admin.this, settings.class)));
        logout_iv.setOnClickListener(view -> startActivity(new Intent(update_admin.this, MainActivity.class)));
        logut.setOnClickListener(view -> startActivity(new Intent(update_admin.this, MainActivity.class)));

        if (companyCode.isEmpty()) {
            Toast.makeText(this, "Company code is missing!", Toast.LENGTH_SHORT).show();
        } else {
            companyCodeField.setText(companyCode);
            fetchAdminDetails();
        }

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
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equals("success")) {
                            Toast.makeText(update_admin.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            if (message.contains("Profile picture uploaded")) {
                                Toast.makeText(update_admin.this, "Profile picture uploaded!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(update_admin.this, "Update failed: " + message, Toast.LENGTH_SHORT).show();
                        }
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

                // Attach Base64 encoded image
                if (!base64Image.isEmpty()) {
                    params.put("profile_picture", base64Image);
                }

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
            base64Image = getBase64FromUri(imageUri);
        }
    }


    private String getBase64FromUri(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
