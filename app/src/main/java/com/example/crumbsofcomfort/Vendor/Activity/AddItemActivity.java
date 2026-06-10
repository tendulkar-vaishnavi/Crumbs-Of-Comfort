package com.example.crumbsofcomfort.Vendor.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.crumbsofcomfort.User.Helper.BottomSheet;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.Vendor.Helper.CloudinaryHelper;
import com.example.crumbsofcomfort.databinding.ActivityAddItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class AddItemActivity extends AppCompatActivity {
    private ActivityAddItemBinding binding;
    private ArrayList<String> sizeList = new ArrayList<>();
    private ArrayList<Uri> selectedImages = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String shopId = getIntent().getStringExtra("shopId");
        if (shopId == null || shopId.isEmpty()) {
            Toast.makeText(this, "Missing shop ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

        binding.btnGenerateId.setOnClickListener(view -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if (shopId == null || shopId.isEmpty()) {
                Toast.makeText(this, "Shop ID not found", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference shopRef = FirebaseDatabase.getInstance().getReference()
                    .child("Shops").child(shopId);

            shopRef.get().addOnSuccessListener(shopSnapshot -> {
                if (!shopSnapshot.exists() || !shopSnapshot.hasChild("prefix")) {
                    Toast.makeText(this, "Shop prefix not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                String prefix = shopSnapshot.child("prefix").getValue(String.class);
                DatabaseReference itemRef = shopRef.child("items");

                itemRef.get().addOnSuccessListener(itemsSnapshot -> {
                    long nextIndex = itemsSnapshot.getChildrenCount() + 1;
                    String paddedIndex = String.format("%03d", nextIndex);
                    String itemId = prefix + "_" + paddedIndex;

                    binding.etItemId.setText(itemId);
                    Toast.makeText(this, "Generated Item ID: " + itemId, Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch items", Toast.LENGTH_SHORT).show();
                    Log.e("ITEM_ID_GENERATE", "Error fetching items", e);
                });

            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to fetch shop data", Toast.LENGTH_SHORT).show();
                Log.e("ITEM_ID_GENERATE", "Error fetching shop prefix", e);
            });
        });


        CloudinaryHelper.initCloudinary(this);

        binding.btnAddSize.setOnClickListener(v -> {
            String size = binding.etSize.getText().toString().trim();
            if (!size.isEmpty()) {
                sizeList.add(size);
                binding.etSize.setText("");
                updateSizeList();
            }
        });

        binding.btnSelectImages.setOnClickListener(v -> pickImages());

        binding.btnSubmitItem.setOnClickListener(v -> {
            String name = binding.etItemName.getText().toString().trim();
            String price = binding.etPrice.getText().toString().trim();
            String desc = binding.etDescription.getText().toString().trim();
            String categoryId = binding.etCategoryId.getText().toString().trim();

            if (name.isEmpty() || price.isEmpty() || desc.isEmpty() || sizeList.isEmpty() || selectedImages.isEmpty() || categoryId.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Uploading images...", Toast.LENGTH_SHORT).show();

            ArrayList<String> uploadedUrls = new ArrayList<>();

            CloudinaryHelper.uploadMultipleImages(this, selectedImages, new CloudinaryHelper.CloudinaryCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    uploadedUrls.add(imageUrl);
                    Log.d("CloudinaryUpload", "Uploaded: " + imageUrl);

                    if (uploadedUrls.size() == selectedImages.size()) {
                        Log.d("FINAL_UPLOAD", "Images ready, uploading to DB...");
                        fetchShopNameAndUpload(name, price, desc, categoryId, uploadedUrls);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(AddItemActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.etCategoryId.setOnClickListener(view -> {
            BottomSheet.showCategoryPicker(
                    AddItemActivity.this,
                    "Select Category",
                    "Cup Cakes",
                    "Cakes",
                    "Cookies",
                    "Donuts",
                    "Breads",
                    selected -> binding.etCategoryId.setText(selected)
            );
        });
    }
    private void uploadNewItemWithShopPrefix(ItemsModel item) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        DatabaseReference vendorRef = FirebaseDatabase.getInstance().getReference("Vendors").child(uid);

        vendorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(AddItemActivity.this, "Vendor not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                String prefix = snapshot.child("prefix").getValue(String.class);
                if (prefix == null || prefix.isEmpty()) {
                    Toast.makeText(AddItemActivity.this, "Shop prefix missing", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference itemsRef = vendorRef.child("items");
                itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = snapshot.getChildrenCount() + 1;
                        String formatted = String.format("%03d", count);
                        String itemId = prefix + "_" + formatted;

                        item.setItemId(itemId);

                        Log.d("FIREBASE_WRITE", "Writing to path: Vendors/" + uid + "/items/" + count);

                        itemsRef.child(String.valueOf(count)).setValue(item)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(AddItemActivity.this, "Item uploaded successfully", Toast.LENGTH_SHORT).show();
                                    finish(); // Go back
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AddItemActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("FIREBASE_WRITE", "Error uploading item: ", e);
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FIREBASE_WRITE", "Cancelled: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FIREBASE_WRITE", "Vendor fetch cancelled: " + error.getMessage());
            }
        });
    }
    private static final HashMap<String, Integer> CATEGORY_MAP = new HashMap<>();
    static {
        CATEGORY_MAP.put("Cup Cakes", 0);
        CATEGORY_MAP.put("Cakes", 1);
        CATEGORY_MAP.put("Cookies", 2);
        CATEGORY_MAP.put("Donuts", 3);
        CATEGORY_MAP.put("Breads", 4);
    }

    private void fetchShopNameAndUpload(String name, String price, String desc, String categoryId, ArrayList<String> uploadedUrls) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String shopId = getIntent().getStringExtra("shopId");
        if (shopId == null || shopId.isEmpty()) {
            Toast.makeText(this, "Shop ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference shopRef = FirebaseDatabase.getInstance().getReference()
                .child("Shops").child(shopId);

        shopRef.get().addOnSuccessListener(snapshot -> {
            String prefix = snapshot.child("prefix").getValue(String.class);
            String shopName = snapshot.child("name").getValue(String.class);

            if (prefix == null || prefix.isEmpty()) {
                Toast.makeText(this, "Prefix not found for this shop", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference itemRef = shopRef.child("items");

            itemRef.get().addOnSuccessListener(itemSnapshot -> {
                long nextIndex = itemSnapshot.getChildrenCount() + 1;
                String paddedIndex = String.format("%03d", nextIndex);
                String itemId = prefix + "_" + paddedIndex;

                binding.etItemId.setText(itemId);

                uploadItemToFirebase(shopId, shopName, name, price, desc, categoryId, uploadedUrls);

            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to count existing items", Toast.LENGTH_SHORT).show();
                Log.e("ITEM_ID", "Error counting items", e);
            });

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch shop info", Toast.LENGTH_SHORT).show();
            Log.e("SHOP_INFO", "Error fetching shop info", e);
        });
    }



    private void uploadItemToFirebase(String shopId, String shopName, String name, String price, String desc, String categoryId, ArrayList<String> imageUrls) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        userRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(this, "Vendor data not found", Toast.LENGTH_SHORT).show();
                return;
            }

            saveItemToFirebase(shopId, shopName, name, price, desc, categoryId, imageUrls);

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to get vendor info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveItemToFirebase(String shopId, String shopName, String name, String price, String desc, String categoryId, ArrayList<String> imageUrls) {
        String itemId = binding.etItemId.getText().toString().trim();

        if (itemId.isEmpty()) {
            Toast.makeText(this, "Please generate an item ID first", Toast.LENGTH_SHORT).show();
            return;
        }

        ItemsModel itemData = new ItemsModel();
        itemData.setItemId(itemId);
        itemData.setTitle(name);
        itemData.setPrice(Double.parseDouble(price));
        itemData.setDescription(desc);
        itemData.setSize(sizeList);
        itemData.setPicUrl(imageUrls);
        itemData.setCategoryId(CATEGORY_MAP.getOrDefault(categoryId, -1));

        Log.d("ItemData", new Gson().toJson(itemData));

        DatabaseReference itemsRef = FirebaseDatabase.getInstance()
                .getReference("Shops")
                .child(shopId)
                .child("items");

        itemsRef.get().addOnSuccessListener(snapshot -> {
            ArrayList<Object> itemList = new ArrayList<>();

            if (snapshot.exists()) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Object item = child.getValue();
                    itemList.add(item);
                }
            }

            itemList.add(itemData);

            itemsRef.setValue(itemList).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Item added successfully to list", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save item: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            });

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load existing items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("FIREBASE_WRITE", "Read error", e);
        });
    }


    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Images"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            selectedImages.clear();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    selectedImages.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                selectedImages.add(data.getData());
            }
            Toast.makeText(this, selectedImages.size() + " image(s) selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSizeList() {
        binding.tvSizes.setText("Sizes: " + String.join(", ", sizeList));
    }
}
