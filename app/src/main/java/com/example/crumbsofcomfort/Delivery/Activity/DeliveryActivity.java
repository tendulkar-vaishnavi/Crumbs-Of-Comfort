package com.example.crumbsofcomfort.Delivery.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.crumbsofcomfort.Delivery.Adapter.DeliveryOrdersAdapter;
import com.example.crumbsofcomfort.Delivery.Model.DeliveryOrderModel;
import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.User.Activity.LoginActivity;
import com.example.crumbsofcomfort.User.Activity.ProfileActivity;
import com.example.crumbsofcomfort.databinding.ActivityDeliveryBinding;
import com.example.crumbsofcomfort.databinding.DrawerHeaderBinding;
import com.google.android.gms.location.LocationCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.Manifest;
import android.content.pm.PackageManager;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;

public class DeliveryActivity extends AppCompatActivity {

    private ActivityDeliveryBinding binding;
    private ArrayList<DeliveryOrderModel> orderList;
    private DeliveryOrdersAdapter adapter;
    private String currentDeliveryPartnerUid;
    private boolean isAvailable = true;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private String currentAssignedOrderId;

    private LocationCallback locationCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliveryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getCurrentLocation();


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        startLocationUpdatesToFirebase();

        currentDeliveryPartnerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        orderList = new ArrayList<>();
        adapter = new DeliveryOrdersAdapter(this, orderList, DeliveryOrdersAdapter.MODE_ASSIGNED);

        binding.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.ordersRecyclerView.setAdapter(adapter);

        setupSpinner();

        binding.btnRefresh.setOnClickListener(v -> {
            orderList.clear();
            loadAssignedOrders();
        });

