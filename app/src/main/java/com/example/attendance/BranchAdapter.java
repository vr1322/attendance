package com.example.attendance;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BranchAdapter extends RecyclerView.Adapter<BranchAdapter.BranchViewHolder> {
    private List<Branch> branchList;
    private Context context;
    private OnBranchActionListener listener;

    // Interface for Edit & Delete actions
    public interface OnBranchActionListener {
        void onDeleteBranch(int position, int branchId);
        void onEditBranch(Branch branch);
    }

    // Constructor
    public BranchAdapter(Context context, OnBranchActionListener listener) {
        this.branchList = new ArrayList<>();
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BranchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.branch_item, parent, false);
        return new BranchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchViewHolder holder, int position) {
        Branch branch = branchList.get(position);

        // Set branch details
        holder.branchName.setText(branch.getName());
        holder.branchAddress.setText(branch.getAddress());
        holder.branchLatLng.setText("Lat: " + branch.getLatitude() + ", Lng: " + branch.getLongitude());
        holder.branchRadius.setText(branch.getRadius() + " Meters Radius For Attendance");

        // Open Google Maps when the map button is clicked
        holder.openMapButton.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:" + branch.getLatitude() + "," + branch.getLongitude() + "?q=" + Uri.encode(branch.getAddress()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            context.startActivity(mapIntent);
        });

        // Open EditBranchActivity with branch details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditBranch(branch);
            }
        });

        // Three-dot menu for Edit & Delete options
        holder.menuOptions.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.menuOptions);
            popupMenu.inflate(R.menu.branch_menu);

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.edit_branch) {
                    if (listener != null) {
                        listener.onEditBranch(branch);
                    }
                    return true;
                } else if (item.getItemId() == R.id.delete_branch) {
                    if (listener != null) {
                        listener.onDeleteBranch(position, branch.getId());
                    }
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return branchList.size();
    }

    public static class BranchViewHolder extends RecyclerView.ViewHolder {
        TextView branchName, branchAddress, branchLatLng, branchRadius;
        ImageView openMapButton, menuOptions;

        public BranchViewHolder(@NonNull View itemView) {
            super(itemView);
            branchName = itemView.findViewById(R.id.branchName);
            branchAddress = itemView.findViewById(R.id.branchAddress);
            branchLatLng = itemView.findViewById(R.id.branchLatLng);
            branchRadius = itemView.findViewById(R.id.branchRadius);
            openMapButton = itemView.findViewById(R.id.openMapButton);
            menuOptions = itemView.findViewById(R.id.menuOptions);
        }
    }

    // ✅ Update branches dynamically
    public void updateBranches(List<Branch> newBranches) {
        this.branchList.clear();
        this.branchList.addAll(newBranches);
        notifyDataSetChanged();
    }

    // ✅ Remove branch from list
    public void removeBranch(int position) {
        if (position >= 0 && position < branchList.size()) {
            branchList.remove(position);
            notifyItemRemoved(position);
        }
    }
}
