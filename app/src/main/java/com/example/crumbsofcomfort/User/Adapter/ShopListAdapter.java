package com.example.crumbsofcomfort.User.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.User.Activity.ShopActivity;
import com.example.crumbsofcomfort.User.Model.ShopModel;
import com.example.crumbsofcomfort.User.Model.ShopWithItemsModel;
import com.example.crumbsofcomfort.databinding.ViewholderShopListBinding;

import java.util.List;

public class ShopListAdapter extends RecyclerView.Adapter<ShopListAdapter.ViewHolder> {

    private final List<ShopModel> shopList;
    private final Context context;

    public ShopListAdapter(Context context, List<ShopModel> shopList) {
        this.context = context;
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderShopListBinding binding = ViewholderShopListBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShopModel shop = shopList.get(position);

        holder.binding.textShopName.setText(shop.getSellerName());
        holder.binding.textShopAddress.setText(shop.getAddress());

        Glide.with(context)
                .load(shop.getSellerPic())
                .into(holder.binding.imageShop);

        // Navigate to ShopActivity
        holder.itemView.setOnClickListener(v -> {
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

        // Call icon click
        holder.binding.imageCall.setOnClickListener(v -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + shop.getSellerTell()));
            context.startActivity(dialIntent);
        });

        // Message icon click
        holder.binding.imageMessage.setOnClickListener(v -> {
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setData(Uri.parse("sms:" + shop.getSellerTell()));
            context.startActivity(smsIntent);
        });
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderShopListBinding binding;

        public ViewHolder(@NonNull ViewholderShopListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
