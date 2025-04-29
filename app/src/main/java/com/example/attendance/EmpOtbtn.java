package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class EmpOtbtn extends AppCompatActivity {

    ImageView backBtn;
    Button seeAllOtBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emp_otbtn); // Make sure this matches your XML filename

        backBtn = findViewById(R.id.back);
        seeAllOtBtn = findViewById(R.id.see_all_ot);

        // Back button finishes the activity
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // See All Overtime button goes to the next activity
        seeAllOtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmpOtbtn.this, EmployeeHomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
