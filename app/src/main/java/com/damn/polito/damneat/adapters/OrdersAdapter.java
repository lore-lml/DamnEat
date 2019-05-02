package com.damn.polito.damneat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneat.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private List<Order> orders;
    private Context ctx;
    private OnItemClickListener mListener;

    public OrdersAdapter(List<Order> orders, Context context){
        this.orders= orders;
        this.ctx = context;
    }

    public interface OnItemClickListener { void onItemClick(int position); }

    public void setOnItemClickListener (OnItemClickListener listener) { mListener = listener; }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.order_layout, parent, false);
        return new OrderViewHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        DateFormat dateFormat = new SimpleDateFormat(ctx.getString(R.string.date_format), Locale.getDefault());
        Order selected = orders.get(position);
        //Calendar ciao= Calendar.getInstance();
        holder.id.setText(ctx.getString(R.string.order_id, selected.getId()));
        holder.date.setText(dateFormat.format(selected.getDate()));
        holder.nDish.setText(ctx.getString(R.string.order_num_dishes, selected.DishesNumber()));
        holder.price.setText(ctx.getString(R.string.order_price, selected.getPrice()));
        holder.deliverer_name.setText(selected.getDelivererName());
        String restaurant_info = ctx.getString(R.string.restaurant)+ "\n" + selected.getRestaurant().getRestaurantName();
        holder.customer_info.setText(restaurant_info);
        holder.root.setOnClickListener(v->{
            selected.changeExpanded();
            Toast.makeText(ctx, "TEST", Toast.LENGTH_LONG);
        });
       // holder.date.setText(dateFormat.format(ciao.getTime()));
        String dish_list_str = "";
        List<Dish> dishes = selected.getDishes();
        Double price = 0.;
        for (Dish d:dishes) {
            String p = String.format("%.2f", d.getPrice());
            dish_list_str += d.getQuantity() +"x\t"+ d.getName()+"\t"+ p + "€\n";
            price += d.getQuantity()*d.getPrice();
        }
        if(selected.getRestaurant().getRestaurant_price_ship() != null && selected.getRestaurant().getRestaurant_price_ship() != 0.) {
            String p = String.format("%.2f", selected.getRestaurant().getRestaurant_price_ship());
            dish_list_str += ctx.getString(R.string.ship) + " " + p + "€";
            Log.d("test", selected.getRestaurant().getRestaurant_price_ship().toString());
            price += selected.getRestaurant().getRestaurant_price_ship();
        }
        holder.dishes_list.setText(dish_list_str);



        if (!selected.Expanded()) {
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

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView id,date,price,nDish, deliverer_name, dishes_list, customer_info;
        private CardView root;

        public OrderViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);

            root =itemView.findViewById(R.id.card_order_customer);
            id= itemView.findViewById(R.id.order_id);
            date = itemView.findViewById(R.id.order_date_value);
            price = itemView.findViewById(R.id.order_price);
            nDish = itemView.findViewById(R.id.order_num_dishes);
            deliverer_name = itemView.findViewById(R.id.order_deliverer_name_textview);
            dishes_list = itemView.findViewById(R.id.dishes_list);
            customer_info =itemView.findViewById(R.id.order_customer_info);
        }
    }
}
