package com.example.crumbsofcomfort.Vendor.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.crumbsofcomfort.Vendor.Model.CustomRequestModel;
import com.example.crumbsofcomfort.databinding.ViewholderItemCustomRequestBinding;

import java.util.List;

public class CustomRequestAdapter extends RecyclerView.Adapter<CustomRequestAdapter.RequestViewHolder> {

    private List<CustomRequestModel> requestList;

    public CustomRequestAdapter(List<CustomRequestModel> requestList) {
        this.requestList = requestList;
    }

    public void updateList(List<CustomRequestModel> newList) {
        this.requestList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderItemCustomRequestBinding binding = ViewholderItemCustomRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RequestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        CustomRequestModel model = requestList.get(position);

        holder.binding.txtTitle.setText(model.getTitle());
        holder.binding.txtNote.setText("Note: " + model.getNote());
        holder.binding.txtSize.setText("Size: " + model.getSize());
        holder.binding.txtQuantity.setText("Quantity: " + model.getQuantity());

        Glide.with(holder.itemView.getContext())
                .load(model.getImageUrl())
                .into(holder.binding.imgCustom);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        ViewholderItemCustomRequestBinding binding;

        public RequestViewHolder(@NonNull ViewholderItemCustomRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
