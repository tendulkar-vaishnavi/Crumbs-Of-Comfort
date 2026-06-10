package com.example.crumbsofcomfort.User.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.crumbsofcomfort.User.Adapter.FavouriteAdapter;
import com.example.crumbsofcomfort.User.Helper.ManagementFav;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.databinding.ActivityFavouriteBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class FavouriteActivity extends BaseActivity {

    private ActivityFavouriteBinding binding;
    private ManagementFav managementFav;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavouriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = systemBars.bottom + 3;
            v.setLayoutParams(params);
            return insets;
        });

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        managementFav = new ManagementFav(this, uid);

        ArrayList<ItemsModel> favList = managementFav.getFavList();

        if (favList == null || favList.isEmpty()) {
            Toast.makeText(this, "No favorites to display", Toast.LENGTH_SHORT).show();
        } else {
            FavouriteAdapter adapter = new FavouriteAdapter(favList, this);
            binding.favRecycler.setLayoutManager(new LinearLayoutManager(this));
            binding.favRecycler.setAdapter(adapter);
        }
        binding.backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(FavouriteActivity.this,MainActivity.class);
            startActivity(intent);
        });

        Window window=getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        View decor=window.getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}
