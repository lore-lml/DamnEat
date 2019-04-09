package com.damn.polito.damneatrestaurant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.damn.polito.damneatrestaurant.adapters.DishesAdapter;
import com.damn.polito.damneatrestaurant.beans.Dish;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectDishes extends AppCompatActivity {
    private List<Dish> dishesList = new ArrayList<>();
    private final int AdD_DISH = 101;
    private RecyclerView recyclerView;
    private DishesAdapter adapter;
    private FloatingActionButton fab_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_dishes);
        //initDishes();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        fab_add = findViewById(R.id.fab_add);
        fab_add.setOnClickListener(v-> {
            Intent i = new Intent(this, AddDish.class);
            startActivityForResult(i, AdD_DISH);
        });
        loadData();
        initReyclerView();

    }
    private void initDishes(){
        dishesList.add(new Dish("Pizzaaaaaaa", "Chi non conosce la pizza??", 6,20, 0));
        dishesList.add(new Dish("Carbonara", "Un piatto buonissimo", 7,10, 1));
        dishesList.add(new Dish("Gelato", "Un qualcosa ancora più buono", 3,15, 2));
        dishesList.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50,3, 3));
        dishesList.add(new Dish("Petto di pollo", "Non so cosa dire", 5,12, 4));
        dishesList.add(new Dish("Insalata", "Altra roba verde", (float)4.50,5, 5));
    }
    private void storeData() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
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
            Log.d("shared_pref", txt);
        }
    }
    private void loadData() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AdD_DISH){
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


    private void initReyclerView(){
        recyclerView = findViewById(R.id.recyclerViewDishes2);
        adapter = new DishesAdapter(this, dishesList, true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        storeData();
        this.finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        storeData();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        dishesList.clear();
        loadData();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                storeData();
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
