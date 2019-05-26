package com.damn.polito.damneat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Customer;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.commonresources.beans.RateObject;
import com.damn.polito.damneat.R;
import com.damn.polito.damneat.Welcome;
import com.damn.polito.damneat.dialogs.DialogType;
import com.damn.polito.damneat.dialogs.HandleDismissDialog;
import com.damn.polito.damneat.dialogs.RateDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private List<Order> orders;
    private Context ctx;
    private OnItemClickListener mListener;
    private Bitmap default_image;

    public OrdersAdapter(List<Order> orders, Context context){
        this.orders= orders;
        this.ctx = context;
        default_image = BitmapFactory.decodeResource(ctx.getResources(),R.drawable.profile_sample);
    }

    public interface OnItemClickListener { void onItemClick(int position); }

    public void setOnItemClickListener (OnItemClickListener listener) { mListener = listener; }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==0) {
            View view = LayoutInflater.from(ctx).inflate(R.layout.order_layout, parent, false);
            return new OrderViewHolder(view);
        }
        View view = LayoutInflater.from(ctx).inflate(R.layout.order_layout_not_delivered, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order selected = orders.get(position);

        String dish_list_str = getDishesList(selected);
        holder.dishes_list.setText(dish_list_str);
        holder.date.setText(Utility.dateString(selected.getDate()));
        String id = ctx.getString(R.string.order_id_s, selected.Id());
        holder.id.setText(id);
        holder.nDish.setText(ctx.getString(R.string.order_num_dishes, selected.DishesNumber()));
        holder.price.setText(ctx.getString(R.string.order_price, selected.getPrice()));
        holder.restaurant_info.setText(ctx.getString(R.string.restaurant, selected.getRestaurant().getRestaurantName()));

        if(selected.getState().toLowerCase().equals("confirmed") || selected.getState().toLowerCase().equals("rejected")){

            holder.deliverer_name.setVisibility(View.GONE);
            holder.restaurant_info.setVisibility(View.VISIBLE);

            if (selected.getRestaurant().getPhoto() == null)
                holder.deliverer_photo.setImageBitmap(default_image);
            else if(selected.getRestaurant().getPhoto().equals("NO_PHOTO"))
               holder.deliverer_photo.setImageBitmap(default_image);
            else
                holder.deliverer_photo.setImageBitmap(Utility.StringToBitMap(selected.getRestaurant().getPhoto()));

            holder.root.setOnClickListener(v->{
                selected.changeExpanded();
                expandOrContract(holder, selected.Expanded());
            });
            if(selected.getState().toLowerCase().equals("confirmed")) {
                holder.state.setText(ctx.getString(R.string.confirmed));
                holder.state.setTextColor(ctx.getColor(R.color.colorGreen));

            }
            if(selected.getState().toLowerCase().equals("rejected")){
                holder.state.setTextColor(ctx.getColor(R.color.colorAccent));
                holder.state.setText(ctx.getString(R.string.rejected));
            }


            expandOrContract(holder, selected.Expanded());

        }
        else {
            String deliverer_name = selected.getDelivererName();
            if(deliverer_name.equals("NOT_ASSIGNED_YET"))
                deliverer_name = "--";
            holder.deliverer_name.setText(deliverer_name);
            if (selected.getDelivererPhoto() == null)
                holder.deliverer_photo.setImageBitmap(default_image);
            else if(selected.getDelivererPhoto().equals("NO_PHOTO"))
                holder.deliverer_photo.setImageBitmap(default_image);
            else
                holder.deliverer_photo.setImageBitmap(Utility.StringToBitMap(selected.getDelivererPhoto()));

            holder.note.setText(ctx.getString(R.string.note, selected.getNote()));
            holder.delivery_time.setText(ctx.getString(R.string.delivery_time, selected.getDeliveryTime()));

            if(selected.getState().toLowerCase().equals("ordered") || selected.getState().toLowerCase().equals("accepted")) {
                holder.deliverer_photo.setVisibility(View.INVISIBLE);
                holder.deliverer_name.setVisibility(View.INVISIBLE);
                holder.state.setText(ctx.getString(R.string.ordered));
            } else {
                holder.deliverer_photo.setVisibility(View.VISIBLE);
                holder.deliverer_name.setVisibility(View.VISIBLE);
            }

            if(selected.getState().toLowerCase().equals("assigned") )
                holder.state.setText(ctx.getString(R.string.accepted));


            if(selected.getState().toLowerCase().equals("shipped"))
                holder.state.setText(ctx.getString(R.string.shipped));

            if(selected.getState().toLowerCase().equals("delivered")){
                holder.confirmButton.setVisibility(View.VISIBLE);
                holder.state.setText(ctx.getString(R.string.delivered));
                holder.confirmButton.setOnClickListener(v->{
                    setConfirmed(selected.Id());

                    FragmentManager fm = ((AppCompatActivity)ctx).getSupportFragmentManager();
                    RateDialog rateDialog = new RateDialog();
                    rateDialog.setListener(holder);
                    rateDialog.show(fm, "Rate Dialog");
                });
            } else {
                holder.confirmButton.setVisibility(View.GONE);
            }
        }
             // holder.date.setText(dateFormat.format(ciao.getTime()));
    }

    private void setConfirmed(String id){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("ordini/"+ id + "/state");
        dbRef.setValue("confirmed");
    }

    private String getDishesList(Order selected){
        String dish_list_str = "";
        List<Dish> dishes = selected.getDishes();
        Double price = 0.;
        for (Dish d:dishes) {
            String p = String.format("%.2f", d.getPrice());
            dish_list_str += d.getQuantity() +"\tx\t"+ d.getName()+"\t"+ p + "€\n";
            price += d.getQuantity()*d.getPrice();
        }
        if(selected.getRestaurant().getRestaurant_price_ship() != null && selected.getRestaurant().getRestaurant_price_ship() != 0.) {
            String p = String.format("%.2f", selected.getRestaurant().getRestaurant_price_ship());
            dish_list_str += ctx.getString(R.string.ship) + " " + p + "€";
            Log.d("test", selected.getRestaurant().getRestaurant_price_ship().toString());
            price += selected.getRestaurant().getRestaurant_price_ship();
        }
        return dish_list_str;
    }

    private void expandOrContract(OrderViewHolder holder, boolean state){
        if (!state) {
            holder.date.setVisibility(View.GONE);
            holder.dishes_list.setVisibility(View.GONE);
            holder.id.setVisibility(View.GONE);

        }else{
            holder.date.setVisibility(View.VISIBLE);
            holder.id.setVisibility(View.VISIBLE);
            holder.dishes_list.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(orders.get(position).getState().toLowerCase().equals("confirmed") || orders.get(position).getState().toLowerCase().equals("rejected"))
            return 0;
        return 1;
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder implements HandleDismissDialog{
        private TextView id,date,price,nDish, deliverer_name, dishes_list, restaurant_info, state, note, delivery_time;
        private CardView root;
        private ImageView deliverer_photo;
        private Button confirmButton;

        public OrderViewHolder(View itemView) {
            super(itemView);

            root =itemView.findViewById(R.id.card_order_customer);
            id= itemView.findViewById(R.id.order_id);
            date = itemView.findViewById(R.id.order_date_value);
            price = itemView.findViewById(R.id.order_price);
            nDish = itemView.findViewById(R.id.order_num_dishes);
            deliverer_name = itemView.findViewById(R.id.order_deliverer_name_textview);
            deliverer_photo = itemView.findViewById(R.id.circleImageView);
            dishes_list = itemView.findViewById(R.id.dishes_list);
            restaurant_info =itemView.findViewById(R.id.order_customer_info);
            state =itemView.findViewById(R.id.state_tv_edit);
            delivery_time =itemView.findViewById(R.id.delivery_time_tv);
            note =itemView.findViewById(R.id.note_tv);
            confirmButton =itemView.findViewById(R.id.confirmOrder);
        }

        @Override
        public void handleOnDismiss(DialogType type, String text) {
            if (type == DialogType.RateDialog) {
                updateServiceRate(text);
            }
        }

        private void updateServiceRate(String text){
            try {
                JSONObject result = new JSONObject(text);
                int rate = result.getInt("value");
                String note = result.getString("note");
                Customer customer = new Customer();
                customer.setCustomerName(Welcome.getProfile().getName());
                customer.setCustomerID(Welcome.getDbKey());
                customer.setCustomerPhoto(Welcome.getProfile().getBitmapProf());

                RateObject rateObject = new RateObject(rate, note, RateObject.RateType.Service, customer);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("reviews/");
                DatabaseReference id = ref.push();
                id.setValue(rateObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
