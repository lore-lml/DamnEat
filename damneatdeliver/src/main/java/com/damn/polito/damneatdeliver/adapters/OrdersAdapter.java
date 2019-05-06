package com.damn.polito.damneatdeliver.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneatdeliver.R;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private List<Order> orders;
    private Context ctx;
    private OnItemClickListener mListener;
    private boolean free = false;

    public OrdersAdapter(List<Order> orders, Context context){
        this.orders= orders;
        this.ctx = context;
    }

    public interface OnItemClickListener { void onItemClick(int position); }

    public void setOnItemClickListener (OnItemClickListener listener) { mListener = listener; }


    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if (viewType == 1) {
//            View view = LayoutInflater.from(ctx).inflate(R.layout.order_layout_first, parent, false);
//            return new OrderViewHolder(view,mListener);
//        }else {
            View view = LayoutInflater.from(ctx).inflate(R.layout.order_layout, parent, false);
            return new OrderViewHolder(view,mListener);
//        }
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order selected = orders.get(position);
        Bitmap img = null;
        if(selected == null){
            holder.root.setVisibility(View.GONE);
            return;
        }
        holder.root.setVisibility(View.VISIBLE);

        if(selected.getRestaurant().getPhoto()!=null)
            if(!selected.getRestaurant().getPhoto().equals("NO_PHOTO")){
                img = Utility.StringToBitMap(selected.getRestaurant().getPhoto());
                holder.image.setImageBitmap(img);
            }

        String id = ctx.getString(R.string.order_id_s, selected.Id());
        holder.id.setText(id);
        holder.date.setText(Utility.dateString(selected.getDate()));
        holder.nDish.setText(ctx.getString(R.string.order_num_dishes, selected.DishesNumber()));
        holder.price.setText(ctx.getString(R.string.order_price, selected.getPrice()));

        holder.customer_info.setText(ctx.getString(R.string.string, selected.getCustomerName()+"\n"+selected.getCustomerAddress()));
        holder.restaurant_i.setText(ctx.getString(R.string.string, selected.getRestaurant().getRestaurantName()+"\n"+selected.getRestaurant().getRestaurantAddress()));
//        holder.delivery_time.setText(ctx.getString(R.string.delivery_time, selected.getDeliveryTime()));
//        holder.note.setText(ctx.getString(R.string.string, ""+selected.getNote()));

        if(img != null)
            holder.image.setImageBitmap(img);
        else
            holder.image.setImageResource(R.drawable.dish_preview);

        String dish_list_str = "";
        List<Dish> dishes = selected.getDishes();
        Double price = 0.;
        for (Dish d:dishes) {
            String p = String.format("%.2f", d.getPrice());
            dish_list_str += d.getQuantity() +"\tx\t"+ d.getName()+"\t"+ p + "€\n";
            price += d.getQuantity()*d.getPrice();
        }
        if(selected.getRestaurant().getRestaurant_price_ship() != null && selected.getRestaurant().getRestaurant_price_ship() != 0.) {
            String p = String.format("\n%.2f", selected.getRestaurant().getRestaurant_price_ship());
            dish_list_str += ctx.getString(R.string.ship) + " " + p + "€";
            Log.d("test", selected.getRestaurant().getRestaurant_price_ship().toString());
            price += selected.getRestaurant().getRestaurant_price_ship();
        }

        holder.dishes_list.setText(dish_list_str);
        if(selected.getState().equals("ordered"))
            holder.state_tv.setText(ctx.getString(R.string.state, ctx.getString(R.string.ordered)));
        if(selected.getState().equals("delivered"))
            holder.state_tv.setText(ctx.getString(R.string.state, ctx.getString(R.string.delivered)));
        if(selected.getState().equals("confirmed"))
            holder.state_tv.setText(ctx.getString(R.string.state, ctx.getString(R.string.confirmed)));
        if(selected.getState().equals("rejected"))
            holder.state_tv.setText(ctx.getString(R.string.state, ctx.getString(R.string.reject)));
        if(selected.getState().equals("accepted"))
            holder.state_tv.setText(ctx.getString(R.string.state, ctx.getString(R.string.accepted)));
        if(selected.getState().equals("shipped"))
            holder.state_tv.setText(ctx.getString(R.string.state, ctx.getString(R.string.shipped)));
        if(selected.getState().equals("assigned"))
            holder.state_tv.setText(ctx.getString(R.string.state, ctx.getString(R.string.assigned)));

//        if (position == 0 && !free) {
//            holder.price.setVisibility(View.VISIBLE);
//            holder.image.setVisibility(View.VISIBLE);
//            holder.price.setVisibility(View.VISIBLE);
//            holder.date.setVisibility(View.VISIBLE);
////            holder.button.setVisibility(View.VISIBLE);
////            holder.acceptButton.setVisibility(View.GONE);
//            holder.refuseButton.setVisibility(View.GONE);
//
//            holder.button.setOnClickListener((View v) -> {
//                DeliveredStatus(selected.getId(), holder);
//            });
//        }else if(free){
//            holder.price.setVisibility(View.GONE);
//            holder.image.setVisibility(View.GONE);
//            holder.price.setVisibility(View.GONE);
//            holder.date.setVisibility(View.GONE);
//            holder.dishes_list.setVisibility(View.GONE);
//            holder.nDish.setVisibility(View.GONE);
////            holder.button.setText(R.string.acceptOrder);
//        }

        if (!orders.get(position).Expanded()) {
            holder.id.setVisibility(View.GONE);
            holder.price.setVisibility(View.GONE);
            holder.dishes_list.setVisibility(View.GONE);
            holder.nDish.setVisibility(View.GONE);
        }else{
            holder.id.setVisibility(View.VISIBLE);
            holder.price.setVisibility(View.VISIBLE);
            holder.dishes_list.setVisibility(View.VISIBLE);
            holder.nDish.setVisibility(View.VISIBLE);

        }
    }

//    private boolean DeliveredStatus(String id, OrderViewHolder holder) {
//        DatabaseReference ordini = FirebaseDatabase.getInstance().getReference("ordini").child(id);
//
//        orders.get(0).setState("delivered");
//        ordini.setValue(orders.get(0));
//        free = true;
//
////            holder.button.setVisibility(View.GONE);
//// //            holder.acceptButton.setVisibility(View.VISIBLE);
////            holder.refuseButton.setVisibility(View.VISIBLE);
//
//        Toast.makeText(ctx, "Delivered succesfully", Toast.LENGTH_LONG).show();
//
//        notifyItemChanged(0);
//
//        return true;
//    }

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
        private TextView id,date,price,nDish, dishes_list, customer_info, restaurant_i, delivery_time, note, state_tv;
        private TextView message;
        private CardView root;
        private ImageView image;

        public OrderViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);

            root =itemView.findViewById(R.id.card_order);
            id= itemView.findViewById(R.id.order_id);
            date = itemView.findViewById(R.id.order_date_value);
            price = itemView.findViewById(R.id.order_price);
            nDish = itemView.findViewById(R.id.order_num_dishes);
            dishes_list = itemView.findViewById(R.id.dishes_list);
            customer_info =itemView.findViewById(R.id.order_customer_info);
            restaurant_i = itemView.findViewById(R.id.phone_big_text);
            delivery_time = itemView.findViewById(R.id.order_delivery_time);
            note = itemView.findViewById(R.id.order_note);
            image = itemView.findViewById(R.id.circleImageView);
            state_tv = itemView.findViewById(R.id.state_tv);

            message = itemView.findViewById(R.id.order_message_id);

            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });

        }
    }
}
