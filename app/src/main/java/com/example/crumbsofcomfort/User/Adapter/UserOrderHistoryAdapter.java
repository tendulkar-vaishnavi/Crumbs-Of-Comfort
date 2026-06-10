package com.example.crumbsofcomfort.User.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.User.Model.UserOrderModel;
import com.example.crumbsofcomfort.databinding.ViewholderItemMyorderBinding;

import java.util.List;

public class UserOrderHistoryAdapter extends RecyclerView.Adapter<UserOrderHistoryAdapter.OrderViewHolder> {

    private final Context context;
    private final List<UserOrderModel> orderList;

    public UserOrderHistoryAdapter(Context context, List<UserOrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewholderItemMyorderBinding binding = ViewholderItemMyorderBinding.inflate(inflater, parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        UserOrderModel order = orderList.get(position);

        holder.binding.txtItemName.setText(order.getProductName());
        holder.binding.txtShopName.setText("Shop: " + order.getShopName());
        holder.binding.txtSize.setText("Size: " + order.getWeight());
        holder.binding.txtQuantity.setText("Quantity: " + order.getQuantity());
        holder.binding.txtStatus.setText("Status: " + order.getStatus());
        holder.binding.txtDeliveryDate.setText("Delivery Date: " + order.getDeliveryTime());
        holder.binding.txtDeliverytime.setText("Delivery time: " + order.getDate());
        holder.binding.txtPrice.setText("₹ " + order.getTotalAmount());

        Glide.with(context)
                .load(order.getImageUrl())
                .placeholder(com.example.crumbsofcomfort.R.drawable.cam)
                .into(holder.binding.orderImage);

        // Hide the feedback button completely in history
        holder.binding.btnGiveFeedback.setVisibility(View.GONE);

        // Optional status color
        switch (order.getStatus()) {
            case "Prepared":
                holder.binding.txtStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "Out for Delivery":
                holder.binding.txtStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "Delivered":
                holder.binding.txtStatus.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                break;
            default:
                holder.binding.txtStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        ViewholderItemMyorderBinding binding;

        public OrderViewHolder(@NonNull ViewholderItemMyorderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
