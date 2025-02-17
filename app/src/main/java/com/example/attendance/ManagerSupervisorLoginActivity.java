package com.example.attendance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ManagerSupervisorLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private CheckBox checkShowPassword;
    private ImageButton btnSubmit;
    private TextView tvForgot;
    private Button btnCreateAdmin;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_supervisor_login);

        etEmail = findViewById(R.id.manager_email);
        etPassword = findViewById(R.id.manaeger_password);
        checkShowPassword = findViewById(R.id.check_show_mana_password);
        btnSubmit = findViewById(R.id.btn_mana_submit);
        tvForgot = findViewById(R.id.manager_forgot);
        btnCreateAdmin = findViewById(R.id.btn_create_manager);

        checkShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPassword.setSelection(etPassword.length());
        });

        btnSubmit.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(ManagerSupervisorLoginActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(ManagerSupervisorLoginActivity.this, home.class);
            startActivity(intent);
        });

        tvForgot.setOnClickListener(v -> {
            Toast.makeText(ManagerSupervisorLoginActivity.this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show();
            // Implement forgot password logic
        });

        btnCreateAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerSupervisorLoginActivity.this, Create_Manager.class);
            startActivity(intent);
        });
    }
}
