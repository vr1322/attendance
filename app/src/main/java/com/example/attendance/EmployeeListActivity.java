package com.example.attendance;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EmployeeListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EmployeeAdapter adapter;
    private ArrayList<Employee> employeeList;
    private ImageView backButton, searchButton, downloadButton;
    private TextView empListTxt;

    private static final String FETCH_EMPLOYEES_URL = "https://devonix.io/ems_api/fetch_employees.php"; // API URL

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);

        recyclerView = findViewById(R.id.expandableListView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        backButton = findViewById(R.id.back);
        searchButton = findViewById(R.id.search);
        downloadButton = findViewById(R.id.download);
        empListTxt = findViewById(R.id.emp_list_txt);

        employeeList = new ArrayList<>();
        adapter = new EmployeeAdapter(employeeList); // ✅ Corrected Adapter Initialization
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchEmployees(); // Load data

        // Pull to refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchEmployees();
            swipeRefreshLayout.setRefreshing(false);
        });

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Search button
        searchButton.setOnClickListener(v -> Toast.makeText(this, "Search clicked!", Toast.LENGTH_SHORT).show());

        // Download button
        downloadButton.setOnClickListener(v -> Toast.makeText(this, "Download clicked!", Toast.LENGTH_SHORT).show());
    }

    private void fetchEmployees() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, FETCH_EMPLOYEES_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        employeeList.clear();

                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                Employee employee = new Employee(
                                        obj.getString("name"),
                                        obj.getString("designation"),
                                        obj.getString("id"),
                                        false,  // Default for isParkingAvailable
                                        false,  // Default for isParkingAssigned
                                        obj.getString("phone"),
                                        obj.getString("profile_pic"),
                                        "N/A",  // Default for attendanceStatus
                                        obj.getString("branch")
                                );

                                employeeList.add(employee);
                            }
                            adapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(EmployeeListActivity.this, "Parsing error", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EmployeeListActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                        Log.e("API_ERROR", error.toString());
                    }
                });

        queue.add(request);
    }
}
