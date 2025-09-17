package com.example.attendance;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddReportActivity extends AppCompatActivity {

    EditText etReportTitle, etReportDesc;
    Button btnSubmitReport, btnPickImages;
    RecyclerView rvSelectedImages;

    SelectedImageAdapter imageAdapter;
    List<Uri> selectedImages = new ArrayList<>();

    String employeeId = "";
    String companyCode = "";

    ActivityResultLauncher<String[]> pickImagesLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        etReportTitle = findViewById(R.id.etReportTitle);
        etReportDesc = findViewById(R.id.etReportDesc);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
        btnPickImages = findViewById(R.id.btnPickImages);
        rvSelectedImages = findViewById(R.id.rvSelectedImages);

        // ✅ Back button from header
        ImageView backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> finish()); // closes the activity

        // ✅ Load session dynamically (employee/admin/manager/supervisor)
        if (!loadSession()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvSelectedImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        imageAdapter = new SelectedImageAdapter(this, selectedImages, uri -> {
            selectedImages.remove(uri);
            imageAdapter.notifyDataSetChanged();
        });
        rvSelectedImages.setAdapter(imageAdapter);

        pickImagesLauncher = registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris != null) {
                        selectedImages.addAll(uris);
                        imageAdapter.notifyDataSetChanged();
                    }
                });

        btnPickImages.setOnClickListener(v -> pickImagesLauncher.launch(new String[]{"image/*"}));
        btnSubmitReport.setOnClickListener(v -> submitReport());
    }

    // Load session dynamically
    private boolean loadSession() {
        SharedPreferences empSession = getSharedPreferences("EmployeeSession", MODE_PRIVATE);
        SharedPreferences adminSession = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        SharedPreferences mgrSession = getSharedPreferences("ManagerSession", MODE_PRIVATE);
        SharedPreferences supSession = getSharedPreferences("SupervisorSession", MODE_PRIVATE);

        if (empSession.contains("employee_id")) {
            employeeId = empSession.getString("employee_id", "");
            companyCode = empSession.getString("company_code", "");
        } else if (adminSession.contains("employee_id")) {
            employeeId = adminSession.getString("employee_id", "");
            companyCode = adminSession.getString("company_code", "");
        } else if (mgrSession.contains("manager_id")) {
            employeeId = mgrSession.getString("manager_id", "");
            companyCode = mgrSession.getString("company_code", "");
        } else if (supSession.contains("supervisor_id")) {
            employeeId = supSession.getString("supervisor_id", "");
            companyCode = supSession.getString("company_code", "");
        }

        return !employeeId.isEmpty() && !companyCode.isEmpty();
    }

    private void submitReport() {
        String title = etReportTitle.getText().toString().trim();
        String desc = etReportDesc.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String url = "https://devonix.io/ems_api/add_report.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equalsIgnoreCase("success")) {
                            Toast.makeText(this, "Report submitted!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Server error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("employee_id", employeeId);
                params.put("company_code", companyCode);
                params.put("report_title", title);
                params.put("report_description", desc);
                params.put("image_count", String.valueOf(selectedImages.size()));

                for (int i = 0; i < selectedImages.size(); i++) {
                    Uri uri = selectedImages.get(i);
                    try {
                        InputStream is = getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        byte[] bytes = baos.toByteArray();
                        String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
                        params.put("images" + i, encoded);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
