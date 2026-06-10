package com.example.crumbsofcomfort.Vendor.Activity;

import android.os.Bundle;
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

import com.example.crumbsofcomfort.Vendor.Adapter.ProductAdapter;
import com.example.crumbsofcomfort.Vendor.Model.ProductModel;
import com.example.crumbsofcomfort.databinding.ActivityManageItemBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageItemActivity extends AppCompatActivity {

    ActivityManageItemBinding binding;
    private ProductAdapter productAdapter;
    private String shopId;
    private final List<ProductModel> productList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageItemBinding.inflate(getLayoutInflater());
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

        shopId = getIntent().getStringExtra("shopId");

        productAdapter = new ProductAdapter(productList, shopId);
        binding.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewProducts.setAdapter(productAdapter);

        fetchProducts();
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void fetchProducts() {
        DatabaseReference productsRef = FirebaseDatabase.getInstance()
                .getReference("Shops")
                .child(shopId)
                .child("items");

        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    ProductModel model = snap.getValue(ProductModel.class);
                    if (model != null) {
                        String key = snap.getKey();
                        model.setItemId(key);
                        model.setId(key);

                        if (model.getPicUrl() != null && !model.getPicUrl().isEmpty()) {
                            model.setImageUrl(model.getPicUrl().get(0));
                        }
                        productList.add(model);
                    }
                }
                productAdapter.updateList(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageItemActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
