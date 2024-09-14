package com.townlift.townlift_customer.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.townlift.townlift_customer.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<JSONObject> messageList;
    private int userId;

    public MessageAdapter(List<JSONObject> messageList, int userId) {
        this.messageList = messageList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_msg, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        JSONObject message = messageList.get(position);
        try {
            String msgContent = message.getString("msg_content");
            int senderId = message.getInt("sender_id");
            String createdAt = message.getString("createdat");
            String formattedTime = formatTime(createdAt);
            boolean isOrderMessage = message.has("order_id");
            holder.txtMessageTime.setText(formattedTime);
            holder.txtMessage.setText(msgContent);

            if (isOrderMessage) {
                // Set dashed line visibility based on message type
                holder.dashedLine1.setVisibility(View.VISIBLE);
                holder.dashedLine2.setVisibility(View.VISIBLE);
                holder.txtOrderDetails.setVisibility(View.VISIBLE);
                holder.txtGrandTotal.setVisibility(View.VISIBLE);
                // Split the message content by new lines
                String[] lines = msgContent.split("\n");

                if (lines.length > 0) {
                    holder.txtOrderDetails.setText(lines[0]); // First line: Order details or message
                }
                if (lines.length > 1) {
                    String totalLine = lines[1];
                    holder.txtOrderDetails.setText(String.format("%s\n%s", holder.txtOrderDetails.getText(), totalLine)); // Add total line
                }
                if (lines.length > 2) {
                    String deliveryChargesLine = lines[2];
                    String grandTotalLine = lines.length > 3 ? lines[3] : "";
                    holder.txtOrderDetails.setText(String.format("%s\n%s", holder.txtOrderDetails.getText(), deliveryChargesLine));
                    holder.txtGrandTotal.setText(grandTotalLine);
                }

//                holder.btnCheckOrder.setVisibility(View.VISIBLE);
//
//                // Handle order button click if necessary
//                holder.btnCheckOrder.setOnClickListener(v -> {
//                    // Navigate to order details
//                });
            }


            // Set alignment based on sender ID
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageItemRoot.getLayoutParams();
            if (senderId == userId) {
                params.gravity = Gravity.END;
            } else {
                params.gravity = Gravity.START;
            }
            holder.messageItemRoot.setLayoutParams(params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        TextView txtMessageTime;
        LinearLayout messageItemRoot;
        View dashedLine1, dashedLine2;
        TextView txtOrderDetails;
        TextView txtGrandTotal;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txt_message);
            txtMessageTime = itemView.findViewById(R.id.txt_message_time);
            messageItemRoot = itemView.findViewById(R.id.message_item_root);
            dashedLine1 = itemView.findViewById(R.id.dashed_line1); // Initialize dashed line view
            dashedLine2 = itemView.findViewById(R.id.dashed_line2); // Initialize dashed line view
            txtOrderDetails = itemView.findViewById(R.id.txt_order_details);
            txtGrandTotal = itemView.findViewById(R.id.txt_grand_total);
        }
    }


    private String formatTime(String isoTime) {
        try {
            // Parse the original ISO time string
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = isoFormat.parse(isoTime);

            // Format to the desired time format
            SimpleDateFormat desiredFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return desiredFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return ""; // Return empty string in case of error
        }
    }
}
