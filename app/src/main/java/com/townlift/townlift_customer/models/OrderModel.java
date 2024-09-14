package com.townlift.townlift_customer.models;

public class OrderModel {
    private int orderId;
    private String orderDate;
    private String orderStatus;
    private String orderDay;
    private String orderMonth;
    private String shopName;
    private String shopAddress;
    private double totalPrice;
    private double devliveryCharges;
    private double grandTotal;

    public OrderModel(int orderId, String orderDate, String orderStatus, String orderDay, String orderMonth, String shopName, String shopAddress, double totalPrice, double devliveryCharges, double grandTotal) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.orderDay = orderDay;
        this.orderMonth = orderMonth;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.totalPrice = totalPrice;
        this.devliveryCharges = devliveryCharges;
        this.grandTotal = grandTotal;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderDay() {
        return orderDay;
    }

    public void setOrderDay(String orderDay) {
        this.orderDay = orderDay;
    }

    public String getOrderMonth() {
        return orderMonth;
    }

    public void setOrderMonth(String orderMonth) {
        this.orderMonth = orderMonth;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getDevliveryCharges() {
        return devliveryCharges;
    }

    public void setDevliveryCharges(double devliveryCharges) {
        this.devliveryCharges = devliveryCharges;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }
}


