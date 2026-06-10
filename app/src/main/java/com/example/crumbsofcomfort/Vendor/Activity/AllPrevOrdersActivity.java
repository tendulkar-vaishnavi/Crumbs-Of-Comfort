package com.example.crumbsofcomfort.Vendor.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.crumbsofcomfort.Vendor.Adapter.PreviousOrdersAdapter;
import com.example.crumbsofcomfort.Vendor.Model.OrderModel;
import com.example.crumbsofcomfort.databinding.ActivityAllPrevOrdersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AllPrevOrdersActivity extends AppCompatActivity {
    ActivityAllPrevOrdersBinding binding;
    private PreviousOrdersAdapter previousOrdersAdapter;
    private List<OrderModel> previousOrdersList = new ArrayList<>();
    private String vendorId;
    private String shopId;
    private static final String TAG = "AllPrevOrdersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllPrevOrdersBinding.inflate(getLayoutInflater());
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

        previousOrdersAdapter = new PreviousOrdersAdapter(previousOrdersList);
        binding.recyclerAllPrevOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerAllPrevOrders.setAdapter(previousOrdersAdapter);

        vendorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fetchShopIdAndLoadOrders();
    }

    private void fetchShopIdAndLoadOrders() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(vendorId);
        userRef.child("shopId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    shopId = snapshot.getValue(String.class);
                    fetchPreviousOrders();
                } else {
                    Toast.makeText(AllPrevOrdersActivity.this, "Shop ID not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AllPrevOrdersActivity.this, "Failed to load shop ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPreviousOrders() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance()
                .getReference("Vendors").child(shopId).child("Orders");

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                previousOrdersList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    OrderModel model = snap.getValue(OrderModel.class);
                    if (model != null && "delivered".equalsIgnoreCase(model.getStatus())) {
                        model.setOrderId(snap.getKey());
                        model.setShopId(shopId);
                        previousOrdersList.add(model);
                    }
                }
                previousOrdersAdapter.notifyDataSetChanged();
                Log.d(TAG, "Loaded delivered orders: " + previousOrdersList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load orders: " + error.getMessage());
            }
        });
    }
}
