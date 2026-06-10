package com.example.crumbsofcomfort.User.Helper;

import android.content.Context;
import android.widget.Toast;

import com.example.crumbsofcomfort.User.Model.ItemsModel;

import java.util.ArrayList;

public class ManagmentCart {
    private TinyDB tinyDB;
    private Context context;
    private String uid;

    public ManagmentCart(Context context, String uid) {
        tinyDB = new TinyDB(context);
        this.context = context;
        this.uid = uid;
    }

    public void insertItems(ItemsModel item) {
        ArrayList<ItemsModel> listFood = getListCart();
        boolean existAlready = false;

        // ⚠️ Validate shopId
        if (item.getShopId() == null || item.getShopId().isEmpty()) {
            Toast.makeText(context, "Error: Shop ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        for (ItemsModel existingItem : listFood) {
            if (existingItem.getTitle().equals(item.getTitle()) &&
                    existingItem.getSelectedSize() != null &&
                    existingItem.getSelectedSize().equals(item.getSelectedSize())) {
                existAlready = true;
                break;
            }
        }

        if (existAlready) {
            Toast.makeText(context, "Item already in cart", Toast.LENGTH_SHORT).show();
        } else {
            listFood.add(item);
            tinyDB.putListObject("CartList_" + uid, listFood);
            Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show();
        }
    }
    public ArrayList<ItemsModel> getListCart() {
        return tinyDB.getListObject("CartList_" + uid);
    }



    public void minusItem(ArrayList<ItemsModel> listItems, int position, ChangeNumberItemsListener listener) {
        if (listItems.get(position).getNumberInCart() == 1) {
            listItems.remove(position);
        } else {
            listItems.get(position).setNumberInCart(listItems.get(position).getNumberInCart() - 1);
        }
        tinyDB.putListObject("CartList_" + uid, listItems);
        listener.onChanged();
    }

    public void plusItem(ArrayList<ItemsModel> listItems, int position, ChangeNumberItemsListener listener) {
        listItems.get(position).setNumberInCart(listItems.get(position).getNumberInCart() + 1);
        tinyDB.putListObject("CartList_" + uid, listItems);
        listener.onChanged();
    }

    public double getTotalFee() {
        ArrayList<ItemsModel> listFood = getListCart();
        double fee = 0.0;
        for (ItemsModel item : listFood) {
            fee += item.getPrice() * item.getNumberInCart();
        }
        return fee;
    }
}
