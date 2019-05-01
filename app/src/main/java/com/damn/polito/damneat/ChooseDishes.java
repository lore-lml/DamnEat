package com.damn.polito.damneat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneat.adapters.DishesAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChooseDishes extends AppCompatActivity {
    private List<Dish> dishesList = new ArrayList<>();
    private RecyclerView recyclerView;
    private DishesAdapter adapter;
    private FloatingActionButton fab_cart;
    private ImageView no_dishes_img;
    private TextView no_dishes_tv;
    private String restaurant_name = "Test restaurant";
    private String restaurantID = "luigis@ristorante|it";
    //private String restaurantID = "ste@lo|it";
    private String address = "Test address";
    private String name = "Test name";
    private String clientID = "-LdjJX4rCHX-MZ4rvjRr";
    private Double price = -1.;
    private Context ctx;
    private String restaurant_photo;
    private final int CART = 10;
    private String orderID_key;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_dishes);
        no_dishes_img = findViewById(R.id.no_dishes_img);
        no_dishes_tv = findViewById(R.id.no_dishes_tv);

        fab_cart = findViewById(R.id.fab_cart);
        fab_cart.setOnClickListener(v-> startCart());
        ctx = ChooseDishes.this;
        init();
        initReyclerView();
    }

    private void startCart(){
        String data = "";
        Intent i = new Intent(this, Cart.class);
        List<Dish> cart_dishes = getCartDishes();
        storeData(cart_dishes);
        for (Dish d:cart_dishes) {
            data += d.getQuantity() +"x\t"+ d.getName() + "\n";
            price += d.getQuantity()*d.getPrice();
        }
        i.putExtra("list",data);
        i.putExtra("price", price);
        i.putExtra("name", restaurant_name);
        i.putExtra("address", address);
        startActivityForResult(i, CART);
    }

    private List<Dish> getCartDishes(){
        List<Dish> cart_dishes = new ArrayList<>();
        for (Dish dish:dishesList) {
            if(dish.getQuantity()>0){
                if(dish.getQuantity()>dish.getAvailability()){
                    Toast.makeText(this, "Quantità non sufficiente per soddisfare la richiesta", Toast.LENGTH_LONG).show();
                    dish.setQuantity(dish.getAvailability());
                }
                cart_dishes.add(dish);
            }
        }
        return cart_dishes;
    }

    private void initReyclerView(){
        recyclerView = findViewById(R.id.recyclerViewDishes);
        adapter = new DishesAdapter(this, dishesList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void init(){
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("ristoranti/"+ restaurantID +"/piatti_del_giorno/");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key;
                Dish dish;
                dishesList.clear();
                for (DataSnapshot chidSnap : dataSnapshot.getChildren()) {
                    Log.d("tmz",""+ chidSnap.getKey()); //displays the key for the node
                    Log.d("tmz",""+ chidSnap.getValue());   //gives the value for given keyname
                    //DataPacket value = dataSnapshot.getValue(DataPacket.class);
                    key = chidSnap.getKey();
                    dish = chidSnap.getValue(Dish.class);
                    dishesList.add(dish);
                    dishesList.get(dishesList.size()-1).setId(key);
                }
                adapter.notifyDataSetChanged();
                //Log.d("Load", dishesList.get(0).getName());
                if(dishesList.size() == 0){
                    recyclerView.setVisibility(View.GONE);
                    fab_cart.setVisibility(View.GONE);
                    no_dishes_img.setVisibility(View.VISIBLE);
                    no_dishes_tv.setVisibility(View.VISIBLE);
                }
                else {
                    recyclerView.setVisibility(View.VISIBLE);
                    fab_cart.setVisibility(View.VISIBLE);
                    no_dishes_img.setVisibility(View.GONE);
                    no_dishes_tv.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
//    private void init(){
//        dishesList.add(new Dish("Pizzaaaaaaaaa", "Buonaaaaaaaaa", (float)5.5, 10));
//        dishesList.add(new Dish("Gelatoooooooo", "Grossooooooooo", (float)3, 20));
//        dishesList.add(new Dish("Pizzaaaaaaaaa", "Buonaaaaaaaaa", (float)5.5, 10));
//        dishesList.add(new Dish("Gelatoooooooo", "Grossooooooooo", (float)3, 20));
//    }

    public void snackBar(int index){

        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.select_dishes_coordinator), R.string.dish_added, Snackbar.LENGTH_LONG);
        mySnackbar.setAction(R.string.undo_string, v -> {
            dishesList.get(index).decreaseQuantity();
            adapter.notifyItemChanged(index);
            recyclerView.smoothScrollToPosition(index);
            });
        mySnackbar.show();
    }

     public void storeData(List<Dish> cart_dishes) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            JSONArray array = new JSONArray();
            for (Dish element:cart_dishes) {
                JSONObject values = new JSONObject();
                try {
                    values.put("name", element.getName());
                    values.put("quantity", element.getQuantity());
                    values.put("price", element.getPrice());
                    values.put("id", element.getId());
                    array.put(values);
                    Log.d("StoreDataDish", "Store: " + array.toString());
                }catch (JSONException e) {
                    Log.d("StoreDataDish", "Errore salavataggio");
                    e.printStackTrace();
                }
                String txt = array.toString();
                pref.edit().putString("dishes_cart", array.toString()).apply();
                Log.d("shared_pref", txt);
            }
        }

    private List<Dish> loadData() {
        List<Dish> cart_list = new ArrayList<>();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String s = pref.getString("dishes_cart", null);
        Log.d("shared_pref", "Init load");

        if (s == null) return cart_list;
        Log.d("shared_pref", "Not null");

        try {

            JSONArray array = new JSONArray(s);
            Log.d("shared_pref", "Json caricato: " + array.toString());

            JSONObject values;
            for (int i=0; i<array.length(); i++) {
                values = array.getJSONObject(i);
                cart_list.add(new Dish(values.getString("name"), values.getInt("quantity"), values.getDouble("price"), values.getString("id")));
                Log.d("shared_pref", "Adding to list: " + values.toString());
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cart_list;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CART){
            List<Dish> cart_dishes = loadData();

            //AGGIUNGO LA CHIAVE AGLI ORDINI PENDENTI DEL RISTORANTE
            DatabaseReference dbRefRestaurant = database.getReference("ristoranti/"+restaurantID+"/piatti_del_giorno/");
//            DatabaseReference id_restaurant = dbRefRestaurant.push();
//            id_restaurant.setValue(orderID.getKey());

            dbRefRestaurant.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    //currentData.setValue(orderID_key);
                    Dish d = null;
                    String dishID = null;
                    for (Dish cart_dish:cart_dishes) {
                        for (MutableData child:(currentData.getChildren())){
                            Dish dish = child.getValue(Dish.class);
                            Log.d("transazione", child.getValue().toString());
                            Log.d("transazione", cart_dish.getId());
                            dishID = child.getKey();
                            if (dishID != null && dishID.equals(cart_dish.getId())) {
                                d = dish;
                                break;
                            }
                        }
                        if(d!=null && cart_dish.getQuantity()>d.getAvailability()){
                            //creare stringa
                            Toast.makeText(ctx, "Errore quantità", Toast.LENGTH_LONG).show();
                            return Transaction.abort();
                        }
                        if(d == null){
                            Toast.makeText(ctx, "Piatto non trovato", Toast.LENGTH_LONG).show();
                            return Transaction.abort();
                        }

                        Log.d("transazione", d.getName());
                        //String dishID = d.getId();
                        MutableData dbRefDish = currentData.child(dishID);
                        d.setAvailability(d.getAvailability()-cart_dish.getQuantity());
                        dbRefDish.setValue(d);
                    }
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                    Log.d("transazione", String.valueOf(b));
                    if(b) {
                        Order order = new Order(cart_dishes, new Date(), address, name, price);
                        DatabaseReference dbRefOrdini = database.getReference("ordini/");
                        DatabaseReference orderID = dbRefOrdini.push();
                        orderID.setValue(order);
                        orderID_key = orderID.getKey();
                        //AGGIUNGO LA CHIAVE AGLI ORDINI PENDENTI DEL RISTORANTE
                        DatabaseReference dbRefRestaurant = database.getReference("ristoranti/" + restaurantID + "/ordini_pendenti/");
                        DatabaseReference id_restaurant = dbRefRestaurant.push();
                        id_restaurant.setValue(orderID.getKey());
                        //AGGIUNGO LA CHIAVE AGLI ORDINI DEL CLIENTE
                        DatabaseReference dbRefClient = database.getReference("clienti/" + clientID + "/lista_ordini/");
                        DatabaseReference id_client = dbRefClient.push();
                        id_client.setValue(orderID.getKey());
                        Toast.makeText(ctx, R.string.order_succesfull, Toast.LENGTH_LONG).show();
                    }
                    else
                        Toast.makeText(ctx, R.string.order_error, Toast.LENGTH_LONG).show();

                }
            });
        }
    }
}
