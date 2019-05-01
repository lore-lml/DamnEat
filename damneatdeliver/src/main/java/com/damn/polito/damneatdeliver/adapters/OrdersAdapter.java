package com.damn.polito.damneatdeliver.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.damneatdeliver.R;
import com.damn.polito.damneatdeliver.beans.Dish;
import com.damn.polito.damneatdeliver.beans.Order;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private List<Order> orders;
    private Context ctx;
    private OnItemClickListener mListener;
    private boolean colored = false;

    public OrdersAdapter(List<Order> orders, Context context){
        this.orders= orders;
        this.ctx = context;
    }

    public interface OnItemClickListener { void onItemClick(int position); }

    public void setOnItemClickListener (OnItemClickListener listener) { mListener = listener; }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view = LayoutInflater.from(ctx).inflate(R.layout.order_layout_first, parent, false);
            return new OrderViewHolder(view,mListener);
        } else {
            View view = LayoutInflater.from(ctx).inflate(R.layout.order_layout, parent, false);
            return new OrderViewHolder(view,mListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        DateFormat dateFormat = new SimpleDateFormat(ctx.getString(R.string.date_format), Locale.getDefault());
        Order selected = orders.get(position);
        holder.id.setText(ctx.getString(R.string.order_id, selected.getId()));
        holder.date.setText(dateFormat.format(selected.getDate()));
        holder.nDish.setText(ctx.getString(R.string.order_num_dishes, selected.getDishesNumber()));
        holder.price.setText(ctx.getString(R.string.order_price, selected.getPrice()));
        holder.deliverer_name.setText(selected.getDelivererName());
        holder.customer_info.setText(ctx.getString(R.string.order_customer,"\n"+selected.getCustomerName()+"\n"+selected.getCustomerAddress()));

       // holder.date.setText(dateFormat.format(ciao.getTime()));
        String dish_list_str = "";
        List<Dish> dishes = selected.getCumulatedDishes();
        for(int i=0; i<dishes.size(); i++){
            dish_list_str += dishes.get(i).getNumber() +"x\t"+ dishes.get(i).getName() + "\n";

        }
        holder.dishes_list.setText(dish_list_str);

        if (position == 0)
            holder.button.setOnClickListener((View v) -> {
                Toast.makeText(ctx, "Delivered", Toast.LENGTH_LONG).show();
            });

        if (!orders.get(position).isExpanded()) {
            holder.deliverer_name.setVisibility(View.GONE);
            holder.date.setVisibility(View.GONE);
            holder.dishes_list.setVisibility(View.GONE);
            holder.customer_info.setVisibility(View.GONE);
        }else{
            holder.deliverer_name.setVisibility(View.VISIBLE);
            holder.date.setVisibility(View.VISIBLE);
            holder.dishes_list.setVisibility(View.VISIBLE);
            holder.customer_info.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 1;
        else return 2;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView id,date,price,nDish, deliverer_name, dishes_list, customer_info;
        private CardView root;
        private Button button;

        public OrderViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);

            root =itemView.findViewById(R.id.card_order);
            id= itemView.findViewById(R.id.order_id);
            date = itemView.findViewById(R.id.order_date_value);
            price = itemView.findViewById(R.id.order_price);
            nDish = itemView.findViewById(R.id.order_num_dishes);
            deliverer_name = itemView.findViewById(R.id.order_deliverer_name_textview);
            dishes_list = itemView.findViewById(R.id.dishes_list);
            customer_info =itemView.findViewById(R.id.order_customer_info);
            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
            button = itemView.findViewById(R.id.confirmOrder);
        }
    }
}