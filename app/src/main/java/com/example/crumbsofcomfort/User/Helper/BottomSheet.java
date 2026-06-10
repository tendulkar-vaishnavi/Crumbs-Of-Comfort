package com.example.crumbsofcomfort.User.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.example.crumbsofcomfort.databinding.BottomSheetDialogBinding;
import com.example.crumbsofcomfort.databinding.BottomSheetRolePickerBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class BottomSheet {

    public static void show(Context context, String title, String message, View.OnClickListener onConfirmClick) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        // Inflate layout using ViewBinding
        BottomSheetDialogBinding binding = BottomSheetDialogBinding.inflate(LayoutInflater.from(context));
        bottomSheetDialog.setContentView(binding.getRoot());

        // Set title and message dynamically
        binding.bottomSheetTitle.setText(title);
        binding.bottomSheetMessage.setText(message);

        // Handle confirm button click
        binding.btnConfirm.setOnClickListener(v -> {
            if (onConfirmClick != null) {
                onConfirmClick.onClick(v);
            }
            bottomSheetDialog.dismiss();
        });

        // Handle cancel button click
        binding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // Show the bottom sheet
        bottomSheetDialog.show();
    }
    public static void showOptionPicker(Context context, String title, String option1Text, String option2Text,String option3Text, OnOptionSelectedListener listener) {
            BottomSheetDialog dialog = new BottomSheetDialog(context);
            BottomSheetRolePickerBinding binding = BottomSheetRolePickerBinding.inflate(LayoutInflater.from(context));
            dialog.setContentView(binding.getRoot());

            binding.title.setText(title);
            binding.option1.setText(option1Text);
            binding.option2.setText(option2Text);
            binding.option3.setText(option3Text);
            binding.option4.setVisibility(View.GONE);
            binding.option5.setVisibility(View.GONE);

            binding.option1.setOnClickListener(v -> {
                listener.onOptionSelected(option1Text);
                dialog.dismiss();
            });

            binding.option2.setOnClickListener(v -> {
                listener.onOptionSelected(option2Text);
                dialog.dismiss();
            });
            binding.option3.setOnClickListener(v -> {
                listener.onOptionSelected(option3Text);
                dialog.dismiss();
            });

            dialog.show();
        }
    public interface OnOptionSelectedListener {
        void onOptionSelected(String option);
    }
    public static void showCategoryPicker(Context context, String title, String option1, String option2, String option3, String option4, String option5, OnOptionSelectedListener listener) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        BottomSheetRolePickerBinding binding = BottomSheetRolePickerBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        binding.title.setText(title);
        binding.option1.setText(option1);
        binding.option2.setText(option2);
        binding.option3.setText(option3);
        binding.option1.setVisibility(View.VISIBLE);
        binding.option2.setVisibility(View.VISIBLE);
        binding.option3.setVisibility(View.VISIBLE);

        // Optional: repurpose buttons for additional categories
        binding.option4.setVisibility(View.VISIBLE);
        binding.option5.setVisibility(View.VISIBLE);
        binding.option4.setText(option4);
        binding.option5.setText(option5);

        binding.option1.setOnClickListener(v -> {
            listener.onOptionSelected(option1);
            dialog.dismiss();
        });
        binding.option2.setOnClickListener(v -> {
            listener.onOptionSelected(option2);
            dialog.dismiss();
        });
        binding.option3.setOnClickListener(v -> {
            listener.onOptionSelected(option3);
            dialog.dismiss();
        });
        binding.option4.setOnClickListener(v -> {
            listener.onOptionSelected(option4);
            dialog.dismiss();
        });
        binding.option5.setOnClickListener(v -> {
            listener.onOptionSelected(option5);
            dialog.dismiss();
        });

        dialog.show();
    }


}
