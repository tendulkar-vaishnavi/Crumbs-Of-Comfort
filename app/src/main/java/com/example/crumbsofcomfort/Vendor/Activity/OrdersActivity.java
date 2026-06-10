package com.example.crumbsofcomfort.Vendor.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.Vendor.Model.OrderModel;
import com.example.crumbsofcomfort.Vendor.Model.ProductItemModel;
import com.example.crumbsofcomfort.databinding.ActivityOrdersBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    ActivityOrdersBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = systemBars.bottom + 3;
            v.setLayoutParams(params);
            return insets;
        });
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        View decor = window.getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        String shopId = getIntent().getStringExtra("shopId");
        String orderId = getIntent().getStringExtra("orderId");

        String[] statusOptions = {"Pending", "Prepared", "Out for Delivery", "Delivered", "Cancelled"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.statusSpinner.setAdapter(adapter);

        final String[] originalStatus = {null};

        if (shopId != null && orderId != null) {
            FirebaseDatabase.getInstance()
                    .getReference("Vendors")
                    .child(shopId)
                    .child("Orders")
                    .child(orderId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            OrderModel order = snapshot.getValue(OrderModel.class);
                            if (order != null) {
                                populateOrderDetails(order);

                                String currentStatus = order.getStatus();
                                if (currentStatus != null) {
                                    originalStatus[0] = currentStatus;
                                    int pos = adapter.getPosition(currentStatus);
                                    if (pos >= 0) binding.statusSpinner.setSelection(pos);
                                }
                            }
                        } else {
                            Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                        }
                    });
        }


        binding.btnBack.setOnClickListener(v -> onBackPressed());

        binding.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean isFirstSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = parent.getItemAtPosition(position).toString();

                if (isFirstSelection) {
                    isFirstSelection = false;
                    return;
                }

                if (!selectedStatus.equals(originalStatus[0])) {
                    originalStatus[0] = selectedStatus; // update memory
                    FirebaseDatabase.getInstance()
                            .getReference("Vendors")
                            .child(shopId)
                            .child("Orders")
                            .child(orderId)
                            .child("status")
                            .setValue(selectedStatus)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(OrdersActivity.this, "Status updated to " + selectedStatus, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(OrdersActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        List<String> deliveryPartnerNames = new ArrayList<>();
        List<String> deliveryPartnerIds = new ArrayList<>();

        ArrayAdapter<String> deliveryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deliveryPartnerNames);
        deliveryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       // binding.deliveryPartnerSpinner.setAdapter(deliveryAdapter);

        FirebaseDatabase.getInstance().getReference("DeliveryPartners")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DataSnapshot partnerSnapshot : snapshot.getChildren()) {
                        String name = partnerSnapshot.child("name").getValue(String.class);
                        if (name != null) {
                            deliveryPartnerNames.add(name);
                            deliveryPartnerIds.add(partnerSnapshot.getKey());
                        }
                    }
                    deliveryAdapter.notifyDataSetChanged();
                });

        /*binding.deliveryPartnerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean isFirst = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirst) {
                    isFirst = false;
                    return;
                }

                String selectedPartnerId = deliveryPartnerIds.get(position);
                if (shopId != null && orderId != null && selectedPartnerId != null) {
                    FirebaseDatabase.getInstance().getReference("Vendors")
                            .child(shopId)
                            .child("Orders")
                            .child(orderId)
                            .child("deliveryPartnerId")
                            .setValue(selectedPartnerId)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(OrdersActivity.this, "Assigned to delivery partner", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(OrdersActivity.this, "Failed to assign delivery partner", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });*/

    }

    private void populateOrderDetails(OrderModel order) {
        ProductItemModel item = order.getItem();

        if (item != null) {
            binding.productId.setText(item.getProductId());
            binding.productName.setText(item.getProductName());
            binding.messageOnCake.setText(item.getMessageOnCake());
            binding.eggPreference.setText(item.getEggType());
            binding.orderWeight.setText(item.getWeight());
            binding.orderQuantity.setText("x" + item.getQuantity());

            Glide.with(this)
                    .load(item.getImageUrl())
                    .into(binding.imgOrder);
        }

        binding.deliveryDate.setText(order.getDate());
        binding.deliveryTime.setText(order.getDeliveryTime());
        binding.deliveryAddress.setText(order.getUserAddress());
        binding.shopAddress.setText(order.getShopAddress());

        // Optional display below spinner: current status text (if needed)
        // binding.orderStatus.setText(order.getStatus());
    }
}
