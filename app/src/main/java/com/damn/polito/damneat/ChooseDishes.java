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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.Customer;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.commonresources.beans.Restaurant;
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
import java.util.Objects;

public class ChooseDishes extends AppCompatActivity {
    private List<Dish> dishesList = new ArrayList<>();
    private RecyclerView recyclerView;
    private DishesAdapter adapter;
    private FloatingActionButton fab_cart;
    private ImageView no_dishes_img;
    private TextView no_dishes_tv;

    // Dettagli ristorante
    private Restaurant restaurant = new Restaurant();
    private String restaurant_description;

    private String note = "test";
    private String deliveryTime = "19.30";


    private Customer customer = new Customer();

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
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_choose_dishes);
        no_dishes_img = findViewById(R.id.no_dishes_img);
        no_dishes_tv = findViewById(R.id.no_dishes_tv);

        fab_cart = findViewById(R.id.fab_cart);
        fab_cart.setOnClickListener(v-> startCart());
        ctx = ChooseDishes.this;
        getIntentData();
        getSharedData();
        init();
        initReyclerView();
    }

    private void getIntentData(){
        Intent i = getIntent();
        restaurant.setRestaurantName(i.getStringExtra("rest_name"));
        restaurant.setRestaurantID(i.getStringExtra("rest_key"));
        restaurant.setRestaurantAddress(i.getStringExtra("rest_address"));
        restaurant.setRestaurantPhone(i.getStringExtra("rest_phone"));
        restaurant.setPhoto(i.getStringExtra("rest_image"));
        restaurant_description = i.getStringExtra("rest_description");
        restaurant.setRestaurant_price_ship(i.getDoubleExtra("rest_priceship", 0));
        Log.d("restaurant", restaurant.getRestaurantName());
        Log.d("restaurant", restaurant.getRestaurantAddress());
    }

    private void getSharedData() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
        customer.setCustomerName(pref.getString("clientname", ""));
        customer.setCustomerAddress(pref.getString("clientaddress", ""));
        customer.setCustomerMail(pref.getString("clientmail", ""));
        customer.setCustomerPhone(pref.getString("clientphone", ""));
        customer.setCustomerID(pref.getString("dbkey", ""));
    }


    private void startCart(){
        String data = "";
        Intent i = new Intent(this, Cart.class);
        List<Dish> cart_dishes = getCartDishes();
        storeData(cart_dishes);
        price = 0.;
        for (Dish d:cart_dishes) {
            String p = String.format("%.2f", d.getPrice());
            data += d.getQuantity() +"x\t"+ d.getName()+"\t"+ p + "€\n";
            price += d.getQuantity()*d.getPrice();
        }
        if(price == 0){
            Toast.makeText(this, R.string.cart_empty, Toast.LENGTH_LONG);
            return;
        }
        if(restaurant.getRestaurant_price_ship() != 0.) {
            String p = String.format("%.2f", restaurant.getRestaurant_price_ship());
            data += getString(R.string.ship) + " " + p + "€";
            Log.d("test", restaurant.getRestaurant_price_ship().toString());
            price += restaurant.getRestaurant_price_ship();
        }
        i.putExtra("list", data);
        i.putExtra("price", price);
        i.putExtra("restaurant_name", restaurant.getRestaurantName());
        i.putExtra("restaurant_address", restaurant.getRestaurantAddress());
        i.putExtra("restaurant_photo", restaurant.getPhoto());
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
        dbRef = database.getReference("ristoranti/"+ restaurant.getRestaurantID() +"/piatti_del_giorno/");
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
                    values.put("client_name", element.getName());
                    values.put("quantity", element.getQuantity());
                    values.put("price", element.getPrice());
                    values.put("id", element.Id());
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
                cart_list.add(new Dish(values.getString("client_name"), values.getInt("quantity"), values.getDouble("price"), values.getString("id")));
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
            DatabaseReference dbRefRestaurant = database.getReference("ristoranti/"+restaurant.getRestaurantID()+"/piatti_del_giorno/");
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
                            Log.d("transazione", cart_dish.Id());
                            dishID = child.getKey();
                            if (dishID != null && dishID.equals(cart_dish.Id())) {
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
                        //String dishID = d.Id();
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
                        Order order = new Order(cart_dishes, new Date(), restaurant, customer, price, note, deliveryTime);
                        DatabaseReference dbRefOrdini = database.getReference("ordini/");
                        DatabaseReference orderID = dbRefOrdini.push();
                        orderID_key = orderID.getKey();
                        order.sId(orderID_key);
                        orderID.setValue(order);
                        //AGGIUNGO LA CHIAVE AGLI ORDINI PENDENTI DEL RISTORANTE
                        DatabaseReference dbRefRestaurant = database.getReference("ristoranti/" + restaurant.getRestaurantID() + "/ordini_pendenti/");
                        DatabaseReference id_restaurant = dbRefRestaurant.push();
                        id_restaurant.setValue(orderID.getKey());
                        //AGGIUNGO LA CHIAVE AGLI ORDINI DEL CLIENTE
                        DatabaseReference dbRefClient = database.getReference("clienti/" + customer.getCustomerID() + "/lista_ordini/");
                        DatabaseReference id_client = dbRefClient.push();
                        id_client.setValue(orderID.getKey());
                        Toast.makeText(ctx, R.string.order_succesfull, Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                    else
                        Toast.makeText(ctx, R.string.order_error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
