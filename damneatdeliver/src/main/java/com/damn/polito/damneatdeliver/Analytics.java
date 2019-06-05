package com.damn.polito.damneatdeliver;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Order;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static com.damn.polito.commonresources.Utility.showWarning;

public class Analytics extends AppCompatActivity {
    private ListView listView;
    private TextView totalDistance,money;
    private float totDistance, totMoney;
    private static final String TAG= "Analytics";
    private FirebaseDatabase database;
    private int fTimestamp=0;
    private int count=0;
    private Map<String,Float> travelsList;
    private List<Order> orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        orders = Welcome.getOrders();
        travelsList=new TreeMap<>((String e1, String e2) -> -(e1.compareTo(e2)));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        totalDistance = findViewById(R.id.analytics_total_distance_text);
        listView = findViewById(R.id.analytics_list);
        database = FirebaseDatabase.getInstance();
        totalDistance.setText( getApplicationContext().getString(R.string.distance_traveled,0.0));
        DatabaseReference distanceState = database.getReference("deliverers/" + Welcome.getDbKey() + "/analytics/");
        money=findViewById(R.id.you_earned_tot_money);
        money.setText( getApplicationContext().getString(R.string.money_earned,0.0));
        Collections.sort(orders,(Order e1, Order e2) -> -(e1.getDate().compareTo(e2.getDate())));

        DecimalFormat df = new DecimalFormat("#.#");
        DecimalFormat dfPrice = new DecimalFormat("#.##");
        List<HashMap<String, String>> listItems = new ArrayList<>();
        SimpleAdapter adapter;
        adapter = new SimpleAdapter(getApplicationContext(), listItems, R.layout.list_item,
                new String[]{"First Line", "Second Line","Third Line"},
                new int[]{R.id.text1, R.id.text2, R.id.text3});

        for(Order o: orders){
            //travelsList.put("AAA", (float) o.getDistance());
            Date time;
            double price;
            double km;

            time=o.getDate();

            km = o.getDistance()/1000;
            totDistance+=km;

            price=km*0.5+2;
            totMoney+=price;


            HashMap<String, String> resultsMap = new HashMap<>();
            resultsMap.put("First Line", Utility.dateString(time));
            resultsMap.put("Second Line", df.format(km)+" Km");
            resultsMap.put("Third Line", dfPrice.format(price)+" €");
            listItems.add(resultsMap);

        }
        money.setText( getApplicationContext().getString(R.string.money_earned,totMoney));
        totalDistance.setText( getApplicationContext().getString(R.string.distance_traveled,totDistance));
        listView.setAdapter(adapter);




//                    Iterator it = travelsList.entrySet().iterator();
//                    while (it.hasNext())
//        distanceState.orderByKey().addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                //Map<String, Double> map = (Map<String, Double>) dataSnapshot.getValue();
//                String key = dataSnapshot.getKey();
//                long time = Integer.valueOf(dataSnapshot.getKey());
//                count++;
//                float distance = Float.valueOf(dataSnapshot.getValue().toString());
//                totDistance+=distance/1000;
//                Date d = new Date((long)time*1000);
//                DateFormat f = new SimpleDateFormat("yyyy-MM-dd\tHH:mm");
//                travelsList.put(f.format(d),distance/1000);
//                Float priceTMP =  distance/1000;
//                priceTMP/=2;
//                priceTMP+=2;
//                if(count>=dataSnapshot.getChildrenCount()){
//                    totalDistance.setText( getApplicationContext().getString(R.string.distance_traveled,totDistance));
//                    List<HashMap<String, String>> listItems = new ArrayList<>();
//                    SimpleAdapter adapter;
//                    adapter = new SimpleAdapter(getApplicationContext(), listItems, R.layout.list_item,
//                            new String[]{"First Line", "Second Line","Third Line"},
//                            new int[]{R.id.text1, R.id.text2, R.id.text3});
//                    {
//                        HashMap<String, String> resultsMap = new HashMap<>();
//                        Map.Entry pair = (Map.Entry)it.next();
//                        resultsMap.put("First Line", pair.getKey().toString());
//                        DecimalFormat df = new DecimalFormat("#.#");
//                        DecimalFormat dfPrice = new DecimalFormat("#.##");
//                        resultsMap.put("Second Line", df.format(pair.getValue())+" Km");
//                        Float price = (Float) pair.getValue();
//                        price/=2;
//                        price+=2;
//                        resultsMap.put("Third Line", dfPrice.format(price)+" €");
//                        listItems.add(resultsMap);
//
//                    }
//                    totMoney+=priceTMP;
//                    money.setText( getApplicationContext().getString(R.string.money_earned,totMoney));
//
//                    listView.setAdapter(adapter);
//                }
//
//
//
//
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//
//        });
//
//
//
//
//        init();
    }



    @Override
    public void onBackPressed() {

        finish();

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

            this.finish();
            return true;
    }


}