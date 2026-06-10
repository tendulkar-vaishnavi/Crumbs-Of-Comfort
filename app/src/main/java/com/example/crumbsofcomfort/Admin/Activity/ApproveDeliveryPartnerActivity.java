package com.example.crumbsofcomfort.Admin.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.crumbsofcomfort.Admin.Model.DeliveryModel;
import com.example.crumbsofcomfort.Admin.Adapter.DeliveryApprovalAdapter;
import com.example.crumbsofcomfort.databinding.ActivityApproveDeliveryPartnerBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ApproveDeliveryPartnerActivity extends AppCompatActivity {

    private ActivityApproveDeliveryPartnerBinding binding;
    private ArrayList<DeliveryModel> deliveryList;
    private DeliveryApprovalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityApproveDeliveryPartnerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        deliveryList = new ArrayList<>();
        adapter = new DeliveryApprovalAdapter(this, deliveryList);

        binding.recyclerDelivery.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerDelivery.setAdapter(adapter);

        loadPendingDeliveryPartners();
    }

    private void loadPendingDeliveryPartners() {
        binding.progressBar.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference("DeliveryRequests")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        deliveryList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            DeliveryModel delivery = dataSnapshot.getValue(DeliveryModel.class);
                            if (delivery != null) {
                                delivery.setUid(dataSnapshot.getKey());
                                deliveryList.add(delivery);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);

                        /*if (deliveryList.isEmpty()) {
                            binding.txtNoPending.setVisibility(View.VISIBLE);
                        } else {
                            binding.txtNoPending.setVisibility(View.GONE);
                        }*/
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(ApproveDeliveryPartnerActivity.this,
                                "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
