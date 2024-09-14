package com.townlift.townlift_customer.models;

public class ShopModel {
    private int id;
    private String shopImageUrl;
    private String shopName;
    private String deliveryCharges;
    private String deliveryTime;
    private String rating;

    public ShopModel(int id, String shopImageUrl, String shopName, String deliveryCharges, String deliveryTime, String rating) {
        this.id = id;
        this.shopImageUrl = shopImageUrl;
        this.shopName = shopName;
        this.deliveryCharges = deliveryCharges;
        this.deliveryTime = deliveryTime;
        this.rating = rating;
    }

    public int getShopId() {
        return id;
    }

    public void setShopId(int id) {
        this.id = id;
    }

    public String getShopImageUrl() {
        return shopImageUrl;
    }

    public void setShopImageUrl(String shopImageUrl) {
        this.shopImageUrl = shopImageUrl;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getDeliveryCharges() {
        return deliveryCharges;
    }

    public void setDeliveryCharges(String deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }


}
