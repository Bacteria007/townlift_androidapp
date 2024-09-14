package com.townlift.townlift_customer.services;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class SocketManager {

    private static Socket socket;
    private static SocketManager instance;
    private final String serverUrl = URLConstants.BASE_URL; // Replace with your server URL
    private String userId;

    private SocketManager() {
        // Private constructor to prevent instantiation
    }

    public static SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public void initialize(String userId) {
        this.userId = userId;
        try {
            socket = IO.socket(serverUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void connect(int conversationId, Emitter.Listener onNewMessage) {
        socket.on(Socket.EVENT_CONNECT, args -> {
            Log.d("SocketIO", "Connected to the server");
            emitUserId();
            joinPrivateChat(conversationId);
        });

        socket.on("receive_message", args -> {
            Log.d("SocketIO", "Message received in SocketManager: " + args[0]);
            if (onNewMessage != null) {
                onNewMessage.call(args);
            }
        });

        socket.connect();
    }

    private void emitUserId() {
        if (userId != null && socket != null) {
            socket.emit("set_user_id", userId);
            Log.d("SocketIO", "UserId emitted: " + userId);
        }
    }

    private void joinPrivateChat(int conversationId) {
        try {
            JSONObject joinData = new JSONObject();
            joinData.put("conversation_id", conversationId);
            socket.emit("join_private_chat", joinData);
            Log.d("SocketIO", "Joined private chat: " + joinData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String userId, JSONObject conversation, String messageText, final MyAck ack) {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
            ZonedDateTime truncatedNow = now.truncatedTo(ChronoUnit.MILLIS);
            String isoTimestamp = truncatedNow.format(DateTimeFormatter.ISO_INSTANT);
            JSONObject messageData = new JSONObject();
            messageData.put("sender_id", userId);
            messageData.put("receiver_id", conversation.getString("admin_id"));
            messageData.put("msg_content", messageText);
            messageData.put("conversation_id", conversation.getString("id"));
            messageData.put("createdat", isoTimestamp);
            messageData.put("status", "pending");
//            Log.d("SocketIO", "Message sent: " + messageData.toString());

            socket.emit("send_message", messageData, new io.socket.client.Ack() {
                @Override
                public void call(Object... args) {
                    if (args.length > 0 && args[0] instanceof JSONObject) {
                        JSONObject ackData = (JSONObject) args[0];
                        ack.myAckcall(ackData);
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(Emitter.Listener onNewMessage) {
        if (socket != null) {
            socket.off("receive_message", onNewMessage);
            socket.disconnect();
            Log.d("SocketIO", "Socket disconnected");
        }
    }
}

