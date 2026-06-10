package com.example.crumbsofcomfort.Admin.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.crumbsofcomfort.Admin.Adapter.VendorApproval;
import com.example.crumbsofcomfort.Admin.Model.VendorAModel;
import com.example.crumbsofcomfort.databinding.ActivityApproveVendorsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ApproveVendorsActivity extends AppCompatActivity {

    private ActivityApproveVendorsBinding binding;
    private ArrayList<VendorAModel> vendorList;
    private VendorApproval adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityApproveVendorsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vendorList = new ArrayList<>();
        adapter = new VendorApproval(this, vendorList);

        binding.recyclerVendors.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerVendors.setHasFixedSize(true);
        binding.recyclerVendors.setAdapter(adapter);

        loadPendingVendors();
    }

    private void loadPendingVendors() {
        binding.progressBar.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference("VendorRequests")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        vendorList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            VendorAModel vendor = ds.getValue(VendorAModel.class);
                            if (vendor != null) {
                                vendor.setUid(ds.getKey());
                                vendorList.add(vendor);
                            }
                        }

                        adapter.notifyDataSetChanged();
                        binding.progressBar.setVisibility(View.GONE);

                        if (vendorList.isEmpty()) {
                            Toast.makeText(ApproveVendorsActivity.this, "No pending vendor approvals.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(ApproveVendorsActivity.this, "Failed to load vendors: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
