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
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneatrestaurant.AddDish;
import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.SelectDishes;
import com.damn.polito.damneatrestaurant.adapters.DishesAdapter;
import com.damn.polito.damneatrestaurant.beans.Dish;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
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
    private String srcFile = "dishes.save";

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

        /*adapter.setOnLongItemClickListener(position -> {
             // SET THE MENU LAUNCHER
                PopupMenu pop = new PopupMenu(ctx, view);
                pop.getMenuInflater().inflate(R.menu.context_racyclerview_menu, pop.getMenu());
                pop.setOnMenuItemClickListener(item->{
                    switch (item.getItemId()){
                        case R.id.item_edit:
                            itemEdit();
                            return true;
                        case R.id.item_delete:
                            itemDelete();
                            return true;
                        default:
                            return DishesFragment.super.onContextItemSelected(item);
                    }
                });
                pop.show();
        });*/
    }

    private void itemDelete() {
        //Toast.makeText(ctx, "DELETE", Toast.LENGTH_SHORT ).show();
    }

    private void itemEdit() {
        //Toast.makeText(ctx, "DELETE", Toast.LENGTH_SHORT ).show();

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
        File f = new File(ctx.getFilesDir(), srcFile);
        if(f.exists()) {
            try {
                FileInputStream fis = ctx.openFileInput(srcFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Object o = ois.readObject();
                if(o instanceof String){
                    Log.d("loadData", (String) o);
                    JSONArray array = new JSONArray((String) o);
                    JSONObject values;
                    for (int i=0; i<array.length(); i++) {
                        values = array.getJSONObject(i);
                        if(values.getBoolean("dotd")) {
                            dishesList.add(new Dish(values.getString("name"), values.getString("description"), (float) values.getDouble("price"), values.getInt("available")));
                            if (!values.get("photo").equals("NO_PHOTO")) {
                                Bitmap bmp = Utility.StringToBitMap(values.getString("photo"));
                                dishesList.get(dishesList.size()-1).setPhoto(bmp);
                            }
                        }
                    }
                }
                ois.close();
                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
//    private void loadData() {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
//        String s = pref.getString("dishes", null);
//        Log.d("shared_pref", "Init load");
//
//        if (s == null) return;
//        Log.d("shared_pref", "Not null");
//
//        try {
//
//            JSONArray array = new JSONArray(s);
//            Log.d("shared_pref", "Json caricato: " + array.toString());
//
//            JSONObject values;
//            for (int i=0; i<array.length(); i++) {
//                values = array.getJSONObject(i);
//                if(values.getBoolean("dotd")) {
//                    dishesList.add(new Dish(values.getString("name"), values.getString("description"), (float) values.getDouble("price"), values.getInt("available")));
//                    if(!values.get("photo").equals("NO_PHOTO")){
//                        Bitmap bmp = Utility.StringToBitMap(values.getString("photo"));
//                        dishesList.get(i).setPhoto(bmp);
//                    }
//                    Log.d("shared_pref", "Adding to list: " + values.toString());
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final int pos = item.getGroupId();
        final Dish dish = dishesList.get(pos);

        switch (item.getItemId()){
            case DishesAdapter.ViewHolder.EDIT_CODE:
                itemEdit();
                return true;

            case DishesAdapter.ViewHolder.DELETE_CODE:
                itemDelete();
                return true;

            default:
                return super.onContextItemSelected(item);
        }


    }
}
