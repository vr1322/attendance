package com.example.attendance;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final List<String> groupList;
    private final HashMap<String, List<Employee>> childMap;
    private final boolean isAttendanceReport;  // New flag for layout switching
    private HashMap<String, List<Attendance>> attendanceData;
    // Constructor with flag for Attendance Report
    public ExpandableListAdapter(Context context,
                                 List<String> groupList,
                                 HashMap<String, List<Employee>> childMap,
                                 HashMap<String, List<Attendance>> attendanceData,  // Corrected this line
                                 boolean isAttendanceReport) {
        this.context = context;
        this.groupList = groupList;
        this.childMap = childMap;
        this.attendanceData = attendanceData;
        this.isAttendanceReport = isAttendanceReport;  // Flag for different layout
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<Employee> employees = childMap.get(groupList.get(groupPosition));
        return (employees == null || employees.isEmpty()) ? 1 : employees.size(); // Handle empty list
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<Employee> employees = childMap.get(groupList.get(groupPosition));
        if (employees == null || employees.isEmpty()) {
            return "No employees added";
        }
        return employees.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String groupTitle = (String) getGroup(groupPosition);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        }

        TextView groupTextView = convertView.findViewById(R.id.textViewGroup);
        groupTextView.setText(groupTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        List<Employee> employees = childMap.get(groupList.get(groupPosition));

        if (employees == null || employees.isEmpty()) {
            TextView noEmployeesTextView = new TextView(context);
            noEmployeesTextView.setText("No employees added");
            noEmployeesTextView.setPadding(50, 10, 10, 10);
            noEmployeesTextView.setTextSize(16);
            noEmployeesTextView.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            return noEmployeesTextView;
        }

        // Choose Layout based on `isAttendanceReport` flag
        int layoutResource = isAttendanceReport ? R.layout.item_child_attendance : R.layout.item_child;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(layoutResource, parent, false);
        }

        Employee employee = employees.get(childPosition);

        TextView nameTextView = convertView.findViewById(R.id.employee_name);
        TextView detailsTextView = convertView.findViewById(R.id.employee_details);
        CircleImageView profilePic = convertView.findViewById(R.id.profile_pic);

        nameTextView.setText(employee.getName());
        detailsTextView.setText(employee.getDesignation() + " " + employee.getId());

        if (profilePic != null) {
            String profilePicUrl = employee.getProfilePic();
            Log.d("ProfilePicURL", "Employee: " + employee.getName() + ", URL: " + profilePicUrl);

            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                Glide.with(context)
                        .load(profilePicUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(profilePic);
            } else {
                profilePic.setImageResource(R.drawable.ic_profile);
            }
        }


        // Attendance Status (Only for Attendance Report)
        if (isAttendanceReport) {
            ImageView attendanceStatus = convertView.findViewById(R.id.attendance_status);
            String status = employee.getAttendanceStatus().trim(); // Trim spaces

            Log.d("ATTENDANCE_STATUS", "Employee: " + employee.getName() + ", Status: " + status);

            switch (status.toLowerCase()) {
                case "present":
                    Log.d("ICON_UPDATE", employee.getName() + ": Present Icon Set");
                    attendanceStatus.setImageResource(R.drawable.ic_p);
                    break;
                case "absent":
                    Log.d("ICON_UPDATE", employee.getName() + ": Absent Icon Set");
                    attendanceStatus.setImageResource(R.drawable.ic_a);
                    break;
                case "half day":
                    attendanceStatus.setImageResource(R.drawable.ic_hf);
                    break;
                case "overtime":
                    attendanceStatus.setImageResource(R.drawable.ic_ot);
                    break;
                default:
                    Log.d("ICON_UPDATE", employee.getName() + ": Default (Not Marked) Icon Set");
                    attendanceStatus.setImageResource(R.drawable.ic_nm); // Default icon
                    break;
            }
        }
        return convertView;
    }

    public void resetAttendanceIcons() {
        for (String branch : attendanceData.keySet()) {
            for (Attendance employee : attendanceData.get(branch)) {
                employee.setAttendanceStatus("");  // Clear status
            }
        }
        notifyDataSetChanged(); // Refresh UI
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
