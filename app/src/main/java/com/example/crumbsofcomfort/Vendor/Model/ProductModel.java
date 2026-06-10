package com.example.crumbsofcomfort.Vendor.Model;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

public class ProductModel implements Serializable {
    private String itemId;
    private String title;
    private double price;
    private double rating;
    private String description;
    private List<String> picUrl;
    private List<String> size;

    private String id;
    private String imageUrl;

    public ProductModel() {}

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getPicUrl() { return picUrl; }
    public void setPicUrl(List<String> picUrl) { this.picUrl = picUrl; }

    public List<String> getSize() { return size; }
    public void setSize(List<String> size) { this.size = size; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
