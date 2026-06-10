package com.example.crumbsofcomfort.Delivery.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crumbsofcomfort.Delivery.Activity.DeliveryActivity;
import com.example.crumbsofcomfort.Delivery.Activity.TrackDeliveryActivity;
import com.example.crumbsofcomfort.Delivery.Model.DeliveryOrderModel;
import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.databinding.ItemDeliveryOrderBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class DeliveryOrdersAdapter extends RecyclerView.Adapter<DeliveryOrdersAdapter.OrderViewHolder> {

    private Context context;
    private List<DeliveryOrderModel> ordersList;
    public static final int MODE_ASSIGNED = 0;
    public static final int MODE_AVAILABLE = 1;
    public static final int MODE_DELIVERED = 2;
    private int displayMode = MODE_ASSIGNED;
    private DeliveryActivity activity;


    public DeliveryOrdersAdapter(DeliveryActivity activity, List<DeliveryOrderModel> ordersList, int mode) {
        this.activity = activity;
        this.context = activity;
        this.ordersList = ordersList;
        this.displayMode = mode;
    }

    public void setDisplayMode(int mode) {
        this.displayMode = mode;
        notifyDataSetChanged();
    }

    public void updateList(List<DeliveryOrderModel> updatedList, int mode) {
        this.ordersList = updatedList;
        this.displayMode = mode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemDeliveryOrderBinding binding = ItemDeliveryOrderBinding.inflate(inflater, parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        DeliveryOrderModel order = ordersList.get(position);
        ItemDeliveryOrderBinding b = holder.binding;

        b.textOrderId.setText("Order ID: " + order.getOrderId());
        b.textShopName.setText("Shop: " + order.getShopName());
        b.textShopAddress.setText("PickUp: " + order.getShopAddress());
        b.textUserName.setText("Customer: " + order.getUserName());
        b.textAddress.setText("Address: " + order.getUserAddress());
        b.textDateTime.setText("Delivery: " + order.getDate() + " at " + order.getDeliveryTime());
        b.textDeliveryStatus.setText("Status: " + order.getStatus());
        b.textUserPhone.setText("Phone: " + (order.getUserPhone() != null ? order.getUserPhone() : "N/A"));


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.delivery_status_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        b.spinnerStatus.setAdapter(adapter);

        int spinnerPosition = adapter.getPosition(order.getStatus() != null ? order.getStatus() : "");
        b.spinnerStatus.setSelection(spinnerPosition);

        if (displayMode == MODE_AVAILABLE) {
            b.acceptButton.setVisibility(View.VISIBLE);
            b.acceptButton.setEnabled(true);
            b.spinnerStatus.setEnabled(false);

            b.acceptButton.setOnClickListener(v -> assignOrderToDeliveryPartner(order));

        } else if (displayMode == MODE_ASSIGNED) {
            b.acceptButton.setVisibility(View.GONE);
            b.spinnerStatus.setEnabled(true);

        } else if (displayMode == MODE_DELIVERED) {
            b.acceptButton.setVisibility(View.GONE);
            b.spinnerStatus.setEnabled(false);
        }
        String currentDeliveryUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if ((order.getStatus().equals("Prepared") || order.getStatus().equals("Out for Delivery"))
                && order.getAssignedTo() != null
                && order.getAssignedTo().equals(currentDeliveryUid)) {

            holder.binding.btnTrackDelivery.setVisibility(View.VISIBLE);
            holder.binding.btnTrackDelivery.setOnClickListener(v -> {

                assignOrderToDeliveryPartner(order);

                Intent intent = new Intent(context, TrackDeliveryActivity.class);
                intent.putExtra("userUid", order.getuId());
                intent.putExtra("deliveryUid", order.getAssignedTo());
                context.startActivity(intent);
            });

        } else {
            holder.binding.btnTrackDelivery.setVisibility(View.GONE);
        }



        b.spinnerStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                String selectedStatus = parent.getItemAtPosition(pos).toString();
                if (displayMode != MODE_DELIVERED && !selectedStatus.equals(order.getStatus())) {
                    updateOrderStatus(order, selectedStatus);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // no-op
            }
        });
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ItemDeliveryOrderBinding binding;

        public OrderViewHolder(ItemDeliveryOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private void assignOrderToDeliveryPartner(DeliveryOrderModel order) {
        String deliveryPartnerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference vendorOrderRef = FirebaseDatabase.getInstance().getReference()
                .child("Vendors")
                .child(order.getShopId())
                .child("Orders")
                .child(order.getOrderId());

        vendorOrderRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String shopAddress = snapshot.child("shopAddress").getValue(String.class);
                String userAddress = snapshot.child("userAddress").getValue(String.class);

                order.setAssignedTo(deliveryPartnerId);
                order.setStatus("Out for Delivery");
                order.setAvailable(false);
                order.setShopAddress(shopAddress != null ? shopAddress : "");
                order.setUserAddress(userAddress != null ? userAddress : "");

                long timestamp = System.currentTimeMillis();

                vendorOrderRef.child("assignedTo").setValue(deliveryPartnerId);
                vendorOrderRef.child("deliveryPartnerId").setValue(deliveryPartnerId);
                vendorOrderRef.child("deliveryAssignedTime").setValue(timestamp);
                vendorOrderRef.child("status").setValue("Out for Delivery");
                vendorOrderRef.child("available").setValue(false);

                DatabaseReference deliveryOrderRef = FirebaseDatabase.getInstance().getReference()
                        .child("Deliveries")
                        .child(deliveryPartnerId)
                        .child("Orders")
                        .child(order.getOrderId());

                deliveryOrderRef.setValue(order)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Order accepted!", Toast.LENGTH_SHORT).show();

                            String shopId = order.getShopId();
                            String orderId = order.getOrderId();
                            if (context instanceof DeliveryActivity) {
                                ((DeliveryActivity) context).onOrderAccepted(orderId,shopId);
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Failed to assign order", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(context, "Order not found in vendor node", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to fetch order details", Toast.LENGTH_SHORT).show();
        });
    }
    private void updateOrderStatus(DeliveryOrderModel order, String newStatus) {
        String deliveryPartnerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference vendorOrderRef = FirebaseDatabase.getInstance().getReference()
                .child("Vendors")
                .child(order.getShopId())
                .child("Orders")
                .child(order.getOrderId());

        DatabaseReference deliveryOrderRef = FirebaseDatabase.getInstance().getReference()
                .child("Deliveries")
                .child(deliveryPartnerId)
                .child("Orders")
                .child(order.getOrderId());

        vendorOrderRef.child("status").setValue(newStatus);
        deliveryOrderRef.child("status").setValue(newStatus);

        Toast.makeText(context, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
    }
}