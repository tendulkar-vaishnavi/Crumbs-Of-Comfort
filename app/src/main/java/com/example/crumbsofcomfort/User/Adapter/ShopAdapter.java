package com.example.crumbsofcomfort.User.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.crumbsofcomfort.User.Activity.ShopActivity;
import com.example.crumbsofcomfort.User.Model.ShopModel;
import com.example.crumbsofcomfort.User.Model.ShopWithItemsModel;
import com.example.crumbsofcomfort.databinding.ViewholderShopBinding;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ViewHolder> {

    private List<ShopModel> shopList;
    private Context context;

    public ShopAdapter(List<ShopModel> shopList) {
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public ShopAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderShopBinding binding = ViewholderShopBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopAdapter.ViewHolder holder, int position) {
        ShopModel shop = shopList.get(position);

        holder.binding.textshop.setText(shop.getSellerName());

        Glide.with(context)
                .load(shop.getSellerPic())
                .apply(new RequestOptions().transform(new CenterCrop()))
                .into(holder.binding.imageBestSeller);

        holder.itemView.setOnClickListener(view -> {
            ShopWithItemsModel shopBundle = new ShopWithItemsModel(
                    shop.getSellerName(),
                    shop.getSellerPic(),
                    shop.getSellerTell(),
                    shop.getAddress(),
                    shop.getShopId(),
                    shop.getItems()
            );


            Intent intent = new Intent(context, ShopActivity.class);
            intent.putExtra("shopBundle", shopBundle);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderShopBinding binding;

        public ViewHolder(ViewholderShopBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
