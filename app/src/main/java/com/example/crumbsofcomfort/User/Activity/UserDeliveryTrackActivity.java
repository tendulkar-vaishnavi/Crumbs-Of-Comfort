package com.example.crumbsofcomfort.User.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.Api.LocationIQConstants;
import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.databinding.ActivityUserDeliveryTrackBinding;
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

public class UserDeliveryTrackActivity extends AppCompatActivity {
    ActivityUserDeliveryTrackBinding binding;
    private String orderId, shopId;
    private String deliveryId;
    private Double userLat, userLon;

    private final Handler handler = new Handler();
    private final int REFRESH_INTERVAL = 10000;
    private final String locationIQApiKey = "pk.d9522a8c638ce1672dd6493a8d7dd380";

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (deliveryId != null && userLat != null && userLon != null) {
                fetchAndUpdateDeliveryLocation();
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDeliveryTrackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra("orderId");
        shopId = getIntent().getStringExtra("shopId");

        if (orderId == null || shopId == null) {
            Toast.makeText(this, "Missing order or shop ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchOrderDetailsAndStartTracking();
    }

    private void fetchOrderDetailsAndStartTracking() {
        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("Vendors")
                .child(shopId)
                .child("Orders")
                .child(orderId);

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(UserDeliveryTrackActivity.this, "Order not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                deliveryId = snapshot.child("deliveryPartnerId").getValue(String.class);
                userLat = snapshot.child("userLatitude").getValue(Double.class);
                userLon = snapshot.child("userLongitude").getValue(Double.class);

                if (deliveryId == null || userLat == null || userLon == null) {
                    Toast.makeText(UserDeliveryTrackActivity.this, "Missing delivery/user location", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Start live tracking
                handler.post(refreshRunnable);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDeliveryTrackActivity.this, "Failed to fetch order", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAndUpdateDeliveryLocation() {
        DatabaseReference deliveryRef = FirebaseDatabase.getInstance()
                .getReference("LiveLocations")
                .child(deliveryId);

        deliveryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double deliveryLat = snapshot.child("latitude").getValue(Double.class);
                Double deliveryLon = snapshot.child("longitude").getValue(Double.class);

                if (deliveryLat == null || deliveryLon == null) {
                    Toast.makeText(UserDeliveryTrackActivity.this, "Delivery location not available", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Load static map
                String mapUrl = "https://maps.locationiq.com/v3/staticmap?key=" + locationIQApiKey +
                        "&center=" + deliveryLat + "," + deliveryLon +
                        "&zoom=15&size=600x400" +
                        "&markers=icon:large-red-cutout|" + deliveryLat + "," + deliveryLon +
                        "&markers=icon:large-green-cutout|" + userLat + "," + userLon;

                Glide.with(UserDeliveryTrackActivity.this)
                        .load(mapUrl)
                        .into(binding.mapImageView);

                fetchETAandDistance(deliveryLat, deliveryLon, userLat, userLon);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDeliveryTrackActivity.this, "Failed to load delivery location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchETAandDistance(double startLat, double startLng, double endLat, double endLng) {
        String url = "https://us1.locationiq.com/v1/directions/driving/" +
                startLng + "," + startLat + ";" + endLng + "," + endLat +
                "?key=" + locationIQApiKey + "&overview=false";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(UserDeliveryTrackActivity.this, "Failed to fetch ETA", Toast.LENGTH_SHORT).show();
                    binding.etaTextView.setText("ETA: -");
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

                        runOnUiThread(() -> binding.etaTextView.setText("ETA: " + etaText));
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> binding.etaTextView.setText("ETA: parsing error"));
                    }
                } else {
                    runOnUiThread(() -> binding.etaTextView.setText("ETA: error"));
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
