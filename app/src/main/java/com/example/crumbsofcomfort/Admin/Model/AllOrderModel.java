package com.example.crumbsofcomfort.Admin.Model;

public class AllOrderModel {
    private String orderId;
    private String shopName;
    private String date;
    private String deliveryTime;
    private String productName;
    private int quantity;
    private String weight;
    private double totalAmount;
    private String status;
    private String userName;

    public AllOrderModel() {
    }

//    public AllOrderModel(String orderId, String shopName, String date, String deliveryTime, String productName, int quantity, String weight, double totalAmount, String status, String userName) {
//        this.orderId = orderId;
//        this.shopName = shopName;
//        this.date = date;
//        this.deliveryTime = deliveryTime;
//        this.productName = productName;
//        this.quantity = quantity;
//        this.weight = weight;
//        this.totalAmount = totalAmount;
//        this.status = status;
//        this.userName = userName;
//    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
