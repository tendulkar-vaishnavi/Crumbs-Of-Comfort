package com.example.crumbsofcomfort.Delivery.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.Api.LocationIQConstants;
import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.databinding.ActivityTrackDeliveryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TrackDeliveryActivity extends AppCompatActivity {

    private ActivityTrackDeliveryBinding binding;
    private String deliveryId;
    private final String locationIQApiKey = "pk.d9522a8c638ce1672dd6493a8d7dd380";
    private final Handler handler = new Handler();
    private final int REFRESH_INTERVAL = 10000; // 10 seconds
    private String orderId;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (orderId != null) {
                loadMapWithMarkers();
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrackDeliveryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        deliveryId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fetchOrderIdFromFirebase();
    }

    private void fetchOrderIdFromFirebase() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance()
                .getReference("Deliveries")
                .child(deliveryId)
                .child("Orders");

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(TrackDeliveryActivity.this, "No orders found for this delivery", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    orderId = orderSnap.getKey();
                    Log.d("ORDER_ID_FOUND", "Order ID: " + orderId);
                    handler.postDelayed(refreshRunnable, 2000);
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrackDeliveryActivity.this, "Failed to fetch order ID", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadMapWithMarkers() {
        DatabaseReference liveLocationRef = FirebaseDatabase.getInstance()
                .getReference("LiveLocations")
                .child(deliveryId);

        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("Deliveries")
                .child(deliveryId)
                .child("Orders")
                .child(orderId);

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot orderSnapshot) {
                String shopId = orderSnapshot.child("shopId").getValue(String.class);
                if (shopId == null) {
                    Toast.makeText(TrackDeliveryActivity.this, "Shop ID not found for this order", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference userLocationRef = FirebaseDatabase.getInstance()
                        .getReference("Deliveries")
                        .child(deliveryId)
                        .child("Orders")
                        .child(orderId);

                liveLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot liveSnapshot) {
                        Double deliveryLat = liveSnapshot.child("latitude").getValue(Double.class);
                        Double deliveryLon = liveSnapshot.child("longitude").getValue(Double.class);

                        if (deliveryLat == null || deliveryLon == null) {
                            Toast.makeText(TrackDeliveryActivity.this, "Missing delivery location", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        userLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                Double userLat = userSnapshot.child("userLatitude").getValue(Double.class);
                                Double userLon = userSnapshot.child("userLongitude").getValue(Double.class);

                                if (userLat == null || userLon == null) {
                                    Toast.makeText(TrackDeliveryActivity.this, "Missing user location", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String url = "https://maps.locationiq.com/v3/staticmap?key=" + locationIQApiKey +
                                        "&center=" + deliveryLat + "," + deliveryLon +
                                        "&zoom=15&size=600x400" +
                                        "&markers=icon:large-red-cutout|" + deliveryLat + "," + deliveryLon +
                                        "&markers=icon:large-green-cutout|" + userLat + "," + userLon;
                                Log.d("MAP_URL", url);

                                Glide.with(TrackDeliveryActivity.this).load(url).into(binding.mapView);
                                fetchETAandDistance(deliveryLat, deliveryLon, userLat, userLon);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(TrackDeliveryActivity.this, "Failed to fetch user location", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TrackDeliveryActivity.this, "Failed to fetch delivery location", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrackDeliveryActivity.this, "Failed to fetch shopId", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void fetchETAandDistance(double startLat, double startLng, double endLat, double endLng) {
        String url = "https://us1.locationiq.com/v1/directions/driving/" +
                startLng + "," + startLat + ";" + endLng + "," + endLat +
                "?key=" + LocationIQConstants.LOCATIONIQ_API_KEY +
                "&overview=false";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(TrackDeliveryActivity.this, "Failed to fetch ETA", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        JSONArray routes = json.getJSONArray("routes");
                        JSONObject legs = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0);

                        double distanceInMeters = legs.getDouble("distance");
                        double durationInSeconds = legs.getDouble("duration");

                        String etaText = String.format(Locale.getDefault(),
                                "%.1f km, %.0f min",
                                distanceInMeters / 1000.0,
                                durationInSeconds / 60.0);

                        runOnUiThread(() -> {
                            TextView etaTextView = findViewById(R.id.etaTextView);
                            etaTextView.setText("ETA: " + etaText);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(refreshRunnable);
    }
}
