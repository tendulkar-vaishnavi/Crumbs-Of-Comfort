package com.example.crumbsofcomfort.Admin.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crumbsofcomfort.Admin.Model.VendorAModel;
import com.example.crumbsofcomfort.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class VendorApproval extends RecyclerView.Adapter<VendorApproval.ViewHolder> {
    private Context context;
    private ArrayList<VendorAModel> vendorList;

    public VendorApproval(Context context, ArrayList<VendorAModel> vendorList) {
        this.context = context;
        this.vendorList = vendorList;
    }

    @NonNull
    @Override
    public VendorApproval.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_approve_vendor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VendorApproval.ViewHolder holder, int position) {
        VendorAModel vendor = vendorList.get(position);

        holder.txtShopName.setText(vendor.getShopName());
        holder.txtEmail.setText(vendor.getEmail());
        holder.txtReg.setText("Shop Reg: " + vendor.getShopReg());
        holder.txtAadhar.setText("Aadhar: " + vendor.getAadhaar());
        holder.txtPhoneNumber.setText("Phone Numer: "+vendor.getPhoneNum());

        holder.btnApprove.setOnClickListener(v -> {
            vendor.setApproved(true);
            //vendor.setRole("vendor");

            FirebaseDatabase.getInstance().getReference("Users")
                    .child(vendor.getUid())
                    .setValue(vendor.toMap())
                    .addOnSuccessListener(unused -> {
                        FirebaseDatabase.getInstance().getReference("VendorRequests")
                                .child(vendor.getUid())
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Vendor approved", Toast.LENGTH_SHORT).show();
                                    vendorList.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Failed to remove vendor request: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Approval failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

    }

    @Override
    public int getItemCount() {
        return vendorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtShopName, txtEmail, txtReg, txtAadhar,txtPhoneNumber;
        Button btnApprove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtShopName = itemView.findViewById(R.id.txtShopName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtReg = itemView.findViewById(R.id.txtShopReg);
            txtAadhar = itemView.findViewById(R.id.txtAdhaar);
            txtPhoneNumber = itemView.findViewById(R.id.txtPhoneNumber);
            btnApprove = itemView.findViewById(R.id.btnApprove);
        }
    }
}
