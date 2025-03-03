package com.example.attendance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class home extends AppCompatActivity {  // Renamed to follow conventions
    private ImageButton el_button, ab_Button, ma_Button, sal_Button;
    private CardView el_View, ab_View, ma_View, sal_View;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;
    private NavigationView navigationView;

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

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("E-Attendence");
        // Setup Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Setup Navigation View
        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                // Handle navigation item clicks
                if (id == R.id.settings) {
                    startActivity(new Intent(home.this, settings.class));
                }
                drawerLayout.closeDrawer(GravityCompat.START); // Close drawer
                return true;
            }
        });

        // Assign Click Listeners
        assignClickListener(el_button, emp_list.class);
        assignClickListener(ab_Button, AddBranchActivity.class);
        assignClickListener(ma_Button, add_emp.class);
        assignClickListener(sal_Button, salary_calculation.class);

        assignClickListener(el_View, emp_list.class);
        assignClickListener(ab_View, AddBranchActivity.class);
        assignClickListener(ma_View, add_emp.class);
        assignClickListener(sal_View, salary_calculation.class);
    }

    /**
     * Helper method to assign click listeners to buttons and views.
     */
    private void assignClickListener(View view, final Class<?> targetActivity) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, targetActivity);
                startActivity(intent);
            }
        });
    }
}
