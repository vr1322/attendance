package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditEmployeeActivity extends AppCompatActivity {

    private EditText nameEditText, designationEditText, phoneEditText;
    private Button saveButton;
    private ImageView photoImageView;
    private Employee employee;  // Declare employee object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_employee);

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        designationEditText = findViewById(R.id.designationEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        photoImageView = findViewById(R.id.photoImageView);
        saveButton = findViewById(R.id.saveButton);

        // Retrieve the employee object passed from MainActivity
        employee = (Employee) getIntent().getSerializableExtra("employee");

        // Pre-populate the EditText fields with the current employee data
        if (employee != null) {
            nameEditText.setText(employee.getName());
            designationEditText.setText(employee.getDesignation());
            phoneEditText.setText(employee.getPhoneNumber());
            // Set the employee photo if available (you can replace the placeholder image)
            // If you have a photo, set it here (e.g. photoImageView.setImageBitmap(employee.getPhotoUri()))
        }

        // Save Button Click
        saveButton.setOnClickListener(v -> {
            String updatedName = nameEditText.getText().toString().trim();
            String updatedDesignation = designationEditText.getText().toString().trim();
            String updatedPhone = phoneEditText.getText().toString().trim();

            if (updatedName.isEmpty() || updatedDesignation.isEmpty() || updatedPhone.isEmpty()) {
                Toast.makeText(EditEmployeeActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Update the employee object with the new values
                employee.setName(updatedName);
                employee.setDesignation(updatedDesignation);
                employee.setPhoneNumber(updatedPhone);

                // Create an Intent to send the updated employee data back to MainActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedEmployee", employee);  // Send updated employee object
                setResult(RESULT_OK, resultIntent);  // Set the result code and return the data
                finish();  // Close the current activity and return to MainActivity
            }
        });
    }
}
