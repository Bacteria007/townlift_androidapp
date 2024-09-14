package com.townlift.townlift_customer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
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

import com.townlift.townlift_customer.adapters.MessageAdapter;
import com.townlift.townlift_customer.helpers.SnackbarUtils;
import com.townlift.townlift_customer.services.MyAck;
import com.townlift.townlift_customer.services.ServiceProvider;
import com.townlift.townlift_customer.services.SocketManager;
import com.townlift.townlift_customer.services.URLConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.socket.emitter.Emitter;

public class NewOrder extends AppCompatActivity {
    private SocketManager socketManager;
    TextView txtHeaderTitle;
    ImageView cmdNavBack;
    EditText msgInput;
    ImageView sendBtn;
    private JSONObject shopDetails;
    private JSONObject conversation;
    private JSONObject message;
    int userId, adminId, shopId, conversationId;
    List<JSONObject> messages = new ArrayList<>();
    // Add this in your onCreate method
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);
        // Adjust for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SocketManager
        socketManager = SocketManager.getInstance();
        txtHeaderTitle = findViewById(R.id.header_title);
        cmdNavBack = findViewById(R.id.cmd_nav_back);
        cmdNavBack.setOnClickListener(v -> onBackPressed());
        sendBtn = findViewById(R.id.cmd_send_message);
        msgInput = findViewById(R.id.edt_message_input);
        swipeRefreshLayout = findViewById(R.id.msgListSwipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::apiGetMessages);
        apiGetMessages();
        try {
            conversation = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("conversation")));
            shopDetails = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("shop_details")));
            conversationId = conversation.getInt("id");
            userId = conversation.getInt("user_id");
            adminId = conversation.getInt("admin_id");
            shopId = conversation.getInt("shop_id");
            txtHeaderTitle.setText(shopDetails.getString("name"));
            // Initialize SocketManager with userId
            socketManager = SocketManager.getInstance();
            socketManager.initialize(String.valueOf(userId));
            socketManager.connect(conversationId, onNewMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        recyclerView = findViewById(R.id.msg_list_recycler_view);
        messageAdapter = new MessageAdapter(messages, userId);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Add TextWatcher to EditText
        msgInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Change the icon based on whether the input is empty or not
                if (s.toString().trim().isEmpty()) {
                    sendBtn.setImageResource(R.drawable.send_disabeled);
                    sendBtn.setEnabled(false);
                } else {
                    sendBtn.setImageResource(R.drawable.baseline_send_24);
                    sendBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No need to handle this
            }
        });

        sendBtn.setOnClickListener(v -> {
            String messageText = msgInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                // Create message data
                JSONObject messageData = new JSONObject();
                try {
                    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
                    ZonedDateTime truncatedNow = now.truncatedTo(ChronoUnit.MILLIS);
                    String isoTimestamp = truncatedNow.format(DateTimeFormatter.ISO_INSTANT);

                    messageData.put("sender_id", String.valueOf(userId));
                    messageData.put("receiver_id", conversation.getString("admin_id"));
                    messageData.put("msg_content", messageText);
                    messageData.put("conversation_id", conversation.getString("id"));
                    messageData.put("createdat", isoTimestamp);
                    messageData.put("status", "pending");

                    // Send message and handle acknowledgment
                    socketManager.sendMessage(String.valueOf(userId), conversation, messageText, ackData -> {
                        try {
                            if (ackData.getBoolean("status")) {
                                Log.d("SocketIO", "Message sent successfully");

                                // Add message to the list and update RecyclerView
                                messages.add(messageData);
                                runOnUiThread(() -> {
                                    messageAdapter.notifyItemInserted(messages.size() - 1);
                                    recyclerView.scrollToPosition(messages.size() - 1);
                                });
                            } else {
                                Log.d("SocketIO", "Message NOT sent");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                msgInput.setText("");
            }
        });
    }

    void apiGetMessages() {
        String url = String.format("%s%d", URLConstants.GET_MESSAGES_URL, conversationId);
        swipeRefreshLayout.setRefreshing(true);
        ServiceProvider.getInstance(this).sendPostRequest(this, url, null,
                response -> {
                    swipeRefreshLayout.setRefreshing(false);
                    try {
                        JSONArray data = (JSONArray) response;
                        messages.clear(); // Clear existing messages
                        for (int i = 0; i < data.length(); i++) {
                            messages.add(data.getJSONObject(i));
                        }
                        messageAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        SnackbarUtils.showErrorSnackbar(this, "Failed to parse response");
                    }
                },
                error -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Log.d("SocketIO", "Error fetching messages: " + error.getMessage());
                }
        );
    }


    private final Emitter.Listener onNewMessage = args -> {
        runOnUiThread(() -> {
            Log.d("SocketIO", "New message received in NewOrder: " + args[0]);
            try {
                JSONObject messageData = (JSONObject) args[0];
                messages.add(messageData);
                messageAdapter.notifyItemInserted(messages.size() - 1);
                recyclerView.scrollToPosition(messages.size() - 1);
            } catch (Exception e) {
                Log.e("SocketIO", "Error processing new message in NewOrder: " + e.getMessage());
            }
        });
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        socketManager.disconnect(onNewMessage);
    }

}
