package com.townlift.townlift_customer;

import static com.townlift.townlift_customer.services.URLConstants.GET_ORDER_CHAT_MESSAGES_URL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.townlift.townlift_customer.adapters.MessageAdapter;
import com.townlift.townlift_customer.services.ServiceProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrderDetails extends AppCompatActivity {

    TextView headerTitle, txtOrderId, txtOrderDay, txtOrderMonth, txtShopName, txtShopAddress, txtOrderStatus;
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
        cmdNavBack = findViewById(R.id.cmd_nav_back);
        orderChatRecyclerView = findViewById(R.id.order_chat_recyclerview);

        headerTitle.setText("Order Details");
        cmdNavBack.setOnClickListener(v -> onBackPressed());

        // Get the Intent and extract the JSON String
        Intent intent = getIntent();
        String orderDetailsString = intent.getStringExtra("order_details");

        try {
            // Convert the String back to a JSONObject
            JSONObject orderDetails = new JSONObject(orderDetailsString);

            // Extract values from the JSONObject
            orderId = orderDetails.getInt("id");
            System.out.println("Order intent"+orderDetails.toString());
            String orderDay = orderDetails.getString("createdat").split("-")[2];
            String orderMonth = orderDetails.getString("createdat").split("-")[1] + " " + orderDetails.getString("createdat").split("-")[0];
            String shopName = orderDetails.getString("shop_name");
            String shopAddress = orderDetails.getString("shop_address");
            String orderTotal = "Total: " + orderDetails.getDouble("total") + " Rs";
            String deliveryCharges = "Delivery Charges: " + orderDetails.getDouble("delivery_charges") + " Rs";
            String grandTotal = "Grand Total: " + orderDetails.getDouble("grand_total") + " Rs";
            String orderStatus = orderDetails.getString("status");
            conversationId = orderDetails.getInt("conversation_id");

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

        } catch (JSONException e) {
            e.printStackTrace();
            // Handle JSON parsing error

        }
        userPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = userPreferences.getInt("id", -1);
        orderChatRecyclerView = findViewById(R.id.order_chat_recyclerview);
        orderChatAdapter = new MessageAdapter(orderChatList, userPreferences.getInt("id", -1));
        orderChatRecyclerView.setAdapter(orderChatAdapter);
        orderChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        apiGetOrderChatMessages();
    }

    void apiGetOrderChatMessages() {
        String url = String.format("%s%d%s%d", GET_ORDER_CHAT_MESSAGES_URL, orderId, "/", conversationId);
        System.out.println("chat messages URL: " + url);
        ServiceProvider.getInstance(this).sendPostRequest(this, url, null,
                response -> {
                    JSONArray data = (JSONArray) response;
                    orderChatList.clear();
                    for (int i = 0; i < data.length(); i++) {
                        try {
                            orderChatList.add(data.getJSONObject(i));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    orderChatAdapter.notifyDataSetChanged();
                    System.out.println("Chat Messages Response: " + data.toString());
                }
                ,
                error -> {
                    Log.d("API Error", "Error fetching order chat: " + error.getMessage());
                });
    }
}
