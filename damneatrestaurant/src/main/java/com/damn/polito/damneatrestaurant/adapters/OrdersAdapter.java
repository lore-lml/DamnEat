package com.damn.polito.damneatrestaurant.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneatrestaurant.beans.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private List<Order> orders;
    private Context ctx;
    private OnItemClickListener mListener;
    private OnButtonClickListener bListener;
    private String key;
    public OrdersAdapter(List<Order> orders, Context context){
        this.orders= orders;
        this.ctx = context;


    }

    public interface OnItemClickListener { void onItemClick(int position); }


    public void setOnItemClickListener (OnItemClickListener listener) { mListener = listener; }

    public interface OnButtonClickListener { void onButtonClick(int position); }


    public void setOnButtonClickListener (OnButtonClickListener listener) { bListener = listener; }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.order_layout, parent, false);
        return new OrderViewHolder(view,mListener,bListener);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        DateFormat dateFormat = new SimpleDateFormat(ctx.getString(R.string.date_format), Locale.getDefault());
        Order selected = orders.get(position);
        //Calendar ciao= Calendar.getInstance();
        holder.id.setText(ctx.getString(R.string.order_id_s, selected.sId()));
        holder.date.setText(dateFormat.format(selected.getDate()));
        holder.nDish.setText(ctx.getString(R.string.order_num_dishes, selected.DishesNumber()));
        holder.price.setText(ctx.getString(R.string.order_price, selected.getPrice()));
        holder.deliverer_name.setText(selected.getDelivererName());
        holder.customer_info.setText(ctx.getString(R.string.order_customer,"\n"+selected.getCustomerName()+"\n"+selected.getCustomerAddress()));

       // holder.date.setText(dateFormat.format(ciao.getTime()));
        String dish_list_str = "";
        List<Dish> dishes = selected.getDishes();
        for (Dish d:dishes) {
            String p = String.format("%.2f", d.getPrice());
            dish_list_str += d.getQuantity() +"\tx\t"+ d.getName()+"\t"+ p + "€\n";
        }
        holder.dishes_list.setText(dish_list_str);

        if (!orders.get(position).Expanded()) {
            holder.deliverer_name.setVisibility(View.GONE);
            holder.date.setVisibility(View.GONE);
            holder.dishes_list.setVisibility(View.GONE);
            holder.customer_info.setVisibility(View.GONE);
            if(selected.getState().equals("ordered")){
                holder.findDeliverer.setVisibility(View.VISIBLE);
            }
            else{
                holder.findDeliverer.setVisibility(View.GONE);
            }
        }else{
            if(selected.getState().equals("ordered")){
                holder.deliverer_name.setVisibility(View.GONE);
                holder.findDeliverer.setVisibility(View.VISIBLE);
            }
            else{
                holder.deliverer_name.setVisibility(View.VISIBLE);
                holder.findDeliverer.setVisibility(View.GONE);
            }
            holder.date.setVisibility(View.VISIBLE);
            holder.dishes_list.setVisibility(View.VISIBLE);
            holder.customer_info.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView id,date,price,nDish, deliverer_name, dishes_list, customer_info;
        private CardView root;
        private Button findDeliverer;

        public OrderViewHolder(View itemView, OnItemClickListener listener,OnButtonClickListener buttonListener) {
            super(itemView);

            root =itemView.findViewById(R.id.card_order);
            id= itemView.findViewById(R.id.order_id);
            date = itemView.findViewById(R.id.order_date_value);
            price = itemView.findViewById(R.id.order_price);
            nDish = itemView.findViewById(R.id.order_num_dishes);
            deliverer_name = itemView.findViewById(R.id.order_deliverer_name_textview);
            dishes_list = itemView.findViewById(R.id.dishes_list);
            customer_info =itemView.findViewById(R.id.order_customer_info);
            findDeliverer=itemView.findViewById(R.id.order_find_deliverer_button);
            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });

            findDeliverer.setOnClickListener(v ->{
                if (buttonListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        buttonListener.onButtonClick(position);
                    }
                }
            });
        }
    }

    public String stringOrDefault(String s) {
        return (s == null || s.trim().isEmpty()) ? "" : s;
    }

}
