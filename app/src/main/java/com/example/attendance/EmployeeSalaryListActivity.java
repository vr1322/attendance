package com.example.attendance;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

import android.Manifest;
import android.content.pm.PackageManager;

public class EmployeeSalaryListActivity extends AppCompatActivity {

    private ImageView backButton, searchIv, downloadIv;
    private TextView empListTxt;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listGroupTitles;
    private HashMap<String, List<Employee>> listData;
    private RequestQueue requestQueue;
    private static final String BASE_URL = "https://devonix.io/ems_api/";
    private static final String GET_BRANCHES_URL = BASE_URL + "get_branches_employees.php";
    private static final int STORAGE_PERMISSION_CODE = 100;

    private boolean isOpen = false;
    private Animation fabOpen, fabClose, rotateForward, rotateBackward;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_list);

        requestQueue = Volley.newRequestQueue(this);

        backButton = findViewById(R.id.back);
        searchIv = findViewById(R.id.search);
        downloadIv = findViewById(R.id.download);
        empListTxt = findViewById(R.id.emp_list_txt);
        expandableListView = findViewById(R.id.expandableListView);

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        listGroupTitles = new ArrayList<>();
        listData = new HashMap<>();

        adapter = new ExpandableListAdapter(this, listGroupTitles, listData, null, false);
        expandableListView.setAdapter(adapter);

        loadBranchesAndEmployees();

        // Swipe-to-refresh only at top
        expandableListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (expandableListView.getChildCount() > 0) {
                    boolean firstItemVisible = (firstVisibleItem == 0);
                    boolean topOfFirstItemVisible = expandableListView.getChildAt(0).getTop() == 0;
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipeRefreshLayout.setEnabled(enable);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            listGroupTitles.clear();
            listData.clear();
            loadBranchesAndEmployees();
            swipeRefreshLayout.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 1500);
        });

        backButton.setOnClickListener(view -> finish());
        empListTxt.setOnClickListener(view -> finish());

        searchIv.setOnClickListener(view -> showSearchDialog());
        downloadIv.setOnClickListener(view -> showDownloadOptionsDialog());

        requestStoragePermission();

        // Employee click: open salary calculation
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Employee employee = listData.get(listGroupTitles.get(groupPosition)).get(childPosition);

            // Get company code from intent (must be passed from previous activity)
            String companyCode = getIntent().getStringExtra("company_code");
            if (companyCode == null) {
                Toast.makeText(this, "Company code missing!", Toast.LENGTH_SHORT).show();
                return true;
            }

            Intent intent = new Intent(EmployeeSalaryListActivity.this, salary_calculation.class);
            intent.putExtra("employee_id", employee.getId());
            intent.putExtra("company_code", companyCode); // Pass company code
            intent.putExtra("employee_name", employee.getName());
            intent.putExtra("designation", employee.getDesignation());
            startActivity(intent);
            return true;
        });
    }

    private void loadBranchesAndEmployees() {
        String companyCode = getIntent().getStringExtra("company_code");
        if (companyCode == null) {
            Toast.makeText(this, "Company code missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String email = getIntent().getStringExtra("email");
        String role = getIntent().getStringExtra("role");

        String url;
        if ("admin".equalsIgnoreCase(role)) {
            url = GET_BRANCHES_URL + "?company_code=" + companyCode + "&admin_email=" + email;
        } else if ("manager".equalsIgnoreCase(role)) {
            url = GET_BRANCHES_URL + "?company_code=" + companyCode + "&manager_email=" + email;
        } else if ("supervisor".equalsIgnoreCase(role)) {
            url = GET_BRANCHES_URL + "?company_code=" + companyCode + "&supervisor_email=" + email;
        } else if ("employee".equalsIgnoreCase(role)) {
            url = GET_BRANCHES_URL + "?company_code=" + companyCode + "&employee_email=" + email;
        } else {
            url = GET_BRANCHES_URL + "?company_code=" + companyCode;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray branchesArray = response.getJSONArray("branches");

                        for (int i = 0; i < branchesArray.length(); i++) {
                            JSONObject branchObj = branchesArray.getJSONObject(i);
                            String branchName = branchObj.getString("branch_name");

                            if (!listGroupTitles.contains(branchName)) {
                                listGroupTitles.add(branchName);
                            }

                            List<Employee> employees = listData.getOrDefault(branchName, new ArrayList<>());
                            JSONArray employeesArray = branchObj.getJSONArray("employees");

                            for (int j = 0; j < employeesArray.length(); j++) {
                                JSONObject empObj = employeesArray.getJSONObject(j);
                                String empId = empObj.getString("employee_id");

                                boolean exists = employees.stream().anyMatch(emp -> emp.getId().equals(empId));
                                if (!exists) {
                                    Employee employee = new Employee(
                                            empObj.getString("employee_name"),
                                            empObj.getString("designation"),
                                            empId,
                                            false,
                                            false,
                                            empObj.getString("phone"),
                                            BASE_URL + empObj.getString("profile_pic"),
                                            null,
                                            branchName
                                    );
                                    employees.add(employee);
                                }
                            }

                            listData.put(branchName, employees);
                        }

                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(EmployeeSalaryListActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(EmployeeSalaryListActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // important!

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permission Granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Employee");
        final EditText input = new EditText(this);
        input.setHint("Enter Branch/Name/ID");
        builder.setView(input);
        builder.setPositiveButton("Search", (dialog, which) -> {
            String searchText = input.getText().toString().trim();
            if (!searchText.isEmpty()) searchEmployee(searchText);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void searchEmployee(String searchText) {
        List<String> filteredBranches = new ArrayList<>();
        HashMap<String, List<Employee>> filteredData = new HashMap<>();

        for (String branch : listGroupTitles) {
            List<Employee> filteredEmployees = new ArrayList<>();
            for (Employee emp : listData.get(branch)) {
                if (branch.toLowerCase().contains(searchText.toLowerCase())
                        || emp.getName().toLowerCase().contains(searchText.toLowerCase())
                        || emp.getId().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredEmployees.add(emp);
                }
            }
            if (!filteredEmployees.isEmpty()) {
                filteredBranches.add(branch);
                filteredData.put(branch, filteredEmployees);
            }
        }

        if (!filteredBranches.isEmpty()) {
            adapter = new ExpandableListAdapter(this, filteredBranches, filteredData, null, false);
            expandableListView.setAdapter(adapter);
            Toast.makeText(this, "Search results displayed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No matching employees found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDownloadOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Download Employee List");
        String[] options = {"Download All Employees", "Download Specific Branch"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) downloadEmployeeList("all");
            else showBranchSelectionDialog();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showBranchSelectionDialog() {
        AlertDialog.Builder branchDialog = new AlertDialog.Builder(this);
        branchDialog.setTitle("Select Branch");
        String[] branchArray = listGroupTitles.toArray(new String[0]);
        branchDialog.setItems(branchArray, (dialog, which) -> downloadEmployeeList(branchArray[which]));
        branchDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        branchDialog.show();
    }

    private void downloadEmployeeList(String branch) {
        AlertDialog.Builder formatDialog = new AlertDialog.Builder(this);
        formatDialog.setTitle("Select File Format");
        String[] formats = {"PDF", "Word", "XLS"};
        formatDialog.setItems(formats, (dialog, which) -> {
            String format = formats[which].toLowerCase();
            try {
                String downloadUrl = "https://devonix.io/ems_api/download_employee_list.php?branch="
                        + URLEncoder.encode(branch, "UTF-8") + "&format=" + format;
                startDownload(downloadUrl, format);
            } catch (UnsupportedEncodingException e) { e.printStackTrace(); }
        });
        formatDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        formatDialog.show();
    }

    private void startDownload(String downloadUrl, String format) {
        Toast.makeText(this, "Downloading " + format + "...", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Executors.newSingleThreadExecutor().execute(() -> {
                try (InputStream inputStream = new URL(downloadUrl).openStream()) {
                    String ext = format.equals("word") ? ".docx" : format.equals("xls") ? ".xlsx" : ".pdf";
                    String mime = format.equals("word") ? "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            : format.equals("xls") ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" : "application/pdf";

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, "Employee_List" + ext);
                    values.put(MediaStore.Downloads.MIME_TYPE, mime);
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                    Uri fileUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (fileUri != null) {
                        try (OutputStream outputStream = getContentResolver().openOutputStream(fileUri)) {
                            byte[] buffer = new byte[4096];
                            int length;
                            while ((length = inputStream.read(buffer)) != -1) outputStream.write(buffer, 0, length);
                        }
                        runOnUiThread(() -> Toast.makeText(this, format.toUpperCase() + " downloaded successfully!", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) { e.printStackTrace(); }
            });
        } else {
            Toast.makeText(this, "Downloading on legacy devices not implemented", Toast.LENGTH_SHORT).show();
        }
    }
}
