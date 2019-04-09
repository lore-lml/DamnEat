package com.damn.polito.damneatrestaurant.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneatrestaurant.AddDish;
import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.SelectMenu;
import com.damn.polito.damneatrestaurant.adapters.DishesAdapter;
import com.damn.polito.damneatrestaurant.beans.Dish;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class DishesFragment extends Fragment {

    private List<Dish> dishesList = new ArrayList<>();
    private final int UPDATE_DISHES_OF_DAY = 100;
    private RecyclerView recyclerView;
    private DishesAdapter adapter;
    private FloatingActionButton fab;
    private Context ctx;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dishes_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ctx = view.getContext();
        AppCompatActivity activity = ((AppCompatActivity)getActivity());
        assert activity != null;
        Objects.requireNonNull(activity.getSupportActionBar()).setTitle(R.string.app_name);

        fab = view.findViewById(R.id.fab_add_dish);

        //initDishes();
        loadData();
        initReyclerView(view);

        fab.setOnClickListener(v-> {
            Intent i = new Intent(view.getContext(), SelectMenu.class);
            startActivityForResult(i, UPDATE_DISHES_OF_DAY);
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
        if (requestCode == UPDATE_DISHES_OF_DAY){
            if (resultCode == RESULT_OK){
                String name = data.getStringExtra("name");
                String description = data.getStringExtra("description");
                float price =  Float.parseFloat(data.getStringExtra("price"));
                int avaibility = Integer.parseInt(data.getStringExtra("availabity"));
                //int photo = data.getIntExtra("photo", -1);
                // todo: errori in caso di valori negativi di prezzo e disponibilità
                dishesList.add(new Dish(name, description, price, avaibility, -1));
                adapter.notifyDataSetChanged();
                storeData();
            }
        }
    }

    private void storeData() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        JSONArray array = new JSONArray();
        for (Dish element:dishesList) {
            JSONObject values = new JSONObject();
            try {
                values.put("name", element.getName());
                values.put("description", element.getDescription());
                values.put("price", element.getPrice());
                values.put("available", element.getAvailability());
                values.put("photo", element.getPhoto());
                values.put("dotd", element.isDishOtd());
                array.put(values);
            }catch (JSONException e) {
                e.printStackTrace();
            }
            String txt = array.toString();
            pref.edit().putString("dishes", array.toString()).apply();
        }
    }

    private void loadData() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        String s = pref.getString("dishes", null);
        if (s == null) return;

        try {
            JSONArray array = new JSONArray(s);
            JSONObject values;
            for (int i=0; i<array.length(); i++) {
                values = array.getJSONObject(i);
                dishesList.add(new Dish(values.getString("name"), values.getString("description"),(float) values.getDouble("price"), values.getInt("available"), values.getInt("photo")));
                dishesList.get(i).setDishOtd(values.getBoolean("dotd"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
