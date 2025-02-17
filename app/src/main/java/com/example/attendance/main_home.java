package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class main_home extends AppCompatActivity {

    private Button btnAdminLogin, btnManSupLogin, btnEmployeeLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);

        btnAdminLogin = findViewById(R.id.btn_admin_login);
        btnManSupLogin = findViewById(R.id.btn_man_sup_login);
        btnEmployeeLogin = findViewById(R.id.btn_employe_login);

        btnAdminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(main_home.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btnManSupLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(main_home.this, ManagerSupervisorLoginActivity.class);
                startActivity(intent);
            }
        });

        btnEmployeeLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(main_home.this, Employee_Login.class);
                startActivity(intent);
            }
        });
    }
}
