package com.damn.polito.damneatrestaurant.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.beans.Dish;

import java.util.List;


public class RecyclerAdapterDishes extends RecyclerView.Adapter<RecyclerAdapterDishes.MyViewHolder> {

    private List<Dish> dishes;

    public RecyclerAdapterDishes(List<Dish> dishes){
        this.dishes= dishes;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        CardView Dish;
        TextView name, desc, price, nDish;
        ImageView photo;

        public MyViewHolder(CardView itemView) {

            super(itemView);
            Dish = itemView.findViewById(R.id.card_order);
            name= itemView.findViewById(R.id.textView4);
            desc= itemView.findViewById(R.id.textView9);
            price= itemView.findViewById(R.id.textView7);
            nDish= itemView.findViewById(R.id.textView8);
            photo= itemView.findViewById(R.id.imageView4);
        }
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    @Override
    public RecyclerAdapterDishes.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(cardView);

        return  myViewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(dishes.get(position).getName());
        holder.desc.setText(dishes.get(position).getDescription());
        holder.nDish.setText(String.valueOf(dishes.get(position).getDisponibility()));
        holder.price.setText(String.valueOf(dishes.get(position).getPrice()));
        holder.photo.setImageResource(dishes.get(position).getPhoto());
    }
}