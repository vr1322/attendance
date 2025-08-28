package com.example.attendance;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

public class MarkAttendanceActivity extends AppCompatActivity {

    MaterialCalendarView materialCalendarView;
    HashMap<String, String[]> attendanceData; // Date -> [Status, InTime, OutTime]

    HashSet<CalendarDay> presentDates = new HashSet<>();
    HashSet<CalendarDay> absentDates = new HashSet<>();
    HashSet<CalendarDay> halfDayDates = new HashSet<>();
    HashSet<CalendarDay> overtimeDates = new HashSet<>();

    private TextView employeeName, branchName,presentCount, absentCount, halfdayCount, overtimeCount;
    private ImageView backiv , searchiv;
    private Button btnMarkAttendance;


    private static final String BASE_URL = "https://devonix.io/ems_api/";
    private static final String GET_ALL_ATTENDANCE_URL = BASE_URL + "get_all_attendance.php";
    private static final String GET_ATTENDANCE_SUMMARY_URL = BASE_URL + "get_attendance_summary.php";
    private static final String SEARCH_ATTENDANCE_URL = BASE_URL + "search_attendance.php";
    private static final String GET_OVERTIME_ATTENDANCE_URL = BASE_URL + "get_overtime_attendance.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        // UI Elements
        materialCalendarView = findViewById(R.id.calendarGrid);
        employeeName = findViewById(R.id.employee_name);
        branchName = findViewById(R.id.branch_name);
        backiv = findViewById(R.id.back);
        searchiv = findViewById(R.id.search);
        presentCount = findViewById(R.id.present_count);
        absentCount = findViewById(R.id.absent_count);
        halfdayCount = findViewById(R.id.halfday_count);
        overtimeCount = findViewById(R.id.overtime_count);
        btnMarkAttendance = findViewById(R.id.btn_mark_attendance);

        // ✅ Get all values from Intent only
        Intent intent = getIntent();
        String employeeId   = intent.getStringExtra("employee_id");
        String empName      = intent.getStringExtra("employee_name");
        String branch       = intent.getStringExtra("branch");
        String companyCode  = intent.getStringExtra("company_code");
        String email        = intent.getStringExtra("email");
        String role         = intent.getStringExtra("role");


        employeeName.setText(empName);
        branchName.setText(branch);

        backiv.setOnClickListener(view -> {
            Intent backIntent = new Intent(MarkAttendanceActivity.this, attendance_report.class);
            backIntent.putExtra("company_code", companyCode);
            backIntent.putExtra("email", email);
            backIntent.putExtra("role", role);
            startActivity(backIntent);
            finish();
        });


        btnMarkAttendance.setOnClickListener(view -> {
            AttendanceDetailsBottomSheet bottomSheet = new AttendanceDetailsBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });
        searchiv.setOnClickListener(v -> showDatePicker());

