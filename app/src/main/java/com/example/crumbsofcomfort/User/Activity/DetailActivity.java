package com.example.crumbsofcomfort.User.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.User.Adapter.PicListAdapter;
import com.example.crumbsofcomfort.User.Adapter.WeightAdapter;
import com.example.crumbsofcomfort.User.Helper.ManagementFav;
import com.example.crumbsofcomfort.User.Helper.ManagmentCart;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.databinding.ActivityDetailBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DetailActivity extends BaseActivity {
    private ActivityDetailBinding binding;
    private ItemsModel item;
    private ManagmentCart managmentCart;
    private ManagementFav managementFav;
    private double basePrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = systemBars.bottom + 3;
            v.setLayoutParams(params);
            return insets;
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : null;

        managmentCart = new ManagmentCart(this, uid);
        managementFav = new ManagementFav(this, uid);

        getBundleExtra();
        initLists();
        binding.favBtn.setImageResource(managementFav.heartIcon(item.getTitle()));
        binding.backbtn.setOnClickListener(view -> finish());

        binding.favBtn.setOnClickListener(view -> {
            if (managementFav.isFavorite(item.getTitle())) {
                managementFav.removeFavByTitle(item.getTitle());
            } else {
                managementFav.insertFav(item);
            }
            binding.favBtn.setImageResource(managementFav.heartIcon(item.getTitle()));
        });

        final String[] selectedEggOption = {""};

        binding.eggOptionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_with_egg) {
                selectedEggOption[0] = "With Egg";
            } else if (checkedId == R.id.radio_eggless) {
                selectedEggOption[0] = "Eggless";
            }
            Log.d("EggOption", "Selected: " + selectedEggOption[0]);
        });

        binding.addToCartBtn.setOnClickListener(view -> {
            if (item.getSelectedSize() == null || item.getSelectedSize().isEmpty()) {
                showValidationError("Please select a size");
                return;
            }

            if (item.getShopId() == null || item.getShopId().isEmpty()) {
                showValidationError("Missing shop ID. Please try again.");
                return;
            }

            int selectedEggId = binding.eggOptionGroup.getCheckedRadioButtonId();
            if (selectedEggId == -1) {
                showValidationError("Please select egg preference");
                return;
            }

            RadioButton selectedEggButton = findViewById(selectedEggId);
            String eggType = selectedEggButton.getText().toString();
            item.setEggType(eggType);

            String messageOnCake = binding.editMessage.getText().toString().trim();
            item.setMessageOnCake(messageOnCake);

            if (isItemAlreadyInCart(item)) {
                startActivity(new Intent(this, CartActivity.class));
            } else {
                item.setNumberInCart(1);
                managmentCart.insertItems(item);
                binding.addToCartBtn.setText("Go to Cart");
            }
        });


        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        View decor = window.getDecorView();
        decor.setSystemUiVisibility(0);
    }

    private void getBundleExtra() {
        item = (ItemsModel) getIntent().getSerializableExtra("object");
        String shopIdFromIntent = getIntent().getStringExtra("shopId");

        Glide.with(this)
                .load(item.getSellerPic())
                .circleCrop()
                .into(binding.picSeller);

        if ((item.getShopId() == null || item.getShopId().isEmpty()) && shopIdFromIntent != null) {
            item.setShopId(shopIdFromIntent);
            Log.e("DEBUG_DETAIL", "ShopId restored manually: " + shopIdFromIntent);
        } else {
            Log.e("DEBUG_DETAIL", "ShopId in item: " + item.getShopId());
        }

        basePrice = item.getPrice();

        binding.txtTitle.setText(item.getTitle());
        binding.descriptionText.setText(item.getDescription());
        binding.txtPrice.setText("₹" + String.format("%.2f", basePrice));
        if (item.getRatingCount() > 0) {
            // case: normal products with average rating
            double averageRating = item.getTotalRating() / item.getRatingCount();
            binding.txtRating.setText(String.format("%.1f", averageRating));
        } else if (item.getRating() > 0) {
            // case: bestseller or items with simple rating
            binding.txtRating.setText(String.format("%.1f", item.getRating()));
        } else {
            binding.txtRating.setText("No ratings");
        }

        binding.sellerName.setText(item.getSellerName());
        if (item.getShopId() != null && !item.getShopId().isEmpty()) {
            fetchSellerAddress(item.getShopId());
        } else {
            binding.sellerAddress.setText("Shop ID missing");
        }
    }
    private void fetchSellerAddress(String shopId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Shops").child(shopId);
        ref.child("address").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String address = snapshot.getValue(String.class);
                    binding.sellerAddress.setText(address);
                } else {
                    binding.sellerAddress.setText("Address not available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.sellerAddress.setText("Error loading address");
            }
        });
    }


    private void initLists() {
        ArrayList<String> weightList = new ArrayList<>(item.getSize());
        binding.weightList.setAdapter(new WeightAdapter(weightList, selectedWeight -> {
            item.setSelectedSize(selectedWeight);
            double multiplier = extractMultiplier(selectedWeight);
            double updatedPrice = basePrice * multiplier;
            item.setPrice(updatedPrice);
            binding.txtPrice.setText("₹" + String.format("%.2f", updatedPrice));

            String servingText;
            switch (selectedWeight.toLowerCase()) {
                case "mini":
                    servingText = "Serves 1 person";
                    break;
                case "small":
                    servingText = "Serves 4 to 6 people";
                    break;
                case "medium":
                    servingText = "Serves 8 to 12 people";
                    break;
                case "large":
                    servingText = "Serves 12 to 16 people";
                    break;
                default:
                    servingText = "";
                    break;
            }
            binding.textServing.setText(servingText);
            binding.textServing.setVisibility(servingText.isEmpty() ? View.GONE : View.VISIBLE);

            if (isItemAlreadyInCart(item)) {
                binding.addToCartBtn.setText("Go to Cart");
            } else {
                binding.addToCartBtn.setText("Add to Cart");
            }
        }));
        binding.weightList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        ArrayList<String> picList = new ArrayList<>(item.getPicUrl());
        Glide.with(this).load(picList.get(0)).into(binding.picMain);
        binding.picList.setAdapter(new PicListAdapter(picList, binding.picMain));
        binding.picList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private boolean isItemAlreadyInCart(ItemsModel itemToCheck) {
        ArrayList<ItemsModel> cartList = managmentCart.getListCart();
        for (ItemsModel cartItem : cartList) {
            if (cartItem.getTitle().equals(itemToCheck.getTitle()) &&
                    cartItem.getSelectedSize() != null &&
                    cartItem.getSelectedSize().equals(itemToCheck.getSelectedSize())) {
                return true;
            }
        }
        return false;
    }

    private void showValidationError(String message) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_validation, null);
        bottomSheetDialog.setContentView(view);

        TextView textMessage = view.findViewById(R.id.textMessage);
        AppCompatButton btnOk = view.findViewById(R.id.btnOk);

        textMessage.setText(message);
        btnOk.setOnClickListener(v -> bottomSheetDialog.dismiss());
        bottomSheetDialog.show();
    }

    private double extractMultiplier(String sizeString) {
        try {
            String numOnly = sizeString.trim().split(" ")[0].replaceAll("[^0-9.]", "");
            return Double.parseDouble(numOnly);
        } catch (Exception e) {
            return 1;
        }
    }
}
