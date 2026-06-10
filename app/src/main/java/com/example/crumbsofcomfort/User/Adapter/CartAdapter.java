package com.example.crumbsofcomfort.User.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.User.Helper.ChangeNumberItemsListener;
import com.example.crumbsofcomfort.User.Helper.ManagmentCart;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.databinding.ViewholderCartBinding;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private ArrayList<ItemsModel> listItemSelected;
    private ManagmentCart managmentCart;
    private String uid;
    private ChangeNumberItemsListener changeNumberItemsListener;

    public CartAdapter(ArrayList<ItemsModel> listItemSelected, Context context, String uid, ChangeNumberItemsListener changeNumberItemsListener) {
        this.listItemSelected = listItemSelected;
        this.changeNumberItemsListener = changeNumberItemsListener;
        this.managmentCart = new ManagmentCart(context, uid);
        this.uid = uid;
    }

    @NonNull
    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderCartBinding binding = ViewholderCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, int position) {
        ItemsModel item = listItemSelected.get(position);

        holder.binding.txtTitle.setText(item.getTitle());
        holder.binding.feeEachItem.setText("₹" + item.getPrice());
        holder.binding.totalEachItem.setText("₹" + Math.round(item.getNumberInCart() * item.getPrice()));
        holder.binding.numberItem.setText(String.valueOf(item.getNumberInCart()));
        holder.binding.itemQuantity.setText(String.valueOf(item.getSelectedSize()));
        holder.binding.eggPreference.setText("Preference: " + item.getEggType());


        String message = item.getMessageOnCake();
        if (message != null && !message.trim().isEmpty()) {
            holder.binding.messageOnCake.setVisibility(View.VISIBLE);
            holder.binding.messageOnCake.setText("Message: " + message);
        } else {
            holder.binding.messageOnCake.setVisibility(View.GONE);
        }

        if (item.getPicUrl() != null && !item.getPicUrl().isEmpty()) {
            String rawUrl = item.getPicUrl().get(0);
            String optimizedUrl = rawUrl.replace("/upload/", "/upload/f_auto/");

            Glide.with(holder.itemView.getContext())
                    .load(optimizedUrl)
                    .apply(new RequestOptions().centerCrop())
                    .placeholder(R.drawable.cam) // optional placeholder
                    .into(holder.binding.picCart);

        } else {
            holder.binding.picCart.setImageResource(R.drawable.cam); // You can use a default image
        }

        holder.binding.plusCartBtn.setOnClickListener(view -> managmentCart.plusItem(listItemSelected, position, () -> {
            notifyDataSetChanged();
            if (changeNumberItemsListener != null) {
                changeNumberItemsListener.onChanged();
            }
        }));

        holder.binding.minusCartBtn.setOnClickListener(view -> managmentCart.minusItem(listItemSelected, position, () -> {
            notifyDataSetChanged();
            if (changeNumberItemsListener != null) {
                changeNumberItemsListener.onChanged();
            }
        }));
    }

    @Override
    public int getItemCount() {
        return listItemSelected.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderCartBinding binding;

        public ViewHolder(ViewholderCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
