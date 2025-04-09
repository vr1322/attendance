package com.example.attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class OvertimeRequestsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OvertimeAdapter adapter;
    private List<OvertimeRequest> overtimeRequests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overtime_requests);

        recyclerView = findViewById(R.id.recyclerOvertimeRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with click listeners
        adapter = new OvertimeAdapter(this, overtimeRequests, new OvertimeAdapter.OnActionClickListener() {
            @Override
            public void onApprove(int id) {
                updateStatus(id, "Approved");
            }

            @Override
            public void onReject(int id) {
                updateStatus(id, "Rejected");
            }
        });

        recyclerView.setAdapter(adapter);

        fetchOvertimeRequests();
    }

    private void fetchOvertimeRequests() {
        // Dummy Data (Replace with API call)
        overtimeRequests.add(new OvertimeRequest(1, "John Doe", 2.5, 100, 250));
        overtimeRequests.add(new OvertimeRequest(2, "Jane Smith", 3.0, 120, 360));
        overtimeRequests.add(new OvertimeRequest(3, "Mike Johnson", 1.5, 110, 165));

        adapter.notifyDataSetChanged();  // Refresh RecyclerView
    }

    private void updateStatus(int id, String status) {
        Toast.makeText(this, "Request ID " + id + " " + status, Toast.LENGTH_SHORT).show();
        // TODO: Make an API call to update status in the database
    }
}
