package com.example.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ViewReportsAdapter extends RecyclerView.Adapter<ViewReportsAdapter.ReportViewHolder> {

    private Context context;
    private List<AllReportsModel> reportList;

    public ViewReportsAdapter(Context context, List<AllReportsModel> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_all_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        AllReportsModel report = reportList.get(position);

        holder.tvEmployeeName.setText("Name: " + report.getEmployeeName());
        holder.tvEmployeeId.setText("ID: " + report.getEmployeeId());
        holder.tvBranchName.setText("Branch: " + report.getBranchName());
        holder.tvTitle.setText("Title: " + report.getTitle());
        holder.tvDesc.setText("Description: " + report.getDescription());
        holder.tvDate.setText("Date: " + report.getDate());

        // Images RecyclerView
        if (report.getImages() != null && !report.getImages().isEmpty()) {
            holder.rvImages.setVisibility(View.VISIBLE);
            holder.rvImages.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            ImagesAdapter imagesAdapter = new ImagesAdapter(context, report.getImages());
            holder.rvImages.setAdapter(imagesAdapter);
        } else {
            holder.rvImages.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployeeName, tvEmployeeId, tvBranchName, tvTitle, tvDesc, tvDate;
        RecyclerView rvImages;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvEmployeeId = itemView.findViewById(R.id.tvEmployeeId);
            tvBranchName = itemView.findViewById(R.id.tvBranchName);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvDate = itemView.findViewById(R.id.tvDate);
            rvImages = itemView.findViewById(R.id.rvImages);
        }
    }
}
