package com.damn.polito.damneatrestaurant.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneatrestaurant.AddDish;
import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.SelectDishes;
import com.damn.polito.damneatrestaurant.adapters.DishesAdapter;
import com.damn.polito.damneatrestaurant.beans.Dish;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.damn.polito.commonresources.Utility.showWarning;

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
        dishesList.clear();
        loadData();
        initReyclerView(view);

        fab.setOnClickListener(v-> {
            Intent i = new Intent(view.getContext(), SelectDishes.class);
            startActivityForResult(i, UPDATE_DISHES_OF_DAY);
        });
    }


    private void initReyclerView(View view){
        recyclerView = view.findViewById(R.id.recyclerViewDishes);
        adapter = new DishesAdapter(view.getContext(), dishesList, false);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        dishesList.clear();
        loadData();
        adapter.notifyDataSetChanged();
    }

    private void loadData() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        String s = pref.getString("dishes", null);
        Log.d("shared_pref", "Init load");

        if (s == null) return;
        Log.d("shared_pref", "Not null");

        try {

            JSONArray array = new JSONArray(s);
            Log.d("shared_pref", "Json caricato: " + array.toString());

            JSONObject values;
            for (int i=0; i<array.length(); i++) {
                values = array.getJSONObject(i);
                if(values.getBoolean("dotd")) {
                    dishesList.add(new Dish(values.getString("name"), values.getString("description"), (float) values.getDouble("price"), values.getInt("available")));
                    if(!values.get("photo").equals("NO_PHOTO")){
                        Bitmap bmp = Utility.StringToBitMap(values.getString("photo"));
                        dishesList.get(i).setPhoto(bmp);
                    }
                    Log.d("shared_pref", "Adding to list: " + values.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
