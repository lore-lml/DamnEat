package com.damn.polito.damneatrestaurant.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.SelectDishes;
import com.damn.polito.damneatrestaurant.beans.Dish;
import java.util.List;
import java.util.Locale;

public class DishesAdapter extends RecyclerView.Adapter<DishesAdapter.ViewHolder>{
    private List<Dish> dishesList;
    private Context context;
    private boolean select_dishes_layout;
    private Bitmap default_image;
    private OnLongItemClickListener mLongListener;

    public DishesAdapter(Context context, List<Dish> dishesList, boolean select_dishes_layout) {
        this.dishesList = dishesList;
        this.context = context;
        default_image = BitmapFactory.decodeResource(context.getResources(),R.drawable.dishes_empty);
        this.select_dishes_layout = select_dishes_layout;
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

        /*GESTIRE IN FASE DI LOAD*/
        /*if(!selected.isDishOtd() && !allDishes){
            viewHolder.parentLayout.setVisibility(View.GONE);
            return;
        }*/

        //viewHolder.image.setImageBitmap(selected.getImage());
        viewHolder.name.setText(selected.getName());
        viewHolder.description.setText(selected.getDescription());
        viewHolder.price.setText(String.format(Locale.UK,"%.2f",selected.getPrice()));
        viewHolder.quantity.setText((String.valueOf(selected.getAvailability())));
        if(!(selected.getPhotoStr().equals("NO_PHOTO"))){
            viewHolder.image.setImageBitmap(selected.getPhoto());
        }else {
            viewHolder.image.setImageBitmap(default_image);
        }
        viewHolder.parentLayout.setOnClickListener(v -> Toast.makeText(context, selected.getName(), Toast.LENGTH_SHORT).show());
        if(select_dishes_layout){
            viewHolder.selected_switch.setChecked(selected.isDishOtd());
            viewHolder.selected_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                selected.setDishOtd(isChecked);
                Log.d("Switch", "Ha cambiato valore a " + isChecked);
                ((SelectDishes)context).storeData();
            });
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
            } else
                parentLayout = itemView.findViewById(R.id.dish_root);

            parentLayout.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(context.getString(R.string.select_option));
            menu.add(this.getAdapterPosition(), EDIT_CODE, 0, context.getString(R.string.context_edit));
            menu.add(this.getAdapterPosition(), DELETE_CODE, 1, context.getString(R.string.context_delete));
        }
    }
}
