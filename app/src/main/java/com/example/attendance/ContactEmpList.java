package com.example.attendance;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

public class ContactEmpList extends AppCompatActivity {

    private ImageView backButton, searchIv;
    private TextView emp_listTxt;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listGroupTitles;
    private HashMap<String, List<Employee>> listData;
    private HashMap<String, List<Attendance>> attendanceData;
    List<Attendance> attendanceList = new ArrayList<>(); // Add attendance data here if required
    private final Handler refreshHandler = new Handler();
    private Runnable refreshRunnable;



    private RequestQueue requestQueue;
    private static final String BASE_URL = "https://devonix.io/ems_api/";
    private static final String GET_BRANCHES_URL = BASE_URL + "get_branches_employees.php";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_emp_list);

        requestQueue = Volley.newRequestQueue(this);

        backButton = findViewById(R.id.back);
        searchIv = findViewById(R.id.search);
        emp_listTxt = findViewById(R.id.emp_list_txt);
        expandableListView = findViewById(R.id.expandableListView);

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Enable SwipeRefresh only when scrolled to the top
        expandableListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // No action needed here
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean enable = false;

                if (expandableListView != null && expandableListView.getChildCount() > 0) {
                    // Check if the first visible item is at the top and fully visible
                    boolean firstItemVisible = (firstVisibleItem == 0);
                    boolean topOfFirstItemVisible = expandableListView.getChildAt(0).getTop() == 0;
                    enable = firstItemVisible && topOfFirstItemVisible;
                }

                swipeRefreshLayout.setEnabled(enable);
            }
        });

        // Set the refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Show loading spinner
            swipeRefreshLayout.setRefreshing(true);

            // Clear existing data to prevent duplication
            listGroupTitles.clear();
            listData.clear();

            // Load updated data
            loadBranchesAndEmployees();

            // Delay to stop refresh animation (for better UX)
            expandableListView.postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(ContactEmpList.this, "Data refreshed successfully", Toast.LENGTH_SHORT).show();
            }, 1500);
        });


        listGroupTitles = new ArrayList<>();
        listData = new HashMap<>();
        attendanceData = new HashMap<>();


        // Load data for employees and attendance
        loadBranchesAndEmployees();


// Ensure 'listData' is initialized as a List<Attendance>

        adapter = new ExpandableListAdapter(this, listGroupTitles, listData, attendanceData, false);
        // Attendance Report
        expandableListView.setAdapter(adapter);
        setupEmployeeClickListeners();


        FloatingActionButton refreshFab = findViewById(R.id.refresh_fab);
        refreshFab.setOnClickListener(view -> refreshData());


        // Back Button
        backButton.setOnClickListener(v -> finish());
        emp_listTxt.setOnClickListener(v -> finish());
        // Search
        searchIv.setOnClickListener(view -> showSearchDialog());


    }

    private void loadBranchesAndEmployees() {
        String companyCode = getSharedPreferences("AdminPrefs", MODE_PRIVATE).getString("company_code", "");
        String url = GET_BRANCHES_URL + "?company_code=" + companyCode;

        // ✅ Clear data at the start to prevent duplication
        listGroupTitles.clear();
        listData.clear();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray branchesArray = response.getJSONArray("branches");

                        for (int i = 0; i < branchesArray.length(); i++) {
                            JSONObject branchObj = branchesArray.getJSONObject(i);
                            String branchName = branchObj.getString("branch_name");

                            // Ensure unique branch entries
                            if (!listGroupTitles.contains(branchName)) {
                                listGroupTitles.add(branchName);
                            }

                            JSONArray employeesArray = branchObj.getJSONArray("employees");
                            List<Employee> employees = new ArrayList<>();

                            for (int j = 0; j < employeesArray.length(); j++) {
                                JSONObject empObj = employeesArray.getJSONObject(j);

                                String employeeId = empObj.getString("employee_id");

                                // ✅ Prevent duplicate employees
                                boolean isDuplicate = false;
                                for (Employee emp : employees) {
                                    if (emp.getId().equals(employeeId)) {
                                        isDuplicate = true;
                                        break;
                                    }
                                }

                                if (!isDuplicate) {
                                    Employee employee = new Employee(
                                            empObj.getString("employee_name"),
                                            empObj.getString("designation"),
                                            empObj.getString("employee_id"),
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
                        Toast.makeText(ContactEmpList.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(ContactEmpList.this, "Error fetching data", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
    }





    private void refreshData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Refreshing Data").setMessage("Please wait...").setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        listGroupTitles.clear();
        listData.clear();
        attendanceData.clear();

        loadBranchesAndEmployees();

        expandableListView.postDelayed(dialog::dismiss, 1500);
    }

    private void selectSingleEmployee(Employee employee) {

        // Save Employee Name and Branch in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("employee_id", employee.getId());
        editor.putString("employee_name", employee.getName());
        editor.putString("branch", employee.getBranch());
        editor.apply(); // Save data

        // Start MarkAttendanceActivity
        Intent intent = new Intent(ContactEmpList.this, ComtactEmpInfo.class);
        intent.putExtra("employee_id", employee.getId());  // Pass employee ID for attendance details
        startActivity(intent);
    }


    private void setupEmployeeClickListeners() {
        // OnClick - select Single Employee
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Employee selectedEmployee = listData.get(listGroupTitles.get(groupPosition)).get(childPosition);
            selectSingleEmployee(selectedEmployee);  // ✅ OnClick for Single Employee
            return true;
        });

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
            adapter = new ExpandableListAdapter(this, filteredBranches, filteredData, attendanceData, true);
            expandableListView.setAdapter(adapter);
            Toast.makeText(this, "Search results displayed", Toast.LENGTH_SHORT).show();
        }
    }

}
