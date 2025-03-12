package com.example.attendance;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class attendance_report extends AppCompatActivity {

    private ImageView backButton, searchIv, downloadIv;
    private TextView reportTxt;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listGroupTitles;
    private HashMap<String, List<Employee>> listData;
    private HashMap<String, List<Attendance>> attendanceData;



    private RequestQueue requestQueue;
    private static final String BASE_URL = "http://192.168.168.239/ems_api/";
    private static final String GET_BRANCHES_URL = BASE_URL + "get_branches_employees.php";
    private static final String GET_ATTENDANCE_URL = BASE_URL + "get_attendance.php";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);

        requestQueue = Volley.newRequestQueue(this);

        backButton = findViewById(R.id.back);
        searchIv = findViewById(R.id.search);
        downloadIv = findViewById(R.id.download);
        reportTxt = findViewById(R.id.attendence_txt);
        expandableListView = findViewById(R.id.expandableListView);

        listGroupTitles = new ArrayList<>();
        listData = new HashMap<>();
        attendanceData = new HashMap<>();


        // Load data for employees and attendance
        loadBranchesAndEmployees();
        loadAttendanceData();

        adapter = new ExpandableListAdapter(this, listGroupTitles, listData);
        expandableListView.setAdapter(adapter);

        FloatingActionButton refreshFab = findViewById(R.id.refresh_fab);
        refreshFab.setOnClickListener(view -> refreshData());


        // Back Button
        backButton.setOnClickListener(view -> startActivity(new Intent(attendance_report.this, home.class)));
        reportTxt.setOnClickListener(view -> startActivity(new Intent(attendance_report.this, home.class)));
        // Search
        searchIv.setOnClickListener(view -> showSearchDialog());

        // Download
        downloadIv.setOnClickListener(view -> showDownloadOptionsDialog());
    }

    // ==============================
    // Load Employees Data
    // ==============================
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

                                Employee employee = new Employee(
                                        empObj.getString("employee_name"),
                                        empObj.getString("designation"),
                                        empObj.getString("employee_id"),
                                        false,  // Default value for isParkingAvailable
                                        false,  // Default value for isParkingAssigned
                                        empObj.getString("phone"),
                                        profilePic
                                );

                                employees.add(employee);
                            }

                            listData.put(branchName, employees);
                        }

                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(attendance_report.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(attendance_report.this, "Error fetching data", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
    }
    // ==============================
    // Load Attendance Data
    // ==============================
    private void loadAttendanceData() {
        String companyCode = getSharedPreferences("AdminPrefs", MODE_PRIVATE).getString("company_code", "");
        String url = "http://192.168.168.239/ems_api/get_attendance.php?company_code=" + companyCode;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        attendanceData.clear();

                        JSONArray branchesArray = response.getJSONArray("branches");

                        for (int i = 0; i < branchesArray.length(); i++) {
                            JSONObject branchObj = branchesArray.getJSONObject(i);
                            String branchName = branchObj.getString("branch_name");

                            JSONArray attendanceArray = branchObj.getJSONArray("attendance");
                            List<Attendance> attendanceList = new ArrayList<>();

                            for (int j = 0; j < attendanceArray.length(); j++) {
                                JSONObject attObj = attendanceArray.getJSONObject(j);

                                Attendance attendance = new Attendance(
                                        attObj.getString("employee_id"),
                                        attObj.getString("employee_name"),
                                        attObj.getString("in_time"),
                                        attObj.getString("out_time"),
                                        attObj.getString("attendance_status"),
                                        attObj.getString("geofenced_attendance"),
                                        attObj.getString("date")
                                );

                                attendanceList.add(attendance);
                            }

                            attendanceData.put(branchName, attendanceList);
                        }

                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(attendance_report.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(attendance_report.this, "Error fetching data", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
    }

    private void refreshData() {
        // Show a loading dialog for better UI
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Refreshing Data")
                .setMessage("Please wait while data is being refreshed...")
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Clear data to avoid duplication
        listGroupTitles.clear();
        listData.clear();
        attendanceData.clear();

        // Reload data from the database
        loadBranchesAndEmployees();
        loadAttendanceData();

        // Dismiss the dialog after data is loaded
        expandableListView.postDelayed(dialog::dismiss, 1500);

        // Show success message
        Toast.makeText(this, "Data refreshed successfully", Toast.LENGTH_SHORT).show();
    }


    // ==============================
    // Search Functionality
    // ==============================
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
            adapter = new ExpandableListAdapter(this, filteredBranches, filteredData);
            expandableListView.setAdapter(adapter);
            Toast.makeText(this, "Search results displayed", Toast.LENGTH_SHORT).show();
        }
    }
    // ==============================
    // Download Functionality
    // ==============================
    private void showDownloadOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Download Attendance Report");

        String[] options = {"Download All Records", "Download Specific Branch"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Toast.makeText(this, "Downloading All Records...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Downloading Specific Branch...", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
