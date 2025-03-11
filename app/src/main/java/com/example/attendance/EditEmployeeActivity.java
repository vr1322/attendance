package com.example.attendance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditEmployeeActivity extends AppCompatActivity {
    private EditText etEmployeeName, etEmployeeId, etDateOfBirth, etJoiningDate, etDesignation, etPhone, etAddress, etEmail, etPassword, etBasicPay, etOvertimeAllowance;
    private ImageView backButton, icCalendarDob, icCalendarJoining;
    private CircleImageView profilePic;
    private TextView addemp_text;
    private AutoCompleteTextView etBranch;
    private RadioGroup paymentTypeGroup;
    private RadioButton perDay, monthly;
    private Button saveButton;
    private Bitmap bitmap;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_employee);


    }
}
