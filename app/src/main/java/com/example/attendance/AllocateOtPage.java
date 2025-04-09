package com.example.attendance; // Change this to your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class AllocateOtPage extends AppCompatActivity {

    private ImageButton backButton;
    private Button btnAllocateOt, btnViewRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allocate_ot_page); // Replace with your actual XML filename without `.xml`

        // Initialize Views
        backButton = findViewById(R.id.back_btn); // if you gave it an ID; else set the ID in XML
        btnAllocateOt = findViewById(R.id.Allocate_ot);
        btnViewRequest = findViewById(R.id.view_request);

        // Back Button
        backButton.setOnClickListener(v -> finish()); // Close current activity

        // Allocate Overtime Button
        btnAllocateOt.setOnClickListener(v -> {
            // Navigate to Allocate Overtime Screen
            Intent intent = new Intent(AllocateOtPage.this, AllocateOvertimeActivity.class);
            startActivity(intent);
        });

        // View Request Button
        btnViewRequest.setOnClickListener(v -> {
            // Navigate to View Request Screen
            Intent intent = new Intent(AllocateOtPage.this, OvertimeRequestsActivity.class);
            startActivity(intent);
        });
    }
}
