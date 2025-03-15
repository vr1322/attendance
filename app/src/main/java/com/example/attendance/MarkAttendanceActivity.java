package com.example.attendance;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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


    private static final String BASE_URL = "https://devonix.io/ems_api/";
    private static final String GET_ALL_ATTENDANCE_URL = BASE_URL + "get_all_attendance.php";
    private static final String GET_ATTENDANCE_SUMMARY_URL = BASE_URL + "get_attendance_summary.php";
    private static final String SEARCH_ATTENDANCE_URL = BASE_URL + "search_attendance.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        materialCalendarView = findViewById(R.id.calendarGrid);
        employeeName = findViewById(R.id.employee_name);
        branchName = findViewById(R.id.branch_name);
        backiv = findViewById(R.id.back);
        searchiv = findViewById(R.id.search);
        presentCount = findViewById(R.id.present_count);
        absentCount = findViewById(R.id.absent_count);
        halfdayCount = findViewById(R.id.halfday_count);
        overtimeCount = findViewById(R.id.overtime_count);

        // Fetch Employee Name & Branch from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);
        String companyCode = sharedPreferences.getString("company_code", "");
        String empName = sharedPreferences.getString("employee_name", "").trim();
        String branch = sharedPreferences.getString("branch", "").trim();

        employeeName.setText(empName);
        branchName.setText(branch);

        backiv.setOnClickListener(view -> startActivity(new Intent(MarkAttendanceActivity.this, attendance_report.class)));
        searchiv.setOnClickListener(v -> showDatePicker());
        // Load Attendance Data from Database

        if (companyCode.isEmpty() || empName.isEmpty() || branch.isEmpty()) {
            Toast.makeText(this, "Missing data in SharedPreferences!", Toast.LENGTH_SHORT).show();
            return;
        }

        new LoadAllAttendanceData().execute(companyCode, branch, empName);



        if (!companyCode.isEmpty()) {
            new LoadAttendanceSummary().execute(companyCode, empName, branch);
        } else {
            Toast.makeText(this, "Company code not found!", Toast.LENGTH_SHORT).show();
        }


        // Date Click Listener for Showing Details
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
    // AsyncTask to Fetch Attendance Summary from API
    private class LoadAttendanceSummary extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String companyCode = params[0];
            String employeeName = params[1];
            String branch = params[2];

            String apiUrl = GET_ATTENDANCE_SUMMARY_URL
                    + "?company_code=" + companyCode
                    + "&employee_name=" + employeeName
                    + "&branch=" + branch;

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
                        int present = jsonObject.optInt("present", 0);
                        int absent = jsonObject.optInt("absent", 0);
                        int halfDay = jsonObject.optInt("half_day", 0);
                        int overtime = jsonObject.optInt("overtime", 0);

                        presentCount.setText(String.valueOf(present));
                        absentCount.setText(String.valueOf(absent));
                        halfdayCount.setText(String.valueOf(halfDay));
                        overtimeCount.setText(String.valueOf(overtime));

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
