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

public class Employee_Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private CheckBox checkShowPassword;
    private ImageButton btnSubmit;
    private TextView tvForgot;
    private Button btnCreateAdmin;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_login);

        etEmail = findViewById(R.id.employee_email);
        etPassword = findViewById(R.id.employee_password);
        checkShowPassword = findViewById(R.id.check_show_emp_password);
        btnSubmit = findViewById(R.id.btn_emp_submit);
        tvForgot = findViewById(R.id.employee_forgot);
        btnCreateAdmin = findViewById(R.id.btn_create_employee);

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
                Toast.makeText(Employee_Login.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Employee_Login.this, home.class);
            startActivity(intent);
        });

        tvForgot.setOnClickListener(v -> {
            Toast.makeText(Employee_Login.this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show();
            // Implement forgot password logic
        });

        btnCreateAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(Employee_Login.this, Create_emp_acc.class);
            startActivity(intent);
        });
    }
}