        binding.btnToggleAvailability.setOnClickListener(v -> {
            isAvailable = !isAvailable;
            binding.btnToggleAvailability.setText(isAvailable ? "Mark as Unavailable" : "Mark as Available");

            FirebaseDatabase.getInstance().getReference("DeliveryPartners")
                    .child(currentDeliveryPartnerUid)
                    .child("available")
                    .setValue(isAvailable);
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
                startActivity(new Intent(DeliveryActivity.this, ProfileActivity.class));
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(DeliveryActivity.this, LoginActivity.class));
                finish();
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        View headerView = binding.navigationView.getHeaderView(0);
        DrawerHeaderBinding headerBinding = DrawerHeaderBinding.bind(headerView);

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(currentDeliveryPartnerUid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                if (name != null) {
                    headerBinding.navHeaderName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DeliveryActivity.this, "Failed to load profile info", Toast.LENGTH_SHORT).show();
            }
        });
    }
    /*private void startLocationUpdates(String orderId) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    DatabaseReference locRef = FirebaseDatabase.getInstance()
                            .getReference("Deliveries")
                            .child(orderId)
                            .child("currentLocation");

                    Map<String, Object> locMap = new HashMap<>();
                    locMap.put("latitude", lat);
                    locMap.put("longitude", lng);

                    locRef.setValue(locMap);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }*/
    private void startLocationUpdatesToFirebase() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    String deliveryPartnerId = FirebaseAuth.getInstance().getUid();
                    DatabaseReference locRef = FirebaseDatabase.getInstance()
                            .getReference("LiveLocations")
                            .child(deliveryPartnerId);

                    Map<String, Object> locationMap = new HashMap<>();
                    locationMap.put("latitude", lat);
                    locationMap.put("longitude", lng);
                    locationMap.put("timestamp", System.currentTimeMillis());

                    locRef.setValue(locationMap)
                            .addOnSuccessListener(aVoid ->
                                    Log.d("LIVE_LOCATION", "Location updated"))
                            .addOnFailureListener(e ->
                                    Log.e("LIVE_LOCATION", "Failed to update location", e));
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }
    private void setupSpinner() {
        String[] deliveryOptions = {"Assigned Deliveries", "Available Orders", "Delivered Orders"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deliveryOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.filterSpinner.setAdapter(spinnerAdapter);

        binding.filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                orderList.clear();
                switch (position) {
                    case 0:
                        adapter.setDisplayMode(DeliveryOrdersAdapter.MODE_ASSIGNED);
                        loadAssignedOrders();
                        break;
                    case 1:
                        adapter.setDisplayMode(DeliveryOrdersAdapter.MODE_AVAILABLE);
                        getCurrentLocation();
                        break;
                    case 2:
                        adapter.setDisplayMode(DeliveryOrdersAdapter.MODE_DELIVERED);
                        loadDeliveredOrders();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    private void uploadLiveLocation(String orderId, double latitude, double longitude) {
        if (latitude == 0.0 || longitude == 0.0) {
            Log.e("LOCATION_UPLOAD", "Invalid location (0.0). Skipping update.");
            return;
        }

        String deliveryPartnerId = FirebaseAuth.getInstance().getUid();

        if (deliveryPartnerId == null || orderId == null) {
            Log.e("LOCATION_UPLOAD", "Missing UID or Order ID.");
            return;
        }

        // ✅ Update under Deliveries/{deliveryPartnerId}/Orders/{orderId}
        DatabaseReference orderLocationRef = FirebaseDatabase.getInstance()
                .getReference("Deliveries")
                .child(deliveryPartnerId)
                .child("Orders")
                .child(orderId);

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("deliveryLatitude", latitude);
        locationData.put("deliveryLongitude", longitude);

        orderLocationRef.updateChildren(locationData)
                .addOnSuccessListener(aVoid -> Log.d("LOCATION_UPLOAD", "Updated in delivery partner order path"))
                .addOnFailureListener(e -> Log.e("LOCATION_UPLOAD", "Failed to update delivery path", e));

        // (Optional) 🔄 Also update global order-based path (if you still need it)
        DatabaseReference globalRef = FirebaseDatabase.getInstance()
                .getReference("Deliveries")
                .child(orderId);

        globalRef.updateChildren(locationData)
                .addOnSuccessListener(aVoid -> Log.d("LOCATION_UPLOAD", "Also updated in global order path"))
                .addOnFailureListener(e -> Log.e("LOCATION_UPLOAD", "Failed to update global path", e));
    }


    private void loadAssignedOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Vendors");
        final int[] todaysDeliveries = {0};
        final int[] activeOrders = {0};
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                List<DeliveryOrderModel> tempList = new ArrayList<>();
                String currentDeliveryId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                for (DataSnapshot shopSnap : snapshot.getChildren()) {
                    String shopId = shopSnap.getKey();

                    DataSnapshot ordersSnap = shopSnap.child("Orders");
                    for (DataSnapshot orderSnap : ordersSnap.getChildren()) {
                        String shopAddress = orderSnap.child("shopAddress").getValue(String.class);
                        String assignedPartnerId = orderSnap.child("assignedTo").getValue(String.class);

                        if (currentDeliveryId.equals(assignedPartnerId)) {
                            DeliveryOrderModel order = orderSnap.getValue(DeliveryOrderModel.class);
                            if (order != null) {
                                order.setOrderId(orderSnap.getKey());
                                order.setShopId(shopId);
                                order.setShopAddress(shopAddress);
                                order.setUserAddress(orderSnap.child("userAddress").getValue(String.class));

                                if (today.equals(order.getDate())) todaysDeliveries[0]++;
                                if (!"Delivered".equalsIgnoreCase(order.getStatus())) activeOrders[0]++;

                                tempList.add(order);
                            }
                        }
                    }
                }
                updateUI(tempList, todaysDeliveries[0], activeOrders[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(DeliveryActivity.this, "Failed to load assigned orders", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateUI(List<DeliveryOrderModel> orders, int todaysDeliveries, int activeOrders) {
        orderList.clear();
        orderList.addAll(orders);
        adapter.notifyDataSetChanged();
        binding.textTodaysDeliveries.setText(String.valueOf(todaysDeliveries));
        binding.textActiveOrders.setText(String.valueOf(activeOrders));
        binding.noOrdersText.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
        binding.progressBar.setVisibility(View.GONE);
    }
    private void loadAvailableOrders() {
        FirebaseDatabase.getInstance().getReference("Vendors")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();
                        for (DataSnapshot vendorSnap : snapshot.getChildren()) {
                            if (vendorSnap.hasChild("Orders")) {
                                for (DataSnapshot orderSnap : vendorSnap.child("Orders").getChildren()) {
                                    DeliveryOrderModel order = orderSnap.getValue(DeliveryOrderModel.class);
                                    String status = orderSnap.child("status").getValue(String.class);
                                    String assignedTo = orderSnap.child("deliveryPartnerId").getValue(String.class);

                                    if (order != null &&
                                            "Prepared".equalsIgnoreCase(status) &&
                                            (assignedTo == null || assignedTo.isEmpty())) {

                                        order.setOrderId(orderSnap.getKey());
                                        orderList.add(order);
                                    }
                                }
                            }
                        }

                        adapter.notifyDataSetChanged();
                        binding.noOrdersText.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DeliveryActivity.this, "Failed to load available orders", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loadDeliveredOrders() {
        FirebaseDatabase.getInstance().getReference("Vendors")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();
                        for (DataSnapshot vendorSnap : snapshot.getChildren()) {
                            if (vendorSnap.hasChild("Orders")) {
                                for (DataSnapshot orderSnap : vendorSnap.child("Orders").getChildren()) {
                                    DeliveryOrderModel order = orderSnap.getValue(DeliveryOrderModel.class);
                                    if (order != null &&
                                            currentDeliveryPartnerUid.equals(order.getAssignedTo()) &&
                                            "Delivered".equalsIgnoreCase(order.getStatus())) {
                                        order.setOrderId(orderSnap.getKey());
                                        orderList.add(order);
                                    }
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        binding.noOrdersText.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DeliveryActivity.this, "Failed to load delivered orders", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void getCurrentLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showLocationExplanationDialog();
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLat = location.getLatitude();
                        currentLng = location.getLongitude();
                        Log.d("LOCATION", "Lat: " + currentLat + ", Lng: " + currentLng);
                        loadNearbyOrders();
                    } else {
                        showLocationEnableDialog();
                    }
                });
    }
    private void showLocationEnableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_enable_location, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        AppCompatButton btnCancel = view.findViewById(R.id.btnCancel);
        AppCompatButton btnSettings = view.findViewById(R.id.btnSettings);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSettings.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });

        dialog.show();
    }
    private void loadNearbyOrders() {
        FirebaseDatabase.getInstance().getReference("Vendors")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();
                        for (DataSnapshot vendorSnap : snapshot.getChildren()) {
                            String shopId = vendorSnap.getKey();


                            if (vendorSnap.hasChild("Orders")) {
                                for (DataSnapshot orderSnap : vendorSnap.child("Orders").getChildren()) {
                                    String shopAddress = orderSnap.child("shopAddress").getValue(String.class);
                                    String status = orderSnap.child("status").getValue(String.class);
                                    String assignedTo = orderSnap.child("deliveryPartnerId").getValue(String.class);

                                    Double shopLat = orderSnap.child("vendorLatitude").getValue(Double.class);
                                    Double shopLng = orderSnap.child("vendorLongitude").getValue(Double.class);

                                    if ("Prepared".equalsIgnoreCase(status) &&
                                            (assignedTo == null || assignedTo.isEmpty()) &&
                                            shopLat != null && shopLng != null) {

                                        float[] results = new float[1];
                                        Location.distanceBetween(currentLat, currentLng, shopLat, shopLng, results);
                                        float distanceInMeters = results[0];

                                        if (distanceInMeters <= 5000) {
                                            DeliveryOrderModel order = orderSnap.getValue(DeliveryOrderModel.class);
                                            if (order != null) {
                                                order.setOrderId(orderSnap.getKey());
                                                order.setShopId(shopId);
                                                order.setShopAddress(shopAddress);
                                                orderList.add(order);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        adapter.notifyDataSetChanged();
                        binding.noOrdersText.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DeliveryActivity.this, "Failed to load nearby orders", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void showLocationExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Location Access Needed")
                .setMessage("To show nearby delivery orders, we need access to your location.")
                .setPositiveButton("Allow", (dialog, which) -> requestLocationPermissions())
                .setNegativeButton("Deny", (dialog, which) ->
                        Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show()
                )
                .show();
    }
    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }
    public void onOrderAccepted(String orderId, String shopId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(500)
                .setNumUpdates(1);  // One-time update; adjust for ongoing if needed

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e("LOCATION_ERROR", "No location result available");
                    Toast.makeText(DeliveryActivity.this, "Unable to get location. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    Log.d("LOCATION_RESULT", "lat=" + lat + ", lon=" + lon);

                    // Directly upload using the passed orderId (no null check)
                    uploadLiveLocation(orderId, lat, lon);
                }
            }
        }, Looper.getMainLooper());
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Permission denied. Can't access nearby orders.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }




}
