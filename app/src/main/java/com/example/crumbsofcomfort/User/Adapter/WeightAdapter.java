package com.example.crumbsofcomfort.User.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.databinding.ViewholderWeightBinding;

import java.util.List;

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.viewholder> {
    private List<String> items;
    private int selectedPosition = -1;
    private int lastSelectedPosition = -1;
    private Context context;
    private OnSizeClickListener listener;

    public interface OnSizeClickListener {
        void onSizeClicked(String selectedWeight);
    }

    public WeightAdapter(List<String> items, OnSizeClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WeightAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderWeightBinding binding = ViewholderWeightBinding.inflate(
                LayoutInflater.from(context),parent,false);

        return new viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightAdapter.viewholder holder, int position) {
        String weight = items.get(position);
        holder.binding.weight.setText(weight);


        holder.binding.getRoot().setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            lastSelectedPosition = selectedPosition;
            selectedPosition = currentPosition;

            notifyItemChanged(lastSelectedPosition);
            notifyItemChanged(selectedPosition);

            listener.onSizeClicked(items.get(currentPosition));
        });

        if (selectedPosition == position) {
            holder.binding.weightLayout.setBackgroundResource(R.drawable.brown_bg_2);
            holder.binding.weight.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            holder.binding.weightLayout.setBackgroundResource(R.drawable.grey_bg);
            holder.binding.weight.setTextColor(context.getResources().getColor(R.color.black));
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        ViewholderWeightBinding binding;
        public viewholder(ViewholderWeightBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
