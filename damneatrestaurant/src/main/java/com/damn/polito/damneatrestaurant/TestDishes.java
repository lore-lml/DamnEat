package com.damn.polito.damneatrestaurant;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.damn.polito.damneatrestaurant.adapters.DishesAdapter;
import com.damn.polito.damneatrestaurant.beans.Dish;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

public class TestDishes extends AppCompatActivity {

    private List<Dish> dishesList = new ArrayList<>();
    private final int ADD_DISH = 101;
    DishesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dishes_fragment);
        initDishes();
        initReyclerView();

        FloatingActionButton fab = findViewById(R.id.fab_add_dish);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), AddDish.class);
                startActivityForResult(i, ADD_DISH);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_DISH){
            if (resultCode == RESULT_OK){
                String name = data.getStringExtra("name");
                String description = data.getStringExtra("description");
                float price =  Float.parseFloat(data.getStringExtra("price"));
                int avaibility = Integer.parseInt(data.getStringExtra("availabity"));
                //int photo = data.getIntExtra("photo", -1);
                // todo: errori in caso di valori negativi di prezzo e disponibilità
                dishesList.add(new Dish(name, description, price, avaibility, -1));
                adapter.notifyDataSetChanged();
            }
        }

    }

    private void initDishes(){
        dishesList.add(new Dish("Pizzaaaaaaa", "Chi non conosce la pizza??", 6,20, 0));
        dishesList.add(new Dish("Carbonara", "Un piatto buonissimo", 7,10, 1));
        dishesList.add(new Dish("Gelato", "Un qualcosa ancora più buono", 3,15, 2));
        dishesList.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50,3, 3));
        dishesList.add(new Dish("Petto di pollo", "Non so cosa dire", 5,12, 4));
        dishesList.add(new Dish("Insalata", "Altra roba verde", (float)4.50,5, 5));
    }

    private void initReyclerView(){
        RecyclerView recyclerView = findViewById(R.id.recyclerViewDishes);
        adapter = new DishesAdapter(this, dishesList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
