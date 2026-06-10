package com.example.crumbsofcomfort.Admin.Model;

import java.util.HashMap;
import java.util.Map;

public class VendorAModel {
    private String uid, shopName, email, shopReg, aadhaar,phoneNum;
    private boolean approved;

    public VendorAModel() {}

    public VendorAModel(String uid, String shopName, String email, String shopReg, String aadhaar, String phoneNum, boolean approved) {
        this.uid = uid;
        this.shopName = shopName;
        this.email = email;
        this.shopReg = shopReg;
        this.aadhaar = aadhaar;
        this.phoneNum = phoneNum;
        this.approved = approved;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("shopName", shopName);
        map.put("email", email);
        map.put("shopReg", shopReg);
        map.put("aadhaar", aadhaar);
        map.put("phoneNum",phoneNum);
        map.put("approved", approved);
        map.put("role", "vendor");
        return map;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getShopName() { return shopName; }
    public String getEmail() { return email; }
    public String getShopReg() { return shopReg; }
    public String getAadhaar() { return aadhaar; }
    public boolean isApproved() { return approved; }

    public void setShopName(String shopName) { this.shopName = shopName; }
    public void setEmail(String email) { this.email = email; }
    public void setShopReg(String shopReg) { this.shopReg = shopReg; }
    public void setAadhaar(String aadhaar) { this.aadhaar= aadhaar; }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public void setApproved(boolean approved) { this.approved = approved; }
}
