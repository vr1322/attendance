package com.example.attendance;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class emp_list extends AppCompatActivity {

    private static final int REQUEST_ADD_EMPLOYEE = 1;
    private static final int REQUEST_EDIT_EMPLOYEE = 2;
    private ImageView backbutton;
    private TextView emplist_txt;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listGroupTitles;
    private HashMap<String, List<Employee>> listData;
    private RequestQueue requestQueue;
    private static final String BASE_URL = "http://192.168.168.239/ems_api/";
    private static final String GET_BRANCHES_URL = BASE_URL + "get_branches_employees.php";

    private FloatingActionButton addFab, addBranchFab, addEmployeeFab;
    private TextView addAlarmText, addPersonText;
    private boolean isOpen = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_list);

        requestQueue = Volley.newRequestQueue(this);

        backbutton = findViewById(R.id.back);
        emplist_txt = findViewById(R.id.emp_list_txt);
        expandableListView = findViewById(R.id.expandableListView);
        addFab = findViewById(R.id.add_fab);
        addBranchFab = findViewById(R.id.add_branch_fab);
        addEmployeeFab = findViewById(R.id.add_employee_fab);
        addAlarmText = findViewById(R.id.add_alarm_action_text);
        addPersonText = findViewById(R.id.add_person_action_text);


        backbutton.setOnClickListener(view -> startActivity(new Intent(emp_list.this, home.class)));
        emplist_txt.setOnClickListener(view -> startActivity(new Intent(emp_list.this, home.class)));

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

        adapter = new ExpandableListAdapter(this, listGroupTitles, listData);
        expandableListView.setAdapter(adapter);


        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Employee employee = listData.get(listGroupTitles.get(groupPosition)).get(childPosition);
            showEmployeeOptionsDialog(employee, listGroupTitles.get(groupPosition));
            return true;
        });
    }
    private void toggleFabVisibility(boolean open) {
        if (open) {
            addBranchFab.show();
            addEmployeeFab.show();
            addAlarmText.setVisibility(View.VISIBLE);
            addPersonText.setVisibility(View.VISIBLE);
        } else {
            addBranchFab.hide();
            addEmployeeFab.hide();
            addAlarmText.setVisibility(View.INVISIBLE);
            addPersonText.setVisibility(View.INVISIBLE);
        }
        isOpen = open;
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
                        Toast.makeText(emp_list.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(emp_list.this, "Error fetching data", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
    }

    private void showEmployeeOptionsDialog(Employee employee, String branch) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(employee.getName());
        builder.setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
            if (which == 0) {
                Intent intent = new Intent(emp_list.this, EditEmployeeActivity.class);
                intent.putExtra("employee", employee);
                startActivityForResult(intent, REQUEST_EDIT_EMPLOYEE);
            } else {
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
