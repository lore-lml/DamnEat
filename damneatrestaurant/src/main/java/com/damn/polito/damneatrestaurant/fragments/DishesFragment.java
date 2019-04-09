package com.damn.polito.damneatrestaurant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.damn.polito.damneatrestaurant.AddDish;
import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.adapters.DishesAdapter;
import com.damn.polito.damneatrestaurant.beans.Dish;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class DishesFragment extends Fragment {

    private List<Dish> dishesList = new ArrayList<>();
    private final int ADD_DISH = 101;
    private RecyclerView recyclerView;
    private DishesAdapter adapter;
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dishes_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = ((AppCompatActivity)getActivity());
        assert activity != null;
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle(R.string.app_name);

        fab = view.findViewById(R.id.fab_add_dish);

        initDishes();
        initReyclerView(view);

        fab.setOnClickListener(v-> {
            Intent i = new Intent(view.getContext(), AddDish.class);
            startActivityForResult(i, ADD_DISH);
        });
    }

    private void initDishes(){
        dishesList.add(new Dish("Pizzaaaaaaa", "Chi non conosce la pizza??", 6,20, 0));
        dishesList.add(new Dish("Carbonara", "Un piatto buonissimo", 7,10, 1));
        dishesList.add(new Dish("Gelato", "Un qualcosa ancora più buono", 3,15, 2));
        dishesList.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50,3, 3));
        dishesList.add(new Dish("Petto di pollo", "Non so cosa dire", 5,12, 4));
        dishesList.add(new Dish("Insalata", "Altra roba verde", (float)4.50,5, 5));
    }

    private void initReyclerView(View view){
        recyclerView = view.findViewById(R.id.recyclerViewDishes);
        adapter = new DishesAdapter(view.getContext(), dishesList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
}
