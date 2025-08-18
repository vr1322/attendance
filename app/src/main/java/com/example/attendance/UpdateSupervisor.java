package com.example.attendance;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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

public class UpdateSupervisor extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int UCROP_REQUEST_CODE = 2;

    private CircleImageView profilePic;
    private EditText supervisorName, supervisorEmail, mobileNo, address;
    private TextView companyName, companyRegNo, companyCode, logoutTv, updateText, designation;
    private ImageView back, logout_iv;
    private Button updateBtn;

    private String companyCodeStr, supervisorIdStr, base64Image = "";
    private Uri imageUri;

    private final String apiUrlFetch = "https://devonix.io/ems_api/fetch_supervisor_profile.php";
    private final String apiUrlUpdate = "https://devonix.io/ems_api/update_supervisor_profile.php";

    private AlertDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_supervisor);

        // Bind Views
        profilePic = findViewById(R.id.profilePic_supervisor);
        companyName = findViewById(R.id.companyName_supervisor);
        companyRegNo = findViewById(R.id.companyRegNo_supervisor);
        companyCode = findViewById(R.id.companyCode_supervisor);

        supervisorName = findViewById(R.id.supervisorName);
        designation = findViewById(R.id.designation_supervisor);
        supervisorEmail = findViewById(R.id.supervisorEmail);
        mobileNo = findViewById(R.id.supervisorMobileNo);
        address = findViewById(R.id.supervisorAddress);

        updateBtn = findViewById(R.id.updateBtn_supervisor);
        updateText = findViewById(R.id.updttext_supervisor);
        logoutTv = findViewById(R.id.logout_supervisor);
        back = findViewById(R.id.back_supervisor);
        logout_iv = findViewById(R.id.logout_iv_supervisor);

        // Get session
        SharedPreferences sharedPreferences = getSharedPreferences("SupervisorSession", MODE_PRIVATE);
        companyCodeStr = sharedPreferences.getString("company_code", "").trim();
        supervisorIdStr = sharedPreferences.getString("supervisor_id", "").trim();

        Log.d("UpdateSupervisor", "Company Code: " + companyCodeStr + " | Supervisor ID: " + supervisorIdStr);

        // Back & Logout
        if (back != null) back.setOnClickListener(v -> startActivity(new Intent(this, SupervisorHomeActivity.class)));
        if (updateText != null) updateText.setOnClickListener(v -> startActivity(new Intent(this, SupervisorHomeActivity.class)));
        if (logout_iv != null) logout_iv.setOnClickListener(v -> showLogoutDialog());
        if (logoutTv != null) logoutTv.setOnClickListener(v -> showLogoutDialog());

        // Load supervisor data
        if (!companyCodeStr.isEmpty()) {
            companyCode.setText(companyCodeStr);
            fetchSupervisorDetails();
        } else {
            Toast.makeText(this, "Company code missing!", Toast.LENGTH_SHORT).show();
        }

        // Pick image
        profilePic.setOnClickListener(view -> openFileChooser());

        // Update button with validation
        updateBtn.setOnClickListener(view -> {
            if (validateInputs()) {
                updateSupervisorDetails();
            }
        });
    }

    // Input validation
    private boolean validateInputs() {
        String name = supervisorName.getText().toString().trim();
        String email = supervisorEmail.getText().toString().trim();
        String phone = mobileNo.getText().toString().trim();
        String addr = address.getText().toString().trim();
        String desig = designation.getText().toString().trim();

        if (name.isEmpty()) {
            supervisorName.setError("Name cannot be empty");
            supervisorName.requestFocus();
            return false;
        }

        if (desig.isEmpty()) {
            designation.setError("Designation cannot be empty");
            designation.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            supervisorEmail.setError("Email cannot be empty");
            supervisorEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            supervisorEmail.setError("Enter a valid email");
            supervisorEmail.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            mobileNo.setError("Mobile number cannot be empty");
            mobileNo.requestFocus();
            return false;
        }

        if (phone.length() != 10) {
            mobileNo.setError("Enter a valid 10-digit number");
            mobileNo.requestFocus();
            return false;
        }

        if (addr.isEmpty()) {
            address.setError("Address cannot be empty");
            address.requestFocus();
            return false;
        }

        return true;
    }

    // Open image picker
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == UCROP_REQUEST_CODE && resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Crop error: " + cropError, Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchSupervisorDetails() {
        showProgressDialog("Fetching details...");
        String url = apiUrlFetch + "?company_code=" + companyCodeStr + "&supervisor_id=" + supervisorIdStr;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    dismissProgressDialog();
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            JSONObject data = json.getJSONObject("data");

                            companyName.setText(data.getString("company_name"));
                            companyRegNo.setText(data.getString("company_reg_no"));
                            companyCode.setText(data.getString("company_code"));

                            supervisorName.setText(data.getString("supervisor_name"));
                            designation.setText(data.getString("supervisor_designation"));
                            supervisorEmail.setText(data.getString("supervisor_email"));
                            mobileNo.setText(data.getString("supervisor_phone"));
                            address.setText(data.getString("supervisor_address"));

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
                    dismissProgressDialog();
                    Toast.makeText(this, "Fetch failed!", Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void updateSupervisorDetails() {
        showProgressDialog("Updating profile...");
        StringRequest request = new StringRequest(Request.Method.POST, apiUrlUpdate,
                response -> {
                    dismissProgressDialog();
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
                    dismissProgressDialog();
                    Toast.makeText(this, "Error updating profile!", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("company_code", companyCodeStr);
                params.put("supervisor_id", supervisorIdStr);
                params.put("supervisor_name", supervisorName.getText().toString());
                params.put("designation", designation.getText().toString());
                params.put("supervisor_email", supervisorEmail.getText().toString());
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
                    SharedPreferences.Editor editor = getSharedPreferences("SupervisorSession", MODE_PRIVATE).edit();
                    editor.clear();
                    editor.apply();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            ProgressBar progressBar = new ProgressBar(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(progressBar);
            builder.setMessage(message);
            builder.setCancelable(false);
            progressDialog = builder.create();
        }
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
