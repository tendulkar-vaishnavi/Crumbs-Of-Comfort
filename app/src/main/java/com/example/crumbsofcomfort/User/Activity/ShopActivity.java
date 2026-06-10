package com.example.crumbsofcomfort.User.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.User.Adapter.ShopMainAdapter;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.User.Model.ShopWithItemsModel;
import com.example.crumbsofcomfort.databinding.ActivityShopBinding;

import java.util.ArrayList;

public class ShopActivity extends BaseActivity {

    ActivityShopBinding binding;
    private ShopMainAdapter adapter;
    private ShopWithItemsModel shop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.cream));
        View decor = window.getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = systemBars.bottom + 3;
            v.setLayoutParams(params);
            return insets;
        });

        binding.backBtn.setOnClickListener(view -> onBackPressed());

        shop = (ShopWithItemsModel) getIntent().getSerializableExtra("shopBundle");
        if (shop == null) return;

        binding.txtShopName.setText(shop.getShopName());
        binding.txtShopAddress.setText(shop.getShopAddress());

        binding.callToSellerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + shop.getShopContact()));
            startActivity(intent);
        });

        binding.msgToSellerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:" + shop.getShopContact()));
            startActivity(intent);
        });

        ArrayList<ItemsModel> itemList = shop.getItems();

        if (itemList == null || itemList.isEmpty()) {
            binding.shopView.setVisibility(View.GONE);

            return;
        }

        for (ItemsModel item : itemList) {
            item.setSellerName(shop.getShopName());
            item.setSellerPic(shop.getShopImage());
            item.setSellerTell(shop.getShopContact());
            item.setShopId(shop.getShopId());
        }

        adapter = new ShopMainAdapter(itemList);
        binding.shopView.setLayoutManager(new LinearLayoutManager(this));
        binding.shopView.setHasFixedSize(true);
        binding.shopView.setNestedScrollingEnabled(false);
        binding.shopView.setAdapter(adapter);
    }
}
