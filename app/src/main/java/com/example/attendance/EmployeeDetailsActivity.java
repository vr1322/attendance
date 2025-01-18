package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class EmployeeDetailsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_PHOTO = 1;

    private EditText nameEditText, idEditText, designationEditText, phoneEditText;
    private Spinner branchSpinner;
    private Button saveButton, selectPhotoButton;
    private ImageView photoImageView;

    private ArrayList<String> branchList;
    private String selectedPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_details);

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        idEditText = findViewById(R.id.idEditText);
        designationEditText = findViewById(R.id.designationEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        branchSpinner = findViewById(R.id.branchSpinner);
        saveButton = findViewById(R.id.saveButton);
        selectPhotoButton = findViewById(R.id.selectPhotoButton);
        photoImageView = findViewById(R.id.photoImageView);

        // Get branch list from Intent
        branchList = getIntent().getStringArrayListExtra("branchList");

        // Set up branch spinner
        if (branchList != null && !branchList.isEmpty()) {
            ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, branchList);
            branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            branchSpinner.setAdapter(branchAdapter);
        } else {
            Toast.makeText(this, "No branches available", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(false); // Disable save if no branches exist
        }

        // Select photo logic
        selectPhotoButton.setOnClickListener(v -> openGallery());

        // Save button logic
        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String id = idEditText.getText().toString().trim();
            String designation = designationEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String selectedBranch = branchSpinner.getSelectedItem() != null ? branchSpinner.getSelectedItem().toString() : null;

            if (name.isEmpty() || id.isEmpty() || designation.isEmpty() || phone.isEmpty() || selectedBranch == null) {
                Toast.makeText(this, "Please fill all fields and select a branch", Toast.LENGTH_SHORT).show();
            } else {
                Employee newEmployee = new Employee(name, designation, id, true, false, phone, selectedPhotoUri);

                // Pass the employee and branch back to MainActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("newEmployee", newEmployee);
                resultIntent.putExtra("branch", selectedBranch);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    // Open the gallery to pick a photo
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
    }

    // Handle photo selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_PHOTO && resultCode == RESULT_OK && data != null) {
            selectedPhotoUri = data.getData().toString();
            photoImageView.setImageURI(data.getData());
        } else {
            Toast.makeText(this, "No photo selected", Toast.LENGTH_SHORT).show();
        }
    }
}
