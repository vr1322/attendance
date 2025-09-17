package com.example.attendance;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    private List<ReportModel> reports;

    public ReportAdapter(List<ReportModel> reports) {
        this.reports = reports;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ReportModel report = reports.get(position);
        holder.title.setText(report.getTitle());
        holder.description.setText(report.getDescription());
        holder.date.setText(report.getDate());
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date;
        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.reportTitle);
            description = itemView.findViewById(R.id.reportDesc);
            date = itemView.findViewById(R.id.reportDate);
        }
    }
}
