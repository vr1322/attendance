package com.example.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OvertimeAdapter extends RecyclerView.Adapter<OvertimeAdapter.ViewHolder> {
    private final Context context;
    private final List<OvertimeRequest> overtimeList;
    private final OnActionClickListener listener;

    public interface OnActionClickListener {
        void onApprove(int id);
        void onReject(int id);
    }

    public OvertimeAdapter(Context context, List<OvertimeRequest> overtimeList, OnActionClickListener listener) {
        this.context = context;
        this.overtimeList = overtimeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_overtime_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OvertimeRequest request = overtimeList.get(position);

        holder.tvEmployeeName.setText(request.getEmployeeName());
        holder.tvOvertimeHours.setText(String.format("Hours: %.1f", request.getOvertimeHours()));
        holder.tvOvertimeRate.setText(String.format("Rate: ₹%.2f", request.getOvertimeRate()));
        holder.tvTotalAmount.setText(String.format("Total: ₹%.2f", request.getTotalAmount()));

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(request.getId()));
        holder.btnReject.setOnClickListener(v -> listener.onReject(request.getId()));
    }

    @Override
    public int getItemCount() {
        return overtimeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployeeName, tvOvertimeHours, tvOvertimeRate, tvTotalAmount;
        Button btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvOvertimeHours = itemView.findViewById(R.id.tvOvertimeHours);
            tvOvertimeRate = itemView.findViewById(R.id.tvOvertimeRate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
