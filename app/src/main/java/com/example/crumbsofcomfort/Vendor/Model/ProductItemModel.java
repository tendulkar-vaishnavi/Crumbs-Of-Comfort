package com.example.crumbsofcomfort.Vendor.Model;

import java.io.Serializable;

public class ProductItemModel implements Serializable {
    private String productName;
    private String productId;
    private String imageUrl;
    private int quantity;
    private String weight;
    private double price;
    private String messageOnCake;
    private String eggType;

    public ProductItemModel() {}

    public String getProductName() { return productName; }
    public String getProductId() { return productId; }
    public String getImageUrl() { return imageUrl; }
    public int getQuantity() { return quantity; }
    public String getWeight() { return weight; }
    public double getPrice() { return price; }

    public String getMessageOnCake() {
        return messageOnCake;
    }

    public String getEggType() {
        return eggType;
    }
}
