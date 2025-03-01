package com.mingmingdecoder.bhms;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentBillsAdapter extends RecyclerView.Adapter<RecentBillsAdapter.ViewHolder> {

    private List<Bill> bills;
    private Context context;
    private OnBillClickListener listener;

    public interface OnBillClickListener {
        void onBillClick(Bill bill, int position);
    }

    public RecentBillsAdapter(List<Bill> bills) {
        this.bills = bills;
    }

    public RecentBillsAdapter(List<Bill> bills, OnBillClickListener listener) {
        this.bills = bills;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_bill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bill bill = bills.get(position);
        holder.bindData(bill);
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }

    public void updateData(List<Bill> newBills) {
        this.bills = newBills;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTenantName;
        private TextView tvBillType;
        private TextView tvAmount;
        private TextView tvDate;
        private TextView tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenantName = itemView.findViewById(R.id.tv_tenant_name);
            tvBillType = itemView.findViewById(R.id.tv_bill_type);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvStatus = itemView.findViewById(R.id.tv_status);

            // Set click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onBillClick(bills.get(position), position);
                    }
                }
            });
        }

        public void bindData(Bill bill) {
    // Set tenant name
    tvTenantName.setText(bill.getTenantName());

    // Set bill type with proper capitalization
    String type = bill.getType();
    if (type != null && !type.isEmpty()) {
        type = type.substring(0, 1).toUpperCase() + type.substring(1);
        tvBillType.setText(type);

        // Set type color
        int typeColor;
        switch (bill.getType().toLowerCase()) {
            case "rent":
                typeColor = Color.rgb(76, 175, 80); // Green
                break;
            case "water":
                typeColor = Color.rgb(33, 150, 243); // Blue
                break;
            case "electric":
                typeColor = Color.rgb(255, 193, 7); // Amber
                break;
            default:
                typeColor = Color.GRAY;
                break;
        }
        tvBillType.setTextColor(typeColor);
    }

    // Format amount with Philippine Peso currency
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
    tvAmount.setText(currencyFormat.format(bill.getAmount()));

    // Format date
    SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    try {
        Date date = inputFormat.parse(bill.getDate());
        tvDate.setText(outputFormat.format(date));
    } catch (ParseException e) {
        tvDate.setText(bill.getDate());
    }

    // Set payment status
    if (bill.isPaid()) {
        tvStatus.setText("PAID");
        tvStatus.setTextColor(Color.rgb(76, 175, 80)); // Green
    } else {
        tvStatus.setText("UNPAID");
        tvStatus.setTextColor(Color.rgb(244, 67, 54)); // Red
    }
}
}
}