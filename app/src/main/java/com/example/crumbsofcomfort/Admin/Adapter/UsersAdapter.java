package com.example.crumbsofcomfort.Admin.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.crumbsofcomfort.Admin.Model.UserModel;
import com.example.crumbsofcomfort.R;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final Context context;
    private List<UserModel> userList;

    public UsersAdapter(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    public void updateList(List<UserModel> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);

        holder.txtName.setText(user.getName() != null ? user.getName() : "No Name");
        holder.txtEmail.setText(user.getEmail() != null ? user.getEmail() : "No Email");
        holder.txtPhone.setText(user.getPhone() != null ? user.getPhone() : "No Phone");
        holder.txtRole.setText("Role: " + (user.getRole() != null ? user.getRole() : "Unknown"));
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtEmail, txtPhone, txtRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            txtRole = itemView.findViewById(R.id.txtRole);
        }
    }
}
