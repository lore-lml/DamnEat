package com.damn.polito.damneatrestaurant;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneatrestaurant.adapters.DishesAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

import static com.damn.polito.commonresources.Utility.showWarning;
import static com.damn.polito.damneatrestaurant.Welcome.dbKey;

public class StatisticsActivity extends AppCompatActivity {
    private TreeSet<Dish> dishes = new TreeSet<>((e1, e2) -> e1.getnOrders()-e2.getnOrders());
    private TreeMap<String, Integer> times = new TreeMap<>();
    private FirebaseDatabase database;
    private TextView[] tv_v = new TextView[3];
    private CardView[] card_v = new CardView[3];
    private TextView tv_time;
    private ImageView[] im_v = new ImageView[3];

    public StatisticsActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_statistics);
        card_v[0] = findViewById(R.id.dish_root);
        card_v[1] = findViewById(R.id.dish_root_2);
        card_v[2] = findViewById(R.id.dish_root_3);

        tv_v[0] = findViewById(R.id.tv_dish_name1);
        tv_v[1] = findViewById(R.id.tv_dish_name2);
        tv_v[2] = findViewById(R.id.tv_dish_name3);

        tv_time = findViewById(R.id.tv_time);

        im_v[0] = findViewById(R.id.dish_image_1);
        im_v[1] = findViewById(R.id.dish_image_2);
        im_v[2] = findViewById(R.id.dish_image_3);

        database = FirebaseDatabase.getInstance();

        for(int i=0; i<3; i++)
            card_v[i].setVisibility(View.GONE);

        loadPopularDishes();
        loadPopularTime();
    }

    private void loadPopularTime() {
        if(Welcome.getDbKey() == null) finish(); 
        
        DatabaseReference dbRef = database.getReference("ordini/");
        String dbKey = Welcome.getDbKey();
        Query dbQuery = dbRef.orderByChild("restaurant/restaurantID").equalTo(dbKey);
        dbQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d: dataSnapshot.getChildren()){
                    Order o = d.getValue(Order.class);
                    if(o!=null && o.getDate()!=null){
                        insertTime(o);
                    }
                }
                setTime();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setTime() {
        if(times.isEmpty())
            return;
        tv_time.setText(times.firstKey());
    }

    private void insertTime(Order o) {
        int hour = o.getDate().getHours();
        int min = o.getDate().getMinutes();
        String min_str;
        String hour_str = String.valueOf(hour);
        if(min<15)
            min_str = "00";
        else if(min<45)
            min_str = "30";
        else {
            if(hour==23)
                hour_str="00";
            else{
                hour++;
                hour_str=String.valueOf(hour);
            }
            min_str = "00";
        }
        String tod = ""+hour_str+":"+min_str;

        Log.d("time: ", tod);
        if(times.containsKey(tod)){
            Integer i = times.get(tod);
            i++;
            //times.put(tod, i);
        }
        else{
            times.put(tod, 1);
        }
    }

    private void loadPopularDishes() {
        DatabaseReference ref = database.getReference("/ristoranti/" + Welcome.getDbKey() + "/piatti_totali/");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d: dataSnapshot.getChildren()){
                    Dish dish = d.getValue(Dish.class);
                    if(dish!=null)
                        dishes.add(dish);
                }
                setPodium();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setPodium() {
        Iterator<Dish> iterator = dishes.iterator();

        for(int i=0; i<3 && i<dishes.size(); i++){
            card_v[i].setVisibility(View.VISIBLE);
            Dish d = iterator.next();
            tv_v[i].setText(d.getName());
            if(d.getPhoto()!=null && !d.getPhoto().equals("NO_PHOTO")){
                im_v[i].setImageBitmap(Utility.StringToBitMap(d.getPhoto()));
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
