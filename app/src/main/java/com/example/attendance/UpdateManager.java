package com.example.attendance;

import android.annotation.SuppressLint;
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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateManager extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int UCROP_REQUEST_CODE = 2;

    private CircleImageView profilePic;
    private EditText managerName, designation, managerEmail, mobileNo, address;
    private TextView companyName, companyRegNo, companyCode, logoutTv, updateText;
    private ImageView back, logout_iv;
    private Button updateBtn;

    private String companyCodeStr, managerIdStr, base64Image = "";
    private Uri imageUri;

    // API URLs
    private final String apiUrlFetch = "https://devonix.io/ems_api/fetch_manager_profile.php";
    private final String apiUrlUpdate = "https://devonix.io/ems_api/update_manager_profile.php";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_manager);

        // Bind Views
        profilePic = findViewById(R.id.profilePic);
        companyName = findViewById(R.id.companyName);
        companyRegNo = findViewById(R.id.companyRegNo);
        companyCode = findViewById(R.id.companyCode);

        managerName = findViewById(R.id.managerName);
        designation = findViewById(R.id.designation);
        managerEmail = findViewById(R.id.managerEmail);
        mobileNo = findViewById(R.id.mobileNo);
        address = findViewById(R.id.address);

        updateBtn = findViewById(R.id.updateBtn);
        updateText = findViewById(R.id.updttext);
        logoutTv = findViewById(R.id.logout);
        back = findViewById(R.id.back);
        logout_iv = findViewById(R.id.logout_iv);

        // ✅ Get session from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("ManagerSession", MODE_PRIVATE);
        companyCodeStr = sharedPreferences.getString("company_code", "").trim();
        managerIdStr = sharedPreferences.getString("manager_id", "").trim();

        Log.d("UpdateManager", "Company Code: " + companyCodeStr + " | Manager ID: " + managerIdStr);

        // Back & Logout
        back.setOnClickListener(view -> startActivity(new Intent(UpdateManager.this, ManagerHomeActivity.class)));
        updateText.setOnClickListener(view -> startActivity(new Intent(UpdateManager.this, ManagerHomeActivity.class)));
        logout_iv.setOnClickListener(view -> showLogoutDialog());
        logoutTv.setOnClickListener(view -> showLogoutDialog());

        // Load manager data
        if (companyCodeStr.isEmpty()) {
            Toast.makeText(this, "Company code missing!", Toast.LENGTH_SHORT).show();
        } else {
            companyCode.setText(companyCodeStr);
            fetchManagerDetails();
        }

        // Pick image
        profilePic.setOnClickListener(view -> openFileChooser());

        // Update button
        updateBtn.setOnClickListener(view -> updateManagerDetails());
    }

    private void fetchManagerDetails() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching details...");
        progressDialog.show();

        String url = apiUrlFetch + "?company_code=" + companyCodeStr + "&manager_id=" + managerIdStr;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    progressDialog.dismiss();
                    Log.d("FetchManagerResponse", response);

                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            JSONObject data = json.getJSONObject("data");

                            companyName.setText(data.getString("company_name"));
                            companyRegNo.setText(data.getString("company_reg_no"));
                            companyCode.setText(data.getString("company_code"));

                            managerName.setText(data.getString("manager_name"));
                            designation.setText(data.getString("manager_designation"));
                            managerEmail.setText(data.getString("manager_email"));
                            mobileNo.setText(data.getString("manager_phone"));
                            address.setText(data.getString("manager_address"));

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

    private void updateManagerDetails() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, apiUrlUpdate,
                response -> {
                    progressDialog.dismiss();
                    Log.d("UpdateManagerResponse", response);
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
                params.put("manager_id", managerIdStr);
                params.put("manager_name", managerName.getText().toString());
                params.put("designation", designation.getText().toString());
                params.put("manager_email", managerEmail.getText().toString());
                params.put("mobile_no", mobileNo.getText().toString());
                params.put("address", address.getText().toString());

                if (!base64Image.isEmpty()) {
                    params.put("profile_pic", base64Image);
                }
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
                    .withAspectRatio(1, 1) // Square crop
                    .withMaxResultSize(800, 800) // Resize
                    .start(this, UCROP_REQUEST_CODE);

        } else if (requestCode == UCROP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    profilePic.setImageBitmap(bitmap);
                    base64Image = getCompressedBase64(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == UCROP_REQUEST_CODE && resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Crop error: " + cropError, Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Compress and convert to Base64
    private String getCompressedBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // compress 70%
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Do you really want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SharedPreferences.Editor editor = getSharedPreferences("ManagerSession", MODE_PRIVATE).edit();
                    editor.clear();
                    editor.apply();
                    Intent intent = new Intent(UpdateManager.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
