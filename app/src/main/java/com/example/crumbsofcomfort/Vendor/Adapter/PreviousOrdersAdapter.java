package com.example.crumbsofcomfort.Vendor.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.Vendor.Model.Feedback;
import com.example.crumbsofcomfort.Vendor.Model.OrderModel;
import com.example.crumbsofcomfort.Vendor.Model.ProductItemModel;
import com.example.crumbsofcomfort.databinding.ViewholderItemPreviousOrderBinding;

import java.util.List;

public class PreviousOrdersAdapter extends RecyclerView.Adapter<PreviousOrdersAdapter.OrderViewHolder> {

    private List<OrderModel> orders;

    public PreviousOrdersAdapter(List<OrderModel> orders) {
        this.orders = orders;
    }

    public void updateList(List<OrderModel> newList) {
        this.orders = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderItemPreviousOrderBinding binding = ViewholderItemPreviousOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = orders.get(position);
        ProductItemModel item = order.getItem();

        if (item != null && item.getImageUrl() != null) {
            Glide.with(holder.binding.getRoot().getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.cam)
                    .into(holder.binding.orderImage);
        } else {
            holder.binding.orderImage.setImageResource(R.drawable.cam);
        }

        holder.binding.tvOrderItemName.setText(item.getProductName());
        holder.binding.tvOrderDate.setText("Delivered on: " + order.getDate());
        holder.binding.tvOrderItemDeliverTime.setText("Delivery time: " + order.getDeliveryTime());
        holder.binding.tvOrderStatus.setText(order.getStatus());
        holder.binding.tvOrderPrice.setText("₹" + order.getTotalAmount());

        if (order.getFeedback() != null) {
            Feedback feedback = order.getFeedback();

            holder.binding.tvFeedbackRating.setVisibility(View.VISIBLE);
            holder.binding.tvFeedbackRating.setText("Rating: " + feedback.getRating());

            if (feedback.getComment() != null && !feedback.getComment().isEmpty()) {
                holder.binding.tvFeedbackComment.setVisibility(View.VISIBLE);
                holder.binding.tvFeedbackComment.setText("Comment: " + feedback.getComment());
            } else {
                holder.binding.tvFeedbackComment.setVisibility(View.GONE);
            }
        } else {
            holder.binding.tvFeedbackRating.setVisibility(View.GONE);
            holder.binding.tvFeedbackComment.setVisibility(View.GONE);}

    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ViewholderItemPreviousOrderBinding binding;

        public OrderViewHolder(ViewholderItemPreviousOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
