package com.example.crumbsofcomfort.User.Adapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.RequestOptions;
import com.example.crumbsofcomfort.User.Activity.CustomizationActivity;
import com.example.crumbsofcomfort.User.Model.SliderModel;
import com.example.crumbsofcomfort.R;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewholder> {
    public interface SliderClickListener {
        void onSliderClick(int position, SliderModel model);
    }

    private List<SliderModel> sliderModels;
    private ViewPager2 viewPager2;
    private SliderClickListener clickListener;
    private Context context;

    public SliderAdapter(List<SliderModel> sliderModels, ViewPager2 viewPager2, SliderClickListener clickListener) {
        this.sliderModels = sliderModels;
        this.viewPager2 = viewPager2;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public SliderViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.slider_image_container, parent, false);
        return new SliderViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewholder holder, int position) {
        holder.setImageView(sliderModels.get(position), context);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onSliderClick(position, sliderModels.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return sliderModels.size();
    }

    public static class SliderViewholder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public SliderViewholder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgSlide);
        }

        public void setImageView(SliderModel model, Context context) {
            Glide.with(context).load(model.getUrl()).into(imageView);
        }
    }
}
