package com.example.attendance;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class salary_calculation extends AppCompatActivity {

    TextView tvEmpName, tvDesignation, tvBasicPay, tvPresentDays, tvAbsentDays, tvOvertimePay, tvTotalDeduction, tvNetSalary, tvMonth, tvTotalDays;
    EditText etAdvance, etUniform, etFine, etPF, etEsicPt;
    Button btnGenerate, btnSave, btnSelectMonth;

    String empId, companyCode;
    String month; // selected month in YYYY-MM
    double netSalary = 0, deduction = 0, overtimePay = 0, basicPay = 0;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_calculation);

        // Bind views
        tvEmpName = findViewById(R.id.tvEmpName);
        tvDesignation = findViewById(R.id.tvDesignation);
        tvBasicPay = findViewById(R.id.tvBasicPay);
        tvPresentDays = findViewById(R.id.tvPresentDays);
        tvAbsentDays = findViewById(R.id.tvAbsentDays);
        tvOvertimePay = findViewById(R.id.tvOvertimePay);
        tvTotalDeduction = findViewById(R.id.tvTotalDeduction);
        tvNetSalary = findViewById(R.id.tvNetSalary);
        tvMonth = findViewById(R.id.tvMonth);
        tvTotalDays = findViewById(R.id.tvTotalDays); // Add this TextView in your XML layout

        etAdvance = findViewById(R.id.etAdvance);
        etUniform = findViewById(R.id.etUniform);
        etFine = findViewById(R.id.etFine);
        etPF = findViewById(R.id.etPF);
        etEsicPt = findViewById(R.id.etEsicPt);

        btnGenerate = findViewById(R.id.btnGenerate);
        btnSave = findViewById(R.id.btnSave);
        btnSelectMonth = findViewById(R.id.btnSelectMonth);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        empId = getIntent().getStringExtra("employee_id");
        companyCode = getIntent().getStringExtra("company_code");

        // Default month = current month
        month = new java.text.SimpleDateFormat("yyyy-MM").format(new java.util.Date());
        tvMonth.setText("Month: " + month);

        // Month selection
        btnSelectMonth.setOnClickListener(v -> showMonthPicker());

        btnGenerate.setOnClickListener(v -> calculateSalary());
        btnSave.setOnClickListener(v -> saveSalarySlip());

        addTextWatchers(); // auto-recalculate on changes

        fetchSalaryDetails(); // auto-fetch for current month
    }

    private void addTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculateSalary();
            }
        };

        etPF.addTextChangedListener(watcher);
        etEsicPt.addTextChangedListener(watcher);
        etAdvance.addTextChangedListener(watcher);
        etUniform.addTextChangedListener(watcher);
        etFine.addTextChangedListener(watcher);
    }

    private void showMonthPicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int monthCurrent = calendar.get(Calendar.MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, (view, selectedYear, selectedMonth, dayOfMonth) -> {
            month = String.format("%04d-%02d", selectedYear, selectedMonth + 1);
            tvMonth.setText("Month: " + month);
            fetchSalaryDetails();
        }, year, monthCurrent, 1);

        // Hide day spinner
        try {
            java.lang.reflect.Field[] datePickerFields = dpd.getDatePicker().getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : datePickerFields) {
                if (field.getName().equals("mDaySpinner") || field.getName().equals("day")) {
                    field.setAccessible(true);
                    ((android.view.View) field.get(dpd.getDatePicker())).setVisibility(android.view.View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dpd.show();
    }

    private void fetchSalaryDetails() {
        progressDialog.show();
        String url = "https://devonix.io/ems_api/calculate_salary.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject obj = new JSONObject(response);

                        if (!obj.optBoolean("success", false)) {
                            Toast.makeText(this, obj.optString("error", "Failed"), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Employee info
                        tvEmpName.setText(obj.optString("employee_name", "N/A"));
                        tvDesignation.setText(obj.optString("designation", "N/A"));

                        // Numeric salary components
                        basicPay = obj.optDouble("basic_pay", 0);
                        overtimePay = obj.optDouble("overtime_pay", 0);
                        deduction = obj.optDouble("deduction", 0);

                        tvBasicPay.setText("Basic Pay: ₹" + basicPay); // <-- Important fix
                        tvOvertimePay.setText("Overtime Pay: ₹" + overtimePay);

                        // Editable fieldss
                        etPF.setText("0");
                        etEsicPt.setText("300");
                        etAdvance.setText(String.valueOf(obj.optDouble("advance", 0)));
                        etUniform.setText(String.valueOf(obj.optDouble("uniform", 0)));
                        etFine.setText(String.valueOf(obj.optDouble("fine", 0)));

                        // Days
                        int presentDays = obj.optInt("present_days", 0);
                        int absentDays = obj.optInt("absent_days", 0);
                        int totalDays = obj.optInt("total_days", getDaysInMonth(month));

                        tvPresentDays.setText("Present Days: " + presentDays);
                        tvAbsentDays.setText("Absent Days: " + absentDays);
                        tvTotalDays.setText("Total Days: " + totalDays);
                        // If you want total days in a TextView, make sure to create tvTotalDays in XML
                        // tvTotalDays.setText("Total Days: " + totalDays);

                        // Recalculate salary after fetching
                        calculateSalary();

                    } catch (Exception e) {
                        Toast.makeText(this, "Parsing Error", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error fetching salary: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("employee_id", empId);
                params.put("company_code", companyCode);
                params.put("month", month);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void calculateSalary() {
        double pf = 0;             // PF = 0
        double esicPt = 300;       // Fixed ESIC/PT
        double advance = parseDouble(etAdvance.getText().toString());
        double uniform = parseDouble(etUniform.getText().toString());
        double fine = parseDouble(etFine.getText().toString());

        int presentDays = parseInt(tvPresentDays.getText().toString().replaceAll("[^0-9]", ""));
        int absentDays = parseInt(tvAbsentDays.getText().toString().replaceAll("[^0-9]", ""));
        int totalDays = parseInt(tvTotalDays.getText().toString().replaceAll("[^0-9]", ""));
        if (totalDays <= 0) totalDays = getDaysInMonth(month);

        double absentDeduction = (basicPay / totalDays) * absentDays;
        double totalDeduction = absentDeduction + pf + esicPt + advance + uniform + fine;
        double totalNetSalary = basicPay + overtimePay - totalDeduction;

        tvTotalDeduction.setText("Total Deduction: ₹" + totalDeduction);
        tvNetSalary.setText("Net Salary: ₹" + totalNetSalary);
    }

    private void saveSalarySlip() {
        progressDialog.show();
        String url = "https://devonix.io/ems_api/save_salary.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject obj = new JSONObject(response);

                        if (obj.optBoolean("success", false)) {
                            Toast.makeText(this, obj.optString("message", "Salary Slip Saved!"), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, obj.optString("error", "Failed to save salary slip"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Parsing Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Network Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                double pf = 0;             // PF = 0
                double esicPt = 300;       // Fixed ESIC/PT
                double advance = parseDouble(etAdvance.getText().toString());
                double uniform = parseDouble(etUniform.getText().toString());
                double fine = parseDouble(etFine.getText().toString());

                int presentDays = parseInt(tvPresentDays.getText().toString().replaceAll("[^0-9]", ""));
                int absentDays = parseInt(tvAbsentDays.getText().toString().replaceAll("[^0-9]", ""));
                int totalDays = parseInt(tvTotalDays.getText().toString().replaceAll("[^0-9]", ""));
                if (totalDays <= 0) totalDays = getDaysInMonth(month);

                double absentDeduction = (basicPay / totalDays) * absentDays;
                double totalDeduction = absentDeduction + pf + esicPt + advance + uniform + fine;
                double totalNetSalary = basicPay + overtimePay - totalDeduction;

                Map<String, String> params = new HashMap<>();
                params.put("employee_id", empId);
                params.put("company_code", companyCode);
                params.put("month_year", month);
                params.put("designation", tvDesignation.getText().toString());
                params.put("basic_pay", String.valueOf(basicPay));
                params.put("overtime_pay", String.valueOf(overtimePay));
                params.put("pf", String.valueOf(pf));
                params.put("esic_pt", String.valueOf(esicPt));
                params.put("advance", String.valueOf(advance));
                params.put("uniform", String.valueOf(uniform));
                params.put("fine", String.valueOf(fine));
                params.put("deduction", String.valueOf(totalDeduction));
                params.put("net_salary", String.valueOf(totalNetSalary));
                params.put("present_days", String.valueOf(presentDays));
                params.put("absent_days", String.valueOf(absentDays));
                params.put("total_days", String.valueOf(totalDays));

                android.util.Log.d("SalaryParams", params.toString());

                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private int getDaysInMonth(String month) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM");
            java.util.Date date = sdf.parse(month);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        } catch (Exception e) {
            return 30;
        }
    }
}
