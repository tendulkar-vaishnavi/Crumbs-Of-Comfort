package com.example.crumbsofcomfort.Vendor.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.Vendor.Activity.OrdersActivity;
import com.example.crumbsofcomfort.Vendor.Model.OrderModel;
import com.example.crumbsofcomfort.databinding.ViewholderItemOrderBinding;
import com.google.gson.Gson;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private List<OrderModel> orderList;

    public OrdersAdapter(List<OrderModel> orderList) {
        this.orderList = orderList;
    }

    public void updateList(List<OrderModel> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderItemOrderBinding binding = ViewholderItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = orderList.get(position);
/*
        // Access product data from nested item object
        if (order.getItem() != null) {
            holder.binding.productId.setText("Product ID: " + order.getItem().getProductId());
            holder.binding.productName.setText(order.getItem().getProductName());
            holder.binding.orderWeight.setText("Weight: " + order.getItem().getWeight());
            holder.binding.orderQuantity.setText("Quantity: " + order.getItem().getQuantity());

            Glide.with(holder.itemView.getContext())
                    .load(order.getItem().getImageUrl())
                    .into(holder.binding.imgOrder);
        } else {
            holder.binding.productName.setText("Unknown product");
        }

        // Order-level fields
        holder.binding.deliveryDate.setText("Delivery Date: " + order.getDeliveryDate());
        holder.binding.deliveryTime.setText("Delivery Time: " + order.getDeliveryTime());
        holder.binding.deliveryAddress.setText("Address: " + order.getDeliveryAddress());
        holder.binding.orderStatus.setText("Status: " + order.getStatus());*/
        holder.binding.productName.setText(order.getItem().getProductName());
        holder.binding.orderStatus.setText("Status: " + order.getStatus());

        holder.binding.btnViewOrder.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, OrdersActivity.class);
            intent.putExtra("shopId", order.getShopId());
            intent.putExtra("orderId", order.getOrderId());
            intent.putExtra("status", order.getStatus());
            intent.putExtra("order", new Gson().toJson(order));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ViewholderItemOrderBinding binding;

        public OrderViewHolder(@NonNull ViewholderItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
