package com.example.attendance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private CheckBox checkShowPassword;
    private RadioGroup radioGroup;
    private ImageButton btnSubmit;
    private TextView tvForgot;
    private Button btnCreateAdmin;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        checkShowPassword = findViewById(R.id.check_show_password);
        radioGroup = findViewById(R.id.radioGroup);
        btnSubmit = findViewById(R.id.btn_submit);
        tvForgot = findViewById(R.id.tv_forgot);
        btnCreateAdmin = findViewById(R.id.btn_create_admin);

        checkShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPassword.setSelection(etPassword.length());
        });

        btnSubmit.setOnClickListener(v ->  {
            Intent intent = new Intent(MainActivity.this, home.class);
            startActivity(intent);
        });

        tvForgot.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show();
            // Implement forgot password logic
        });

        btnCreateAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateAdminActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (selectedId == -1) {
            Toast.makeText(this, "Please select a login role", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRole = findViewById(selectedId);
        String role = selectedRole.getText().toString();

        Toast.makeText(this, "Logging in as: " + role, Toast.LENGTH_SHORT).show();

        // Implement login authentication logic here
    }
}
