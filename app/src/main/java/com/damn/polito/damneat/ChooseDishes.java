package com.damn.polito.damneat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.FirebaseLogin;
import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Customer;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.commonresources.beans.QueryType;
import com.damn.polito.commonresources.beans.Restaurant;
import com.damn.polito.damneat.adapters.DishesAdapter;
import com.damn.polito.damneat.beans.Profile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChooseDishes extends AppCompatActivity {
    private List<Dish> dishesList = new ArrayList<>();
    private RecyclerView recyclerView;
    private DishesAdapter adapter;
    private FloatingActionButton fab_cart;
    private ImageView no_dishes_img;
    private CardView card;
    private CircleImageView restaurantCircleView;
    private TextView no_dishes_tv, restaurantName, restaurantDesc, restaurantShipPrice, restaurantReviews;
    private RatingBar ratingBar;
    private ValueEventListener listener;

    // Dettagli ristorante
    private Restaurant restaurant = new Restaurant();
    private String restaurant_description;

    private String note;
    private String deliveryTime;


    private Customer customer = new Customer();

    private Double price = -1.;
    private int quantity = -1;
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
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.nav_dishes);

        setContentView(R.layout.activity_choose_dishes);
        no_dishes_img = findViewById(R.id.no_dishes_img);
        no_dishes_tv = findViewById(R.id.no_dishes_tv);
        card = findViewById(R.id.view);
        restaurantCircleView = findViewById(R.id.restaurant_img);
        restaurantDesc = findViewById(R.id.restaurant_category);
        restaurantName = findViewById(R.id.restaurant_name);
        restaurantShipPrice = findViewById(R.id.restaurant_ship_price);
        restaurantReviews = findViewById(R.id.restaurant_reviews);
        ratingBar = findViewById(R.id.restaurant_ratingbar);
        fab_cart = findViewById(R.id.fab_cart);
        fab_cart.setOnClickListener(v-> startCart());
        ctx = ChooseDishes.this;
        getIntentData();
        getCustomerInfo();
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
        int reviews_number = i.getIntExtra("rest_reviews_number", 0);
        int total_rate = i.getIntExtra("rest_total_rate", 0);

                //Log.d("restaurant", restaurant.getRestaurantName());
        //Log.d("restaurant", restaurant.getRestaurantAddress());
        if(reviews_number==0){
            total_rate = 0;
            reviews_number = 1;
        }
        ratingBar.setRating((float)total_rate/(float)reviews_number);
        restaurantName.setText(restaurant.getRestaurantName());
        restaurantDesc.setText(restaurant_description);
        restaurantReviews.setText(Integer.toString(reviews_number));
        restaurantShipPrice.setText(restaurant.getRestaurant_price_ship() == 0 ?
                ctx.getString(R.string.price_free) : ctx.getString(R.string.order_price, restaurant.getRestaurant_price_ship()));
        restaurantDesc.setText(restaurant_description);
        Bitmap bitmap = Utility.StringToBitMap(restaurant.getPhoto());
        if(bitmap != null)
            restaurantCircleView.setImageBitmap(bitmap);
    }

    private void getCustomerInfo() {
        Profile p = Welcome.getProfile();
        customer.setCustomerName(p.getName());
        customer.setCustomerAddress(p.getAddress());
        customer.setCustomerMail(p.getMail());
        customer.setCustomerPhone(p.getPhone());
        customer.setCustomerID(Welcome.getDbKey());
        customer.setCustomerPhoto(p.getBitmapProf());
    }


    private void startCart(){
        StringBuilder data = new StringBuilder();
        StringBuilder prices = new StringBuilder();
        Intent i = new Intent(this, Cart.class);
        List<Dish> cart_dishes = getCartDishes();
        storeData(cart_dishes);
        price = 0.;
        quantity = 0;
        for (Dish d:cart_dishes) {
            String p = String.format(Locale.getDefault(),"%.2f", d.getPrice()*d.getQuantity());
            prices.append(p).append("€ ").append("\n");
            data.append(d.getQuantity()).append("x ").append(d.getName()).append("\n");
            price += d.getQuantity()*d.getPrice();
            quantity += d.getQuantity();
        }
        if(data.length()>0)
            data.deleteCharAt(data.length()-1);
        if(prices.length()>0)
            prices.deleteCharAt(prices.length()-1);

        if(price == 0){
            Toast.makeText(this, R.string.cart_empty, Toast.LENGTH_LONG).show();
            return;
        }
        String ship;
        if(restaurant.getRestaurant_price_ship() != 0.) {
            ship = String.format(Locale.getDefault(),"%.2f €", restaurant.getRestaurant_price_ship());
            Log.d("test", restaurant.getRestaurant_price_ship().toString());
            price += restaurant.getRestaurant_price_ship();
        }else{
            ship = getString(R.string.price_free);
        }
        i.putExtra("list", data.toString());
        i.putExtra("price", price);
        i.putExtra("restaurant_name", restaurant.getRestaurantName());
        i.putExtra("restaurant_address", restaurant.getRestaurantAddress());
        i.putExtra("restaurant_photo", restaurant.getPhoto());
        i.putExtra("restaurant_shipprice", ship);
        i.putExtra("restaurant_dishprices", prices.toString());
        i.putExtra("restaurant_quantity", quantity);
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
        listener = dbRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key;
                Dish dish;
                dishesList.clear();
                for (DataSnapshot chidSnap : dataSnapshot.getChildren()) {
                    //Log.d("tmz",""+ chidSnap.getKey()); //displays the key for the node
                    //Log.d("tmz",""+ chidSnap.getValue());   //gives the value for given keyname
                    //DataPacket value = dataSnapshot.getValue(DataPacket.class);
                    key = chidSnap.getKey();
                    dish = chidSnap.getValue(Dish.class);
                    if(dish!=null && dish.getAvailability()>0) {
                        dishesList.add(dish);
                        dishesList.get(dishesList.size() - 1).setId(key);
                    }
                }
                Collections.sort(dishesList);
                adapter.notifyDataSetChanged();
                //Log.d("Load", dishesList.get(0).getName());
                if (dishesList.size() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    fab_cart.setVisibility(View.GONE);
                    no_dishes_img.setVisibility(View.VISIBLE);
                    no_dishes_tv.setVisibility(View.VISIBLE);
                } else {
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
            note = data.getStringExtra("note");
            deliveryTime = data.getStringExtra("time");
            Log.d("result", note);

            List<Dish> cart_dishes = loadData();
            Order order = new Order(cart_dishes, new Date(), restaurant, customer, price, note, deliveryTime);
            DatabaseReference dbRefOrdini = database.getReference("ordini/");
            DatabaseReference orderID = dbRefOrdini.push();
            orderID_key = orderID.getKey();
            order.setId(orderID_key);
            orderID.setValue(order);
            Toast.makeText(ctx, R.string.order_succesfull, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                this.finish();
                return true;

            case R.id.item_review:
                Intent i = new Intent(this, ReviewsActivity.class);
                i.putExtra("query_type", QueryType.RestaurantReview.toString());
                i.putExtra("restaurant_id", restaurant.getRestaurantID());
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbRef.removeEventListener(listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.review_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


}
