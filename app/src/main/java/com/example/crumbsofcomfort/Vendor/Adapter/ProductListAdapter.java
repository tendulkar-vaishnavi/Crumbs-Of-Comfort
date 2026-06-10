package com.example.crumbsofcomfort.Vendor.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.Vendor.Model.ProductModel;
import com.example.crumbsofcomfort.databinding.ViewholderItemProductListBinding;

import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductViewHolder> {

    private List<ProductModel> productList;
    private String shopId;
    private Context context;

    public ProductListAdapter(List<ProductModel> productList, String shopId) {
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
        ViewholderItemProductListBinding binding = ViewholderItemProductListBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = productList.get(position);

        holder.binding.productID.setText(product.getItemId());
        holder.binding.productName.setText(product.getTitle());
        holder.binding.productPrice.setText("₹" + product.getPrice());
        holder.binding.productdescription.setText(product.getDescription());


        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .into(holder.binding.imgOrder);

    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ViewholderItemProductListBinding binding;

        public ProductViewHolder(@NonNull ViewholderItemProductListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
