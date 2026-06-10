package com.example.crumbsofcomfort.User.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.User.Activity.DetailActivity;
import com.example.crumbsofcomfort.User.Activity.ShopActivity;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.User.Model.SearchModel;
import com.example.crumbsofcomfort.User.Model.ShopModel;
import com.example.crumbsofcomfort.User.Model.ShopWithItemsModel;
import com.example.crumbsofcomfort.databinding.ViewholderSearchBinding;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<SearchModel> searchList;
    private Context context;

    public SearchAdapter(List<SearchModel> searchList) {
        this.searchList = searchList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewholderSearchBinding binding = ViewholderSearchBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchModel result = searchList.get(position);

        if (result.isItem()) {
            ItemsModel item = result.getItem();
            holder.binding.searchText.setText(item.getTitle());
            Glide.with(context).load(item.getPicUrl().get(0)).into(holder.binding.searchImage);

            if (result.getShop() != null && result.getShop().getShopId() != null) {
                item.setShopId(result.getShop().getShopId());
            }

            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("object", item);
                context.startActivity(intent);
            });

        } else {
            ShopModel shop = result.getShop();
            holder.binding.searchText.setText(shop.getSellerName());
            Glide.with(context).load(shop.getSellerPic()).into(holder.binding.searchImage);

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
        }
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderSearchBinding binding;

        public ViewHolder(ViewholderSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
