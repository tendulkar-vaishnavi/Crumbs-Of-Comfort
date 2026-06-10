package com.example.crumbsofcomfort.Admin.Activity;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.crumbsofcomfort.Admin.Adapter.AllOrdersAdapter;
import com.example.crumbsofcomfort.Admin.Model.AllOrderModel;
import com.example.crumbsofcomfort.databinding.ActivityAllOrdersBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllOrdersActivity extends AppCompatActivity {

    private ActivityAllOrdersBinding binding;
    private List<AllOrderModel> orderList;
    private AllOrdersAdapter adapter;
    private DatabaseReference vendorsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderList = new ArrayList<>();
        adapter = new AllOrdersAdapter(orderList);

        binding.recyclerDelivery.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerDelivery.setAdapter(adapter);

        vendorsRef = FirebaseDatabase.getInstance().getReference("Vendors");

        loadAllOrders();
    }

    private void loadAllOrders() {
        vendorsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                for (DataSnapshot vendorSnap : snapshot.getChildren()) {
                    DataSnapshot ordersSnap = vendorSnap.child("Orders");

                    for (DataSnapshot orderSnap : ordersSnap.getChildren()) {
                        AllOrderModel order = new AllOrderModel();

                        order.setOrderId(orderSnap.child("orderId").getValue(String.class));
                        order.setShopName(orderSnap.child("shopName").getValue(String.class));
                        order.setDate(orderSnap.child("date").getValue(String.class));
                        order.setDeliveryTime(orderSnap.child("deliveryTime").getValue(String.class));

                        // Inside 'item'
                        order.setProductName(orderSnap.child("item").child("productName").getValue(String.class));
                        Integer qty = orderSnap.child("item").child("quantity").getValue(Integer.class);
                        order.setQuantity(qty != null ? qty : 0);
                        order.setWeight(orderSnap.child("item").child("weight").getValue(String.class));

                        Double amount = orderSnap.child("totalAmount").getValue(Double.class);
                        order.setTotalAmount(amount != null ? amount : 0.0);

                        order.setStatus(orderSnap.child("status").getValue(String.class));
                        order.setUserName(orderSnap.child("userName").getValue(String.class));

                        orderList.add(order);
                    }
                }

                adapter.notifyDataSetChanged();
                Log.d("ALL_ORDERS", "Loaded " + orderList.size() + " orders");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ALL_ORDERS", "Error: " + error.getMessage());
            }
        });
    }
}
