package com.example.attendance;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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


public class emp_list extends AppCompatActivity {

    private static final int REQUEST_ADD_EMPLOYEE = 1;
    private static final int REQUEST_EDIT_EMPLOYEE = 2;
    private ImageView backbutton , searchiv, downloadiv;
    private TextView emplist_txt;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listGroupTitles;
    private HashMap<String, List<Employee>> listData;
    private RequestQueue requestQueue;
    private static final String BASE_URL = "http://192.168.144.102/ems_api/";
    private static final String GET_BRANCHES_URL = BASE_URL + "get_branches_employees.php";
    private static final int STORAGE_PERMISSION_CODE = 100;

    private FloatingActionButton addFab, addBranchFab, addEmployeeFab;
    private TextView addAlarmText, addPersonText;
    private boolean isOpen = false;
    private Animation fabOpen, fabClose, rotateForward, rotateBackward;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_list);

        requestQueue = Volley.newRequestQueue(this);

        searchiv = findViewById(R.id.search);
        downloadiv = findViewById(R.id.download);
        backbutton = findViewById(R.id.back);
        emplist_txt = findViewById(R.id.emp_list_txt);
        expandableListView = findViewById(R.id.expandableListView);
        addFab = findViewById(R.id.add_fab);
        addBranchFab = findViewById(R.id.add_branch_fab);
        addEmployeeFab = findViewById(R.id.add_employee_fab);
        addAlarmText = findViewById(R.id.add_alarm_action_text);
        addPersonText = findViewById(R.id.add_person_action_text);
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Show loading spinner
            swipeRefreshLayout.setRefreshing(true);

            // Clear existing data to prevent duplication
            listGroupTitles.clear();
            listData.clear();

            // Load updated data
            loadBranchesAndEmployees();

            // Delay for better UX before stopping refresh animation
            expandableListView.postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(emp_list.this, "Data refreshed successfully", Toast.LENGTH_SHORT).show();
            }, 1500);
        });



        backbutton.setOnClickListener(view -> startActivity(new Intent(emp_list.this, home.class)));
        emplist_txt.setOnClickListener(view -> startActivity(new Intent(emp_list.this, home.class)));

        searchiv.setOnClickListener(view -> showSearchDialog());
        downloadiv.setOnClickListener(view -> showDownloadOptionsDialog());
        requestStoragePermission();


        addFab.setOnClickListener(view -> toggleFabVisibility(!isOpen));

        addBranchFab.setOnClickListener(view -> {
            Intent intent = new Intent(emp_list.this, AddBranchActivity.class);
            startActivity(intent);
        });

        addEmployeeFab.setOnClickListener(view -> {
            Intent intent = new Intent(emp_list.this, add_emp.class);
            startActivity(intent);
        });


        listGroupTitles = new ArrayList<>();
        listData = new HashMap<>();

        loadBranchesAndEmployees();

        adapter = new ExpandableListAdapter(this, listGroupTitles, listData, false); // Employee List
        expandableListView.setAdapter(adapter);


        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Employee employee = listData.get(listGroupTitles.get(groupPosition)).get(childPosition);
            showEmployeeOptionsDialog(employee, listGroupTitles.get(groupPosition));
            return true;
        });
    }


    private void toggleFabVisibility(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;

        addBranchFab.setVisibility(visibility);
        addEmployeeFab.setVisibility(visibility);
        addAlarmText.setVisibility(visibility);
        addPersonText.setVisibility(visibility);

        if (show) {
            addFab.startAnimation(rotateForward);
            addBranchFab.startAnimation(fabOpen);
            addEmployeeFab.startAnimation(fabOpen);
            addFab.setImageResource(R.drawable.ic_close);// Change to close icon
        } else {
            addFab.startAnimation(rotateBackward);
            addBranchFab.startAnimation(fabClose);
            addEmployeeFab.startAnimation(fabClose);
            addFab.setImageResource(R.drawable.ic_add);  // Change back to add icon
        }

        // Toggle the state
        isOpen = show;
    }

    private void loadBranchesAndEmployees() {
        String companyCode = getSharedPreferences("AdminPrefs", MODE_PRIVATE).getString("company_code", "");
        String url = GET_BRANCHES_URL + "?company_code=" + companyCode;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        listGroupTitles.clear();
                        listData.clear();

                        JSONArray branchesArray = response.getJSONArray("branches");

                        for (int i = 0; i < branchesArray.length(); i++) {
                            JSONObject branchObj = branchesArray.getJSONObject(i);
                            String branchName = branchObj.getString("branch_name");
                            listGroupTitles.add(branchName);

                            JSONArray employeesArray = branchObj.getJSONArray("employees");
                            List<Employee> employees = new ArrayList<>();

                            for (int j = 0; j < employeesArray.length(); j++) {
                                JSONObject empObj = employeesArray.getJSONObject(j);

                                // Check if profile_pic exists and is not null
                                String profilePic = "http://192.168.168.239/ems_api/" + empObj.getString("profile_pic");
                                String attendanceStatus = "http://192.168.168.239/ems_api/" +empObj.optString("attendance_status", "Not Marked");  // ✅ Correct Usage


                                Employee employee = new Employee(
                                        empObj.getString("employee_name"),
                                        empObj.getString("designation"),
                                        empObj.getString("employee_id"),
                                        false,  // Default value for isParkingAvailable
                                        false,  // Default value for isParkingAssigned
                                        empObj.getString("phone"),
                                        profilePic,
                                        attendanceStatus,   // ✅ Correct Usage
                                        branchName

                                );

                                employees.add(employee);
                            }

                            listData.put(branchName, employees);
                        }

                        adapter.notifyDataSetChanged();
                        expandableListView.setNestedScrollingEnabled(true);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(emp_list.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(emp_list.this, "Error fetching data", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
    }

    private void requestStoragePermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. File download may fail.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Employee");

        // Input field for search text
        final EditText input = new EditText(this);
        input.setHint("Enter Branch/Name/ID");
        builder.setView(input);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String searchText = input.getText().toString().trim();
            if (!searchText.isEmpty()) {
                searchEmployee(searchText);
            } else {
                Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    // Search logic
    private void searchEmployee(String searchText) {
        List<String> filteredBranches = new ArrayList<>();
        HashMap<String, List<Employee>> filteredData = new HashMap<>();

        for (String branch : listGroupTitles) {
            List<Employee> filteredEmployees = new ArrayList<>();
            for (Employee employee : listData.get(branch)) {
                if (branch.toLowerCase().contains(searchText.toLowerCase()) ||
                        employee.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                        employee.getId().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredEmployees.add(employee);
                }
            }
            if (!filteredEmployees.isEmpty()) {
                filteredBranches.add(branch);
                filteredData.put(branch, filteredEmployees);
            }
        }

        if (filteredBranches.isEmpty()) {
            Toast.makeText(this, "No matching employees found", Toast.LENGTH_SHORT).show();
        } else {
            adapter = new ExpandableListAdapter(this, filteredBranches, filteredData,false);
            expandableListView.setAdapter(adapter);
            Toast.makeText(this, "Search results displayed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDownloadOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Download Employee List");

        String[] options = {"Download All Employees", "Download Specific Branch"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                downloadEmployeeList("all");
            } else {
                showBranchSelectionDialog();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showBranchSelectionDialog() {
        AlertDialog.Builder branchDialog = new AlertDialog.Builder(this);
        branchDialog.setTitle("Select Branch");

        String[] branchArray = listGroupTitles.toArray(new String[0]);
        branchDialog.setItems(branchArray, (dialog, which) -> {
            String selectedBranch = branchArray[which];
            downloadEmployeeList(selectedBranch);
        });

        branchDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        branchDialog.show();
    }

    private void downloadEmployeeList(String branch) {
        AlertDialog.Builder formatDialog = new AlertDialog.Builder(this);
        formatDialog.setTitle("Select File Format");

        String[] formats = {"PDF", "Word", "XLS"};
        formatDialog.setItems(formats, (dialog, which) -> {
            String format = formats[which].toLowerCase(); // Lowercase for consistency

            try {
                String downloadUrl = "http://192.168.168.239/ems_api/download_employee_list.php?"
                        + "branch=" + URLEncoder.encode(branch, "UTF-8")
                        + "&format=" + format;

                // Start download with updated logic
                startDownload(downloadUrl, format);

            } catch (UnsupportedEncodingException e) {
                Toast.makeText(this, "Encoding error occurred", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        formatDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        formatDialog.show();
    }

    private void startDownload(String downloadUrl, String format) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadUsingMediaStore(downloadUrl, format);
        } else {
            downloadUsingDownloadManager(downloadUrl, format);
        }
    }

    // For Android 10 and below
    private void downloadUsingDownloadManager(String downloadUrl, String format) {
        Toast.makeText(this, "Downloading " + format + "...", Toast.LENGTH_SHORT).show();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setTitle("Employee List");
        request.setDescription("Downloading " + format + " file...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Correct extensions
        String fileExtension = format.equals("word") ? ".docx" :
                format.equals("xls") ? ".xlsx" : ".pdf";

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                "Employee_List" + fileExtension);

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }

    // For Android 11 and above (MediaStore API)
    private void downloadUsingMediaStore(String downloadUrl, String format) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                InputStream inputStream = new URL(downloadUrl).openStream();

                // Correct File Extension Handling
                String fileExtension = format.equals("word") ? ".docx" :
                        format.equals("xls") ? ".xlsx" : ".pdf";

                // Correct MIME Types
                String mimeType = format.equals("word") ?
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" :
                        format.equals("xls") ?
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" :
                                "application/pdf"; // Corrected for XLSX

                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, "Employee_List" + fileExtension);
                values.put(MediaStore.Downloads.MIME_TYPE, mimeType);
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri fileUri = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    fileUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                }

                if (fileUri != null) {
                    OutputStream outputStream = getContentResolver().openOutputStream(fileUri);

                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, length);
                    }

                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();

                    runOnUiThread(() ->
                            Toast.makeText(this, format.toUpperCase() + " downloaded successfully!", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Failed to download file.", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Download error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }



    private void showEmployeeOptionsDialog(Employee employee, String branch) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(employee.getName());
        builder.setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
            if (which == 0) { // Edit option
                Intent intent = new Intent(emp_list.this, EditEmployeeActivity.class);
                intent.putExtra("employee_id", employee.getId()); // Pass employee ID
                intent.putExtra("company_code", getSharedPreferences("AdminPrefs", MODE_PRIVATE).getString("company_code", "")); // Pass company code
                startActivity(intent);
            } else { // Delete option
                deleteEmployee(employee.getId(), employee.getName(), branch);
            }
        });
        builder.show();
    }


    private void deleteEmployee(String employeeId, String employeeName, String branch) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Employee")
                .setMessage("Do you really want to delete " + employeeName + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String url = "http://192.168.168.239/ems_api/delete_employee.php?employee_id=" + employeeId;

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            response -> {
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    String status = jsonResponse.getString("status");
                                    String message = jsonResponse.getString("message");

                                    if (status.equals("success")) {
                                        listData.get(branch).removeIf(emp -> emp.getId().equals(employeeId));
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                                }
                            },
                            error -> Toast.makeText(this, "Error deleting employee", Toast.LENGTH_SHORT).show());

                    requestQueue.add(stringRequest);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
