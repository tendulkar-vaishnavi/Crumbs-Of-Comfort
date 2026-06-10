package com.example.crumbsofcomfort.User.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.crumbsofcomfort.databinding.ActivityFeedBackBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class FeedbackActivity extends AppCompatActivity {
    ActivityFeedBackBinding binding;
    String shopId, orderId, itemId;
    DatabaseReference feedbackRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedBackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        shopId = getIntent().getStringExtra("shopId");
        orderId = getIntent().getStringExtra("orderId");
        itemId = getIntent().getStringExtra("itemId");

        feedbackRef = FirebaseDatabase.getInstance().getReference()
                .child("Vendors").child(shopId).child("Orders").child(orderId).child("feedback");


        feedbackRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.btnSubmitFeedback.setEnabled(false);
                    binding.btnSubmitFeedback.setAlpha(0.5f);
                    binding.btnSubmitFeedback.setEnabled(false);
                    binding.editTextFeedback.setEnabled(false);
                    binding.ratingBar.setIsIndicator(true);

                    String existingComment = snapshot.child("comment").getValue(String.class);
                    Float existingRating = snapshot.child("rating").getValue(Float.class);

                    if (existingComment != null) binding.editTextFeedback.setText(existingComment);
                    if (existingRating != null) binding.ratingBar.setRating(existingRating);

                    Toast.makeText(FeedbackActivity.this, "Feedback already submitted", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FEEDBACK", "Feedback check error: " + error.getMessage());
            }
        });


        binding.btnSubmitFeedback.setOnClickListener(v -> {
            String comment = binding.editTextFeedback.getText().toString().trim();
            float rating = binding.ratingBar.getRating();

            if (rating == 0) {
                Toast.makeText(this, "Please give a star rating", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> feedback = new HashMap<>();
            feedback.put("comment", comment);
            feedback.put("rating", rating);

            feedbackRef.setValue(feedback).addOnSuccessListener(unused -> {
                updateItemRating(shopId, itemId, rating);

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                moveOrderToHistory(userId, orderId);

                Toast.makeText(this, "Feedback submitted", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
            });

        });
    }

    private void updateItemRating(String shopId, String itemId, float newRating) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference()
                .child("Shops")
                .child(shopId)
                .child("items");

        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String currentItemId = itemSnapshot.child("itemId").getValue(String.class);
                    if (currentItemId != null && currentItemId.equals(itemId)) {
                        found = true;

                        double totalRating = itemSnapshot.child("totalRating").getValue(Double.class) != null ?
                                itemSnapshot.child("totalRating").getValue(Double.class) : 0.0;
                        int ratingCount = itemSnapshot.child("ratingCount").getValue(Integer.class) != null ?
                                itemSnapshot.child("ratingCount").getValue(Integer.class) : 0;

                        double updatedTotalRating = totalRating + newRating;
                        int updatedRatingCount = ratingCount + 1;
                        double averageRating = updatedTotalRating / updatedRatingCount;

                        HashMap<String, Object> ratingUpdate = new HashMap<>();
                        ratingUpdate.put("totalRating", updatedTotalRating);
                        ratingUpdate.put("ratingCount", updatedRatingCount);
                        ratingUpdate.put("averageRating", averageRating);

                        itemSnapshot.getRef().updateChildren(ratingUpdate)
                                .addOnSuccessListener(unused -> Log.d("FEEDBACK", "✅ Rating updated"))
                                .addOnFailureListener(e -> Log.e("FEEDBACK", "❌ Update failed: " + e.getMessage()));
                        break;
                    }
                }

                if (!found) {
                    Log.e("FEEDBACK", "❌ itemId not found: " + itemId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FEEDBACK", "❌ DB Error: " + error.getMessage());
            }
        });
    }
    private void moveOrderToHistory(String userId, String orderId) {
        DatabaseReference userOrdersRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(userId).child("Orders").child(orderId);

        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(userId).child("OrderHistory").child(orderId);

        userOrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    historyRef.setValue(snapshot.getValue()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            userOrdersRef.removeValue();
                            Log.d("ORDER_HISTORY", "Order moved to history.");
                        } else {
                            Log.e("ORDER_HISTORY", "Failed to move order to history.");
                        }
                    });
                } else {
                    Log.e("ORDER_HISTORY", "Order not found for history move.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ORDER_HISTORY", "DB Error: " + error.getMessage());
            }
        });
    }

}
