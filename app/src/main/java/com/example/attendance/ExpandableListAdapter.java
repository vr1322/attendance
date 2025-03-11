package com.example.attendance;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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

        if (employees == null || employees.isEmpty()) {
            TextView noEmployeesTextView = new TextView(context);
            noEmployeesTextView.setText("No employees added");
            noEmployeesTextView.setPadding(50, 10, 10, 10);
            noEmployeesTextView.setTextSize(16);
            noEmployeesTextView.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            return noEmployeesTextView;
        }

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_child, parent, false);
        }

        Employee employee = employees.get(childPosition);

        TextView nameTextView = convertView.findViewById(R.id.employee_name);
        TextView detailsTextView = convertView.findViewById(R.id.employee_details);
        CircleImageView profilePic = convertView.findViewById(R.id.profile_pic);

        nameTextView.setText(employee.getName());
        detailsTextView.setText(employee.getDesignation() + " " + employee.getId());

        // ✅ Debugging: Print Profile Pic URL
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

        return convertView;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
