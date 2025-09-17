package com.example.attendance;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewReportsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ViewReportsAdapter adapter;
    List<AllReportsModel> reports = new ArrayList<>();
    private static final String GET_REPORTS_URL = "https://devonix.io/ems_api/view_all_reports.php";
    private static final String TAG = "ViewReportsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reports);

        recyclerView = findViewById(R.id.recyclerViewReports);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ViewReportsAdapter(this, reports);
        recyclerView.setAdapter(adapter);

        loadReports();
    }

    private void loadReports() {
        try {
            // POST params (empty to fetch all reports)
            JSONObject params = new JSONObject();

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, GET_REPORTS_URL, params,
                    response -> {
                        try {
                            if (response.getString("status").equals("success")) {
                                reports.clear();
                                JSONArray arr = response.getJSONArray("reports");
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject obj = arr.getJSONObject(i);

                                    // Read images array from "images" key
                                    List<String> images = new ArrayList<>();
                                    JSONArray imgArr = obj.optJSONArray("images");
                                    if (imgArr != null) {
                                        for (int j = 0; j < imgArr.length(); j++) {
                                            images.add(imgArr.getString(j));
                                        }
                                    }

                                    reports.add(new AllReportsModel(
                                            obj.getString("employee_name"),
                                            obj.getString("employee_id"),
                                            obj.optString("branch_name", ""),
                                            obj.getString("title"),       // PHP key 'title'
                                            obj.getString("description"), // PHP key 'description'
                                            images,
                                            obj.getString("date")         // PHP key 'date'
                                    ));
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Parse error: " + e.getMessage());
                        }
                    },
                    error -> Toast.makeText(this, "Server error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            );

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);

        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }
}
