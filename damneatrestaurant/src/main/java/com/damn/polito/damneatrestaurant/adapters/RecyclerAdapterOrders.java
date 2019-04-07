package com.damn.polito.damneatrestaurant.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.beans.Order;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;


public class RecyclerAdapterOrders extends RecyclerView.Adapter<RecyclerAdapterOrders.MyViewHolder> {

    private List<Order> orders;

    public RecyclerAdapterOrders(List<Order> orders){
        this.orders= orders;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        CardView Order;
        TextView id,date,price,nDish;
        public MyViewHolder(CardView itemView){

            super(itemView);
            Order=(CardView)itemView.findViewById(R.id.card_order);
            id=(TextView)itemView.findViewById(R.id.order_id_value);
            date=(TextView)itemView.findViewById(R.id.order_date_value);
            price=(TextView)itemView.findViewById(R.id.order_price);
            nDish=(TextView)itemView.findViewById(R.id.order_num_dishes_value);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }


    @Override
    public RecyclerAdapterOrders.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.single_order_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(cardView);

        return  myViewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

        holder.id.setText(String.valueOf(orders.get(position).getId()));
        holder.date.setText(dateFormat.format(orders.get(position).getDate()));
        holder.nDish.setText(String.valueOf(orders.get(position).getDishes().size()));
        holder.price.setText(String.valueOf(orders.get(position).getPrice()));
    }


}
