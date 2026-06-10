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

import com.example.crumbsofcomfort.Admin.Model.DeliveryModel;
import com.example.crumbsofcomfort.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DeliveryApprovalAdapter extends RecyclerView.Adapter<DeliveryApprovalAdapter.ViewHolder> {

    private Context context;
    private ArrayList<DeliveryModel> deliveryList;

    public DeliveryApprovalAdapter(Context context, ArrayList<DeliveryModel> deliveryList) {
        this.context = context;
        this.deliveryList = deliveryList;
    }

    @NonNull
    @Override
    public DeliveryApprovalAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_delivery_approval, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryApprovalAdapter.ViewHolder holder, int position) {
        DeliveryModel delivery = deliveryList.get(position);

        holder.txtName.setText(delivery.getName());
        holder.txtEmail.setText(delivery.getEmail());
        holder.txtPhone.setText("Phone: " + delivery.getPhone());
        holder.txtAdhaar.setText("Aadhaar: " + delivery.getAadhaar());

        holder.btnApprove.setOnClickListener(v -> {
            delivery.setApproved(true);
            // deliveryPartner.setRole("Delivery Partner");

            FirebaseDatabase.getInstance().getReference("Users")
                    .child(delivery.getUid())
                    .setValue(delivery) // or .setValue(deliveryPartner.toMap()) if you have it
                    .addOnSuccessListener(unused -> {
                        FirebaseDatabase.getInstance().getReference("DeliveryRequests")
                                .child(delivery .getUid())
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Delivery Partner approved", Toast.LENGTH_SHORT).show();
                                    deliveryList.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Failed to remove delivery request: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Approval failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return deliveryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtEmail, txtPhone,txtAdhaar,txtShopReg;
        Button btnApprove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            txtAdhaar = itemView.findViewById(R.id.txtAdhaar);
            btnApprove = itemView.findViewById(R.id.btnApprove);
        }
    }
}
