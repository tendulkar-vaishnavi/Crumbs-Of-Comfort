package com.example.crumbsofcomfort.Vendor.Activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.crumbsofcomfort.Vendor.Model.OrderModel;
import com.example.crumbsofcomfort.Vendor.Adapter.OrdersAdapter;
import com.example.crumbsofcomfort.databinding.ActivityAllRecentordersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AllRecentOrdersActivity extends AppCompatActivity {
    private ActivityAllRecentordersBinding binding;
    private OrdersAdapter ordersAdapter;
    private final List<OrderModel> orderList = new ArrayList<>();

    private DatabaseReference vendorRef;
    private String shopId;
    private String vendorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllRecentordersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vendorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ordersAdapter = new OrdersAdapter(orderList);
        binding.recyclerAllOrders.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerAllOrders.setAdapter(ordersAdapter);

        fetchShopId();
    }

    private void fetchShopId() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(vendorId);
        userRef.child("shopId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    shopId = snapshot.getValue(String.class);
                    vendorRef = FirebaseDatabase.getInstance().getReference("Vendors").child(shopId);
                    fetchOrders();
                } else {
                    Toast.makeText(AllRecentOrdersActivity.this, "Shop ID not found for vendor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AllRecentOrdersActivity.this, "Failed to load vendor details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchOrders() {
        vendorRef.child("Orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    OrderModel model = snap.getValue(OrderModel.class);
                    if (model != null && model.getItem() != null && !"Delivered".equalsIgnoreCase(model.getStatus())) {
                        model.setOrderId(snap.getKey());
                        model.setShopId(shopId);
                        orderList.add(model);
                    }
                }
                ordersAdapter.updateList(orderList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AllRecentOrdersActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
