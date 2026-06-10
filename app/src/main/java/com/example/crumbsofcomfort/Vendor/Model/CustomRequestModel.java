package com.example.crumbsofcomfort.Vendor.Model;

public class CustomRequestModel {
    private String title;
    private String note;
    private String size;
    private String quantity;
    private String imageUrl;

    public CustomRequestModel() {
        // Default constructor
    }

    public CustomRequestModel(String title, String note, String size, String quantity, String imageUrl) {
        this.title = title;
        this.note = note;
        this.size = size;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public String getSize() {
        return size;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
