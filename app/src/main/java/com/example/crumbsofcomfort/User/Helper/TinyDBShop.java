package com.example.crumbsofcomfort.User.Helper;

import android.content.Context;
import com.example.crumbsofcomfort.User.Model.ShopModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TinyDBShop {
    private TinyDB tinyDB;

    public TinyDBShop(Context context) {
        tinyDB = new TinyDB(context);
    }

    public void saveShopList(ArrayList<ShopModel> shopList) {
        Gson gson = new Gson();
        String json = gson.toJson(shopList);
        tinyDB.putString("ShopList", json);
    }

    public ArrayList<ShopModel> getShopList() {
        String json = tinyDB.getString("ShopList");
        Type type = new TypeToken<ArrayList<ShopModel>>() {}.getType();
        return new Gson().fromJson(json, type);
    }
}
