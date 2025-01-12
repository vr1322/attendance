package com.example.attendance;

import android.annotation.SuppressLint;
import android.content.Intent;  // Added missing import for Intent
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class home extends AppCompatActivity {  // Renamed to HomeActivity for convention
    private ImageButton el_button, ab_Button, ma_Button, sal_Button;
    private ImageView el_View, ab_View, ma_View, sal_View;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        el_button = findViewById(R.id.el_button);
        ab_Button = findViewById(R.id.ab_Button);
        ma_Button = findViewById(R.id.ma_Button);
        sal_Button = findViewById(R.id.sal_Button);
        el_View = findViewById(R.id.el_View);
        ab_View = findViewById(R.id.ab_View);
        ma_View = findViewById(R.id.ma_View);
        sal_View = findViewById(R.id.sal_View);


        el_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(home.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ab_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(home.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ma_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(home.this, MainActivity.class);
                startActivity(intent);
            }
        });

        sal_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(home.this, MainActivity.class);
                startActivity(intent);
            }
        });

        el_View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(home.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ab_View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(home.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ma_View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(home.this, MainActivity.class);
                startActivity(intent);
            }
        });

        sal_View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(home.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}


