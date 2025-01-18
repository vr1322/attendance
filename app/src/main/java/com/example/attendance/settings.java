package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class settings extends AppCompatActivity {
    private ImageView back;
    private TextView settext;
    private Button ap_bt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
      back = findViewById(R.id.back);
      settext = findViewById(R.id.settext);
      ap_bt = findViewById(R.id.ap_bt);
      settext.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Intent intent = new Intent(settings.this, home.class);
              startActivity(intent);
          }
      });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(settings.this, home.class);
                startActivity(intent);
            }
        });

        ap_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(settings.this, update_admin.class);
                startActivity(intent);
            }
        });

    }
}