package com.example.crumbsofcomfort.User.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.User.Activity.FeedbackActivity;
import com.example.crumbsofcomfort.User.Activity.UserDeliveryTrackActivity;
import com.example.crumbsofcomfort.User.Model.UserOrderModel;
import com.example.crumbsofcomfort.databinding.ViewholderItemMyorderBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserOrdersAdapter extends RecyclerView.Adapter<UserOrdersAdapter.OrderViewHolder> {

    private final Context context;
    private final List<UserOrderModel> orderList;

    public UserOrdersAdapter(Context context, List<UserOrderModel> orderList) {
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
        if (order.getStatus().equals("Delivered") && order.getFeedBack() == null) {
            holder.binding.btnGiveFeedback.setVisibility(View.VISIBLE);
            holder.binding.btnGiveFeedback.setOnClickListener(v -> {
                Intent intent = new Intent(context, FeedbackActivity.class);
                intent.putExtra("shopId", order.getShopId());
                intent.putExtra("orderId", order.getOrderId());
                intent.putExtra("itemId", order.getItemId());
                context.startActivity(intent);
            });
        } else {
            holder.binding.btnGiveFeedback.setVisibility(View.GONE); // hide for non-delivered
        }

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
        DatabaseReference feedbackRef = FirebaseDatabase.getInstance()
                .getReference("Feedbacks")
                .child(order.getOrderId());

        feedbackRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.binding.btnGiveFeedback.setVisibility(View.GONE);
                } else {
                    holder.binding.btnGiveFeedback.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.binding.btnGiveFeedback.setVisibility(View.VISIBLE);
            }
        });

        if (order.getStatus().equals("Out for Delivery") || order.getStatus().equals("Prepared")) {
            DatabaseReference deliveriesRef = FirebaseDatabase.getInstance().getReference("Deliveries");
            deliveriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean orderFound = false;
                    for (DataSnapshot partnerSnap : snapshot.getChildren()) {
                        DataSnapshot ordersSnapshot = partnerSnap.child("Orders");
                        if (ordersSnapshot.hasChild(order.getOrderId())) {
                            orderFound = true;
                            break;
                        }
                    }

                    if (orderFound) {
                        holder.binding.btnTrackDelivery.setVisibility(View.VISIBLE);
                        holder.binding.btnTrackDelivery.setOnClickListener(v -> {
                            Intent intent = new Intent(context, UserDeliveryTrackActivity.class);
                            intent.putExtra("orderId", order.getOrderId());
                            intent.putExtra("shopId", order.getShopId());
                            context.startActivity(intent);
                        });
                    } else {
                        holder.binding.btnTrackDelivery.setVisibility(View.GONE);
                        // OPTIONAL: Show a toast if you want to notify the user (if this is actually needed)
                        // Toast.makeText(context, "Delivery partner not assigned yet", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.binding.btnTrackDelivery.setVisibility(View.GONE);
                }
            });
        } else {
            holder.binding.btnTrackDelivery.setVisibility(View.GONE);
        }
        if (order.getStatus().equals("Pending") || order.getStatus().equals("Prepared")) {
            holder.binding.btnCancelOrder.setVisibility(View.VISIBLE);
        } else {
            holder.binding.btnCancelOrder.setVisibility(View.GONE);
        }

        holder.binding.btnCancelOrder.setOnClickListener(v -> {
            String status = order.getStatus();
            if (status.equals("Pending") || status.equals("Prepared")) {
                new AlertDialog.Builder(context)
                        .setTitle("Cancel Order")
                        .setMessage("Are you sure you want to cancel this order?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            FirebaseDatabase.getInstance().getReference("Vendors")
                                    .child(order.getShopId())
                                    .child("Orders")
                                    .child(order.getOrderId())
                                    .child("status")
                                    .setValue("Cancelled by User")
                                    .addOnSuccessListener(unused -> {
                                                Toast.makeText(context, "Order cancelled", Toast.LENGTH_SHORT).show();
                                                int pos = holder.getAdapterPosition();
                                                if (pos != RecyclerView.NO_POSITION) {
                                                    orderList.remove(pos);
                                                    notifyItemRemoved(pos);
                                                }
                                            })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else {
                Toast.makeText(context, "Cannot cancel this order", Toast.LENGTH_SHORT).show();
            }
        });

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
