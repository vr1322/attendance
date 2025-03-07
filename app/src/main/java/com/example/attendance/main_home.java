package com.example.attendance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class main_home extends AppCompatActivity {

    private Button btn_rg_cp;
    private ImageButton btn_submit;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);

        btn_submit = findViewById(R.id.btn_submit);
        btn_rg_cp = findViewById(R.id.rg_cp_btn);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(main_home.this, MainActivity.class);
                startActivity(intent);
            }
        });
        btn_rg_cp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(main_home.this, CreateAdminActivity.class);
                startActivity(intent);
            }
        });


    }
}
