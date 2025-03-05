package com.example.attendance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class emp_list extends AppCompatActivity {

    private static final int REQUEST_ADD_EMPLOYEE = 1; // Request code for EmployeeDetailsActivity
    private static final int REQUEST_EDIT_EMPLOYEE = 2; // Request code for EditEmployeeActivity

    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listGroupTitles;
    private HashMap<String, List<Employee>> listData;

    // Declare the FABs
    private boolean isOpen = false;
    private FloatingActionButton addFab, addBranchFab, addEmployeeFab;
    private TextView addAlarmText, addPersonText;
    private Animation rotateForward, rotateBackward, fabOpen, fabClose;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_list);

        // Initialize views
        expandableListView = findViewById(R.id.expandableListView);

        // Initialize the FABs and TextViews
        addFab = findViewById(R.id.add_fab);
        addBranchFab = findViewById(R.id.add_branch_fab);
        addEmployeeFab = findViewById(R.id.add_employee_fab);
        addAlarmText = findViewById(R.id.add_alarm_action_text);
        addPersonText = findViewById(R.id.add_person_action_text);

        // Load animations
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);

        // Initially hide child FABs
        addBranchFab.setVisibility(View.GONE);
        addEmployeeFab.setVisibility(View.GONE);
        addAlarmText.setVisibility(View.GONE);
        addPersonText.setVisibility(View.GONE);
        addFab.setOnClickListener(view -> toggleFabVisibility(!isOpen));
        // Set a click listener for the parent FAB
        addFab.setOnClickListener(view -> toggleFabVisibility(addBranchFab.getVisibility() == View.GONE));

        // Set click listener for the "Add Branch" FAB
        addBranchFab.setOnClickListener(view -> showAddBranchDialog());

        // Set click listener for the "Add Employee" FAB
        addEmployeeFab.setOnClickListener(view -> {
            if (listGroupTitles.isEmpty()) {
                Toast.makeText(emp_list.this, "No branch available. Please add a branch first.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(emp_list.this, add_emp.class);
            intent.putStringArrayListExtra("branchList", new ArrayList<>(listGroupTitles)); // Pass branch list to EmployeeDetailsActivity
            startActivityForResult(intent, REQUEST_ADD_EMPLOYEE);
        });

        // Prepare initial data
        initializeData();

        adapter = new ExpandableListAdapter(this, listGroupTitles, listData);
        expandableListView.setAdapter(adapter);

        // Set listener to view employee details when an item is clicked
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Employee employee = listData.get(listGroupTitles.get(groupPosition)).get(childPosition);

            // Redirect to EditEmployeeActivity instead of EmployeeDetailActivity
            Intent intent = new Intent(emp_list.this, EditEmployeeActivity.class);
            intent.putExtra("employee", employee);  // Pass the employee object to EditEmployeeActivity
            startActivityForResult(intent, REQUEST_EDIT_EMPLOYEE);  // Start EditEmployeeActivity for editing
            return true;
        });
    }

    // Toggles visibility of child FABs and their texts
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
            addFab.setImageResource(R.drawable.ic_close);  // Change to close icon
        } else {
            addFab.startAnimation(rotateBackward);
            addBranchFab.startAnimation(fabClose);
            addEmployeeFab.startAnimation(fabClose);
            addFab.setImageResource(R.drawable.ic_add);  // Change back to add icon
        }

        // Toggle the state
        isOpen = show;
    }



    // Handle the result from EmployeeDetailsActivity and EditEmployeeActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_EMPLOYEE && resultCode == RESULT_OK && data != null) {
            Employee newEmployee = (Employee) data.getSerializableExtra("newEmployee");
            String branch = data.getStringExtra("branch");

            if (branch != null && listData.containsKey(branch)) {
                // Add the new employee to the corresponding branch
                listData.get(branch).add(newEmployee);
                adapter.notifyDataSetChanged(); // Refresh the ExpandableListView
                Toast.makeText(this, "Employee " + newEmployee.getName() + " added to " + branch, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add employee. Invalid branch.", Toast.LENGTH_SHORT).show();
            }
        }

        // Handle result from EditEmployeeActivity (Edit Employee)
        if (requestCode == REQUEST_EDIT_EMPLOYEE && resultCode == RESULT_OK && data != null) {
            Employee updatedEmployee = (Employee) data.getSerializableExtra("updatedEmployee");
            String branch = data.getStringExtra("branch");

            if (branch != null && listData.containsKey(branch)) {
                // Find and update the employee in the corresponding branch
                List<Employee> employees = listData.get(branch);
                int index = -1;
                for (int i = 0; i < employees.size(); i++) {
                    if (employees.get(i).getId().equals(updatedEmployee.getId())) {
                        index = i;
                        break;
                    }
                }

                if (index != -1) {
                    employees.set(index, updatedEmployee);  // Update the employee
                    adapter.notifyDataSetChanged();  // Refresh the ExpandableListView
                    Toast.makeText(this, "Employee " + updatedEmployee.getName() + " updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Employee not found in branch", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showAddBranchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Branch");

        // Create an EditText for user input
        final EditText input = new EditText(this);
        input.setHint("Enter branch name");
        builder.setView(input);

        // Set up "Add" button
        builder.setPositiveButton("Add", (dialog, which) -> {
            String branchName = input.getText().toString().trim();

            if (branchName.isEmpty()) {
                Toast.makeText(emp_list.this, "Branch name cannot be empty", Toast.LENGTH_SHORT).show();
            } else if (listGroupTitles.contains(branchName)) {
                Toast.makeText(emp_list.this, "Branch already exists", Toast.LENGTH_SHORT).show();
            } else {
                listGroupTitles.add(branchName);
                listData.put(branchName, new ArrayList<>()); // Initialize empty list for the new branch
                adapter.notifyDataSetChanged(); // Refresh ExpandableListView
                Toast.makeText(emp_list.this, branchName + " added!", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up "Cancel" button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.show();
    }

    private void initializeData() {
        listGroupTitles = new ArrayList<>();
        listGroupTitles.add("Lake Side");
        listGroupTitles.add("BKC");
        listGroupTitles.add("Main");

        listData = new HashMap<>();
        listData.put("Lake Side", getLakeSideEmployees());
        listData.put("BKC", getBKCEmployees());
        listData.put("Main", getMainEmployees());
    }

    private List<Employee> getLakeSideEmployees() {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("John Smith", "Manager", "112", true, true, "555-1234", null));
        employees.add(new Employee("James Wayn", "Employee", "113", true, false, "555-1235", null));
        employees.add(new Employee("Lima Miller", "Employee", "114", true, true, "555-1236", null));
        return employees;
    }

    private List<Employee> getBKCEmployees() {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("David Brown", "Employee", "201", false, true, "555-2011", null));
        employees.add(new Employee("Maria Green", "Employee", "202", true, false, "555-2012", null));
        return employees;
    }

    private List<Employee> getMainEmployees() {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("Chris Blue", "Manager", "301", false, true, "555-3011", null));
        employees.add(new Employee("Emma Stone", "Employee", "302", true, false, "555-3012", null));
        return employees;
    }
}
