package com.damn.polito.damneatrestaurant.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Restaurant;
import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.commonresources.beans.Dish;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;

import static com.damn.polito.commonresources.Utility.PERMISSION_CODE_WRITE_EXTERNAL;
import static com.damn.polito.commonresources.Utility.REQUEST_PERM_WRITE_EXTERNAL;
import static com.damn.polito.commonresources.Utility.galleryIntent16_9;

public class DishesAdapter extends RecyclerView.Adapter<DishesAdapter.ViewHolder>{
    private List<Dish> dishesList;
    private Context context;
    private boolean select_dishes_layout;
    private Bitmap default_image;
    private OnLongItemClickListener mLongListener;
    private Restaurant restaurant = new Restaurant();

    public DishesAdapter(Context context, List<Dish> dishesList, boolean select_dishes_layout) {
        this.dishesList = dishesList;
        this.context = context;
        default_image = BitmapFactory.decodeResource(context.getResources(),R.drawable.dishes_empty);
        this.select_dishes_layout = select_dishes_layout;

        // OTTENGO LA MAIL DALLE SHARED PREF
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String s = pref.getString("dbkey", null);
        if (s != null) {


                String key = stringOrDefault(s);
                restaurant.setRestaurantID(key);

        }
    }

    public interface OnLongItemClickListener { void onLongItemClick(int position); }

