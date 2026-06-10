package com.example.crumbsofcomfort.User.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.User.Activity.DetailActivity;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.databinding.ViewholderShopMainBinding;

import java.util.List;

public class ShopMainAdapter extends RecyclerView.Adapter<ShopMainAdapter.ViewHolder> {

    private List<ItemsModel> itemList;
    private Context context;

    public ShopMainAdapter(List<ItemsModel> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderShopMainBinding binding = ViewholderShopMainBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemsModel item = itemList.get(position);

        holder.binding.textTitle.setText(item.getTitle());
        holder.binding.textDescription.setText(item.getDescription());
        double avg = item.getAverageRating();
        if (avg == 0) {
            holder.binding.textRating.setText("No ratings");
        } else {
            holder.binding.textRating.setText(String.format("%.1f", avg));
        }
        holder.binding.textPrice.setText("₹" + item.getPrice());

        Glide.with(context)
                .load(item.getPicUrl().get(0))
                .into(holder.binding.imageProduct);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("shopId", item.getShopId());
            intent.putExtra("object", item);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderShopMainBinding binding;

        public ViewHolder(@NonNull ViewholderShopMainBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
