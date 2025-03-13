package com.example.attendance;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.HashMap;
import java.util.HashSet;

public class MarkAttendanceActivity extends AppCompatActivity {

    MaterialCalendarView materialCalendarView;
    HashMap<String, String[]> attendanceData; // Store Date and [Status, InTime, OutTime]

    HashSet<CalendarDay> presentDates = new HashSet<>();
    HashSet<CalendarDay> absentDates = new HashSet<>();
    HashSet<CalendarDay> halfDayDates = new HashSet<>();
    HashSet<CalendarDay> overtimeDates = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        materialCalendarView = findViewById(R.id.calendarGrid);

        // Sample Attendance Data (Date Format: "dd/MM/yyyy")
        attendanceData = new HashMap<>();
        attendanceData.put("05/01/2025", new String[]{"Present", "10:00 AM", "07:00 PM"});
        attendanceData.put("07/01/2025", new String[]{"Absent", "N/A", "N/A"});
        attendanceData.put("09/01/2025", new String[]{"Overtime", "09:00 AM", "08:00 PM"});
        attendanceData.put("13/01/2025", new String[]{"Absent", "N/A", "N/A"});
        attendanceData.put("15/01/2025", new String[]{"Half Day", "10:00 AM", "02:00 PM"});

        // Add Dates to Color-Coded Sets
        for (String date : attendanceData.keySet()) {
            String[] details = attendanceData.get(date);
            String status = details[0];

            String[] splitDate = date.split("/");
            int day = Integer.parseInt(splitDate[0]);
            int month = Integer.parseInt(splitDate[1]) - 1;  // Month is zero-indexed
            int year = Integer.parseInt(splitDate[2]);

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

        // Add Custom Decorators for Each Attendance Type
        materialCalendarView.addDecorator(new CustomDecorator(Color.GREEN, presentDates));  // Present
        materialCalendarView.addDecorator(new CustomDecorator(Color.RED, absentDates));     // Absent
        materialCalendarView.addDecorator(new CustomDecorator(Color.YELLOW, halfDayDates)); // Half Day
        materialCalendarView.addDecorator(new CustomDecorator(Color.BLUE, overtimeDates));  // Overtime

        // Date Click Listener for Showing Details
        materialCalendarView.setOnDateChangedListener((widget, date, selected) -> {
            String selectedDate = String.format("%02d/%02d/%d",
                    date.getDay(), date.getMonth() + 1, date.getYear());

            if (attendanceData.containsKey(selectedDate)) {
                String[] details = attendanceData.get(selectedDate);

                String status = details[0];
                String inTime = details[1];
                String outTime = details[2];

                String message = "Status: " + status +
                        "\nIn-Time: " + inTime +
                        "\nOut-Time: " + outTime;

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "N/A", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
