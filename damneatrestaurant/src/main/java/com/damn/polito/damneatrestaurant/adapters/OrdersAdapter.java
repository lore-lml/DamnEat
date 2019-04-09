package com.damn.polito.damneatrestaurant.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.beans.Order;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private List<Order> orders;
    private Context ctx;

    public OrdersAdapter(List<Order> orders, Context context){
        this.orders= orders;
        this.ctx = context;
    }
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.order_layout, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

        holder.id.setText(String.valueOf(orders.get(position).getId()));
        holder.date.setText(dateFormat.format(orders.get(position).getDate()));
        holder.nDish.setText(String.valueOf(orders.get(position).getDishes().size()));
        holder.price.setText(String.valueOf(orders.get(position).getPrice()));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView id,date,price,nDish;
        private CardView root;

        public OrderViewHolder(View itemView) {
            super(itemView);
            root =itemView.findViewById(R.id.card_order);
            id= itemView.findViewById(R.id.order_id_value);
            date = itemView.findViewById(R.id.order_date_value);
            price = itemView.findViewById(R.id.order_price);
            nDish = itemView.findViewById(R.id.order_num_dishes_value);
        }
    }
}
