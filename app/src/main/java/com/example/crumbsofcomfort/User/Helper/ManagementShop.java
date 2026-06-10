// ManagementShop.java
package com.example.crumbsofcomfort.User.Helper;

import android.content.Context;

import com.example.crumbsofcomfort.User.Model.ShopModel;

import java.util.ArrayList;

public class ManagementShop {
    private TinyDBShop tinyDBShop;

    public ManagementShop(Context context) {
        tinyDBShop = new TinyDBShop(context);
    }

    public void insertShopList(ArrayList<ShopModel> list) {
        tinyDBShop.saveShopList(list);
    }

    public ArrayList<ShopModel> getShopList() {
        return tinyDBShop.getShopList();
    }
}