        if (companyCode.isEmpty() || empName.isEmpty() || branch.isEmpty()) {
            Toast.makeText(this, "Missing data in SharedPreferences!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current month/year
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);

        // Load attendance data
        new LoadAllAttendanceData().execute(companyCode, branch, empName);
        new LoadOvertimeAttendance().execute(companyCode, empName, branch);
        new LoadAttendanceSummaryWithOvertime(currentMonth, currentYear).execute(companyCode, empName, branch);

        // When month changes, refresh monthly summary
        materialCalendarView.setOnMonthChangedListener((widget, date) -> {
            int selectedMonth = date.getMonth() + 1;
            int selectedYear = date.getYear();

            new LoadAttendanceSummaryWithOvertime(selectedMonth, selectedYear).execute(companyCode, empName, branch);
        });

        // Show status on date click
        materialCalendarView.setOnDateChangedListener((widget, date, selected) -> {
            String selectedDate = String.format("%d-%02d-%02d",
                    date.getYear(), date.getMonth() + 1, date.getDay());

            if (attendanceData != null && attendanceData.containsKey(selectedDate)) {
                String[] details = attendanceData.get(selectedDate);

                String status = details[0] != null ? details[0] : "Not Marked";
                String inTime = details[1] != null ? details[1] : "N/A";
                String outTime = details[2] != null ? details[2] : "N/A";

                String message = "Status: " + status +
                        "\nIn-Time: " + inTime +
                        "\nOut-Time: " + outTime;

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "No attendance data found for this date.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // AsyncTask to Fetch Attendance Data from API
    private class LoadAllAttendanceData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String companyCode = params[0];
            String branch = params[1];
            String employeeName = params[2];

            String apiUrl = null;
            try {
                apiUrl = GET_ALL_ATTENDANCE_URL
                        + "?company_code=" + URLEncoder.encode(companyCode, "UTF-8")
                        + "&branch=" + URLEncoder.encode(branch, "UTF-8")
                        + "&employee_name=" + URLEncoder.encode(employeeName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }


            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                return result.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    if (jsonObject.getString("status").equals("success")) {
                        JSONArray attendanceArray = jsonObject.getJSONArray("attendance_data");

                        attendanceData = new HashMap<>();

                        for (int i = 0; i < attendanceArray.length(); i++) {
                            JSONObject attendanceObj = attendanceArray.getJSONObject(i);

                            String date = attendanceObj.getString("date");
                            String status = attendanceObj.optString("attendance_status", "Not Marked");
                            String inTime = attendanceObj.optString("in_time", "N/A");
                            String outTime = attendanceObj.optString("out_time", "N/A");

                            attendanceData.put(date, new String[]{status, inTime, outTime});


                            String[] splitDate = date.split("-");
                            int year = Integer.parseInt(splitDate[0]);
                            int month = Integer.parseInt(splitDate[1]) - 1;
                            int day = Integer.parseInt(splitDate[2]);

                            CalendarDay calendarDay = CalendarDay.from(year, month, day);

                            switch (status) {
                                case "Present":
                                    presentDates.add(calendarDay);
                                    break;
                                case "Absent":
                                    absentDates.add(calendarDay);
                                    break;
                                case "Half Day":
                                    halfDayDates.add(calendarDay);
                                    break;
                                case "Overtime":
                                    overtimeDates.add(calendarDay);
                                    break;
                            }
                        }

                        // Decorate Calendar with Attendance Status
                        materialCalendarView.addDecorator(new CustomDecorator(Color.GREEN, presentDates));
                        materialCalendarView.addDecorator(new CustomDecorator(Color.RED, absentDates));
                        materialCalendarView.addDecorator(new CustomDecorator(Color.YELLOW, halfDayDates));
                        materialCalendarView.addDecorator(new CustomDecorator(Color.BLUE, overtimeDates));

                    } else {
                        Toast.makeText(MarkAttendanceActivity.this,
                                jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MarkAttendanceActivity.this, "Data parsing error!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MarkAttendanceActivity.this, "Failed to load data!", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private class LoadOvertimeAttendance extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String companyCode = params[0];
            String employeeName = params[1];
            String branch = params[2];

            try {
                String apiUrl = GET_OVERTIME_ATTENDANCE_URL
                        + "?company_code=" + URLEncoder.encode(companyCode, "UTF-8")
                        + "&employee_name=" + URLEncoder.encode(employeeName, "UTF-8")
                        + "&branch=" + URLEncoder.encode(branch, "UTF-8");

                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    if (jsonObject.getString("status").equals("success")) {
                        JSONArray attendanceArray = jsonObject.getJSONArray("attendance_data");

                        for (int i = 0; i < attendanceArray.length(); i++) {
                            JSONObject obj = attendanceArray.getJSONObject(i);
                            String date = obj.getString("date");
                            String inTime = obj.optString("in_time", "N/A");
                            String outTime = obj.optString("out_time", "N/A");
                            String overtimeHours = obj.optString("overtime_hours", "0");

                            // Add to calendar data
                            CalendarDay day = CalendarDay.from(
                                    Integer.parseInt(date.substring(0, 4)),
                                    Integer.parseInt(date.substring(5, 7)) - 1,
                                    Integer.parseInt(date.substring(8, 10))
                            );

                            overtimeDates.add(day);
                            attendanceData.put(date, new String[]{"Overtime (" + overtimeHours + " hrs)", inTime, outTime});
                        }

                        // Decorate overtime dates
                        materialCalendarView.addDecorator(new CustomDecorator(Color.BLUE, overtimeDates));

                    } else {
                        Toast.makeText(MarkAttendanceActivity.this,
                                jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MarkAttendanceActivity.this, "Error parsing overtime data!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MarkAttendanceActivity.this, "Failed to load overtime data!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class LoadAttendanceSummaryWithOvertime extends AsyncTask<String, Void, Void> {
        private int present = 0, absent = 0, halfDay = 0, overtime = 0;
        private final int selectedMonth;
        private final int selectedYear;

        public LoadAttendanceSummaryWithOvertime(int month, int year) {
            this.selectedMonth = month;
            this.selectedYear = year;
        }

        @Override
        protected Void doInBackground(String... params) {
            String companyCode = params[0];
            String employeeName = params[1];
            String branch = params[2];

            String summaryUrl = GET_ATTENDANCE_SUMMARY_URL
                    + "?company_code=" + companyCode
                    + "&employee_name=" + employeeName
                    + "&branch=" + branch
                    + "&month=" + selectedMonth
                    + "&year=" + selectedYear;

            String overtimeUrl;
            try {
                overtimeUrl = GET_OVERTIME_ATTENDANCE_URL
                        + "?company_code=" + URLEncoder.encode(companyCode, "UTF-8")
                        + "&employee_name=" + URLEncoder.encode(employeeName, "UTF-8")
                        + "&branch=" + URLEncoder.encode(branch, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }

            try {
                // Attendance summary
                URL url1 = new URL(summaryUrl);
                HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();
                connection1.setRequestMethod("GET");
                BufferedReader reader1 = new BufferedReader(new InputStreamReader(connection1.getInputStream()));
                StringBuilder summaryResult = new StringBuilder();
                String line1;
                while ((line1 = reader1.readLine()) != null) summaryResult.append(line1);
                reader1.close();

                JSONObject jsonSummary = new JSONObject(summaryResult.toString());
                if (jsonSummary.getString("status").equals("success")) {
                    present = jsonSummary.optInt("present", 0);
                    absent = jsonSummary.optInt("absent", 0);
                    halfDay = jsonSummary.optInt("half_day", 0);
                }

                // Overtime
                URL url2 = new URL(overtimeUrl);
                HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
                connection2.setRequestMethod("GET");
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
                StringBuilder overtimeResult = new StringBuilder();
                String line2;
                while ((line2 = reader2.readLine()) != null) overtimeResult.append(line2);
                reader2.close();

                JSONObject jsonOvertime = new JSONObject(overtimeResult.toString());
                if (jsonOvertime.getString("status").equals("success")) {
                    JSONArray overtimeArray = jsonOvertime.getJSONArray("attendance_data");

                    for (int i = 0; i < overtimeArray.length(); i++) {
                        JSONObject obj = overtimeArray.getJSONObject(i);
                        String dateStr = obj.getString("date");

                        String[] parts = dateStr.split("-");
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);

                        // ✅ Use selectedMonth/year here
                        if (month == selectedMonth && year == selectedYear) {
                            overtime++;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            presentCount.setText(String.valueOf(present));
            absentCount.setText(String.valueOf(absent));
            halfdayCount.setText(String.valueOf(halfDay));
            overtimeCount.setText(String.valueOf(overtime));
        }
    }




    // Show DatePicker Dialog for Selecting Dates
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    searchAttendanceByDate(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    // Search Attendance by Date via API
    private void searchAttendanceByDate(String selectedDate) {
        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);
        String companyCode = sharedPreferences.getString("company_code", "");
        String empName = sharedPreferences.getString("employee_name", "");
        String branch = sharedPreferences.getString("branch", "");

        if (!companyCode.isEmpty()) {
            new SearchAttendanceData().execute(companyCode, empName, branch, selectedDate);
        } else {
            Toast.makeText(this, "Company code not found!", Toast.LENGTH_SHORT).show();
        }
    }

    // AsyncTask to Fetch Attendance Data from API
    private class SearchAttendanceData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String companyCode = params[0];
            String employeeName = params[1];
            String branch = params[2];
            String selectedDate = params[3];

            String apiUrl = SEARCH_ATTENDANCE_URL
                    + "?company_code=" + companyCode
                    + "&employee_name=" + employeeName
                    + "&branch=" + branch
                    + "&date=" + selectedDate;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                return result.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    if (jsonObject.getString("status").equals("success")) {
                        JSONArray attendanceArray = jsonObject.getJSONArray("attendance_data");

                        if (attendanceArray.length() > 0) {
                            JSONObject attendanceObj = attendanceArray.getJSONObject(0);

                            String status = attendanceObj.optString("attendance_status", "Not Marked");
                            String inTime = attendanceObj.optString("in_time", "N/A");
                            String outTime = attendanceObj.optString("out_time", "N/A");

                            String message = "Date: " + attendanceObj.optString("date", "N/A") +
                                    "\nStatus: " + status +
                                    "\nIn-Time: " + inTime +
                                    "\nOut-Time: " + outTime;

                            Toast.makeText(MarkAttendanceActivity.this, message, Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(MarkAttendanceActivity.this, "No attendance data found for selected date.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MarkAttendanceActivity.this,
                                jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MarkAttendanceActivity.this, "Data parsing error!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MarkAttendanceActivity.this, "Failed to load data!", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
