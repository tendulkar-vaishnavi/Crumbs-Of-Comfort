package com.example.crumbsofcomfort.User.Model;

import java.io.Serializable;
import java.util.ArrayList;
import com.google.firebase.database.Exclude;


public class ItemsModel implements Serializable {
    private String title;
    private String description;
    private ArrayList<String> picUrl;
    private ArrayList<String> size;
    private double price;
    private double rating;
    private  int numberInCart;
    private int categoryId;
    private String id;
    private String sellerName;
    private int sellerTell;
    private String sellerPic;
    private String selectedSize;
    private String itemId;
    private String messageOnCake;
    private String customDescription;
    private String customSize;
    private String customQuantity;
    private String customImageUrl;
    private String shopId;
    private String address;
    private double totalRating;
    private int ratingCount;
    private String eggType;


    public ItemsModel() {
    }

    public String getMessageOnCake() {
        return messageOnCake;
    }

    public void setMessageOnCake(String messageOnCake) {
        this.messageOnCake = messageOnCake;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(ArrayList<String> picUrl) {
        this.picUrl = picUrl;
    }

    public ArrayList<String> getSize() {
        return size;
    }

    public void setSize(ArrayList<String> size) {
        this.size = size;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
    @Exclude
    public int getNumberInCart() {
        return numberInCart;
    }

    public void setNumberInCart(int numberInCart) {
        this.numberInCart = numberInCart;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }
    @Exclude
    public int getSellerTell() {
        return sellerTell;
    }

    public void setSellerTell(int sellerTell) {
        this.sellerTell = sellerTell;
    }

    public String getSellerPic() {
        return sellerPic;
    }

    public void setSellerPic(String sellerPic) {
        this.sellerPic = sellerPic;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(String selectedSize) {
        this.selectedSize = selectedSize;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getCustomDescription() {
        return customDescription;
    }

    public void setCustomDescription(String customDescription) {
        this.customDescription = customDescription;
    }

    public String getCustomSize() {
        return customSize;
    }

    public void setCustomSize(String customSize) {
        this.customSize = customSize;
    }

    public String getCustomQuantity() {
        return customQuantity;
    }

    public void setCustomQuantity(String customQuantity) {
        this.customQuantity = customQuantity;
    }

    public String getCustomImageUrl() {
        return customImageUrl;
    }

    public void setCustomImageUrl(String customImageUrl) {
        this.customImageUrl = customImageUrl;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public double getTotalRating() {
        return totalRating;
    }

    public void setTotalRating(double totalRating) {
        this.totalRating = totalRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }
    @Exclude
    public double getAverageRating() {
        if (ratingCount == 0) return 0;
        return totalRating / ratingCount;
    }

    public String getEggType() {
        return eggType;
    }

    public void setEggType(String eggType) {
        this.eggType = eggType;
    }
}
