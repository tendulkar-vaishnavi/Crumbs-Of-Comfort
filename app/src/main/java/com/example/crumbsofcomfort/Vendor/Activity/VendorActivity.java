package com.example.crumbsofcomfort.Vendor.Activity;

import android.content.Intent;
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
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.User.Activity.LoginActivity;
import com.example.crumbsofcomfort.User.Activity.MainActivity;
import com.example.crumbsofcomfort.User.Activity.MyOrderActivity;
import com.example.crumbsofcomfort.User.Activity.ProfileActivity;
import com.example.crumbsofcomfort.Vendor.Adapter.CustomRequestAdapter;
import com.example.crumbsofcomfort.Vendor.Adapter.OrdersAdapter;
import com.example.crumbsofcomfort.Vendor.Adapter.PreviousOrdersAdapter;
import com.example.crumbsofcomfort.Vendor.Adapter.ProductAdapter;
import com.example.crumbsofcomfort.Vendor.Adapter.ProductListAdapter;
import com.example.crumbsofcomfort.Vendor.Model.CustomRequestModel;
import com.example.crumbsofcomfort.Vendor.Model.OrderModel;
import com.example.crumbsofcomfort.Vendor.Model.ProductModel;
import com.example.crumbsofcomfort.databinding.ActivityVendorBinding;
import com.example.crumbsofcomfort.databinding.DrawerHeaderBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VendorActivity extends AppCompatActivity {

    private ActivityVendorBinding binding;
    private OrdersAdapter ordersAdapter;
    private ProductListAdapter productListAdapter;
    private PreviousOrdersAdapter previousOrdersAdapter;
    private CustomRequestAdapter requestAdapter;

    private List<OrderModel> orderList = new ArrayList<>();
    private List<ProductModel> productList = new ArrayList<>();
    private List<CustomRequestModel> requestList = new ArrayList<>();
    private List<OrderModel> previousOrdersList = new ArrayList<>();

    private DatabaseReference vendorRef;
    private String vendorId;
    private String shopId;
    private String shopAddress;

    private FusedLocationProviderClient fusedLocationClient;
    private double vendorLat = 0.0, vendorLng = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVendorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        vendorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fetchShopId();
        setupRecyclerViews();

        binding.txtSeeAllRecentOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VendorActivity.this, AllRecentOrdersActivity.class);
                startActivity(intent);
            }
        });
        /*binding.txtSeeAllCustomReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VendorActivity.this, AllCustomRequestActivity.class);
                startActivity(intent);
            }
        });*/
        binding.txtSeeAllPrevOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VendorActivity.this, AllPrevOrdersActivity.class);
                startActivity(intent);
            }
        });
        binding.txtSeeAllProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VendorActivity.this, AllProductsActivity.class);
                startActivity(intent);
            }
        });

        binding.imgMenu.setOnClickListener(v -> {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(VendorActivity.this, ProfileActivity.class));
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(VendorActivity.this, LoginActivity.class));
                finish();
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        View headerView = binding.navigationView.getHeaderView(0);
        DrawerHeaderBinding headerBinding = DrawerHeaderBinding.bind(headerView);

    }

    private void setupRecyclerViews() {
        ordersAdapter = new OrdersAdapter(orderList);
        binding.recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        binding.recyclerViewOrders.setAdapter(ordersAdapter);

        productListAdapter = new ProductListAdapter(productList,shopId);
        binding.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        binding.recyclerViewProducts.setAdapter(productListAdapter);

        requestAdapter = new CustomRequestAdapter(requestList);
        binding.recyclerViewRequests.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewRequests.setAdapter(requestAdapter);

        previousOrdersAdapter = new PreviousOrdersAdapter(previousOrdersList);
        binding.recyclerViewPrevOrders.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        binding.recyclerViewPrevOrders.setAdapter(previousOrdersAdapter);

        binding.btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddItemActivity.class);
            intent.putExtra("shopId", shopId);
            startActivity(intent);
        });
        binding.btnManageProduct.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageItemActivity.class);
            intent.putExtra("shopId", shopId);
            startActivity(intent);
        });

    }
    private void fetchShopId() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(vendorId);
        userRef.child("shopId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    shopId = snapshot.getValue(String.class);
                    vendorRef = FirebaseDatabase.getInstance().getReference("Vendors").child(shopId);

                    fetchOrders();
                    fetchProducts();
                    fetchCustomRequests();
                    fetchPreviousOrders();
                    loadShopName();
                    loadShopAddress();
                    fetchVendorLocationAndSave();

                } else {
                    Toast.makeText(VendorActivity.this, "Shop ID not found for vendor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VendorActivity.this, "Failed to load vendor details", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchOrders() {
        vendorRef.child("Orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    OrderModel model = snap.getValue(OrderModel.class);
                    if (model != null && model.getItem() != null) {
                        if (!"Delivered".equalsIgnoreCase(model.getStatus())) {
                            model.setOrderId(snap.getKey());
                            model.setShopId(shopId);
                            orderList.add(model);
                        }
                        if ("Out for Delivery".equalsIgnoreCase(model.getStatus())) {
                            fetchUserInfo(model.getUid(), new OnUserInfoFetchedListener() {
                                @Override
                                public void onUserInfoFetched(String userName, String userAddress) {
                                    DatabaseReference deliveryRef = FirebaseDatabase.getInstance()
                                            .getReference("DeliveryOrders")
                                            .child(model.getOrderId());

                                    Map<String, Object> deliveryData = new HashMap<>();
                                    deliveryData.put("orderId", model.getOrderId());
                                    deliveryData.put("shopId", shopId);
                                    deliveryData.put("shopName", binding.textShopName.getText().toString());
                                    deliveryData.put("shopAddress", shopAddress != null ? shopAddress : "Not available");
                                    deliveryData.put("customerName", userName);
                                    deliveryData.put("customerAddress", userAddress);
                                    deliveryData.put("deliveryDate", model.getDate());
                                    deliveryData.put("deliveryTime", model.getDeliveryTime());
                                    deliveryData.put("status", model.getStatus());

                                    deliveryRef.setValue(deliveryData);
                                }
                            });
                        }

                    }
                }
                if (orderList.isEmpty()) {
                    binding.textNoOrders.setVisibility(View.VISIBLE);
                } else {
                    binding.textNoOrders.setVisibility(View.GONE);
                }

                ordersAdapter.updateList(orderList);
                binding.totalOrders.setText(String.valueOf(orderList.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VendorActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
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
                        model.setId(model.getItemId());
                        if (model.getPicUrl() != null && !model.getPicUrl().isEmpty()) {
                            model.setImageUrl(model.getPicUrl().get(0));
                        }
                        productList.add(model);
                    }
                }
                if (productList.isEmpty()) {
                    binding.textNoProducts.setVisibility(View.VISIBLE);
                } else {
                    binding.textNoProducts.setVisibility(View.GONE);
                }

                productListAdapter.updateList(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VendorActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchCustomRequests() {
        vendorRef.child("CustomRequests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    CustomRequestModel model = snap.getValue(CustomRequestModel.class);
                    if (model != null) {
                        requestList.add(model);
                    }
                }
                if (requestList.isEmpty()) {
                    binding.textNoRequests.setVisibility(View.VISIBLE);
                } else {
                    binding.textNoRequests.setVisibility(View.GONE);
                }

                requestAdapter.updateList(requestList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VendorActivity.this, "Failed to load custom requests", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchPreviousOrders() {
        Log.d("FETCH_PREV", "Fetching previous orders...");
        vendorRef.child("Orders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                previousOrdersList.clear();
                int deliveredCount = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    OrderModel model = snap.getValue(OrderModel.class);

                    if (model != null && model.getItem() != null) {
                        String status = model.getStatus() != null ? model.getStatus().trim().toLowerCase() : "";

                        Log.d("FETCH_PREV", "OrderID: " + snap.getKey() + " | Status: " + status);

                        if (status.equals("delivered")) {
                            model.setOrderId(snap.getKey());
                            model.setShopId(shopId);
                            previousOrdersList.add(model);
                            deliveredCount++;

                            String orderId = snap.getKey();
                            model.setOrderId(orderId);
                            model.setShopId(shopId);

                            //fetchFeedbackForOrder(orderId, model);

                            previousOrdersList.add(model);
                            deliveredCount++;

                        }
                    }
                }

                Log.d("FETCH_PREV", "Delivered Orders Found: " + deliveredCount);
                previousOrdersAdapter.updateList(previousOrdersList);
                binding.deliveredOrders.setText("Delivered: " + deliveredCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FETCH_PREV", "Firebase error: " + error.getMessage());
            }
        });
        if (previousOrdersList.isEmpty()) {
            binding.textNoPrevOrders.setVisibility(View.VISIBLE);
        } else {
            binding.textNoPrevOrders.setVisibility(View.GONE);
        }
    }
    private void loadShopName() {
        if (shopId == null || shopId.isEmpty()) {
            Log.e("DEBUG", "Shop ID is null or empty");
            return;
        }

        Log.d("DEBUG", "Fetching sellerName for shopId: " + shopId);

        DatabaseReference shopNameRef = FirebaseDatabase.getInstance()
                .getReference("Shops")
                .child(shopId)
                .child("sellerName");

        shopNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String sellerName = snapshot.getValue(String.class);
                    Log.d("DEBUG", "Fetched sellerName: " + sellerName);
                    binding.textShopName.setText(sellerName);
                } else {
                    Log.e("DEBUG", "sellerName not found under shopId: " + shopId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DEBUG", "Failed to fetch sellerName: " + error.getMessage());
            }
        });
    }
   /* private void fetchShopName(String shopId) {
        Log.d("DEBUG", "Fetching sellerName for shopId: " + shopId);

        DatabaseReference shopNameRef = FirebaseDatabase.getInstance()
                .getReference("Shops")
                .child(shopId)
                .child("sellerName");

        shopNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String sellerName = snapshot.getValue(String.class);
                    Log.d("DEBUG", "Fetched sellerName: " + sellerName);
                    binding.textShopName.setText(sellerName);
                } else {
                    Log.e("DEBUG", "sellerName not found under shopId: " + shopId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DEBUG", "Failed to fetch sellerName: " + error.getMessage());
            }
        });
    }*/
    private void loadShopAddress() {
        DatabaseReference shopAddressRef = FirebaseDatabase.getInstance()
                .getReference("Shops")
                .child(shopId)
                .child("address");

        shopAddressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    shopAddress = snapshot.getValue(String.class);
                    Log.d("DEBUG", "Fetched shopAddress: " + shopAddress);
                } else {
                    Log.e("DEBUG", "shopAddress not found under shopId: " + shopId);
                    shopAddress = "Unknown address";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DEBUG", "Failed to fetch shopAddress: " + error.getMessage());
            }
        });
    }
    private void fetchVendorLocationAndSave() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                vendorLat = location.getLatitude();
                vendorLng = location.getLongitude();

                Log.d("LOCATION", "Fetched LatLng: " + vendorLat + ", " + vendorLng);

                DatabaseReference shopRef = FirebaseDatabase.getInstance().getReference("Shops").child(shopId);
                Map<String, Object> locationMap = new HashMap<>();
                locationMap.put("latitude", vendorLat);
                locationMap.put("longitude", vendorLng);

                shopRef.updateChildren(locationMap)
                        .addOnSuccessListener(aVoid -> Log.d("LOCATION", "Vendor location saved"))
                        .addOnFailureListener(e -> Log.e("LOCATION", "Failed to save location", e));
            } else {
                Log.e("LOCATION", "Location is null");
            }
        });
    }

    private void fetchUserInfo(String userId, OnUserInfoFetchedListener listener) {
        if (userId == null || userId.trim().isEmpty()) {
            Log.e("VendorActivity", "fetchUserInfo: userId is null or empty");
            listener.onUserInfoFetched("Unknown User", "Unknown Address");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String address = snapshot.child("address1").getValue(String.class);
                    listener.onUserInfoFetched(name != null ? name : "Unknown", address != null ? address : "Unknown");
                } else {
                    listener.onUserInfoFetched("Unknown User", "Unknown Address");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onUserInfoFetched("Error User", "Error Address");
            }
        });
    }

    interface OnUserInfoFetchedListener {
        void onUserInfoFetched(String userName, String userAddress);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchVendorLocationAndSave();
        } else {
            Toast.makeText(this, "Location permission is required to save your location.", Toast.LENGTH_SHORT).show();
        }
    }

}
