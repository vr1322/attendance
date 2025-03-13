package com.example.attendance;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class attendance_report extends AppCompatActivity {

    private ImageView backButton, searchIv, downloadIv;
    private TextView reportTxt;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> listGroupTitles;
    private HashMap<String, List<Employee>> listData;
    private HashMap<String, List<Attendance>> attendanceData;



    private RequestQueue requestQueue;
    private static final String BASE_URL = "http://192.168.144.102/ems_api/";
    private static final String GET_BRANCHES_URL = BASE_URL + "get_branches_employees.php";
    private static final String GET_ATTENDANCE_URL = BASE_URL + "get_attendance.php";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);

        requestQueue = Volley.newRequestQueue(this);

        backButton = findViewById(R.id.back);
        searchIv = findViewById(R.id.search);
        downloadIv = findViewById(R.id.download);
        reportTxt = findViewById(R.id.attendence_txt);
        expandableListView = findViewById(R.id.expandableListView);

        listGroupTitles = new ArrayList<>();
        listData = new HashMap<>();
        attendanceData = new HashMap<>();


        // Load data for employees and attendance
        loadBranchesAndEmployees();
        loadAttendanceData();

        adapter = new ExpandableListAdapter(this, listGroupTitles, listData, true); // Attendance Report
        expandableListView.setAdapter(adapter);
        setupEmployeeClickListeners();


        FloatingActionButton refreshFab = findViewById(R.id.refresh_fab);
        refreshFab.setOnClickListener(view -> refreshData());


        // Back Button
        backButton.setOnClickListener(view -> startActivity(new Intent(attendance_report.this, home.class)));
        reportTxt.setOnClickListener(view -> startActivity(new Intent(attendance_report.this, home.class)));
        // Search
        searchIv.setOnClickListener(view -> showSearchDialog());

        // Download
        downloadIv.setOnClickListener(view -> showDownloadOptionsDialog());
    }

    // ==============================
    // Load Employees Data
    // ==============================
    private void loadBranchesAndEmployees() {
        String companyCode = getSharedPreferences("AdminPrefs", MODE_PRIVATE).getString("company_code", "");
        String url = GET_BRANCHES_URL + "?company_code=" + companyCode;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        listGroupTitles.clear();
                        listData.clear();

                        JSONArray branchesArray = response.getJSONArray("branches");

                        for (int i = 0; i < branchesArray.length(); i++) {
                            JSONObject branchObj = branchesArray.getJSONObject(i);
                            String branchName = branchObj.getString("branch_name");
                            listGroupTitles.add(branchName);

                            JSONArray employeesArray = branchObj.getJSONArray("employees");
                            List<Employee> employees = new ArrayList<>();

                            for (int j = 0; j < employeesArray.length(); j++) {
                                JSONObject empObj = employeesArray.getJSONObject(j);

                                // Check if profile_pic exists and is not null
                                String profilePic = "http://192.168.144.102/ems_api/" + empObj.getString("profile_pic");
                                String attendanceStatus = empObj.optString("attendance_status", "Not Marked");  // ✅ Correct Usage


                                Employee employee = new Employee(
                                        empObj.getString("employee_name"),
                                        empObj.getString("designation"),
                                        empObj.getString("employee_id"),
                                        false,  // Default value for isParkingAvailable
                                        false,  // Default value for isParkingAssigned
                                        empObj.getString("phone"),
                                        profilePic,
                                        attendanceStatus,
                                        branchName
                                );

                                employees.add(employee);
                            }

                            listData.put(branchName, employees);
                        }

                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(attendance_report.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(attendance_report.this, "Error fetching data", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
    }
    // ==============================
// Load Attendance Data
// ==============================
    private void loadAttendanceData() {
        String companyCode = getSharedPreferences("AdminPrefs", MODE_PRIVATE).getString("company_code", "");
        String url = GET_ATTENDANCE_URL + "?company_code=" + companyCode;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("ATTENDANCE_DATA", response.toString());  // ✅ Add this to verify data

                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray attendanceArray = response.getJSONArray("attendance_data");

                            attendanceData.clear();

                            for (int i = 0; i < attendanceArray.length(); i++) {
                                JSONObject attendanceObj = attendanceArray.getJSONObject(i);

                                Attendance attendance = new Attendance(
                                        attendanceObj.getString("employee_id"),
                                        attendanceObj.getString("employee_name"),
                                        attendanceObj.getString("branch"),
                                        attendanceObj.getString("in_time"),
                                        attendanceObj.getString("out_time"),
                                        attendanceObj.getString("attendance_status"),
                                        attendanceObj.getString("geofenced_status"),
                                        attendanceObj.getString("date")  // ✅ Added date handling
                                );

                                String branchName = attendanceObj.getString("branch");

                                if (!attendanceData.containsKey(branchName)) {
                                    attendanceData.put(branchName, new ArrayList<>());
                                }

                                attendanceData.get(branchName).add(attendance);
                            }

                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(attendance_report.this, "Error parsing attendance data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(attendance_report.this, "Error fetching attendance data", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
    }


    // ==============================
// Update Employee Attendance Status
// ==============================
    private void updateEmployeeAttendanceStatus() {
        for (String branch : listData.keySet()) {
            List<Employee> employees = listData.get(branch);

            for (Employee employee : employees) {
                String employeeId = employee.getId();
                if (attendanceData.containsKey(employeeId)) {
                    List<Attendance> attendanceList = attendanceData.get(employeeId);

                    if (!attendanceList.isEmpty()) {
                        String latestStatus = attendanceList.get(0).getAttendanceStatus();  // Get the most recent status
                        employee.setAttendanceStatus(latestStatus);  // ✅ Update status in Employee object
                    }
                }
            }
        }

        adapter.notifyDataSetChanged();  // Refresh UI
    }


    private void refreshData() {
        // Show a loading dialog for better UI
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Refreshing Data")
                .setMessage("Please wait while data is being refreshed...")
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Clear data to avoid duplication
        listGroupTitles.clear();
        listData.clear();
        attendanceData.clear();

        // Reload data from the database
        loadBranchesAndEmployees();
        loadAttendanceData();

        // Dismiss the dialog after data is loaded
        expandableListView.postDelayed(dialog::dismiss, 1500);

        // Show success message
        Toast.makeText(this, "Data refreshed successfully", Toast.LENGTH_SHORT).show();
    }

    // ==============================
// OnClick - Mark Single Employee Attendance
// ==============================
    private void markSingleEmployeeAttendance(Employee employee) {
        Intent intent = new Intent(attendance_report.this, MarkAttendanceActivity.class);
        intent.putExtra("employee_id", employee.getId());
        intent.putExtra("employee_name", employee.getName());
        startActivity(intent);
    }

    // ==============================
// OnLongClick - Initiate Multiple Employee Selection
// ==============================
    private void initiateMultipleEmployeeSelection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mark Multiple Employees")
                .setMessage("Do you want to mark attendance for multiple employees?")
                .setPositiveButton("Yes", (dialog, which) -> showBranchSelectionDialog())  // ➡️ Select Branch First
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ==============================
// Show Branch Selection Dialog
// ==============================
    private void showBranchSelectionDialog() {
        String[] branchNames = listGroupTitles.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Branch")
                .setItems(branchNames, (dialog, which) -> {
                    String selectedBranch = branchNames[which];
                    showEmployeeSelectionDialog(selectedBranch);  // ➡️ Show Employees in Selected Branch
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ==============================
// Show Employee Selection Dialog
// ==============================
    private void showEmployeeSelectionDialog(String branch) {
        List<Employee> branchEmployees = listData.get(branch);  // ✅ Show Employees from Selected Branch

        boolean[] selectedItems = new boolean[branchEmployees.size()];
        List<Employee> selectedEmployees = new ArrayList<>();

        String[] employeeNames = new String[branchEmployees.size()];
        for (int i = 0; i < branchEmployees.size(); i++) {
            employeeNames[i] = branchEmployees.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Employees")
                .setMultiChoiceItems(employeeNames, selectedItems, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        selectedEmployees.add(branchEmployees.get(which));
                    } else {
                        selectedEmployees.remove(branchEmployees.get(which));
                    }
                })
                .setPositiveButton("Next", (dialog, which) -> {
                    if (!selectedEmployees.isEmpty()) {
                        showAttendanceStatusDialog(selectedEmployees);  // ➡️ Select Attendance Status
                    } else {
                        Toast.makeText(this, "Please select at least one employee.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ==============================
// Show Attendance Status Dialog
// ==============================
    private void showAttendanceStatusDialog(List<Employee> selectedEmployees) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mark Attendance As")
                .setItems(new String[]{"Present", "Absent", "Overtime", "Half Day"}, (dialog, which) -> {
                    String status = "";

                    switch (which) {
                        case 0: status = "Present"; break;
                        case 1: status = "Absent"; break;
                        case 2: status = "Overtime"; break;
                        case 3: status = "Half Day"; break;
                    }

                    showDateTimeDialog(selectedEmployees, status);  // ➡️ Select Date, In-Time, Out-Time
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ==============================
// Show Date, In-Time & Out-Time Picker with Logic
// ==============================
    // ==============================
// Show Date, In-Time & Out-Time Picker with Logic
// ==============================
    private void showDateTimeDialog(List<Employee> selectedEmployees, String status) {
        final Calendar calendar = Calendar.getInstance();
        final String[] finalStatus = {status};

        // Date Picker
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;

            // Check if attendance already marked
            if (isAttendanceAlreadyMarked(selectedEmployees, selectedDate)) {
                Toast.makeText(this, "Attendance already marked for " + selectedDate, Toast.LENGTH_SHORT).show();
                return;
            }

            // In-Time Picker with Title
            showTimePickerDialog("Select In Time", (view1, hourOfDay, minute) -> {
                String inTime = formatTimeWithAMPM(hourOfDay, minute);

                // Out-Time Picker with Title
                showTimePickerDialog("Select Out Time", (view2, hourOfDay1, minute1) -> {
                    String outTime = formatTimeWithAMPM(hourOfDay1, minute1);

                    // Check if Out-Time is in the past
                    if (isOutTimeExceeded(outTime)) {
                        Toast.makeText(this, "Out-time exceeded. Attendance set to 'Not Marked'.", Toast.LENGTH_SHORT).show();
                        finalStatus[0] = "Not Marked";
                    }

                    // ✅ Mark attendance with correct data
                    markMultipleEmployeesAttendance(selectedEmployees, finalStatus[0], selectedDate, inTime, outTime);

                });

            });

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.setTitle("Select Date");
        datePicker.show();
    }

    // ==============================
// Custom Time Picker Dialog with Title
// ==============================
    private void showTimePickerDialog(String title, TimePickerDialog.OnTimeSetListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        TimePicker timePicker = new TimePicker(this);
        timePicker.setIs24HourView(false); // 12-hour format

        builder.setView(timePicker);
        builder.setPositiveButton("OK", (dialog, which) -> {
            listener.onTimeSet(timePicker, timePicker.getHour(), timePicker.getMinute());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    // ==============================
// Format Time with AM/PM
// ==============================
    private String formatTimeWithAMPM(int hourOfDay, int minute) {
        String amPm = (hourOfDay < 12) ? "AM" : "PM";
        int hour = (hourOfDay == 0) ? 12 : (hourOfDay > 12) ? hourOfDay - 12 : hourOfDay;
        return String.format("%02d:%02d %s", hour, minute, amPm);
    }



    // ==============================
// Check if Attendance Already Marked
// ==============================
    private boolean isAttendanceAlreadyMarked(List<Employee> employees, String date) {
        for (Employee employee : employees) {
            if (attendanceData.containsKey(employee.getId())) {
                List<Attendance> attendanceList = attendanceData.get(employee.getId());

                for (Attendance attendance : attendanceList) {
                    if (attendance.getDate().equals(date)) {
                        return true; // Attendance already marked for this date
                    }
                }
            }
        }
        return false;
    }

    // ==============================
// Check if Out-Time is Exceeded
// ==============================
    private boolean isOutTimeExceeded(String outTime) {
        Calendar currentTime = Calendar.getInstance();
        Calendar outTimeCalendar = Calendar.getInstance();

        String[] timeParts = outTime.split(" ");
        String[] hourMinute = timeParts[0].split(":");

        int outHour = Integer.parseInt(hourMinute[0]);
        int outMinute = Integer.parseInt(hourMinute[1]);

        if (timeParts[1].equalsIgnoreCase("PM") && outHour != 12) {
            outHour += 12;
        } else if (timeParts[1].equalsIgnoreCase("AM") && outHour == 12) {
            outHour = 0; // Midnight correction
        }

        outTimeCalendar.set(Calendar.HOUR_OF_DAY, outHour);
        outTimeCalendar.set(Calendar.MINUTE, outMinute);

        return currentTime.after(outTimeCalendar); // True if current time > out-time
    }


    // ==============================
// Mark Attendance for Selected Employees in Database
// ==============================
    private void markMultipleEmployeesAttendance(List<Employee> selectedEmployees, String status, String selectedDate, String inTime, String outTime) {
        String companyCode = getSharedPreferences("AdminPrefs", MODE_PRIVATE).getString("company_code", "");

        for (Employee employee : selectedEmployees) {
            String url = BASE_URL + "mark_attendance.php";

            JSONObject postData = new JSONObject();
            try {
                postData.put("employee_id", employee.getId());
                postData.put("employee_name", employee.getName());
                postData.put("branch", employee.getBranch());
                postData.put("company_code", companyCode);
                postData.put("attendance_status", status);
                postData.put("in_time", inTime);       // ✅ Correct In-Time
                postData.put("out_time", outTime);     // ✅ Correct Out-Time
                postData.put("date", selectedDate);    // ✅ Correct Date

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                        response -> Toast.makeText(this, "Attendance marked for " + employee.getName(), Toast.LENGTH_SHORT).show(),
                        error -> Toast.makeText(this, "Error marking attendance for " + employee.getName(), Toast.LENGTH_SHORT).show()
                );

                requestQueue.add(request);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error adding time for " + employee.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    // ==============================
// Add Click Listeners for Employee Items
// ==============================
    private void setupEmployeeClickListeners() {
        // OnClick - Mark Single Employee Attendance
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Employee selectedEmployee = listData.get(listGroupTitles.get(groupPosition)).get(childPosition);
            markSingleEmployeeAttendance(selectedEmployee);  // ✅ OnClick for Single Employee
            return true;
        });

        // OnLongClick - Ask for Multiple Employee Selection
        expandableListView.setOnItemLongClickListener((parent, view, position, id) -> {
            initiateMultipleEmployeeSelection();  // ✅ Show dialog for multiple employee marking
            return true;
        });
    }


    // ==============================
    // Search Functionality
    // ==============================
    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Employee");

        // Input field for search text
        final EditText input = new EditText(this);
        input.setHint("Enter Branch/Name/ID");
        builder.setView(input);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String searchText = input.getText().toString().trim();
            if (!searchText.isEmpty()) {
                searchEmployee(searchText);
            } else {
                Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    // Search logic
    private void searchEmployee(String searchText) {
        List<String> filteredBranches = new ArrayList<>();
        HashMap<String, List<Employee>> filteredData = new HashMap<>();

        for (String branch : listGroupTitles) {
            List<Employee> filteredEmployees = new ArrayList<>();
            for (Employee employee : listData.get(branch)) {
                if (branch.toLowerCase().contains(searchText.toLowerCase()) ||
                        employee.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                        employee.getId().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredEmployees.add(employee);
                }
            }
            if (!filteredEmployees.isEmpty()) {
                filteredBranches.add(branch);
                filteredData.put(branch, filteredEmployees);
            }
        }

        if (filteredBranches.isEmpty()) {
            Toast.makeText(this, "No matching employees found", Toast.LENGTH_SHORT).show();
        } else {
            adapter = new ExpandableListAdapter(this, filteredBranches, filteredData,true);
            expandableListView.setAdapter(adapter);
            Toast.makeText(this, "Search results displayed", Toast.LENGTH_SHORT).show();
        }
    }
    // ==============================
    // Download Functionality
    // ==============================
    private void showDownloadOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Download Attendance Report");

        String[] options = {"Download All Records", "Download Specific Branch"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Toast.makeText(this, "Downloading All Records...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Downloading Specific Branch...", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
