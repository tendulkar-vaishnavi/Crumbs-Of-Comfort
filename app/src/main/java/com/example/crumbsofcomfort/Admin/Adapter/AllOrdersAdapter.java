package com.example.crumbsofcomfort.Admin.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crumbsofcomfort.Admin.Model.AllOrderModel;
import com.example.crumbsofcomfort.databinding.ItemOrderAdminBinding;

import java.util.List;

public class AllOrdersAdapter extends RecyclerView.Adapter<AllOrdersAdapter.OrderViewHolder> {

    private final List<AllOrderModel> orders;

    public AllOrdersAdapter(List<AllOrderModel> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderAdminBinding binding = ItemOrderAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        AllOrderModel order = orders.get(position);

        holder.binding.tvShopName.setText(order.getShopName());
        holder.binding.tvProductName.setText(order.getProductName());
        holder.binding.tvQuantity.setText("Qty: " + order.getQuantity() + " • " + order.getWeight());
        holder.binding.tvPrice.setText("₹" + order.getTotalAmount());
        holder.binding.tvStatus.setText(order.getStatus());
        holder.binding.tvDateTime.setText(order.getDate() + " • " + order.getDeliveryTime());
        holder.binding.tvUserName.setText("User: " + order.getUserName());
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        ItemOrderAdminBinding binding;

        public OrderViewHolder(ItemOrderAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
