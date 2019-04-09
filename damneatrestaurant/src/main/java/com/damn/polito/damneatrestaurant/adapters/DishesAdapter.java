package com.damn.polito.damneatrestaurant.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.beans.Dish;
import java.util.List;
import java.util.Locale;

public class DishesAdapter extends RecyclerView.Adapter<DishesAdapter.ViewHolder>{
    private List<Dish> dishesList;
    private Context context;

    public DishesAdapter(Context context, List<Dish> dishesList) {
        this.dishesList = dishesList;
        this.context = context;
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
        viewHolder.quantity.setText((String.valueOf(selected.getAvailability())));
        viewHolder.parentLayout.setOnClickListener(v -> Toast.makeText(context, selected.getName(), Toast.LENGTH_SHORT).show());
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
        CardView parentLayout;

        public ViewHolder(View itemView){
            super(itemView);
            name = itemView.findViewById(R.id.dish_name);
            price = itemView.findViewById(R.id.dish_price);
            quantity = itemView.findViewById(R.id.dish_quantity);
            description = itemView.findViewById(R.id.dish_description);
            parentLayout = itemView.findViewById(R.id.dish_root);

        }
    }
}
