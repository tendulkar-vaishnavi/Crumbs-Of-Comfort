package com.example.crumbsofcomfort.Admin.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crumbsofcomfort.Admin.Adapter.FeedbackAdapter;
import com.example.crumbsofcomfort.Admin.Model.FeedbackModel;
import com.example.crumbsofcomfort.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminFeedbackActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textNoFeedback;
    private FeedbackAdapter adapter;
    private List<FeedbackModel> feedbackList;

    private DatabaseReference feedbackRef;
    private DatabaseReference vendorRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_feedback);

        recyclerView = findViewById(R.id.recyclerView);
        textNoFeedback = findViewById(R.id.textNoFeedback);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedbackList = new ArrayList<>();
        adapter = new FeedbackAdapter(this, feedbackList);
        recyclerView.setAdapter(adapter);

        feedbackRef = FirebaseDatabase.getInstance().getReference("Feedbacks");
        vendorRef = FirebaseDatabase.getInstance().getReference("Vendors");

        fetchFeedbacks();
    }

    private void fetchFeedbacks() {
        vendorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot vendorsSnapshot) {
                feedbackList.clear();

                for (DataSnapshot vendorSnap : vendorsSnapshot.getChildren()) {
                    // vendorSnap = e.g. "shp1_7thHeaven"
                    DataSnapshot ordersSnap = vendorSnap.child("Orders");

                    for (DataSnapshot orderSnap : ordersSnap.getChildren()) {

                        // Only proceed if feedback exists
                        if (orderSnap.hasChild("feedback")) {
                            String shopName = orderSnap.child("shopName").getValue(String.class);
                            String comment = orderSnap.child("feedback").child("comment").getValue(String.class);

                            Float rating = null;
                            Object ratingObj = orderSnap.child("feedback").child("rating").getValue();
                            if (ratingObj instanceof Double) {
                                rating = ((Double) ratingObj).floatValue();
                            } else if (ratingObj instanceof Long) {
                                rating = ((Long) ratingObj).floatValue();
                            }

                            String date = orderSnap.child("date").getValue(String.class);

                            if (rating != null) {
                                FeedbackModel model = new FeedbackModel();
                                model.setShopName(shopName != null ? shopName : "Unknown Shop");
                                model.setFeedback(comment != null ? comment : "No comment");
                                model.setRating(rating);
                                model.setDate(date != null ? date : "");
                                feedbackList.add(model);
                            }
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                toggleNoDataView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminFeedbackActivity.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleNoDataView() {
        if (feedbackList.isEmpty()) {
            showNoData();
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textNoFeedback.setVisibility(View.GONE);
        }
    }

    private void showNoData() {
        recyclerView.setVisibility(View.GONE);
        textNoFeedback.setVisibility(View.VISIBLE);
    }
}
