package com.damn.polito.damneatrestaurant;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.damn.polito.damneatrestaurant.adapters.RecyclerAdapterDishes;
import com.damn.polito.damneatrestaurant.beans.Dish;

import java.util.ArrayList;
import java.util.List;

public class DishActivity extends Activity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Dish> dishes;
    private RecyclerAdapterDishes adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dishes_fragment);
        recyclerView = findViewById(R.id.dishes_recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        dishes = new ArrayList<>();
        dishes.add(new Dish("Hamburger", "Un'ottimo piatto della tradizione americana", 10, 21, R.drawable.hamburger));
        dishes.add(new Dish("Hamburger", "Un'ottimo piatto della tradizione americana", 10, 21, R.drawable.hamburger));
        dishes.add(new Dish("Hamburger", "Un'ottimo piatto della tradizione americana", 10, 21, R.drawable.hamburger));
        dishes.add(new Dish("Hamburger", "Un'ottimo piatto della tradizione americana", 10, 21, R.drawable.hamburger));
        dishes.add(new Dish("Hamburger", "Un'ottimo piatto della tradizione americana", 10, 21, R.drawable.hamburger));
        dishes.add(new Dish("Hamburger", "Un'ottimo piatto della tradizione americana", 10, 21, R.drawable.hamburger));
        dishes.add(new Dish("Hamburger", "Un'ottimo piatto della tradizione americana", 10, 21, R.drawable.hamburger));
        dishes.add(new Dish("Hamburger", "Un'ottimo piatto della tradizione americana", 10, 21, R.drawable.hamburger));

        adapter = new RecyclerAdapterDishes(dishes);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

    }
}