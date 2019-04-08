package com.damn.polito.damneatrestaurant.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.beans.Dish;

import java.util.ArrayList;
import java.util.List;

public class DishesAdapter extends RecyclerView.Adapter<DishesAdapter.ViewHolder>{
    private List<Dish> dishesList = new ArrayList<>();
    private Context context;

    public DishesAdapter(Context context, List<Dish> dishesList) {
        this.dishesList = dishesList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dish_layout, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int index) {
        //viewHolder.image.setImageBitmap(dishesList.get(index).getImage());
        viewHolder.name.setText(dishesList.get(index).getName());
        viewHolder.description.setText(dishesList.get(index).getDescription());
        viewHolder.price.setText((String.valueOf(dishesList.get(index).getPrice())));
        viewHolder.quantity.setText((String.valueOf(dishesList.get(index).getAvailability())));
        viewHolder.parentLayout.setOnClickListener(v -> Toast.makeText(context, dishesList.get(index).getName(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return dishesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView price;
        TextView quantity;
        TextView name;
        TextView description;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView){
            super(itemView);
            name = itemView.findViewById(R.id.dish_name);
            price = itemView.findViewById(R.id.dish_price);
            quantity = itemView.findViewById(R.id.dish_quantity);
            description = itemView.findViewById(R.id.dish_description);
            parentLayout = itemView.findViewById(R.id.dish_layout);

        }
    }
}
