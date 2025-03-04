package com.example.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder> {
    private Context context;
    private List<String> currencyList;
    private OnCurrencySelectedListener listener;

    public interface OnCurrencySelectedListener {
        void onCurrencySelected(String currency);
    }

    public CurrencyAdapter(Context context, List<String> currencyList, OnCurrencySelectedListener listener) {
        this.context = context;
        this.currencyList = currencyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_currency, parent, false);
        return new CurrencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyViewHolder holder, int position) {
        String currency = currencyList.get(position);
        holder.currencyText.setText(currency);
        holder.itemView.setOnClickListener(v -> listener.onCurrencySelected(currency));
    }

    @Override
    public int getItemCount() {
        return currencyList.size();
    }

    static class CurrencyViewHolder extends RecyclerView.ViewHolder {
        TextView currencyText;

        CurrencyViewHolder(View itemView) {
            super(itemView);
            currencyText = itemView.findViewById(R.id.currency_text);
        }
    }
}
