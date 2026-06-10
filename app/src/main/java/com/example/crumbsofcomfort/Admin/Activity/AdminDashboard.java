package com.example.crumbsofcomfort.Admin.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.databinding.ActivityAdminDashboardBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboard extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private ActionBarDrawerToggle toggle;
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        //getSupportActionBar().setTitle("Admin Dashboard");

        toggle = new ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.imgMenu.setOnClickListener(v ->
                binding.drawerLayout.openDrawer(GravityCompat.START)
        );

        binding.navigationView.bringToFront();

        binding.navigationView.setNavigationItemSelectedListener(this::handleMenuItemClick);

        binding.btnApproveVendors.setOnClickListener(v ->
                startActivity(new Intent(this, ApproveVendorsActivity.class)));

        binding.btnApproveDelivery.setOnClickListener(v ->
                startActivity(new Intent(this, ApproveDeliveryPartnerActivity.class)));

        binding.btnManageUsers.setOnClickListener(v ->
                startActivity(new Intent(this, AllUsersActivity.class)));

        binding.btnViewAllOrders.setOnClickListener(v ->
                startActivity(new Intent(this, AllOrdersActivity.class)));

        binding.btnFeedback.setOnClickListener(v ->
                startActivity(new Intent(this, AdminFeedbackActivity.class)));

        loadDashboardStats();
    }

    private boolean handleMenuItemClick(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            finish();
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void loadDashboardStats() {
        // Count Users
        dbRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int userCount = 0;
                int vendorCount = 0;
                int deliveryCount = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String role = ds.child("role").getValue(String.class);
                    if (role != null) {
                        if (role.equalsIgnoreCase("User")) {
                            userCount++;
                        } else if (role.equalsIgnoreCase("Vendor")) {
                            vendorCount++;
                        }else if (role.equalsIgnoreCase("Delivery Partner")) {
                            deliveryCount++;
                        }
                    }
                }
                binding.totalUsers.setText(String.valueOf(userCount));
                binding.totalVendors.setText(String.valueOf(vendorCount));
                binding.totalCustomRequests.setText(String.valueOf(deliveryCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Count Orders & Custom Requests
        dbRef.child("Vendors").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int orderCount = 0;
                int customCount = 0;

                for (DataSnapshot vendorSnap : snapshot.getChildren()) {
                    DataSnapshot ordersSnap = vendorSnap.child("Orders");
                    for (DataSnapshot orderSnap : ordersSnap.getChildren()) {
                        orderCount++;
                        String orderId = orderSnap.child("orderId").getValue(String.class);
                        if (orderId != null && orderId.startsWith("custom_")) {
                            customCount++;
                        }
                    }
                }
                binding.totalOrders.setText(String.valueOf(orderCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
