package com.townlift.townlift_customer;

import static com.townlift.townlift_customer.services.URLConstants.ADD_SHOP_TO_FAVORITES_URL;
import static com.townlift.townlift_customer.services.URLConstants.BASE_URL;
import static com.townlift.townlift_customer.services.URLConstants.CREATE_NEW_CONVERSATION_URL;
import static com.townlift.townlift_customer.services.URLConstants.GET_SHOP_DETAILS_URL;
import static com.townlift.townlift_customer.services.URLConstants.GET_USER_PREVIOUS_ORDERS_URL;
import static com.townlift.townlift_customer.services.URLConstants.REMOVE_SHOP_FROM_FAVORITES_URL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.townlift.townlift_customer.adapters.PreviousOrdersAdapter;
import com.townlift.townlift_customer.helpers.SnackbarUtils;
import com.townlift.townlift_customer.services.ServiceProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShopDetails extends AppCompatActivity implements PreviousOrdersAdapter.OnItemClickListener {
    private JSONObject shopDetails;
    //    List<OrderModel> orderList = new ArrayList<>();
    private TextView txtShopName, txtDeliveryCharges, txtDeliveryTime, txtRatings;
    private ImageButton cmdAddToFav;
    private TextView headerTitle;
    ImageView cmdNavBack;
    ImageView shopImageView, imgRatingStar;
    private ShimmerFrameLayout shimmerFrameLayout;
    int shopId, adminId, userId;
    SharedPreferences userPreferences;
    LinearLayout shopDetailsCard;
    Button cmdNewOrder;
    List<JSONObject> orderList = new ArrayList<>();
    RecyclerView previousOrdersRecyclerView;
    PreviousOrdersAdapter ordersAdapter;
    SwipeRefreshLayout orderSwipeRefreshLayout;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        txtShopName = findViewById(R.id.txt_shop_name);
        txtDeliveryCharges = findViewById(R.id.txt_delivery_charges);
        txtDeliveryTime = findViewById(R.id.txt_delivery_time);
        txtRatings = findViewById(R.id.txt_ratings);
        headerTitle = findViewById(R.id.header_title);
        headerTitle.setVisibility(View.INVISIBLE);
        shopImageView = findViewById(R.id.shop_image);
        cmdNavBack = findViewById(R.id.cmd_nav_back);
        cmdAddToFav = findViewById(R.id.cmd_add_to_fav);
        shopDetailsCard = findViewById(R.id.shop_details_card);
        imgRatingStar = findViewById(R.id.img_rating_star);
        cmdNewOrder = findViewById(R.id.cmd_new_order);
//        orderSwipeRefreshLayout = findViewById(R.id.orderSwipeRefreshLayout);
        shimmerFrameLayout = findViewById(R.id.shop_details_shimmer);

        cmdNewOrder.setOnClickListener(v -> {
            apiCreateNewConversation();
        });
        cmdNavBack.setOnClickListener(v -> onBackPressed());
        shopId = getIntent().getIntExtra("id", -1);
        userPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = userPreferences.getInt("id", -1);

        cmdAddToFav.setOnClickListener(v -> {
            try {
                // Toggle favorite state based on current icon
                boolean isShopFav = shopDetails.getBoolean("is_favorite");

                if (isShopFav) {
                    apiRemoveShopFromFavorites();
                } else {
                    apiAddShopToFavorites();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                SnackbarUtils.showErrorSnackbar(this, "Failed to update favorites");
            }
        });

        shimmerFrameLayout.startShimmer();
        try {
            apiGetShopDetails();
            apiGetPreviousOrders();
        } catch (JSONException e) {
            e.printStackTrace();
            SnackbarUtils.showErrorSnackbar(this, "Failed to fetch shop details");
        }
        // Initialize RecyclerView
        previousOrdersRecyclerView = findViewById(R.id.previous_orders);
        ordersAdapter = new PreviousOrdersAdapter(this, orderList, this);
        previousOrdersRecyclerView.setAdapter(ordersAdapter);
        previousOrdersRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        previousOrdersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }





    void apiGetShopDetails() throws JSONException {
        int shopId = getIntent().getIntExtra("id", -1);
        int userId = userPreferences.getInt("id", -1);

        if (shopId == -1) {
            SnackbarUtils.showErrorSnackbar(this, "Invalid Shop ID");
            return;
        }
        String url = String.format("%s%d", GET_SHOP_DETAILS_URL, shopId);
        System.out.println("details URL: " + url);

//        send user id in request body as parameter json object
        JSONObject postData = new JSONObject();
        try {
            postData.put("userId", userId);
            System.out.println("postData: " + postData.toString());
            new Handler().postDelayed(() -> {
                ServiceProvider.getInstance(this).sendPostRequest(this, url, postData,
                        response -> {

                            JSONObject data = (JSONObject) response;
                            Log.d("URL>>>>>>>>>>", "Fetchd Shop details: " + url);
                            Log.d("ShopDetails", "Fetchd Shop details: " + data.toString());

                            try {
                                shopDetails = data;
                                adminId = data.getInt("admin_id");
                                // Handle successful response
                                shimmerFrameLayout.stopShimmer();
                                shimmerFrameLayout.setVisibility(View.GONE);
                                shopDetailsCard.setVisibility(View.VISIBLE);
                                txtShopName.setText(data.getString("name"));
                                txtDeliveryCharges.setText(data.getString("delivery_fee"));
                                txtDeliveryTime.setText(data.getString("delivery_time"));
                                String ratings = data.getString("average_rating");
                                if (ratings.equals("null")) {
                                    ratings = "0.0";
                                    imgRatingStar.setImageResource(R.drawable.star_disabled_24);
                                }
                                txtRatings.setText(ratings);

                                Glide.with(this).load(BASE_URL + data.getString("image")).into(shopImageView);
                                boolean isShopFav = data.getBoolean("is_favorite");
                                if (isShopFav) {
                                    cmdAddToFav.setImageResource(R.drawable.heart_filled_24);  // Shop is in favorites
                                } else {
                                    cmdAddToFav.setImageResource(R.drawable.heart_outline_24);  // Shop is not in favorites
                                }

                            } catch (JSONException e) {
                                shimmerFrameLayout.stopShimmer();
                                shimmerFrameLayout.setVisibility(View.GONE);
                                shopDetailsCard.setVisibility(View.VISIBLE);

                                e.printStackTrace();
                                SnackbarUtils.showErrorSnackbar(this, "Failed to parse response");
                            }
                        },
                        error -> {
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            shopDetailsCard.setVisibility(View.VISIBLE);

                            SnackbarUtils.showErrorSnackbar(this, "Failed to fetch shop details");
                        }
                );

            }, 500);

        } catch (JSONException e) {
            e.printStackTrace();
            SnackbarUtils.showErrorSnackbar(this, "Failed to fetch shop details");
            throw new RuntimeException(e);
        }


    }

    void apiAddShopToFavorites() {
        if (userId == -1 || shopId == -1) {
            SnackbarUtils.showErrorSnackbar(this, "Invalid User or Shop ID");
            return;
        }

        String url = String.format("%s%d%s%d", ADD_SHOP_TO_FAVORITES_URL, userId, "/", shopId);
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        apiGetShopDetails();  // Refresh the details and update UI
                        SnackbarUtils.showSuccessSnackbar(this, "Shop added to favorites!");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        SnackbarUtils.showErrorSnackbar(this, "Failed to parse response");
                    }
                },
                error -> {
                    SnackbarUtils.showErrorSnackbar(this, "Failed to add shop to favorites");
                });

        queue.add(stringRequest);
    }

    void apiRemoveShopFromFavorites() {
        if (userId == -1 || shopId == -1) {
            SnackbarUtils.showErrorSnackbar(this, "Invalid User or Shop ID");
            return;
        }

        String url = String.format("%s%d%s%d", REMOVE_SHOP_FROM_FAVORITES_URL, userId, "/", shopId);
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        apiGetShopDetails();  // Refresh the details and update UI
                        SnackbarUtils.showSuccessSnackbar(this, "Shop removed from favorites!");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        SnackbarUtils.showErrorSnackbar(this, "Failed to parse response");
                    }
                },
                error -> {
                    SnackbarUtils.showErrorSnackbar(this, "Failed to remove shop from favorites");
                });

        queue.add(stringRequest);
    }


    void apiCreateNewConversation() {
        JSONObject newConversationData = new JSONObject();
        try {
            String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            newConversationData.put("user_id", userPreferences.getInt("id", -1));
            newConversationData.put("shop_id", shopId);
            newConversationData.put("admin_id", adminId);
            newConversationData.put("createdat", currentDateTime);
            ServiceProvider.getInstance(this).sendPostRequest(this, CREATE_NEW_CONVERSATION_URL, newConversationData,
                    response -> {
                        JSONObject data = (JSONObject) response;

                        JSONObject conversation = null;
                        try {
                            conversation = data.getJSONObject("conversation");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        Intent intent = new Intent(ShopDetails.this, NewOrder.class);
                        intent.putExtra("shop_details", shopDetails.toString()); // Pass shop details as a String
                        intent.putExtra("conversation", conversation.toString()); // Pass conversation as a String
                        startActivity(intent);
                    },
                    error -> {
                        error.printStackTrace();
                        System.out.println("Error: " + error.toString());
                        SnackbarUtils.showErrorSnackbar(this, "Cannot create new order");
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            SnackbarUtils.showErrorSnackbar(this, "Cannot to create new order");
        }
    }

    void apiGetPreviousOrders() {
//        orderSwipeRefreshLayout.setRefreshing(true);
        int userId = userPreferences.getInt("id", -1);
        if (userId == -1) {
            SnackbarUtils.showErrorSnackbar(this, "Invalid User ID");
            return;
        }
        String url = String.format("%s%d%s%d", GET_USER_PREVIOUS_ORDERS_URL, userId, "/", shopId);
        System.out.println("prev orders URL: " + url);
        ServiceProvider.getInstance(this).sendPostRequest(this, url, null,
                response -> {
//                    orderSwipeRefreshLayout.setRefreshing(false);
                    try {
                        JSONArray data = (JSONArray) response;
                        orderList.clear();
                        for (int i = 0; i < data.length(); i++) {
                            orderList.add(data.getJSONObject(i));
                        }
                        ordersAdapter.notifyDataSetChanged();
                        System.out.println("Orders Response: " + data.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        SnackbarUtils.showErrorSnackbar(this, "Failed to parse response");
                    }
                },
                error -> {
//                    orderSwipeRefreshLayout.setRefreshing(false);
                    Log.d("SocketIO", "Error fetching messages: " + error.getMessage());
                });
    }

    @Override
    public void onItemClick(int position) throws JSONException {
        // Get the selected order object from the list
        JSONObject selectedOrder = orderList.get(position);

        // Extract the order id
        int orderId = selectedOrder.getInt("id");

        // Create an Intent to start the OrderDetails activity
        Intent intent = new Intent(ShopDetails.this, OrderDetails.class);

        // Put the order id as an integer extra in the Intent
        intent.putExtra("order_id", orderId);

        // Log for debugging
        Log.d("OrderDetails", "Selected Order ID: " + orderId);

        // Start the OrderDetails activity
        startActivity(intent);
    }

}