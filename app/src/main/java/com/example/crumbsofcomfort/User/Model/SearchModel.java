package com.example.crumbsofcomfort.User.Model;

public class SearchModel {
    private boolean isItem; // true = Item, false = Shop
    private ItemsModel item;
    private ShopModel shop;

    public SearchModel(ItemsModel item) {
        this.item = item;
        this.isItem = true;
    }

    public SearchModel(ShopModel shop) {
        this.shop = shop;
        this.isItem = false;
    }

    public boolean isItem() {
        return isItem;
    }

    public ItemsModel getItem() {
        return item;
    }

    public ShopModel getShop() {
        return shop;
    }
}



