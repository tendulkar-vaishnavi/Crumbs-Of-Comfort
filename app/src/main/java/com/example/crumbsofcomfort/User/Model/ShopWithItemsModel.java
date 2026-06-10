package com.example.crumbsofcomfort.User.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class ShopWithItemsModel implements Serializable {
    private String shopName;
    private String shopImage;
    private int shopContact;
    private String shopAddress;
    private String shopId;
    private ArrayList<ItemsModel> items;

    public ShopWithItemsModel() {
    }

    public ShopWithItemsModel(String shopName, String shopImage, int shopContact, String shopAddress, String shopId , ArrayList<ItemsModel> items) {
        this.shopName = shopName;
        this.shopImage = shopImage;
        this.shopContact = shopContact;
        this.shopAddress = shopAddress;
        this.shopId = shopId;
        this.items = items;
    }


    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopImage() {
        return shopImage;
    }

    public void setShopImage(String shopImage) {
        this.shopImage = shopImage;
    }

    public int getShopContact() {
        return shopContact;
    }

    public void setShopContact(int shopContact) {
        this.shopContact = shopContact;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
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
