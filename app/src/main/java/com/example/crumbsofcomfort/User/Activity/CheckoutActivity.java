package com.example.crumbsofcomfort.User.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.User.Helper.TinyDB;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.databinding.ActivityCheckoutBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Calendar;


public class CheckoutActivity extends AppCompatActivity implements PaymentResultListener {

    private ActivityCheckoutBinding binding;
    private TinyDB tinyDB;
    private String userPhone, userAddress,userName,userId;

    private ArrayList<ItemsModel> cartList;
    private double subtotal, tax, deliveryFee, totalAmount;

    private String pendingAddress, pendingDate, pendingTime;
    private boolean ordersPlacedSuccessfully = false;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar selectedTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = systemBars.bottom + 3;
            v.setLayoutParams(params);
            return insets;
        });

        Checkout.preload(getApplicationContext());

        tinyDB = new TinyDB(this);
        userPhone = tinyDB.getString("userPhone");
        userAddress = tinyDB.getString("userAddress");

        Intent intent = getIntent();
        cartList = (ArrayList<ItemsModel>) intent.getSerializableExtra("cartList");
        subtotal = intent.getDoubleExtra("subtotal", 0.0);
        tax = intent.getDoubleExtra("tax", 0.0);
        deliveryFee = intent.getDoubleExtra("deliveryFee", 0.0);
        totalAmount = intent.getDoubleExtra("totalAmount", 0.0);

        if (userPhone.isEmpty() || userAddress.isEmpty()) {
            Toast.makeText(this, "Please complete your profile first.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
            return;
        }

        binding.tvPhoneNumber.setText("Phone: " + userPhone);
        binding.tvAddress.setText("Address: " + userAddress);
        binding.tvSubtotal.setText("₹" + subtotal);
        binding.tvTax.setText("₹" + tax);
        binding.tvDeliveryFee.setText("₹" + deliveryFee);
        binding.tvTotal.setText("₹" + totalAmount);

        setupDatePicker();
        setupTimePicker();


        binding.btnConfirmAndPay.setOnClickListener(v -> {
            String selectedDate = binding.tvSelectedDate.getText().toString().trim();
            String selectedTime = binding.tvSelectedTime.getText().toString().trim();

            if (selectedDate.isEmpty()) {
                showValidationError("Please select a delivery date");
                return;
            }
            if (selectedTime.isEmpty()) {
                showValidationError("Please select a delivery time");
                return;
            }

            pendingDate = selectedDate;
            pendingTime = selectedTime;
            pendingAddress = userAddress;

            fetchUserInfo((name, address) -> {
                userName = name;
                startPayment();
            });
        });
    }
    private void setupDatePicker() {
        binding.tvSelectedDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();

            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                    (view, year, monthOfYear, dayOfMonth) -> {
                        selectedDate.set(year, monthOfYear, dayOfMonth);
                        String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        binding.tvSelectedDate.setText(date);
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.setMinDate(now);
            datePickerDialog.show(getSupportFragmentManager(), "DatePickerDialog");
        });
    }

    private void setupTimePicker() {
        binding.tvSelectedTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();

            TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                    (view, hourOfDay, minute, second) -> {
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);
                        selectedTime.set(Calendar.SECOND, 0);

                        String selected = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        binding.tvSelectedTime.setText(selected);
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );
            int minHour = 8;
            int maxHour = 22;
            int minMin = 0;

            if (isSameDay(selectedDate, now)) {
                now.add(Calendar.MINUTE, 30); // 30 min buffer
                minHour = now.get(Calendar.HOUR_OF_DAY);
                minMin = now.get(Calendar.MINUTE);

                if (minHour >= maxHour) {
                    Toast.makeText(this, "Too late to place order for today. Choose another date.", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            timePickerDialog.setMinTime(minHour, minMin, 0);
            timePickerDialog.setMaxTime(maxHour, 0, 0);

            timePickerDialog.show(getSupportFragmentManager(), "TimePickerDialog");
        });
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void startPayment() {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_1DP5mmOlF5G5ag");
        checkout.setFullScreenDisable(true);

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Crumbs of Comfort");
            options.put("description", "Order Payment");
            options.put("currency", "INR");
            options.put("amount", (int) (totalAmount * 100));

            JSONObject prefill = new JSONObject();
            prefill.put("contact", userPhone);
            prefill.put("email", "test@example.com");
            options.put("prefill", prefill);

            checkout.open(this, options);

        } catch (Exception e) {
            showValidationBottomSheet("Payment Error", e.getMessage());
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Log.d("Razorpay", "Payment successful: " + razorpayPaymentID);

        placeOrder();
        showValidationBottomSheet("Success", "Payment success");

        new android.os.Handler().postDelayed(() -> {
            if (!CheckoutActivity.this.isFinishing()) {
                if (ordersPlacedSuccessfully) {
                    showValidationBottomSheet("Success", "Payment Successful! Order placed.");
                } else {
                    showValidationBottomSheet("Warning", "Payment done but order not saved.");
                }
            }
        }, 1000);

        finish();
    }

    @Override
    public void onPaymentError(int code, String response) {
        showValidationBottomSheet("Failure", "Payment Failed! " + response);
    }
    private void placeOrder() {
        Log.d("DEBUG_CARTLIST", "Cart size: " + (cartList != null ? cartList.size() : 0));
        if (cartList == null || cartList.isEmpty()) {
            showValidationBottomSheet("Error", "Your cart is empty!");
            return;
        }

        AtomicInteger placedCount = new AtomicInteger(0);

        for (int i = 0; i < cartList.size(); i++) {
            ItemsModel item = cartList.get(i);
            Log.d("ORDER_LOOP", "Processing item: " + item.getTitle());

            String shopId = item.getShopId();
            if (shopId == null || shopId.trim().isEmpty()) {
                Log.e("ORDER_SKIP", "Shop ID missing for item: " + item.getTitle());
                continue;
            }

            String productId = (item.getId() != null && !item.getId().isEmpty())
                    ? item.getId()
                    : "custom";

            String uniqueKey = FirebaseDatabase.getInstance().getReference().push().getKey();
            if (uniqueKey == null) {
                Log.e("ORDER_ERROR", "Failed to generate Firebase push key");
                continue;
            }

            String orderId = productId + "_" + uniqueKey;

            double itemPrice = item.getPrice();
            double tax = itemPrice * 0.05;
            double deliveryFee = 40.0;
            double total = itemPrice + tax + deliveryFee;

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", productId);
            itemMap.put("productName", item.getTitle());
            itemMap.put("imageUrl", item.getPicUrl() != null && !item.getPicUrl().isEmpty() ? item.getPicUrl().get(0) : "");
            itemMap.put("quantity", item.getNumberInCart());
            itemMap.put("weight", item.getSelectedSize());
            itemMap.put("messageOnCake", item.getMessageOnCake());
            itemMap.put("itemId", item.getItemId());
            itemMap.put("eggType", item.getEggType() != null ? item.getEggType() : "");


            Log.d("SHOP_REF", "Fetching shop info for: " + shopId);

            DatabaseReference shopRef = FirebaseDatabase.getInstance().getReference("Shops").child(shopId);
            shopRef.get().addOnSuccessListener(snapshot -> {
                String shopName = snapshot.child("sellerName").getValue(String.class);
                String shopAddress = snapshot.child("address").getValue(String.class);
                Double vendorLat = snapshot.child("latitude").getValue(Double.class);
                Double vendorLng = snapshot.child("longitude").getValue(Double.class);

                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                userRef.get().addOnSuccessListener(userSnap -> {
                    Double userLat = parseDouble(userSnap.child("latitude").getValue(String.class));
                    Double userLng = parseDouble(userSnap.child("longitude").getValue(String.class));

                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderId", orderId);
                    orderMap.put("userPhone", userPhone);
                    orderMap.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    orderMap.put("userName", userName != null ? userName : "User");
                    orderMap.put("date", pendingDate);
                    orderMap.put("userAddress", pendingAddress);
                    orderMap.put("deliveryTime", pendingTime);
                    orderMap.put("totalAmount", total);
                    orderMap.put("status", "Pending");
                    orderMap.put("shopId", shopId);
                    orderMap.put("shopName", shopName != null ? shopName : "");
                    orderMap.put("shopAddress", shopAddress != null ? shopAddress : "");


                    orderMap.put("vendorLatitude", vendorLat != null ? vendorLat : 0.0);
                    orderMap.put("vendorLongitude", vendorLng != null ? vendorLng : 0.0);

                    orderMap.put("userLatitude", userLat != null ? userLat : 0.0);
                    orderMap.put("userLongitude", userLng != null ? userLng : 0.0);

                    orderMap.put("assignedTo", "");
                    orderMap.put("deliveryPartnerId", "");
                    orderMap.put("deliveryStatus", "Not Assigned");
                    orderMap.put("deliveryAssignedTime", "");
                    orderMap.put("item", itemMap);

                    DatabaseReference orderRef = FirebaseDatabase.getInstance()
                            .getReference("Vendors")
                            .child(shopId)
                            .child("Orders")
                            .child(orderId);

                    orderRef.setValue(orderMap)
                            .addOnSuccessListener(unused -> {
                                placedCount.incrementAndGet();
                                ordersPlacedSuccessfully = true;
                                Log.d("ORDER_SUCCESS", "Order placed to: Vendors/" + shopId + "/Orders/" + orderId);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ORDER_FAILED", "Failed to write order for: " + item.getTitle(), e);
                            });

                });
            });
        }
    }
    private void showValidationBottomSheet(String title, String message) {
        if (isFinishing()) return;

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

    private void showValidationError(String message) {
        if (isFinishing()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_validation, null);
        dialog.setContentView(view);

        TextView textMessage = view.findViewById(R.id.textMessage);
        AppCompatButton btnOk = view.findViewById(R.id.btnOk);
        textMessage.setText(message);
        btnOk.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        userPhone = tinyDB.getString("userPhone");
        userAddress = tinyDB.getString("userAddress");
        if (!userPhone.isEmpty()) binding.tvPhoneNumber.setText("Phone: " + userPhone);
        if (!userAddress.isEmpty()) binding.tvAddress.setText("Address: " + userAddress);
    }
    interface OnUserInfoFetchedListener {
        void onUserInfoFetched(String name, String address);
    }

    private void fetchUserInfo(OnUserInfoFetchedListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String address = snapshot.child("address1").getValue(String.class);
                    listener.onUserInfoFetched(name, address);
                } else {
                    listener.onUserInfoFetched("Unknown", "No Address");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onUserInfoFetched("Error", "Error Address");
            }
        });
    }
    private Double parseDouble(String value) {
        try {
            return value != null ? Double.parseDouble(value) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }


}
