package com.example.crumbsofcomfort.Admin.Model;

public class FeedbackModel {
    private String orderId;
    private String userId;
    private String comment;
    private float rating;
    private String shopName; // optional
    private String date;
    private String feedback;// optional

    public FeedbackModel() {
    }

    public FeedbackModel(String orderId, String userId, String comment, float rating, String shopName, String date) {
        this.orderId = orderId;
        this.userId = userId;
        this.comment = comment;
        this.rating = rating;
        this.shopName = shopName;
        this.date = date;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
