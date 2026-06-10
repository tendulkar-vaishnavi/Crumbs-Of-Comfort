package com.example.crumbsofcomfort.User.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class ShopModel implements Serializable {
    private String sellerName;
    private String sellerPic;
    private int sellerTell;
    private String address;
    private String shopId;

    private ArrayList<ItemsModel> items;

    public ShopModel() {
        // Required empty constructor (for Firebase or deserialization)
    }

    public ShopModel(String sellerName, String sellerPic, int sellerTell,String address,String shopId, ArrayList<ItemsModel> items) {
        this.sellerName = sellerName;
        this.sellerPic = sellerPic;
        this.sellerTell = sellerTell;
        this.address = address;
        this.shopId = shopId;
        this.items = items;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerPic() {
        return sellerPic;
    }

    public void setSellerPic(String sellerPic) {
        this.sellerPic = sellerPic;
    }

    public int getSellerTell() {
        return sellerTell;
    }

    public void setSellerTell(int sellerTell) {
        this.sellerTell = sellerTell;
    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public ArrayList<ItemsModel> getItems() {
        return items;
    }

    public void setItems(ArrayList<ItemsModel> items) {
        this.items = items;
    }
}
