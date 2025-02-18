package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

public class AddBranchActivity extends AppCompatActivity {

    private EditText branchAddress, radiusMeters;
    private ImageView searchLocation;
    private ImageButton submitBranch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_branch); // Ensure XML is linked correctly

        // Initialize UI components
        branchAddress = findViewById(R.id.branchAddress);
        radiusMeters = findViewById(R.id.radiusMeters);
        searchLocation = findViewById(R.id.searchLocation);
        submitBranch = findViewById(R.id.submitBranch);

        // Check if views are null
        if (branchAddress == null || radiusMeters == null || searchLocation == null || submitBranch == null) {
            Log.e("AddBranchActivity", "Error: One or more views are null. Check XML IDs.");
            return;
        }

        // ✅ Fix: Apply insets listener properly
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.post(() -> ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> insets));
        } else {
            Log.e("AddBranchActivity", "Root view is null! Skipping window insets setup.");
        }

        // Open Maps Activity using Activity Result API
        ActivityResultLauncher<Intent> mapActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            String selectedAddress = data.getStringExtra("selected_address");
                            int selectedRadius = data.getIntExtra("selected_radius", 50);

                            if (selectedAddress != null) {
                                branchAddress.setText(selectedAddress);
                            } else {
                                Log.e("AddBranchActivity", "Error: Address data is null");
                            }
                            radiusMeters.setText(String.valueOf(selectedRadius));
                        } else {
                            Log.e("AddBranchActivity", "MapsActivity result was not OK or data was null");
                        }
                    }
                }
        );

        // Search Location Click Listener
        searchLocation.setOnClickListener(v -> {
            Intent intent = new Intent(AddBranchActivity.this, MapsActivity.class);
            mapActivityLauncher.launch(intent);
        });

        // Handle Form Submission
        submitBranch.setOnClickListener(v -> {
            String address = branchAddress.getText().toString().trim();
            String radius = radiusMeters.getText().toString().trim();

            if (address.isEmpty()) {
                branchAddress.setError("Please select a location");
                return;
            }

            if (radius.isEmpty()) {
                radiusMeters.setError("Please enter a valid radius");
                return;
            }

            Toast.makeText(this, "Branch Saved Successfully!", Toast.LENGTH_SHORT).show();
            // TODO: Save data or navigate to another screen
        });
    }
}
