package com.example.attendance;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SalaryViewActivity extends AppCompatActivity {

    private Spinner spinnerMonth, spinnerYear;
    private TextView tvEmpName, tvDesignation, tvMonth,
            tvBasicPay, tvOvertime, tvPF, tvESIC_PT, tvDeduction, tvAdvance,
            tvUniform, tvFine, tvNetSalary, tvPresent, tvAbsent, tvTotal;

    private String selectedMonth, selectedYear, employeeId, companyCode, employeeName;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_view);

        // Initialize Views
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);

        tvEmpName = findViewById(R.id.tvEmpName);
        tvDesignation = findViewById(R.id.tvDesignation);
        tvMonth = findViewById(R.id.tvMonth);
        tvBasicPay = findViewById(R.id.tvBasicPay);
        tvOvertime = findViewById(R.id.tvOvertime);
        tvPF = findViewById(R.id.tvPF);
        tvESIC_PT = findViewById(R.id.tvESIC_PT);
        tvDeduction = findViewById(R.id.tvDeduction);
        tvAdvance = findViewById(R.id.tvAdvance);
        tvUniform = findViewById(R.id.tvUniform);
        tvFine = findViewById(R.id.tvFine);
        tvNetSalary = findViewById(R.id.tvNetSalary);
        tvPresent = findViewById(R.id.tvPresent);
        tvAbsent = findViewById(R.id.tvAbsent);
        tvTotal = findViewById(R.id.tvTotal);

        // Get role from intent
        role = getIntent().getStringExtra("role");
        if (role == null) role = "employee"; // default

        // Load session
        loadSession();

        // Setup Month Spinner
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Setup Year Spinner (last 5 years)
        ArrayList<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= currentYear - 5; i--) years.add(String.valueOf(i));
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Default month/year
        selectedMonth = String.format("%02d", Calendar.getInstance().get(Calendar.MONTH) + 1);
        selectedYear = String.valueOf(currentYear);
        spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH));
        spinnerYear.setSelection(0);

        // Spinner listener
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedMonth = String.format("%02d", spinnerMonth.getSelectedItemPosition() + 1);
                selectedYear = spinnerYear.getSelectedItem().toString();
                fetchSalaryData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        };

        spinnerMonth.setOnItemSelectedListener(listener);
        spinnerYear.setOnItemSelectedListener(listener);

        // Initial fetch
        fetchSalaryData();
    }

    private void loadSession() {
        switch (role.toLowerCase()) {
            case "employee":
                SharedPreferences empPrefs = getSharedPreferences("EmployeeSession", MODE_PRIVATE);
                employeeId = empPrefs.getString("employee_id", "");
                employeeName = empPrefs.getString("employee_name", "");
                companyCode = empPrefs.getString("company_code", "");
                break;

            case "manager":
                SharedPreferences mgrPrefs = getSharedPreferences("ManagerSession", MODE_PRIVATE);
                employeeId = mgrPrefs.getString("manager_id", "");
                employeeName = mgrPrefs.getString("manager_name", "");
                companyCode = mgrPrefs.getString("company_code", "");
                break;

            case "supervisor":
                SharedPreferences supPrefs = getSharedPreferences("SupervisorSession", MODE_PRIVATE);
                employeeId = supPrefs.getString("supervisor_id", "");
                employeeName = supPrefs.getString("supervisor_name", "");
                companyCode = supPrefs.getString("company_code", "");
                break;
        }
    }

    private void fetchSalaryData() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Salary...");
        progressDialog.show();

        String url = "https://devonix.io/ems_api/view_calculated_salary.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            tvEmpName.setText(employeeName);
                            tvDesignation.setText(obj.optString("designation", ""));
                            tvMonth.setText(selectedYear + "-" + selectedMonth);

                            tvBasicPay.setText("Basic Pay: ₹" + obj.optString("basic_pay", "0"));
                            tvOvertime.setText("Overtime: ₹" + obj.optString("overtime_pay", "0"));
                            tvPF.setText("PF: ₹" + obj.optString("pf", "0"));
                            tvESIC_PT.setText("ESIC/PT: ₹" + obj.optString("esic_pt", "0"));
                            tvDeduction.setText("Deductions: ₹" + obj.optString("deduction", "0"));
                            tvAdvance.setText("Advance: ₹" + obj.optString("advance", "0"));
                            tvUniform.setText("Uniform: ₹" + obj.optString("uniform", "0"));
                            tvFine.setText("Fine: ₹" + obj.optString("fine", "0"));
                            tvNetSalary.setText("Net Salary: ₹" + obj.optString("net_salary", "0"));

                            tvPresent.setText(obj.optString("present_days", "0"));
                            tvAbsent.setText(obj.optString("absent_days", "0"));
                            tvTotal.setText(obj.optString("total_days", "0"));
                        } else {
                            Toast.makeText(SalaryViewActivity.this,
                                    "No salary data found!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(SalaryViewActivity.this,
                                "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(SalaryViewActivity.this,
                            "Network Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                // ✅ Always send month for supervisor
                Map<String, String> params = new HashMap<>();
                params.put("employee_id", employeeId);
                params.put("company_code", companyCode);
                params.put("month", selectedYear + "-" + selectedMonth);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}
