package com.example.crumbsofcomfort.User.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.databinding.ViewholderFavouriteBinding;

import java.util.ArrayList;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.ViewHolder> {
    private ArrayList<ItemsModel> favoriteList;
    private Context context;

    public FavouriteAdapter(ArrayList<ItemsModel> favoriteList, Context context) {
        this.favoriteList = favoriteList;
        this.context = context;
    }

    @NonNull
    @Override
    public FavouriteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderFavouriteBinding binding = ViewholderFavouriteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavouriteAdapter.ViewHolder holder, int position) {
        ItemsModel item = favoriteList.get(position);

        holder.binding.txtTitle.setText(item.getTitle());
        holder.binding.feeEachItem.setText("₹" + item.getPrice());


        Glide.with(context)
                .load(item.getPicUrl().get(0))
                .apply(new RequestOptions().centerCrop())
                .into(holder.binding.picFav);

        holder.binding.heartIcon.setOnClickListener(view -> {
            Toast.makeText(context, item.getTitle() + " removed from favorites", Toast.LENGTH_SHORT).show();

        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderFavouriteBinding binding;
        public ViewHolder(ViewholderFavouriteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
