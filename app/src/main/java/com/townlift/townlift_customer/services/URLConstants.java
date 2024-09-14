package com.townlift.townlift_customer.services;

public class URLConstants {
    // Define your URLs here
    public static final String BASE_URL = "http://192.168.1.3:3004/";
//    public static final String BASE_URL = "http://192.168.0.103:3004/";
    public static final String PROFILE_CREATE_URL = BASE_URL + "user/set-profile";
    public static final String LOGIN_URL = BASE_URL + "user/login";
    public static final String GET_ALL_SHOPS_URL = BASE_URL + "shop";
    public static final String ADD_SHOP_TO_FAVORITES_URL = BASE_URL + "user/add-shop-to-fav/";
    public static final String GET_SHOP_DETAILS_URL = BASE_URL + "shop/shop-details/";
    public static final String CREATE_NEW_CONVERSATION_URL = BASE_URL + "messages/create-conversation";
    public static final String GET_MESSAGES_URL = BASE_URL + "messages/all-messages/";
    public static final String GET_USER_PREVIOUS_ORDERS_URL = BASE_URL + "order/user-prev-orders/";
    public static final String GET_ORDER_CHAT_MESSAGES_URL = BASE_URL + "messages/order-chat/";
}
