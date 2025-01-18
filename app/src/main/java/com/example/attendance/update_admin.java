package com.example.attendance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class update_admin extends AppCompatActivity {
    private ImageView back;
    private TextView updttext;;
    private TextView logout;
    private ImageView logout_iv;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_admin);
        back = findViewById(R.id.back);
        updttext = findViewById(R.id.updttext);
        logout = findViewById(R.id.logout);
        logout_iv = findViewById(R.id.logout_iv);
        updttext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.content.Intent intent = new android.content.Intent(update_admin.this, settings.class);
                startActivity(intent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.content.Intent intent = new Intent(update_admin.this, settings.class);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.content.Intent intent = new android.content.Intent(update_admin.this, MainActivity.class);
                startActivity(intent);
            }
        });

        logout_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.content.Intent intent = new android.content.Intent(update_admin.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }
}