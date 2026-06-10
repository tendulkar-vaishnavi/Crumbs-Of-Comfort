package com.example.crumbsofcomfort.User.Model;

public class ShopItem {
    private String shopId;
    private String sellerName;

    public ShopItem(String shopId, String shopName) {
        this.shopId = shopId;
        this.sellerName = shopName;
    }

    public String getShopId() {
        return shopId;
    }

    public String getSellerName() {
        return sellerName;
    }

    @Override
    public String toString() {
        return sellerName;
    }
}

