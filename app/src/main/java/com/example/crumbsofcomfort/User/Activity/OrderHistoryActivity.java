package com.example.crumbsofcomfort.User.Activity;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.crumbsofcomfort.User.Adapter.UserOrderHistoryAdapter;
import com.example.crumbsofcomfort.User.Model.UserOrderModel;
import com.example.crumbsofcomfort.databinding.ActivityCategoryItemsBinding;
import com.example.crumbsofcomfort.databinding.ActivityOrderHistoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private ActivityOrderHistoryBinding binding;
    private DatabaseReference ordersRef;
    private UserOrderHistoryAdapter adapter;
    private List<UserOrderModel> orderList = new ArrayList<>();
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ordersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Orders");

        setupRecyclerView();
        loadCompletedOrders();
    }

    private void setupRecyclerView() {
        adapter = new UserOrderHistoryAdapter(this,orderList);
        binding.orderHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.orderHistoryRecyclerView.setAdapter(adapter);
    }

    private void loadCompletedOrders() {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    UserOrderModel order = orderSnap.getValue(UserOrderModel.class);
                    if (order != null) {
                        String status = order.getStatus();
                        if (status != null) {
                            status = status.toLowerCase(); // normalize
                            if (status.equals("delivered") || status.equals("cancelled") || status.equals("failed")) {
                                orderList.add(order);
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("OrderHistory", "Database error: " + error.getMessage());
            }
        });
    }
}
