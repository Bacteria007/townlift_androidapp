package com.townlift.townlift_customer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.townlift.townlift_customer.adapters.ShopAdapter;
import com.townlift.townlift_customer.helpers.SnackbarUtils;
import com.townlift.townlift_customer.services.ServiceProvider;
import com.townlift.townlift_customer.services.URLConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ShopAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ShopAdapter shopAdapter;
    private List<JSONObject> shopList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView txtGreeting, txtUserName;
    private ImageView cmdLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtGreeting = findViewById(R.id.txt_greeting);
        txtUserName = findViewById(R.id.txt_user_name);
        cmdLogout = findViewById(R.id.cmd_logout);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("name", "");
        txtUserName.setText(userName);

        cmdLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(MainActivity.this, AuthOptions.class);
            startActivity(intent);
            finish();
        });
        // Update greeting based on the time of day
        updateGreetingBasedOnTime();

        ImageView imageView = findViewById(R.id.dp_image);
        String profileUrl = getProfileUrl(); // Method to get profile URL from SharedPreferences or any other source

        // Check if profileUrl is available
        if (profileUrl != null && !profileUrl.isEmpty()) {
            String imageUrl;
            if (profileUrl.startsWith("http") || profileUrl.startsWith("https")) {
                imageUrl = profileUrl; // Use the URL as is
            } else {
                imageUrl = URLConstants.BASE_URL + profileUrl; // Append base URL
            }
            // Load image from the constructed URL
            Glide.with(this)
                    .load(imageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);

        } else {
            // Load default drawable image
            Glide.with(this)
                    .load(R.drawable.dpimage) // Replace with your default drawable resource
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.available_shops);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Adapter with the shopList
        shopAdapter = new ShopAdapter(shopList, this);
        recyclerView.setAdapter(shopAdapter);

        // Set the refresh listener for SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::fetchShopsFromServer);

        // Fetch data from the server
        fetchShopsFromServer();
    }

    private void updateGreetingBasedOnTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            txtGreeting.setText("Good Morning");
        } else if (hour >= 12 && hour < 17) {
            txtGreeting.setText("Good Afternoon");
        } else if (hour >= 17 && hour < 21) {
            txtGreeting.setText("Good Evening");
        } else {
            txtGreeting.setText("Good Night");
        }
    }

    private void fetchShopsFromServer() {
        // Show refresh animation
        swipeRefreshLayout.setRefreshing(true);

        String url = URLConstants.GET_ALL_SHOPS_URL;

        ServiceProvider.getInstance(this).sendPostRequest(
                this,
                url,
                null, // Assuming no post data is required for fetching shops
                data -> {
                    try {
                        if (data instanceof JSONArray) {
                            JSONArray jsonArrayResponse = (JSONArray) data;
                            if (jsonArrayResponse.length() == 0) {
                                showNoShopsAvailableAnimation();
                                swipeRefreshLayout.setRefreshing(false);  // Stop the refresh animation
                                return;
                            }
                            shopList.clear();
                            for (int i = 0; i < jsonArrayResponse.length(); i++) {
                                JSONObject shopJson = jsonArrayResponse.getJSONObject(i);
                                if (shopJson.has("id") && shopJson.has("image") && shopJson.has("name") &&
                                        shopJson.has("delivery_fee") && shopJson.has("delivery_time")) {
                                    shopList.add(shopJson);
                                    System.out.println("Shop JSON Object: " + shopJson.toString());
                                } else {
                                    System.out.println("Missing fields in shop JSON object at index: " + i);
                                }
                            }

                            // Notify the adapter that data has changed
                            shopAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);  // Stop the refresh animation

                        } else if (data instanceof JSONObject) {
                            JSONObject jsonObjectResponse = (JSONObject) data;
                            System.out.println("Server Response: " + jsonObjectResponse.toString());
                            swipeRefreshLayout.setRefreshing(false);  // Stop the refresh animation
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        System.out.println("Parsing Error: " + e.getMessage());
                        SnackbarUtils.showErrorSnackbar(this, "Parsing Error: " + e.getMessage());
                        swipeRefreshLayout.setRefreshing(false);  // Stop the refresh animation
                    }
                },
                error -> {
                    error.printStackTrace();
                    SnackbarUtils.showErrorSnackbar(this, "Error" + error.getMessage());
                    System.out.println("Request Error: " + error.getMessage());
                    swipeRefreshLayout.setRefreshing(false);  // Stop the refresh animation
                }
        );
    }

    private void showNoShopsAvailableAnimation() {
        LottieAnimationView lottieAnimationView = findViewById(R.id.lottie_no_shops);
        lottieAnimationView.setVisibility(View.VISIBLE);
        lottieAnimationView.playAnimation();

        recyclerView.setVisibility(View.GONE);

        System.out.println("No shops available, showing Lottie animation.");
    }

    @Override
    public void onItemClick(int position) {
        JSONObject selectedShop = shopList.get(position);

        try {
            Intent intent = new Intent(MainActivity.this, ShopDetails.class);
            intent.putExtra("id", selectedShop.getInt("id"));
            intent.putExtra("name", selectedShop.getString("name"));
            intent.putExtra("image", selectedShop.getString("image"));
            intent.putExtra("delivery_fee", selectedShop.getString("delivery_fee"));
            intent.putExtra("delivery_time", selectedShop.getString("delivery_time"));
            intent.putExtra("average_rating", selectedShop.getString("average_rating"));
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getProfileUrl() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("profile_url", null); // Replace "profileUrl" with your actual key
    }
}

