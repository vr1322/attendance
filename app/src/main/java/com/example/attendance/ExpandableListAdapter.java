package com.example.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final List<String> groupList;
    private final HashMap<String, List<Employee>> childMap;

    public ExpandableListAdapter(Context context, List<String> groupList, HashMap<String, List<Employee>> childMap) {
        this.context = context;
        this.groupList = groupList;
        this.childMap = childMap;
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
            return "No employees added"; // Return message if no employees exist
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

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_child, parent, false);
        }

        if (employees == null || employees.isEmpty()) {
            // If no employees, show the "No employees added" message in the child view
            TextView noEmployeesTextView = new TextView(context);
            noEmployeesTextView.setText("No employees added");
            noEmployeesTextView.setPadding(50, 10, 10, 10); // Optional styling for the message
            noEmployeesTextView.setTextSize(16); // Optional: You can style the message further here
            noEmployeesTextView.setTextColor(context.getResources().getColor(android.R.color.darker_gray)); // Styling for message
            return noEmployeesTextView;
        } else {
            // Otherwise, show employee details
            Employee employee = employees.get(childPosition);
            TextView nameTextView = convertView.findViewById(R.id.employee_name);
            TextView detailsTextView = convertView.findViewById(R.id.employee_details);
            TextView parkingTextView = convertView.findViewById(R.id.parking_status);

            nameTextView.setText(employee.getName());
            detailsTextView.setText(employee.getDesignation() + " " + employee.getId());
            parkingTextView.setVisibility(employee.isParkingAvailable() ? View.VISIBLE : View.GONE);

            return convertView;
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
