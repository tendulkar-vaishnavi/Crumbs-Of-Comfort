package com.example.crumbsofcomfort.User.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.crumbsofcomfort.User.Adapter.UserOrdersAdapter;
import com.example.crumbsofcomfort.User.Model.UserOrderModel;
import com.example.crumbsofcomfort.Vendor.Activity.VendorActivity;
import com.example.crumbsofcomfort.databinding.ActivityMyOrderBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyOrderActivity extends AppCompatActivity {

    private ActivityMyOrderBinding binding;
    private List<UserOrderModel> orderList = new ArrayList<>();
    private UserOrdersAdapter adapter;
    private DatabaseReference dbRef;
    private String currentUserId;
    private String currentUserPhone;
    private DatabaseReference vendorRef;
    private String vendorId;
    private String shopId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyOrderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        dbRef = FirebaseDatabase.getInstance().getReference("Vendors");

        adapter = new UserOrdersAdapter(this, orderList);
        binding.recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewOrders.setAdapter(adapter);

        fetchOrders();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
    private void fetchShopId() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(vendorId);
        userRef.child("shopId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    shopId = snapshot.getValue(String.class);
                    vendorRef = FirebaseDatabase.getInstance().getReference("Vendors").child(shopId);
                    //fetchOrders();
                } else {
                    Toast.makeText(MyOrderActivity.this, "Shop ID not found for vendor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyOrderActivity.this, "Failed to load vendor details", Toast.LENGTH_SHORT).show();
            }
        });
    }
    /*private void fetchOrders() {
        vendorRef.child("Orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    UserOrderModel model = snap.getValue(UserOrderModel.class);
                    if (model != null && model.getItem() != null) {
                        if (!"Delivered".equalsIgnoreCase(model.getStatus())) {
                            model.setOrderId(snap.getKey());
                            model.setShopId(shopId);
                            orderList.add(this,model);
                        }
                    }
                }
                if (orderList.isEmpty()) {
                    binding.textNoOrders.setVisibility(View.VISIBLE);
                } else {
                    binding.textNoOrders.setVisibility(View.GONE);
                }

                ordersAdapter.updateList(orderList);
                binding.totalOrders.setText(String.valueOf(orderList.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VendorActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private void fetchOrders() {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference vendorRef = FirebaseDatabase.getInstance().getReference().child("Vendors");

        vendorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot vendorsSnapshot) {
                orderList.clear();

                for (DataSnapshot shopSnap : vendorsSnapshot.getChildren()) {
                    String shopId = shopSnap.getKey();
                    DataSnapshot ordersSnap = shopSnap.child("Orders");

                    for (DataSnapshot orderSnap : ordersSnap.getChildren()) {
                        String userId = orderSnap.child("userId").getValue(String.class);

                        if (userId != null && userId.equals(currentUserUid)) {
                            String orderId = orderSnap.child("orderId").getValue(String.class);
                            String status = orderSnap.child("status").getValue(String.class);
                            String date = orderSnap.child("date").getValue(String.class);
                            String deliveryTime = orderSnap.child("deliveryTime").getValue(String.class);
                            Integer totalAmount = orderSnap.child("totalAmount").getValue(Integer.class);



                            DataSnapshot itemSnap = orderSnap.child("item");
                            String productName = itemSnap.child("productName").getValue(String.class);
                            String imageUrl = itemSnap.child("imageUrl").getValue(String.class);
                            String weight = itemSnap.child("weight").getValue(String.class);
                            Integer quantity = itemSnap.child("quantity").getValue(Integer.class);
                            String itemId = itemSnap.child("itemId").getValue(String.class);

                            if (productName != null && imageUrl != null && weight != null &&
                                    totalAmount != null && quantity != null && status != null &&
                                    date != null && deliveryTime != null && orderId != null) {

                                if (!"Cancelled by User".equals(status)) {
                                    UserOrderModel order = new UserOrderModel(
                                            productName, weight, quantity, totalAmount,
                                            imageUrl, status, shopId, orderId, date, deliveryTime, userId, shopId, itemId
                                    );
                                    orderList.add(order);
                                }
                            }
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                binding.recyclerViewOrders.setVisibility(orderList.isEmpty() ? View.GONE : View.VISIBLE);
                binding.text.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyOrderActivity.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}