    public void setOnLongItemClickListener (OnLongItemClickListener longListener) {mLongListener = longListener; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dish_layout, viewGroup, false);
        if(select_dishes_layout)
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dish_layout_add, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int index) {
        Dish selected = dishesList.get(index);
        boolean editMode = selected.EditMode();

        //viewHolder.image.setImageBitmap(selected.getImage());
        viewHolder.name.setText(selected.getName());
        viewHolder.description.setText(selected.getDescription());
        viewHolder.price.setText(String.format(Locale.UK,"%.2f",selected.getPrice()));
        viewHolder.quantity.setText((String.valueOf(selected.getAvailability())));

        if(!(selected.getPhoto().equals("NO_PHOTO"))){
            viewHolder.image.setImageBitmap(Utility.StringToBitMap(selected.getPhoto()));
        }else {
            viewHolder.image.setImageBitmap(default_image);
        }
        //viewHolder.parentLayout.setOnClickListener(v -> Toast.makeText(context, selected.getName(), Toast.LENGTH_SHORT).show());
        if(select_dishes_layout && !editMode){
            viewHolder.selected_switch.setChecked(selected.isDishOtd());
            viewHolder.selected_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                selected.setDishOtd(isChecked);
                if(isChecked)
                    saveDish(selected);
                else
                    deleteDish(selected);
                Log.d("Switch", "Ha cambiato valore a " + isChecked);
                //((SelectDishes)context).storeData();
            });
        }

        if(select_dishes_layout) {
            viewHolder.save.setOnClickListener(v -> {
                setEditMode(viewHolder, selected.EditMode());
                if(checkField(viewHolder)){
                    selected.setEditMode(false);
                    selected.setName(viewHolder.edit_name.getText().toString());
                    selected.setDescription(viewHolder.edit_description.getText().toString());
                    selected.setPrice(Float.valueOf(viewHolder.edit_price.getText().toString()));
                    selected.setAvailability(Integer.valueOf(viewHolder.edit_availabity.getText().toString()));
                    viewHolder.name.setText(selected.getName());
                    viewHolder.description.setText(selected.getDescription());
                    viewHolder.price.setText(String.format(Locale.UK,"%.2f",selected.getPrice()));
                    viewHolder.quantity.setText((String.valueOf(selected.getAvailability())));
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    String dishId = selected.getId();
                    DatabaseReference dbRef = database.getReference("ristoranti/"+ restaurant.getRestaurantID() +"/piatti_totali/" + dishId + "/");
                    dbRef.setValue(selected);
                    if(selected.isDishOtd()){
                        dbRef = database.getReference("ristoranti/"+ restaurant.getRestaurantID() +"/piatti_del_giorno/" + dishId + "/");
                        dbRef.setValue(selected);
                    }
                    //((SelectDishes)context).storeData();
                }
            });
            viewHolder.edit_img.setOnClickListener(v -> {itemGallery(index);});
            viewHolder.edit_name.setText(selected.getName());
            viewHolder.edit_description.setText(selected.getDescription());
            viewHolder.edit_price.setText(String.valueOf(selected.getPrice()));
            viewHolder.edit_availabity.setText(String.valueOf(selected.getAvailability()));

            setEditMode(viewHolder, editMode);
        }
    }

    @Override
    public int getItemCount() {
        return dishesList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        public static final int EDIT_CODE = 121;
        public static final int DELETE_CODE = 122;
        ImageView image;
        TextView price;
        TextView quantity;
        TextView name;
        TextView description;
        CardView parentLayout;
        Switch selected_switch;
        TextView tv_dish_available;
        ImageView im_euro;
        ImageButton save, edit_img;
        CardView circle_card, card_opacity;
        TextView edit_price, edit_availabity, edit_name, edit_description, tv_dish_name, tv_dish_desc, tv_dish_price, tv_dish_availabity;
        View divider;
        public ViewHolder(View itemView){
            super(itemView);
            name = itemView.findViewById(R.id.dish_name);
            price = itemView.findViewById(R.id.dish_price);
            quantity = itemView.findViewById(R.id.dish_quantity);
            description = itemView.findViewById(R.id.dish_description);
            image = itemView.findViewById(R.id.dish_image);



            if(select_dishes_layout) {
                parentLayout = itemView.findViewById(R.id.dish_root_add);
                selected_switch = itemView.findViewById(R.id.selected_switch);
                parentLayout.setOnCreateContextMenuListener(this);

                divider = itemView.findViewById(R.id.divider);

                tv_dish_available = itemView.findViewById(R.id.dish_available);
                im_euro = itemView.findViewById(R.id.euro_image);
                circle_card = itemView.findViewById(R.id.circle_card);
                card_opacity = itemView.findViewById(R.id.card_opacity);

                edit_name = itemView.findViewById(R.id.dish_name_edit);
                edit_description = itemView.findViewById(R.id.description_dish_edit);
                edit_price = itemView.findViewById(R.id.dish_price_edit);
                edit_availabity = itemView.findViewById(R.id.dish_availabity_edit);
                save = itemView.findViewById(R.id.edit_dish_save_image);
                edit_img = itemView.findViewById(R.id.btn_gallery_edit_dish);
                tv_dish_name = itemView.findViewById(R.id.tv_dish_name);
                tv_dish_desc = itemView.findViewById(R.id.tv_description);
                tv_dish_price = itemView.findViewById(R.id.tv_price);
                tv_dish_availabity = itemView.findViewById(R.id.tv_availabity);

            } else
                parentLayout = itemView.findViewById(R.id.dish_root);

        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(context.getString(R.string.select_option));
            menu.add(this.getAdapterPosition(), EDIT_CODE, 0, context.getString(R.string.context_edit));
            menu.add(this.getAdapterPosition(), DELETE_CODE, 1, context.getString(R.string.context_delete));
        }
    }

    private void setEditMode(ViewHolder viewHolder, boolean editMode){
        if(editMode){
            viewHolder.circle_card.setVisibility(View.GONE);
            viewHolder.im_euro.setVisibility(View.GONE);
            viewHolder.name.setVisibility(View.GONE);
            viewHolder.description.setVisibility(View.GONE);
            viewHolder.price.setVisibility(View.GONE);
            viewHolder.quantity.setVisibility(View.GONE);
            viewHolder.card_opacity.setVisibility(View.GONE);
            viewHolder.selected_switch.setVisibility(View.GONE);
            viewHolder.edit_name.setVisibility(View.VISIBLE);
            viewHolder.edit_description.setVisibility(View.VISIBLE);
            viewHolder.edit_price.setVisibility(View.VISIBLE);
            viewHolder.edit_availabity.setVisibility(View.VISIBLE);
            viewHolder.save.setVisibility(View.VISIBLE);
            viewHolder.edit_img.setVisibility(View.VISIBLE);
            viewHolder.tv_dish_availabity.setVisibility(View.VISIBLE);
            viewHolder.tv_dish_price.setVisibility(View.VISIBLE);
            viewHolder.tv_dish_name.setVisibility(View.VISIBLE);
            viewHolder.tv_dish_desc.setVisibility(View.VISIBLE);
            viewHolder.divider.setVisibility(View.INVISIBLE);

        } else {
            viewHolder.circle_card.setVisibility(View.VISIBLE);
            viewHolder.im_euro.setVisibility(View.VISIBLE);
            viewHolder.name.setVisibility(View.VISIBLE);
            viewHolder.description.setVisibility(View.VISIBLE);
            viewHolder.price.setVisibility(View.VISIBLE);
            viewHolder.quantity.setVisibility(View.VISIBLE);
            viewHolder.card_opacity.setVisibility(View.VISIBLE);
            viewHolder.selected_switch.setVisibility(View.VISIBLE);
            viewHolder.edit_name.setVisibility(View.GONE);
            viewHolder.edit_description.setVisibility(View.GONE);
            viewHolder.edit_price.setVisibility(View.GONE);
            viewHolder.edit_availabity.setVisibility(View.GONE);
            viewHolder.save.setVisibility(View.GONE);
            viewHolder.edit_img.setVisibility(View.GONE);
            viewHolder.tv_dish_desc.setVisibility(View.GONE);
            viewHolder.tv_dish_price.setVisibility(View.GONE);
            viewHolder.tv_dish_name.setVisibility(View.GONE);
            viewHolder.tv_dish_availabity.setVisibility(View.GONE);
            viewHolder.divider.setVisibility(View.GONE);
        }

    }
    private boolean checkField(ViewHolder view) {
        String name = view.edit_name.getText().toString();

        //Controllo sui campi vuoti
        if(name.trim().isEmpty()){
            Toast.makeText(context, context.getString(R.string.empty_name), Toast.LENGTH_SHORT).show();
            view.edit_name.requestFocus();
            return false;
        }
        String description = view.edit_description.getText().toString();
        if(description.trim().isEmpty()){
            Toast.makeText(context, context.getString(R.string.empty_desc), Toast.LENGTH_SHORT).show();
            view.edit_description.requestFocus();
            return false;
        }

        String availabity = view.edit_availabity.getText().toString();
        if(availabity.trim().isEmpty()){
            Toast.makeText(context, context.getString(R.string.empty_availabity), Toast.LENGTH_SHORT).show();
            view.edit_availabity.requestFocus();
            return false;
        }
        if(Integer.parseInt(availabity)<0){
            Toast.makeText(context, context.getString(R.string.availabity_too_low), Toast.LENGTH_SHORT).show();
            view.edit_availabity.requestFocus();
            return false;
        }
        String price = view.edit_price.getText().toString();
        if(price.trim().isEmpty()){
            Toast.makeText(context, context.getString(R.string.empty_price), Toast.LENGTH_SHORT).show();
            view.edit_price.requestFocus();
            return false;
        }
        if(Float.parseFloat(price)<=0){
            Toast.makeText(context, context.getString(R.string.price_too_low), Toast.LENGTH_SHORT).show();
            view.edit_price.requestFocus();
            return false;
        }
        return true;
    }
    private void itemGallery(int index){
        if(!checkPermissionFromDevice(REQUEST_PERM_WRITE_EXTERNAL))
            requestPermission(REQUEST_PERM_WRITE_EXTERNAL, PERMISSION_CODE_WRITE_EXTERNAL);
        else
            pickFromGallery(index);
    }
    private boolean checkPermissionFromDevice(String permission) {

        int result = ContextCompat.checkSelfPermission(context, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(final String permission, final int permission_code) {
        ActivityCompat.requestPermissions((Activity)context,new String[]{
                permission
        }, permission_code);
    }


    public void pickFromGallery(int index) {
        Intent intent = galleryIntent16_9();
        ((Activity)context).startActivityForResult(intent, 2000+index);
    }

    private void saveDish(Dish dish){
        String dishId = dish.getId();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef;
        if(dish.isDishOtd()) {
            dbRef = database.getReference("ristoranti/" + restaurant.getRestaurantID() + "/piatti_del_giorno/" + dishId + "/");
            dbRef.setValue(dish);
        }
        dbRef = database.getReference("ristoranti/"+ restaurant.getRestaurantID() +"/piatti_totali/"+ dishId +"/dishOtd/");
        dbRef.setValue(true);
    }

    private void deleteDish(Dish dish){
        String dishId = dish.getId();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("ristoranti/"+ restaurant.getRestaurantID() +"/piatti_del_giorno/" + dishId + "/");
        dbRef.removeValue();
        dbRef = database.getReference("ristoranti/"+ restaurant.getRestaurantID() +"/piatti_totali/"+ dishId +"/dishOtd/");
        dbRef.setValue(false);
    }

    public String stringOrDefault(String s) {
        return (s == null || s.trim().isEmpty()) ? "" : s;
    }

}

