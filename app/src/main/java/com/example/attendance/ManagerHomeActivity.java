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

public class ManagerHomeActivity extends AppCompatActivity {
    private ImageButton el_button, ab_Button, ma_Button, mark_Button;
    private Button ad_pay_btn, all_ot_btn, leave_manage_btn, sal_View, cnt_emp_btn;
    private CardView el_View, ab_View, ma_View;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private NavigationView navigationView;

    // Navigation Header Views
    private CircleImageView profilePic;
    private TextView companyName, designation;

    private String apiUrlFetch = "https://devonix.io/ems_api/get_manager_profile.php";
    private String companyCode = "";

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_home);

        // Initialize Views
        el_button = findViewById(R.id.el_button);
        ab_Button = findViewById(R.id.ab_Button);
        ma_Button = findViewById(R.id.ma_Button);
        mark_Button = findViewById(R.id.sal_Button);
        el_View = findViewById(R.id.el_View);
        ab_View = findViewById(R.id.ab_View);
        ma_View = findViewById(R.id.ma_View);
        sal_View = findViewById(R.id.sal_View);
        ad_pay_btn = findViewById(R.id.advance_paybt);
        all_ot_btn = findViewById(R.id.allocate_otbt);
        leave_manage_btn = findViewById(R.id.leave_managbt);
        cnt_emp_btn = findViewById(R.id.cnt_empbt);

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Attendo");

        all_ot_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Employee List Screen Open Karne Ka Intent
                Intent intent = new Intent(ManagerHomeActivity.this, EmployeeListActivity.class);
                startActivity(intent);
            }
        });
        cnt_emp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Employee List Screen Open Karne Ka Intent
                Intent intent = new Intent(ManagerHomeActivity.this, ContactEmpList.class);
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
                startActivity(new Intent(ManagerHomeActivity.this, settings.class));
            }

            if (item.getItemId() == R.id.logout) {
                new AlertDialog.Builder(ManagerHomeActivity.this)
                        .setTitle("Logout")
                        .setMessage("Do you really want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Clear shared preferences
                            SharedPreferences preferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.clear();
                            editor.apply();

                            // Go back to login screen
                            Intent intent = new Intent(ManagerHomeActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("No", null)
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
        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", MODE_PRIVATE);;
        String email = sharedPreferences.getString("email", "");
        companyCode = sharedPreferences.getString("company_code", "");

        if (companyCode.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Company code or email is missing!", Toast.LENGTH_SHORT).show();
        } else {
            fetchManagerDetails(companyCode, email);
        }


        // Assign Click Listeners
        assignClickListener(el_button, emp_list.class);
        assignClickListener(ab_Button, BranchListActivity.class);
        assignClickListener(ma_Button, attendance_report.class);
        assignClickListener(el_View, emp_list.class);
        assignClickListener(ab_View, BranchListActivity.class);
        assignClickListener(ma_View, attendance_report.class);
        assignClickListener(sal_View, salary_calculation.class);
        assignClickListener(all_ot_btn, AllocateOtPage.class);
    }

    /**
     * Fetches Manager details from API and updates the Nav Drawer Header.
     */
    private void fetchManagerDetails(String companyCode, String email) {
        String url = apiUrlFetch + "?company_code=" + companyCode + "&email=" + email;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONObject data = jsonObject.getJSONObject("data");

                            companyName.setText(data.getString("employee_name")); // Or use "branch_name"
                            designation.setText(data.getString("designation"));

                            String profileUrl = data.getString("profile_pic");
                            if (!profileUrl.isEmpty()) {
                                Glide.with(this).load(profileUrl).into(profilePic);
                            }
                        } else {
                            Toast.makeText(ManagerHomeActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ManagerHomeActivity.this, "Error parsing response!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(ManagerHomeActivity.this, "Failed to fetch manager details!", Toast.LENGTH_SHORT).show());

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }


    /**
     * Helper method to assign click listeners to buttons and views.
     */
    private void assignClickListener(View view, final Class<?> targetActivity) {
        view.setOnClickListener(v -> startActivity(new Intent(ManagerHomeActivity.this, targetActivity)));
    }
}
