package com.example.crumbsofcomfort.User.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.crumbsofcomfort.R;
import com.example.crumbsofcomfort.User.Adapter.CartAdapter;
import com.example.crumbsofcomfort.User.Helper.ManagmentCart;
import com.example.crumbsofcomfort.User.Model.ItemsModel;
import com.example.crumbsofcomfort.databinding.ActivityCartBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.razorpay.Checkout;

import java.util.ArrayList;

public class CartActivity extends BaseActivity {

    private ActivityCartBinding binding;
    private ManagmentCart managmentCart;
    private double tax = 0.0;
    private String uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new android.os.Handler().postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                showNotLoggedInSheet();
            } else {
                uid = user.getUid();
                managmentCart = new ManagmentCart(this, uid);
                setVariable();
                calculatorCart();
                initCartList();
            }
        }, 500);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        View decor = window.getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = systemBars.bottom + 3;
            v.setLayoutParams(params);
            return insets;
        });

        Checkout.preload(getApplicationContext());

        binding.checkoutbtn.setOnClickListener(view -> {
            ArrayList<ItemsModel> cartList = managmentCart.getListCart();

            if (cartList.isEmpty()) {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            String shopId = cartList.get(0).getShopId();
            double subtotal = managmentCart.getTotalFee();
            double tax = Math.round(subtotal * 0.02 * 100) / 100.0;
            double deliveryFee = 30.0;
            double totalAmount = Math.round((subtotal + tax + deliveryFee) * 100) / 100.0;

            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            intent.putExtra("cartList", cartList);
            intent.putExtra("subtotal", subtotal);
            intent.putExtra("tax", tax);
            intent.putExtra("deliveryFee", deliveryFee);
            intent.putExtra("totalAmount", totalAmount);
            intent.putExtra("shopId", shopId);
            String messageOnCake = cartList.get(0).getMessageOnCake();
            intent.putExtra("messageOnCake", messageOnCake);
            String itemId = cartList.get(0).getItemId();
            intent.putExtra("itemId", itemId);


            startActivity(intent);
        });
    }
        private void initCartList() {
        binding.cartView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.cartView.setAdapter(new CartAdapter(
                managmentCart.getListCart(),
                this,
                uid,
                this::calculatorCart
        ));
    }

    private void calculatorCart() {
        double percentTax = 0.02;
        double delivery = 30.0;
        tax = Math.round(managmentCart.getTotalFee() * percentTax * 100) / 100.0;
        double total = Math.round((managmentCart.getTotalFee() + tax + delivery) * 100) / 100.0;
        double itemTotal = Math.round(managmentCart.getTotalFee() * 100) / 100.0;

        binding.txtTotalFee.setText("₹" + itemTotal);
        binding.txtTax.setText("₹" + tax);
        binding.txtDelivery.setText("₹" + delivery);
        binding.txtTotal.setText("₹" + total);
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void showNotLoggedInSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_validation, null);
        dialog.setContentView(view);

        view.findViewById(R.id.btnOk).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });


        dialog.show();
    }
}
