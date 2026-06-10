package com.example.crumbsofcomfort.User.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.crumbsofcomfort.User.Activity.DetailActivity;
import com.example.crumbsofcomfort.User.Helper.ManagementFav;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.databinding.ViewholderBestsellerBinding;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class BestSellerAdapter extends RecyclerView.Adapter<BestSellerAdapter.ViewHolder> {

    private List<ItemsModel> items;
    private Context context;
    private ActivityResultLauncher<Intent> launcher;
    private String uid;
    private AuthCheckListener authCheckListener;
    public interface AuthCheckListener {
        void showNotLoggedInSheet(String title, String msg);
    }


    public BestSellerAdapter(List<ItemsModel> items, ActivityResultLauncher<Intent> launcher, String uid, AuthCheckListener listener) {
        this.items = items;
        this.launcher = launcher;
        this.uid=uid;
        this.authCheckListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderBestsellerBinding binding = ViewholderBestsellerBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemsModel item = items.get(position);

        holder.binding.textTitle.setText(item.getTitle());
        holder.binding.textPrice.setText("₹" + item.getPrice());
        holder.binding.textStart.setText(String.valueOf(item.getRating()));

        Glide.with(context)
                .load(item.getPicUrl().get(0))
                .apply(new RequestOptions().transform(new CenterCrop()))
                .into(holder.binding.imageBestSeller);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : null;

        ManagementFav managementFav = new ManagementFav(context, currentUid);
        holder.binding.heartIcon.setImageResource(managementFav.heartIcon(item.getTitle()));

        holder.binding.heartIcon.setOnClickListener(view -> {
            if (currentUid == null) {
                authCheckListener.showNotLoggedInSheet(
                        "Login Required",
                        "Please login to add items to favourites"
                );
            } else {
                if (managementFav.isFavorite(item.getTitle())) {
                    managementFav.removeFavByTitle(item.getTitle());
                } else {
                    managementFav.insertFav(item);
                }
                notifyItemChanged(position);
            }
        });

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", item);
            launcher.launch(intent);
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderBestsellerBinding binding;

        public ViewHolder(ViewholderBestsellerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
