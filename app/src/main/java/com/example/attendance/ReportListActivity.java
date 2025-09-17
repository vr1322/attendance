package com.example.attendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportListActivity extends AppCompatActivity {

    private RecyclerView reportRecyclerView;
    private ReportAdapter reportAdapter;
    private List<ReportModel> reportList = new ArrayList<>();
    private FloatingActionButton fabAddReport;
    private ImageView backButton;

    private static final String GET_REPORTS_URL = "https://devonix.io/ems_api/get_reports.php";
    private static final String TAG = "ReportListActivity";

    private String employeeId = "", companyCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);

        // 🔹 Fetch session from all possible user types
        if (!loadSession()) {
            // No session found → redirect to login
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize Views
        reportRecyclerView = findViewById(R.id.reportRecyclerView);
        fabAddReport = findViewById(R.id.fab_add_report);
        backButton = findViewById(R.id.back);

        // Setup RecyclerView
        reportRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportAdapter = new ReportAdapter(reportList);
        reportRecyclerView.setAdapter(reportAdapter);

        // Load reports
        loadReports();

        // FAB → Open Add Report Activity
        fabAddReport.setOnClickListener(v -> startActivity(new Intent(this, AddReportActivity.class)));

        // Back button
        backButton.setOnClickListener(v -> finish());
    }

    // Load session dynamically for employee/admin/manager/supervisor
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

    private void loadReports() {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("employee_id", employeeId);
            params.put("company_code", companyCode);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, GET_REPORTS_URL, new JSONObject(params),
                    response -> {
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONArray reportsArray = response.getJSONArray("reports");
                                reportList.clear();
                                for (int i = 0; i < reportsArray.length(); i++) {
                                    JSONObject reportObj = reportsArray.getJSONObject(i);
                                    ReportModel report = new ReportModel(
                                            reportObj.getInt("id"),
                                            reportObj.getString("title"),
                                            reportObj.getString("description"),
                                            reportObj.getString("date")
                                    );
                                    reportList.add(report);
                                }
                                reportAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Parse error: " + e.getMessage());
                        }
                    },
                    error -> {
                        Log.e(TAG, "Volley error: " + error.toString());
                        Toast.makeText(this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
                    });

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);

        } catch (Exception e) {
            Log.e(TAG, "Error preparing request: " + e.getMessage());
        }
    }
}
