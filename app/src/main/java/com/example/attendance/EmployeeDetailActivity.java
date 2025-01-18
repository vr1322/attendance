package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EmployeeDetailActivity extends AppCompatActivity {

    private TextView nameTextView, positionTextView, phoneTextView;
    private EditText nameEditText, positionEditText, phoneEditText;
    private Button editButton, saveButton;
    private Employee employee;
    private String branchName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_detail);

        // Initialize the views
        nameTextView = findViewById(R.id.nameTextView);
        positionTextView = findViewById(R.id.positionTextView);
        phoneTextView = findViewById(R.id.phoneTextView);

        nameEditText = findViewById(R.id.nameEditText);
        positionEditText = findViewById(R.id.positionEditText);
        phoneEditText = findViewById(R.id.phoneEditText);

        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);

        // Initially, hide the EditText views and show the TextView views (read-only)
        setFieldsEditable(false);

        // Get the employee details passed from emp_list
        employee = (Employee) getIntent().getSerializableExtra("employee");
        branchName = getIntent().getStringExtra("branch");

        if (employee != null) {
            // Set initial employee data in the views
            nameTextView.setText(employee.getName());
            positionTextView.setText(employee.getDesignation());
            phoneTextView.setText(employee.getPhoneNumber());

            // Set initial data in EditText fields (just in case for edit mode)
            nameEditText.setText(employee.getName());
            positionEditText.setText(employee.getDesignation());
            phoneEditText.setText(employee.getPhoneNumber());
        }

        // Set up the "Edit" button
        editButton.setOnClickListener(v -> {
            setFieldsEditable(true);
        });

        // Set up the "Save" button
        saveButton.setOnClickListener(v -> {
            // Save the updated details
            String updatedName = nameEditText.getText().toString();
            String updatedPosition = positionEditText.getText().toString();
            String updatedPhone = phoneEditText.getText().toString();

            // Validate the input
            if (updatedName.isEmpty() || updatedPosition.isEmpty() || updatedPhone.isEmpty()) {
                Toast.makeText(EmployeeDetailActivity.this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            } else {
                // Save the updated employee data
                employee.setName(updatedName);
                employee.setDesignation(updatedPosition);
                employee.setPhoneNumber(updatedPhone);

                // Prepare the result to return to the parent activity (emp_list)
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedEmployee", employee);
                resultIntent.putExtra("branch", branchName);

                setResult(RESULT_OK, resultIntent);
                finish(); // Close this activity and return to the emp_list activity

                // Notify the user
                Toast.makeText(EmployeeDetailActivity.this, "Employee details updated", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to toggle between editable and non-editable fields
    private void setFieldsEditable(boolean editable) {
        if (editable) {
            nameTextView.setVisibility(View.GONE);
            positionTextView.setVisibility(View.GONE);
            phoneTextView.setVisibility(View.GONE);

            nameEditText.setVisibility(View.VISIBLE);
            positionEditText.setVisibility(View.VISIBLE);
            phoneEditText.setVisibility(View.VISIBLE);

            editButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);
        } else {
            nameTextView.setVisibility(View.VISIBLE);
            positionTextView.setVisibility(View.VISIBLE);
            phoneTextView.setVisibility(View.VISIBLE);

            nameEditText.setVisibility(View.GONE);
            positionEditText.setVisibility(View.GONE);
            phoneEditText.setVisibility(View.GONE);

            editButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.GONE);
        }
    }
}
