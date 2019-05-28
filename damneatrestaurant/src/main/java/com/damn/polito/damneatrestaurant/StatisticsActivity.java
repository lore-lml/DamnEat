package com.damn.polito.damneatrestaurant;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Dish;
import com.damn.polito.commonresources.beans.Order;
import com.damn.polito.damneatrestaurant.beans.TimesOfDay;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public class StatisticsActivity extends AppCompatActivity {
    private TreeSet<Dish> dishes = new TreeSet<>((e1, e2) -> e2.getnOrders()-e1.getnOrders());
    private List<TimesOfDay> times_l = new LinkedList<>();
    private FirebaseDatabase database;
    private TextView[] tv_v = new TextView[3];
    private TextView[] tv_sold_v = new TextView[3];
    private CardView[] card_v = new CardView[3];
    private TextView tv_time;
    private ImageView[] im_v = new ImageView[3];
    private Button reviews_button;

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

        tv_sold_v[0] = findViewById(R.id.dishes_sold_numer_1);
        tv_sold_v[1] = findViewById(R.id.dishes_sold_numer_2);
        tv_sold_v[2] = findViewById(R.id.dishes_sold_numer_3);

        tv_time = findViewById(R.id.tv_time);

        im_v[0] = findViewById(R.id.dish_image_1);
        im_v[1] = findViewById(R.id.dish_image_2);
        im_v[2] = findViewById(R.id.dish_image_3);



        database = FirebaseDatabase.getInstance();

        reviews_button = findViewById(R.id.button_reviews);
        reviews_button.setOnClickListener(v -> startActivity(new Intent(this, ReviewsActivity.class)));

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
        if(times_l.isEmpty())
            return;
        Collections.sort(times_l);
        tv_time.setText(times_l.get(0).toString());
    }



    private void insertTime(Order o) {
        String tod = calculateKey(o);
        boolean p = false;
        for(TimesOfDay t: times_l){
            if(t.getKey().equals(tod)){
                t.add();
                p = true;
            }
        }
        if(!p){
            TimesOfDay td = new TimesOfDay(tod);
            times_l.add(td);
        }
    }

    private String calculateKey(Order o){
        int hour = o.getDate().getHours();
        int min = o.getDate().getMinutes();
        String min_str;
        String hour_str;
        if(min<15)
            min_str = "00";
        else if(min<45)
            min_str = "30";
        else {
            hour++;
            min_str = "00";
        }

        hour = hour%24;
        if(hour==0)
            hour_str="00";
        else
            hour_str=String.valueOf(hour);
        return ""+hour_str+":"+min_str;
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
                tv_sold_v[i].setText(getString(R.string.dishes_sold_number, d.getnOrders()));
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
