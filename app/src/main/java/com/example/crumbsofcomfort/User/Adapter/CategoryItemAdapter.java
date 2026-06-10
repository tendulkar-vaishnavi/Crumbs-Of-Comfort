package com.example.crumbsofcomfort.User.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.User.Activity.DetailActivity;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.databinding.ViewholderCategoryItemBinding;

import java.util.List;

public class CategoryItemAdapter extends RecyclerView.Adapter<CategoryItemAdapter.ViewHolder> {

    private List<ItemsModel> items;
    private Context context;

    public CategoryItemAdapter(List<ItemsModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderCategoryItemBinding binding = ViewholderCategoryItemBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemsModel item = items.get(position);

        holder.binding.textTitle.setText(item.getTitle());
        holder.binding.textDescription.setText(item.getDescription());
        double avg = item.getAverageRating();
        if (avg == 0) {
            holder.binding.textRating.setText("No ratings");
        } else {
            holder.binding.textRating.setText(String.format("%.1f", avg));
        }
        holder.binding.textPrice.setText("₹" + item.getPrice());
        holder.binding.textShopName.setText(item.getSellerName());

        if (item.getPicUrl() != null && !item.getPicUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getPicUrl().get(0))
                    .centerCrop()
                    .into(holder.binding.imageProduct);
        }

        holder.itemView.setOnClickListener(view -> {
            if (item.getShopId() == null || item.getShopId().isEmpty()) {
                Log.w("CategoryItemAdapter", "item.shopId is null. Setting dummy/fallback shopId.");
                item.setShopId("fallback_shop_id");
            }

            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("shopId", item.getShopId());
            intent.putExtra("object", item);
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderCategoryItemBinding binding;

        public ViewHolder(@NonNull ViewholderCategoryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
