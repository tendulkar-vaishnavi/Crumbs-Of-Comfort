package com.example.crumbsofcomfort.User.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.crumbsofcomfort.User.Adapter.ShopListAdapter;
import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.User.ViewModel.MainViewModel;
import com.example.crumbsofcomfort.databinding.ActivityShopListBinding;

import java.util.ArrayList;

public class ShopListActivity extends BaseActivity {

    private ActivityShopListBinding binding;
    private ShopListAdapter adapter;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopListBinding.inflate(getLayoutInflater());
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
        window.setStatusBarColor(getResources().getColor(R.color.cream)); // optional
        View decor = window.getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setupStatusBar();
        setupRecyclerView();

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getShop().observe(this, shopModels -> {
            if (shopModels != null) {
                adapter = new ShopListAdapter(this, new ArrayList<>(shopModels));
                binding.recyclerViewShops.setAdapter(adapter);
            }
        });

        viewModel.loadShop();

        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void setupStatusBar() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        View decor = window.getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void setupRecyclerView() {
        binding.recyclerViewShops.setLayoutManager(new LinearLayoutManager(this));
    }
}
