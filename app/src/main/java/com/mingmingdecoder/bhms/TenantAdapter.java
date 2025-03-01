package com.mingmingdecoder.bhms;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// Tenant Adapter
public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.TenantViewHolder> {
    private Context context;
    private List<Tenant> tenantList;
    private OnTenantClickListener listener;
    
    public interface OnTenantClickListener {
        void onTenantClick(Tenant tenant, int position);
    }
    
    public TenantAdapter(Context context, List<Tenant> tenantList, OnTenantClickListener listener) {
        this.context = context;
        this.tenantList = tenantList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public TenantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tenant, parent, false);
        return new TenantViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TenantViewHolder holder, int position) {
        Tenant tenant = tenantList.get(position);
        
        holder.nameText.setText(tenant.getName());
        holder.contactText.setText(tenant.getContact());
        holder.roomText.setText(tenant.getRoom());
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTenantClick(tenant, holder.getAdapterPosition());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return tenantList.size();
    }
    
    static class TenantViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, contactText, roomText;
        
        public TenantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            contactText = itemView.findViewById(R.id.contactText);
            roomText = itemView.findViewById(R.id.roomText);
        }
    }
}

// Bill Adapter
 class BillAdapter extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {
    private Context context;
    private List<Bill> billList;
    private OnBillClickListener listener;
    
    public interface OnBillClickListener {
        void onBillClick(Bill bill, int position);
    }
    
    public BillAdapter(Context context, List<Bill> billList, OnBillClickListener listener) {
        this.context = context;
        this.billList = billList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill bill = billList.get(position);
        
        holder.tenantText.setText(bill.getTenantName());
        holder.amountText.setText(String.format("₱%.2f", bill.getAmount()));
        holder.dateText.setText(bill.getDate());
        
        if (bill.isPaid()) {
            holder.statusText.setText("PAID");
            holder.statusText.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            holder.statusText.setText("UNPAID");
            holder.statusText.setTextColor(context.getResources().getColor(R.color.red));
        }
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBillClick(bill, holder.getAdapterPosition());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return billList.size();
    }
    
    static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView tenantText, amountText, dateText, statusText;
        
        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            tenantText = itemView.findViewById(R.id.tenantText);
            amountText = itemView.findViewById(R.id.amountText);
            dateText = itemView.findViewById(R.id.dateText);
            statusText = itemView.findViewById(R.id.statusText);
        }
    }
}