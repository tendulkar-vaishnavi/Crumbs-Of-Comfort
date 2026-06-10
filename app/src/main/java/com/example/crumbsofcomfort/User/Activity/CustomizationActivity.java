package com.example.crumbsofcomfort.User.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.User.Helper.ManagmentCart;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.User.Model.ShopItem;
import com.example.crumbsofcomfort.Vendor.Helper.CloudinaryHelper;
import com.example.crumbsofcomfort.databinding.ActivityCustomizationBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomizationActivity extends BaseActivity {

    private ActivityCustomizationBinding binding;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private DatabaseReference shopRef;
    private List<ShopItem> shopItemList = new ArrayList<>();
    private ManagmentCart managmentCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomizationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = systemBars.bottom + 3;
            v.setLayoutParams(params);
            return insets;
        });

        CloudinaryHelper.initCloudinary(this);


        shopRef = FirebaseDatabase.getInstance().getReference("Shops");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        managmentCart = new ManagmentCart(this, uid);

        loadShopNamesFromDatabase();
        setupImagePicker();
        setupListeners();

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        View decor = window.getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void loadShopNamesFromDatabase() {
        shopItemList.clear();
        shopItemList.add(new ShopItem("", "Select Bakery"));

        shopRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot shopSnap : snapshot.getChildren()) {
                    String shopId = shopSnap.getKey();
                    String sellerName = shopSnap.child("sellerName").getValue(String.class);

                    if (shopId != null && sellerName != null) {
                        shopItemList.add(new ShopItem(shopId, sellerName));
                    }
                }

                ArrayAdapter<ShopItem> adapter = new ArrayAdapter<>(
                        CustomizationActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        shopItemList
                );
                binding.spinnerShop.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomizationActivity.this, "Failed to load shops", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                            binding.imagePreview.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            showValidationBottomSheet("Image Error", "Failed to load selected image.");
                        }
                    }
                }
        );
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        binding.btnAddToCart.setOnClickListener(v -> handleSubmit());

        binding.editSize.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) { calculatePrice(); }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        binding.editQuantity.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) { calculatePrice(); }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void handleSubmit() {
        String description = binding.editDescription.getText().toString().trim();
        String message = binding.editMessage.getText().toString().trim();
        String size = binding.editSize.getText().toString().trim();
        String quantityStr = binding.editQuantity.getText().toString().trim();
        ShopItem selectedShop = (ShopItem) binding.spinnerShop.getSelectedItem();
        String shopId = selectedShop.getShopId();
        String shopName = selectedShop.getSellerName();

        if (shopId.isEmpty()) {
            showValidationBottomSheet("Select a Bakery", "Please choose a bakery from the dropdown.");
            return;
        }

        if (description.isEmpty()) {
            showValidationBottomSheet("Missing Description", "Please describe your cake design or theme.");
            return;
        }

        if (message.isEmpty()) {
            showValidationBottomSheet("Missing Message", "Please enter a message to write on the cake.");
            return;
        }

        if (size.isEmpty()) {
            showValidationBottomSheet("Missing Size", "Please enter the cake size (e.g., 500g, 1kg).");
            return;
        }

        if (quantityStr.isEmpty()) {
            showValidationBottomSheet("Missing Quantity", "Please specify the quantity you want.");
            return;
        }

        if (selectedImageUri == null) {
            showValidationBottomSheet("Image Missing", "Please upload a reference image for the cake.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                showValidationBottomSheet("Invalid Quantity", "Quantity must be more than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showValidationBottomSheet("Invalid Quantity", "Please enter a valid number for quantity.");
            return;
        }

        double finalPrice = calculateEstimatedPrice(size, quantity);
        if (finalPrice == 0) {
            showValidationBottomSheet("Invalid Size", "Please enter a valid size like 500g or 1kg.");
            return;
        }

        CloudinaryHelper.uploadImage(this, selectedImageUri, new CloudinaryHelper.CloudinaryCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                ArrayList<String> imageList = new ArrayList<>();
                imageList.add(imageUrl);

                ItemsModel customItem = new ItemsModel();
                customItem.setTitle("Custom Cake");
                customItem.setDescription(description);
                customItem.setMessageOnCake(message);
                customItem.setSelectedSize(size);
                customItem.setPrice(finalPrice);
                customItem.setNumberInCart(quantity);
                customItem.setSellerName(shopName);
                customItem.setPicUrl(imageList);
                customItem.setShopId(shopId);

                managmentCart.insertItems(customItem);

                Toast.makeText(CustomizationActivity.this, "Custom Cake added to cart", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CustomizationActivity.this, CartActivity.class));
                finish();
            }

            @Override
            public void onError(Exception e) {
                showValidationBottomSheet("Upload Failed", "Image upload failed. Please try again.");
            }
        });
    }

    private double calculateEstimatedPrice(String sizeInput, int quantity) {
        double base = 0;

        if (sizeInput.equalsIgnoreCase("500g") || sizeInput.equalsIgnoreCase("0.5kg")) {
            base = 200;
        } else if (sizeInput.equalsIgnoreCase("1kg")) {
            base = 400;
        } else if (sizeInput.toLowerCase().endsWith("kg")) {
            try {
                float weight = Float.parseFloat(sizeInput.toLowerCase().replace("kg", "").trim());
                base = 400 * weight;
            } catch (Exception ignored) {}
        }

        return base * quantity;
    }
    private void calculatePrice() {
        String sizeInput = binding.editSize.getText().toString().trim();
        String quantityInput = binding.editQuantity.getText().toString().trim();

        if (sizeInput.isEmpty() || quantityInput.isEmpty()) {
            binding.textPrice.setVisibility(View.GONE);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityInput);
        } catch (NumberFormatException e) {
            binding.textPrice.setVisibility(View.GONE);
            return;
        }

        double finalPrice = calculateEstimatedPrice(sizeInput, quantity);

        if (finalPrice > 0) {
            binding.textPrice.setVisibility(View.VISIBLE);
            binding.textPrice.setText("Estimated Price: ₹" + (int) finalPrice);
        } else {
            binding.textPrice.setVisibility(View.GONE);
        }
    }
    private void showValidationBottomSheet(String title, String message) {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_validation, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);

        TextView textTitle = view.findViewById(R.id.textTitle);
        TextView textMessage = view.findViewById(R.id.textMessage);
        AppCompatButton btnOk = view.findViewById(R.id.btnOk);

        textTitle.setText(title);
        textMessage.setText(message);

        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
