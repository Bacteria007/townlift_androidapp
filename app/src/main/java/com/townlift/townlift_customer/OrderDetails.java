package com.townlift.townlift_customer;

import static com.townlift.townlift_customer.services.URLConstants.GET_ORDER_DETAILS_WITH_MESSAGES_URL;
import static com.townlift.townlift_customer.services.URLConstants.RATE_ORDER_URL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.townlift.townlift_customer.adapters.MessageAdapter;
import com.townlift.townlift_customer.services.ServiceProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrderDetails extends AppCompatActivity {

    TextView headerTitle, txtOrderId, txtOrderDay, txtRateOrder, txtOrderMonth, txtShopName, txtShopAddress, txtOrderStatus;
    TextView txtOrderTotal, txtDeliveryCharges, txtGrandTotal;
    ImageView cmdNavBack;
    List<JSONObject> orderChatList = new ArrayList<>();
    RecyclerView orderChatRecyclerView;
    MessageAdapter orderChatAdapter;
    int userId, orderId, conversationId;
    SharedPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        headerTitle = findViewById(R.id.header_title);
        txtOrderId = findViewById(R.id.txt_order_id);
        txtOrderDay = findViewById(R.id.txt_order_day);
        txtOrderMonth = findViewById(R.id.txt_order_month);
        txtShopName = findViewById(R.id.txt_shop_name);
        txtShopAddress = findViewById(R.id.txt_shop_address);
        txtOrderTotal = findViewById(R.id.txt_order_total);
        txtDeliveryCharges = findViewById(R.id.txt_delivery_charges);
        txtGrandTotal = findViewById(R.id.txt_grand_total);
        txtOrderStatus = findViewById(R.id.txt_order_status);
        txtRateOrder = findViewById(R.id.txt_rate_order);
        cmdNavBack = findViewById(R.id.cmd_nav_back);
        orderChatRecyclerView = findViewById(R.id.order_chat_recyclerview);

        txtOrderStatus.setOnClickListener(v -> {
            Intent intent = new Intent(OrderDetails.this, TrackOrder.class);
            intent.putExtra("order_id", orderId);
            startActivity(intent);
        });
        headerTitle.setText("Order Details");
        cmdNavBack.setOnClickListener(v -> onBackPressed());

        // Get the Intent and extract the JSON String
        Intent intent = getIntent();
        userPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = userPreferences.getInt("id", -1);
        int orderId = intent.getIntExtra("order_id", -1);
        if (orderId != -1) {
            apiGetOrderDetailsAndMessages(orderId);
        }
        txtRateOrder.setOnClickListener(v -> openRateOrderBottomSheet(orderId));
        // Initialize RecyclerView and its adapter
        orderChatRecyclerView = findViewById(R.id.order_chat_recyclerview);
        orderChatAdapter = new MessageAdapter(orderChatList, userPreferences.getInt("id", -1), false); // Pass false
        orderChatRecyclerView.setAdapter(orderChatAdapter);
        orderChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    void apiGetOrderDetailsAndMessages(int orderId) {
        String url = GET_ORDER_DETAILS_WITH_MESSAGES_URL + orderId;
        ServiceProvider.getInstance(this).sendPostRequest(this, url, null,
                response -> {
                    try {
                        JSONObject data = (JSONObject) response;
                        Log.d("Order Details API Response", "Response: " + data.toString());

                        // Parse order details
                        JSONObject orderDetails = data.getJSONObject("order_details");
                        String orderDay = orderDetails.getString("createdat").split("T")[0].split("-")[2];
                        String orderMonth = orderDetails.getString("createdat").split("-")[1] + " " + orderDetails.getString("createdat").split("-")[0];
                        String shopName = orderDetails.getString("shop_name");
                        String shopAddress = orderDetails.getString("shop_address");
                        String orderTotal = "Total: " + orderDetails.getDouble("total") + " Rs";
                        String deliveryCharges = "Delivery Charges: " + orderDetails.getDouble("delivery_fee") + " Rs";
                        String grandTotal = "Grand Total: " + orderDetails.getDouble("grand_total") + " Rs";
                        String orderStatus = orderDetails.getString("status").toUpperCase();

                        // Set the values to the TextViews
                        txtOrderId.setText("Order# " + orderId);
                        txtOrderDay.setText(orderDay);
                        txtOrderMonth.setText(orderMonth);
                        txtShopName.setText(shopName);
                        txtShopAddress.setText(shopAddress);
                        txtOrderTotal.setText(orderTotal);
                        txtDeliveryCharges.setText(deliveryCharges);
                        txtGrandTotal.setText(grandTotal);
                        txtOrderStatus.setText(orderStatus);

                        // Parse chat messages
                        JSONArray chatMessages = data.getJSONArray("chat_messages");
                        orderChatList.clear();
                        for (int i = 0; i < chatMessages.length(); i++) {
                            orderChatList.add(chatMessages.getJSONObject(i));
                        }
                        orderChatAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("API Error", "Error parsing response: " + e.getMessage());
                    }
                },
                error -> {
                    Log.d("API Error", "Error fetching data: " + error.getMessage());
                });
    }


    private void openRateOrderBottomSheet(int orderId) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(OrderDetails.this);
        View sheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.rate_order_bottom_sheet, null);


        RatingBar ratingBar = sheetView.findViewById(R.id.rating_bar);
        Button btnSubmitRating = sheetView.findViewById(R.id.btn_submit_rating);
        LottieAnimationView lottieLoading = sheetView.findViewById(R.id.lottie_loading);
        TextView errorMessage = sheetView.findViewById(R.id.error_message);

        // Set submit button click listener
        btnSubmitRating.setOnClickListener(v -> {
            // Hide previous error message
            errorMessage.setVisibility(View.GONE);

            float rating = ratingBar.getRating();
            if (rating == 0) {
                // Show error message if rating is not provided
                errorMessage.setVisibility(View.VISIBLE);
                return; // Exit the method if validation fails
            }

            // Show Lottie animation
            lottieLoading.setVisibility(View.VISIBLE);

//            String comment = editComment.getText().toString().trim();
            JSONObject postData = new JSONObject();
            try {
                postData.put("order_id", orderId);
                postData.put("rating", rating);
                postData.put("reviews", "positive");

                ServiceProvider.getInstance(this).sendPostRequest(this, RATE_ORDER_URL, postData,
                        response -> {
                            // Hide Lottie animation and dismiss dialog on success
                            lottieLoading.setVisibility(View.GONE);
                            bottomSheetDialog.dismiss();
                        },
                        error -> {
                            // Hide Lottie animation on error
                            lottieLoading.setVisibility(View.GONE);
                        }
                );
            } catch (JSONException e) {
                e.printStackTrace();
                // Hide Lottie animation on exception
                lottieLoading.setVisibility(View.GONE);
            }
        });

        // Set the custom layout to the bottom sheet
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

}
