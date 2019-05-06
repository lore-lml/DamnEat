package com.damn.polito.damneatrestaurant.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.google.firebase.database.collection.LLRBNode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private List<Order> orders;
    private Context ctx;
    private OnItemClickListener mListener;
    private OnButtonClickListener bListener;
    private Bitmap default_image;
    private OnButtonShippedClickListener bShipListener;
    private OnButtonRejectedClickListener bRejListener;
    private String key;
    public OrdersAdapter(List<Order> orders, Context context){
        this.orders= orders;
        this.ctx = context;


    }

    public interface OnItemClickListener { void onItemClick(int position); }


    public void setOnItemClickListener (OnItemClickListener listener) { mListener = listener; }

    public interface OnButtonClickListener { void onButtonClick(int position); }


    public void setOnButtonClickListener (OnButtonClickListener listener) { bListener = listener; }

    public interface OnButtonShippedClickListener { void onButtonShippedClick(int position); }


    public void setOnButtonShippedClickListener (OnButtonShippedClickListener listener) { bShipListener = listener; }

    public interface OnButtonRejectedClickListener { void onButtonRejectedClick(int position); }


    public void setOnButtonRejectedClickListener (OnButtonRejectedClickListener listener) { bRejListener = listener; }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.order_layout, parent, false);
        default_image = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.profile_sample);
        return new OrderViewHolder(view,mListener,bListener,bShipListener,bRejListener);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order selected = orders.get(position);
        //Calendar ciao= Calendar.getInstance();
        holder.id.setText(ctx.getString(R.string.order_id_s, selected.getId()));
        holder.date.setText(Utility.dateString(selected.getDate()));
        holder.nDish.setText(ctx.getString(R.string.order_num_dishes, selected.DishesNumber()));
        holder.price.setText(ctx.getString(R.string.order_price, selected.getPrice()));
        holder.deliverer_name.setText(selected.getDelivererName());
        holder.customer_info.setText(ctx.getString(R.string.order_customer_info)+"\n"+selected.getCustomerName()+"\n"+selected.getCustomerAddress());
        holder.time.setText(ctx.getString(R.string.order_delivery_time, selected.getDeliveryTime()));
        if(selected.getDelivererPhoto().equals("NO_PHOTO"))
            holder.deliverer_image.setImageBitmap(default_image);
        else
            holder.deliverer_image.setImageBitmap(Utility.StringToBitMap(selected.getDelivererPhoto()));

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
            holder.setAsRejected.setVisibility(View.GONE);
            if(selected.getState().equals("ordered")){
                holder.findDeliverer.setVisibility(View.VISIBLE);
            }
            else{
                holder.findDeliverer.setVisibility(View.GONE);
            }
            if(selected.getState().equals("accepted") || selected.getState().equals("ordered") || selected.getState().equals("rejected")){
                holder.deliverer_name.setVisibility(View.GONE);
                holder.deliverer_image.setVisibility(View.GONE);
            }
            else{
                holder.deliverer_name.setVisibility(View.VISIBLE);
                holder.deliverer_image.setVisibility(View.VISIBLE);
            }

            if(selected.getState().equals("assigned")){
                holder.setAsShipped.setVisibility(View.VISIBLE);
            }
            else{
                holder.setAsShipped.setVisibility(View.GONE);
            }
        }else{
            if(selected.getState().equals("ordered") ){
                holder.findDeliverer.setVisibility(View.VISIBLE);
            }
            else{
                holder.findDeliverer.setVisibility(View.GONE);
            }
            if(selected.getState().equals("accepted") || selected.getState().equals("ordered") || selected.getState().equals("rejected")){
                holder.deliverer_name.setVisibility(View.GONE);
                holder.deliverer_image.setVisibility(View.GONE);
            }
            else{
                holder.deliverer_name.setVisibility(View.VISIBLE);
                holder.deliverer_image.setVisibility(View.VISIBLE);
                holder.setAsRejected.setVisibility(View.GONE);
            }
            if(selected.getState().equals("ordered")) {
                holder.setAsRejected.setVisibility(View.VISIBLE);
            }
            else{
                holder.setAsRejected.setVisibility(View.GONE);
            }

            if(selected.getState().equals("assigned")){
                holder.setAsShipped.setVisibility(View.VISIBLE);
            }
            else{
                holder.setAsShipped.setVisibility(View.GONE);
            }
            holder.date.setVisibility(View.VISIBLE);
            holder.dishes_list.setVisibility(View.VISIBLE);
            holder.customer_info.setVisibility(View.VISIBLE);
        }

        if(selected.getState().equals("ordered"))
            holder.state.setText(ctx.getString(R.string.ordered));
        if(selected.getState().equals("delivered"))
            holder.state.setText(ctx.getString(R.string.delivered));
        if(selected.getState().equals("rejected")){
            holder.state.setText(ctx.getString(R.string.rejected));
            holder.state.setTextColor(ctx.getColor(R.color.colorAccent));
        }
        if(selected.getState().equals("confirmed")){
            holder.state.setText(ctx.getString(R.string.confirmed));
            holder.state.setTextColor(ctx.getColor(R.color.colorGreen));
        }else {
            holder.state.setTextColor(Color.BLACK);
        }

        if(selected.getState().equals("accepted"))
            holder.state.setText(ctx.getString(R.string.accepted));
        if(selected.getState().equals("shipped"))
            holder.state.setText(ctx.getString(R.string.shipped));
        if(selected.getState().equals("assigned"))
            holder.state.setText(ctx.getString(R.string.assigned));

    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView id,date,price,nDish, deliverer_name, dishes_list, customer_info,state,time;
        private CardView root;
        private Button findDeliverer, setAsShipped, setAsRejected;
        private CircleImageView deliverer_image;

        public OrderViewHolder(View itemView, OnItemClickListener listener,OnButtonClickListener buttonListener, OnButtonShippedClickListener bShipListener, OnButtonRejectedClickListener bRejectedListener) {
            super(itemView);

            root =itemView.findViewById(R.id.card_order);
            id= itemView.findViewById(R.id.order_id);
            date = itemView.findViewById(R.id.order_date_value);
            price = itemView.findViewById(R.id.order_price);
            nDish = itemView.findViewById(R.id.order_num_dishes);
            deliverer_name = itemView.findViewById(R.id.order_deliverer_name_textview);
            dishes_list = itemView.findViewById(R.id.dishes_list);
            customer_info =itemView.findViewById(R.id.order_customer);
            findDeliverer=itemView.findViewById(R.id.order_find_deliverer_button);
            state=itemView.findViewById(R.id.state_tv_edit);
            time=itemView.findViewById(R.id.delivery_time_tv);
            deliverer_image=itemView.findViewById(R.id.circleImageView);
            setAsShipped=itemView.findViewById(R.id.order_set_shipped);
            setAsRejected=itemView.findViewById(R.id.order_button_reject);
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

            setAsShipped.setOnClickListener(v -> {
                if (bShipListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        bShipListener.onButtonShippedClick(position);
                    }
                }
            });

            setAsRejected.setOnClickListener(v -> {
                if (bRejectedListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        bRejectedListener.onButtonRejectedClick(position);
                    }
                }
            });
        }
    }

    public String stringOrDefault(String s) {
        return (s == null || s.trim().isEmpty()) ? "" : s;
    }

}
