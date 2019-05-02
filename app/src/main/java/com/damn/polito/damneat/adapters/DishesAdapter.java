package com.damn.polito.damneat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.damneat.ChooseDishes;
import com.damn.polito.damneat.R;

import java.util.List;
import java.util.Locale;

public class DishesAdapter extends RecyclerView.Adapter<DishesAdapter.ViewHolder>{
    private List<Dish> dishesList;
    private Context context;
    private Bitmap default_image;

    public DishesAdapter(Context context, List<Dish> dishesList) {
        this.dishesList = dishesList;
        this.context = context;
        default_image = BitmapFactory.decodeResource(context.getResources(), R.drawable.dishes_empty);
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dish_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int index) {
        Dish selected = dishesList.get(index);
        //viewHolder.image.setImageBitmap(selected.getImage());
        viewHolder.name.setText(selected.getName());
        viewHolder.description.setText(selected.getDescription());
        viewHolder.price.setText(String.format(Locale.UK,"%.2f",selected.getPrice()));
        viewHolder.availabity.setText((String.valueOf(selected.getAvailability())));
        if(selected.getQuantity()>0) {
            viewHolder.quantity.setText((String.valueOf(selected.getQuantity())));
            viewHolder.delete_button.setVisibility(View.VISIBLE);
        }
        else{
            viewHolder.quantity.setText("");
            viewHolder.delete_button.setVisibility(View.GONE);
        }

        if(!(selected.getPhoto().equals("NO_PHOTO"))){
            viewHolder.image.setImageBitmap(Utility.StringToBitMap(selected.getPhoto()));
        }else {
            viewHolder.image.setImageBitmap(default_image);
        }
        viewHolder.button.setOnClickListener(view ->{
            if(selected.getQuantity()<selected.getAvailability()) {
                selected.increaseQuantity();
                viewHolder.quantity.setText((String.valueOf(selected.getQuantity())));
                viewHolder.delete_button.setVisibility(View.VISIBLE);

         ((ChooseDishes)context).snackBar(index);}});

        viewHolder.delete_button.setOnClickListener(view ->{
            selected.decreaseQuantity();
            if(selected.getQuantity()>0) {
                viewHolder.quantity.setText((String.valueOf(selected.getQuantity())));
                viewHolder.delete_button.setVisibility(View.VISIBLE);
            }
            else{
                viewHolder.quantity.setText("");
                viewHolder.delete_button.setVisibility(View.GONE);
            }
            //((ChooseDishes)context).snackBar(index);
        });
    }

    @Override
    public int getItemCount() {
        return dishesList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView price;
        TextView availabity;
        TextView name;
        TextView quantity;
        TextView description;
        CardView parentLayout;
        Button button;
        ImageButton delete_button;
        public ViewHolder(View itemView){
            super(itemView);
            name = itemView.findViewById(R.id.dish_name);
            price = itemView.findViewById(R.id.dish_price);
            availabity = itemView.findViewById(R.id.dish_availabity);
            description = itemView.findViewById(R.id.dish_description);
            image = itemView.findViewById(R.id.dish_image);
            parentLayout = itemView.findViewById(R.id.dish_root);
            quantity = itemView.findViewById(R.id.dish_quantity);
            button = itemView.findViewById(R.id.add_button);
            delete_button = itemView.findViewById(R.id.delete_button);
        }
    }


}
