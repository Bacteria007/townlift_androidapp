package com.townlift.townlift_customer.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.townlift.townlift_customer.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PreviousOrdersAdapter extends RecyclerView.Adapter<PreviousOrdersAdapter.OrderViewHolder> {

    private List<JSONObject> orderList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public PreviousOrdersAdapter(Context context, List<JSONObject> orderList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.orderList = orderList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_previous_orders, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        JSONObject order = orderList.get(position);
        try {
            String orderId = String.valueOf(order.getInt("id"));
            holder.txtOrderId.setText("Order# " + orderId);
            String createdAt = order.getString("createdat");
            String[] dateParts = createdAt.split("-");
            if (dateParts.length >= 2) {
                String month = dateParts[1];
                String year = dateParts[0];
                holder.txtOrderDay.setText(dateParts[2].substring(0, 2)); // Day
                holder.txtOrderMonth.setText(month + " " + year); // Month and Year
            } else {
                holder.txtOrderDay.setText("--");
                holder.txtOrderMonth.setText("--");
            }

            holder.txtShopName.setText(order.optString("shop_name", "N/A"));
            holder.txtShopAddress.setText(order.optString("shop_address", "N/A"));
            holder.txtOrderStatus.setText(order.optString("status", "Unknown"));

            // Set click listener
            holder.itemView.setOnClickListener(v -> {
                try {
                    onItemClickListener.onItemClick(position);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            holder.txtOrderId.setText("Error");
            holder.txtOrderDay.setText("Error");
            holder.txtOrderMonth.setText("Error");
            holder.txtShopName.setText("Error");
            holder.txtShopAddress.setText("Error");
            holder.txtOrderStatus.setText("Error");

            Log.d("PreviousOrdersAdapter", "Error parsing JSON: " + e.getMessage());
        }
    }


    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtOrderDay, txtOrderMonth, txtShopName, txtShopAddress, txtOrderStatus;
        ImageView cmdViewDetails;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txt_order_id);
            txtOrderDay = itemView.findViewById(R.id.txt_order_day);
            txtOrderMonth = itemView.findViewById(R.id.txt_order_month);
            txtShopName = itemView.findViewById(R.id.txt_shop_name);
            txtShopAddress = itemView.findViewById(R.id.txt_shop_address);
            txtOrderStatus = itemView.findViewById(R.id.txt_order_status);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position) throws JSONException;
    }
}

