package com.example.crumbsofcomfort.Delivery.Model;

public class DeliveryPartnerModel {
    private String id;
    private String name;
    private String phone;
    private boolean available;

    public DeliveryPartnerModel() {}

    public DeliveryPartnerModel(String id, String name, String phone, boolean available) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.available = available;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isAvailable() {
        return available;
    }
}

