package com.example.crumbsofcomfort.Admin.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crumbsofcomfort.Admin.Model.FeedbackModel;
import com.example.crumbsofcomfort.R;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private Context context;
    private List<FeedbackModel> feedbackList;

    public FeedbackAdapter(Context context, List<FeedbackModel> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_feedback_item, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        FeedbackModel feedback = feedbackList.get(position);
        holder.txtShopName.setText(feedback.getShopName() != null ? feedback.getShopName() : "Unknown Shop");
        holder.txtComment.setText(feedback.getFeedback() != null ? feedback.getFeedback() : "No comment");
        holder.txtRating.setText("Rating: " + feedback.getRating());
        holder.txtDate.setText("Date: " + (feedback.getDate() != null ? feedback.getDate() : ""));
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView txtShopName, txtComment, txtRating, txtDate;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            txtShopName = itemView.findViewById(R.id.txtShopName);
            txtComment = itemView.findViewById(R.id.txtComment);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }
}
