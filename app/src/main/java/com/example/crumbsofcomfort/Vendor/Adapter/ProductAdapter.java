package com.example.crumbsofcomfort.Vendor.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.Vendor.Model.ProductModel;
import com.example.crumbsofcomfort.databinding.ViewholderItemProductBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductModel> productList;
    private String shopId;
    private Context context;

    public ProductAdapter(List<ProductModel> productList, String shopId) {
        this.productList = productList;
        this.shopId = shopId;
    }

    public void updateList(List<ProductModel> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderItemProductBinding binding = ViewholderItemProductBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = productList.get(position);

        holder.binding.productID.setText(product.getItemId());
        holder.binding.productName.setText(product.getTitle());
        holder.binding.productPrice.setText("₹" + product.getPrice());

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .into(holder.binding.imgOrder);

        holder.binding.btnDeleteProduct.setOnClickListener(v -> {
            showDeleteConfirmation(holder.itemView.getContext(), () -> {
                deleteProduct(product.getItemId(), holder.getAdapterPosition());
            });
        });


    }

    private void deleteProduct(String itemId, int position) {
        Log.d("DELETE_DEBUG", "Trying to delete: " + itemId + " from shopId: " + shopId);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Shops")
                .child(shopId)
                .child("items")
                .child(itemId);

        ref.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d("DELETE_DEBUG", "Firebase delete success.");
                    productList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("DELETE_DEBUG", "Firebase delete failed: " + e.getMessage());
                    Toast.makeText(context, "Failed to delete product", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ViewholderItemProductBinding binding;

        public ProductViewHolder(@NonNull ViewholderItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    private void showDeleteConfirmation(Context context, Runnable onDeleteConfirmed) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_delete_layout, null);
        dialog.setContentView(view);

        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnDelete = view.findViewById(R.id.btnDelete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            onDeleteConfirmed.run();
        });

        dialog.show();
    }

}
