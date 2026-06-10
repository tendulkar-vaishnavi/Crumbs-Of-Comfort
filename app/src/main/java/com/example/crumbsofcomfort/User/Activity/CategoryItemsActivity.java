package com.example.crumbsofcomfort.User.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.crumbsofcomfort.User.Adapter.CategoryItemAdapter;
import com.example.crumbsofcomfort.User.Helper.ManagementShop;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.User.Model.ShopModel;
import com.example.crumbsofcomfort.databinding.ActivityCategoryItemsBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoryItemsActivity extends AppCompatActivity {

    private ActivityCategoryItemsBinding binding;
    private CategoryItemAdapter adapter;
    private int categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = systemBars.bottom + 3;
            v.setLayoutParams(params);
            return insets;
        });

        categoryId = getIntent().getIntExtra("categoryId", -1);
        if (categoryId == -1) {
            Toast.makeText(this, "Invalid category", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.backBtn.setOnClickListener(view -> onBackPressed());

        loadCategoryItems(categoryId);
    }

    private void loadCategoryItems(int categoryId) {
        ManagementShop managementShop = new ManagementShop(this);
        List<ShopModel> allShops = managementShop.getShopList();

        List<ItemsModel> filteredItems = new ArrayList<>();

        for (ShopModel shop : allShops) {
            for (ItemsModel item : shop.getItems()) {
                if (item.getCategoryId() == categoryId) {
                    item.setSellerName(shop.getSellerName());
                    item.setSellerPic(shop.getSellerPic());
                    item.setShopId(shop.getShopId());
                    filteredItems.add(item);
                }
            }
        }

        if (filteredItems.isEmpty()) {
            binding.emptyText.setVisibility(View.VISIBLE);
        } else {
            binding.emptyText.setVisibility(View.GONE);
            adapter = new CategoryItemAdapter(filteredItems);
            binding.recyclerViewCategoryItems.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerViewCategoryItems.setAdapter(adapter);
        }
    }
}
