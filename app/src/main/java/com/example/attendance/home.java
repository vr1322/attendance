package com.example.attendance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class home extends AppCompatActivity {
    private ImageButton el_button, ab_Button, ma_Button, sal_Button;
    private Button ad_pay_btn, all_ot_btn, leave_manage_btn, cnt_emp_btn, view_report_btn;
    private CardView el_View, ab_View, ma_View, sal_View;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private NavigationView navigationView;
    // Navigation Header Views
    private CircleImageView profilePic;
    private TextView companyName, designation;

    private String apiUrlFetch = "https://devonix.io/ems_api/get_admin_profile.php";
    private String companyCode = "", companyNameStr = "", email = "", role = "admin";

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Views
        el_button = findViewById(R.id.el_button);
        ab_Button = findViewById(R.id.ab_Button);
        ma_Button = findViewById(R.id.ma_Button);
        sal_Button = findViewById(R.id.sal_Button);
        el_View = findViewById(R.id.el_View);
        ab_View = findViewById(R.id.ab_View);
        ma_View = findViewById(R.id.ma_View);
        sal_View = findViewById(R.id.sal_View);
        ad_pay_btn = findViewById(R.id.advance_paybt);
        all_ot_btn = findViewById(R.id.allocate_otbt);
        leave_manage_btn = findViewById(R.id.leave_managbt);
        cnt_emp_btn = findViewById(R.id.cnt_empbt);
        view_report_btn = findViewById(R.id.view_reportbt);

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Attendo");

        all_ot_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Employee List Screen Open Karne Ka Intent
                Intent intent = new Intent(home.this, EmployeeListActivity.class);
                startActivity(intent);
            }
        });

        // Setup Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Setup Navigation View
        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.settings) {
                startActivity(new Intent(home.this, settings.class));
            }

            if (item.getItemId() == R.id.logout) {
                new AlertDialog.Builder(home.this)
                        .setTitle("Logout")
                        .setMessage("Do you really want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Intent intent = new Intent(home.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Initialize Navigation Header Views
        View headerView = navigationView.getHeaderView(0);
        profilePic = headerView.findViewById(R.id.profilePic);
        companyName = headerView.findViewById(R.id.companyName);
        designation = headerView.findViewById(R.id.designation);

        // Load Company Code from Shared Preferences
        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
        companyCode = sharedPreferences.getString("company_code", "").trim();
        companyNameStr = sharedPreferences.getString("company_name", "Company");
        email = sharedPreferences.getString("email", "");
        role = sharedPreferences.getString("role", "admin");

        if (companyCode.isEmpty()) {
            Toast.makeText(this, "Company code is missing!", Toast.LENGTH_SHORT).show();
        } else {
            fetchAdminDetails();
        }

        // Assign Click Listeners
        assignClickListener(el_button, emp_list.class);
        assignClickListener(ab_Button, BranchListActivity.class);
        assignClickListener(ma_Button, attendance_report.class);

        // 🔹 Changed here → First go to emp_list instead of salary_calculation
        assignClickListener(sal_Button, EmployeeSalaryListActivity.class);
        assignClickListener(el_View, emp_list.class);
        assignClickListener(ab_View, BranchListActivity.class);
        assignClickListener(ma_View, attendance_report.class);
        assignClickListener(view_report_btn, ViewReportsActivity.class);

        // 🔹 Changed here → First go to emp_list instead of salary_calculation
        assignClickListener(sal_View, EmployeeSalaryListActivity.class);
        assignClickListener(cnt_emp_btn, ContactEmpList.class);
        ad_pay_btn.setOnClickListener(v -> {
            Toast.makeText(home.this, "🚀 This feature will come in a future update!", Toast.LENGTH_SHORT).show();
        });
        //assignClickListener(all_ot_btn, AllocateOvertimeActivity.class);
        all_ot_btn.setOnClickListener(v -> {
            Toast.makeText(home.this, "🚀 This feature will come in a future update!", Toast.LENGTH_SHORT).show();
        });
        leave_manage_btn.setOnClickListener(v -> {
            Toast.makeText(home.this, "🚀 This feature will come in a future update!", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Fetches Admin details from API and updates the Nav Drawer Header.
     */
    private void fetchAdminDetails() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrlFetch + "?company_code=" + companyCode,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONObject data = jsonObject.getJSONObject("data");

                            companyName.setText(data.getString("company_name"));
                            designation.setText(data.getString("designation"));

                            String profileUrl = data.getString("profile_picture");
                            if (!profileUrl.isEmpty()) {
                                Glide.with(this).load(profileUrl).into(profilePic);
                            }
                        } else {
                            Toast.makeText(home.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(home.this, "Error parsing response!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(home.this, "Failed to fetch admin details!", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    /**
     * Helper method to assign click listeners to buttons and views.
     */
    private void assignClickListener(View view, final Class<?> targetActivity) {
        view.setOnClickListener(v -> {
            Intent intent = new Intent(home.this, targetActivity);
            intent.putExtra("company_code", companyCode);
            intent.putExtra("company_name", companyNameStr);
            intent.putExtra("email", email);
            intent.putExtra("role", role);
            startActivity(intent);
        });
    }
}.
