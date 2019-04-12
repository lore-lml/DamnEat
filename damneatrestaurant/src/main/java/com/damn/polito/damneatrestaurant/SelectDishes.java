package com.damn.polito.damneatrestaurant;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneatrestaurant.adapters.DishesAdapter;
import com.damn.polito.damneatrestaurant.beans.Dish;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.damn.polito.commonresources.Utility.getImageUrlWithAuthority;

public class SelectDishes extends AppCompatActivity {
    private List<Dish> dishesList = new ArrayList<>();
    private final int AdD_DISH = 101;
    private final int UPDATE_DISH = 102;
    private RecyclerView recyclerView;
    private DishesAdapter adapter;
    private FloatingActionButton fab_add;
    private String srcFile = "dishes.save";

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
        dishesList.clear();
        loadData();
        initReyclerView();

    }



    private void initDishes(){
        dishesList.add(new Dish("Pizzaaaaaaa", "Chi non conosce la pizza??", 6,20));
        dishesList.add(new Dish("Carbonara", "Un piatto buonissimo", 7,10));
        dishesList.add(new Dish("Gelato", "Un qualcosa ancora più buono", 3,15));
        dishesList.add(new Dish("Pasta al pesto", "Una roba verde", (float) 6.50,3));
        dishesList.add(new Dish("Petto di pollo", "Non so cosa dire", 5,12));
        dishesList.add(new Dish("Insalata", "Altra roba verde", (float)4.50,5));
        Log.d("List", "List Size" + dishesList.size());
    }
    public void storeData() {
        File f = new File(getFilesDir(), srcFile);
        f.delete();
        if(dishesList != null && dishesList.size() > 0){
            JSONArray array = new JSONArray();
            try {
                for (Dish element:dishesList) {
                    JSONObject values = new JSONObject();
                    values.put("name", element.getName());
                    values.put("description", element.getDescription());
                    values.put("price", element.getPrice());
                    values.put("available", element.getAvailability());
                    values.put("photo", element.getPhoto());
                    values.put("dotd", element.isDishOtd());
                    array.put(values);
                    Log.d("StoreDataDish", "Store: " + array.toString());
                }
                String txt = array.toString();

                FileOutputStream fos = openFileOutput(srcFile, MODE_PRIVATE);
                ObjectOutputStream o = new ObjectOutputStream(fos);
                o.writeObject(txt);
                o.close();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
// public void storeData() {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//        JSONArray array = new JSONArray();
//        for (Dish element:dishesList) {
//            JSONObject values = new JSONObject();
//            try {
//                values.put("name", element.getName());
//                values.put("description", element.getDescription());
//                values.put("price", element.getPrice());
//                values.put("available", element.getAvailability());
//                values.put("photo", element.getPhoto());
//                values.put("dotd", element.isDishOtd());
//                array.put(values);
//                Log.d("StoreDataDish", "Store: " + array.toString());
//            }catch (JSONException e) {
//                Log.d("StoreDataDish", "Errore salavataggio");
//                e.printStackTrace();
//            }
//            String txt = array.toString();
//            pref.edit().putString("dishes", array.toString()).apply();
//            Log.d("shared_pref", txt);
//        }
//    }
    private void loadData() {
        File f = new File(getFilesDir(), srcFile);
        if(f.exists()) {
            try {
                FileInputStream fis = openFileInput(srcFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Object o = ois.readObject();
                if(o instanceof String){
                    Log.d("loadData", (String) o);
                    JSONArray array = new JSONArray((String) o);
                    JSONObject values;
                    for (int i=0; i<array.length(); i++) {
                        values = array.getJSONObject(i);
                        dishesList.add(new Dish(values.getString("name"), values.getString("description"),(float) values.getDouble("price"), values.getInt("available")));
                        dishesList.get(i).setDishOtd(values.getBoolean("dotd"));
                        if(!values.get("photo").equals("NO_PHOTO")){
                            Bitmap bmp = Utility.StringToBitMap(values.getString("photo"));
                            dishesList.get(i).setPhotoBmp(bmp);
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
//  private void loadData() {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//        String s = pref.getString("dishes", null);
//        if (s == null) return;
//
//        try {
//            JSONArray array = new JSONArray(s);
//            JSONObject values;
//            for (int i=0; i<array.length(); i++) {
//                values = array.getJSONObject(i);
//                dishesList.add(new Dish(values.getString("name"), values.getString("description"),(float) values.getDouble("price"), values.getInt("available")));
//                dishesList.get(i).setDishOtd(values.getBoolean("dotd"));
//                if(!values.get("photo").equals("NO_PHOTO")){
//                    Bitmap bmp = Utility.StringToBitMap(values.getString("photo"));
//                    dishesList.get(i).setPhotoBmp(bmp);
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == AdD_DISH) {
            String name = data.getStringExtra("name");
            String description = data.getStringExtra("description");
            float price = Float.parseFloat(data.getStringExtra("price"));
            int avaibility = Integer.parseInt(data.getStringExtra("availabity"));
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            String s = pref.getString("dish_photo", null);
            Log.d("ONRESULT", "s value: " + s);
            if (s != null) {
                Bitmap bmp = Utility.StringToBitMap(s);
                dishesList.add(new Dish(name, description, price, avaibility, bmp));
                pref.edit().remove("dish_photo").apply();
            } else {
                Log.d("ONRESULT", "Adding dish without photo");
                dishesList.add(new Dish(name, description, price, avaibility));
            }
            adapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(dishesList.size() - 1);
            Log.d("ONRESULT", "OnresultActivity");
            storeData();
        }
        if (requestCode < 3000 && requestCode >= 2000) {
            final Bundle extras = data.getExtras();
            if (extras != null) {
                Dish d = dishesList.get(requestCode - 2000);
                d.setPhotoBmp(extras.getParcelable("data"));
                adapter.notifyItemChanged(requestCode-2000);

            } else {
                copyAndCrop(data.getData(), requestCode + 1000);
            }
        }
        if (requestCode < 4000 && requestCode >= 3000) {
            final Bundle extras = data.getExtras();
            if (extras != null) {
                Dish d = dishesList.get(requestCode - 3000);
                d.setPhotoBmp(extras.getParcelable("data"));
                adapter.notifyItemChanged(requestCode-3000);
            } else {
                displayImage(data.getData(), requestCode - 3000);
            }
        }
    }

    private void displayImage(Uri data, int index) {
        try {
            Dish d = dishesList.get(index);
            d.setPhotoBmp(MediaStore.Images.Media.getBitmap(getContentResolver(), data));
            adapter.notifyItemChanged(index);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyAndCrop(Uri uri, int index) {
        assert uri != null;
        Uri newUri = getImageUrlWithAuthority(this, Objects.requireNonNull(uri));
        if(newUri != null) {
            cropImage(newUri, index);
        }
    }

    private void cropImage(Uri uri, int index) {
        try{
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(uri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra("outputX", 1280);
            cropIntent.putExtra("outputY", 720);
            cropIntent.putExtra("aspectX", 16);
            cropIntent.putExtra("aspectY", 9);
            cropIntent.putExtra("scaleUpIfNeeded", true);
            cropIntent.putExtra("return-data", true);

            ((Activity)this).startActivityForResult(cropIntent,index);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
        }
    }

    private void initReyclerView(){
        recyclerView = findViewById(R.id.recyclerViewDishes2);
        adapter = new DishesAdapter(this, dishesList, true);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
                //storeData();
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteDish(int index){
        Dish dish = dishesList.get(index);
        dishesList.remove(index);
        storeData();
        adapter.notifyItemRemoved(index);

        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.select_dishes_coordinator), R.string.dish_deletted, Snackbar.LENGTH_LONG);
        mySnackbar.setAction(R.string.undo_string, v -> {
            if(dish!=null){
            dishesList.add(index, dish);
            adapter.notifyItemInserted(index);
            recyclerView.smoothScrollToPosition(index);
            storeData();

            }});
        mySnackbar.show();
    }


//    private void editDish(int index) {
//        //Crea il corretto intent per l'apertura dell'activity EditProfile
//        Intent intent = new Intent(this, AddDish.class);
//
//        intent.putExtra("name", dishesList.get(index).getName());
//        intent.putExtra("description", dishesList.get(index).getDescription());
//        intent.putExtra("price", dishesList.get(index).getPrice());
//        intent.putExtra("availabity", dishesList.get(index).getAvailability());
//        if (!dishesList.get(index).getPhoto().equals("NO_PHOTO")){
//            PreferenceManager.getDefaultSharedPreferences(this)
//                    .edit().putString("photo", Utility.BitMapToString(dishesList.get(index).getPhotoBmp())).apply();
//
//        }
//        startActivityForResult(intent, UPDATE_DISH);
//    }

    private void itemDelete(int pos) {
        //Toast.makeText(this, "@string/context_delete", Toast.LENGTH_SHORT ).show();
        deleteDish(pos);
    }

    private void itemEdit(int pos) {
        //Toast.makeText(this, "DELETE", Toast.LENGTH_SHORT ).show();
        //editDish(pos);
        dishesList.get(pos).setEditMode(true);
        adapter.notifyItemChanged(pos);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final int pos = item.getGroupId();
        final Dish dish = dishesList.get(pos);

        switch (item.getItemId()){
            case DishesAdapter.ViewHolder.EDIT_CODE:
                itemEdit(pos);
                return true;

            case DishesAdapter.ViewHolder.DELETE_CODE:
                itemDelete(pos);
                return true;

            default:
                return super.onContextItemSelected(item);
        }


    }
}
