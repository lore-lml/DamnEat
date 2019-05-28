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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.damn.polito.commonresources.Utility.showWarning;

public class Analytics extends AppCompatActivity {
    private ListView listView;
    private TextView totalDistance;
    private float totDistance;

    private static final String TAG= "Analytics";
    private FirebaseDatabase database;
    private int fTimestamp=0;
    private int count=0;
    private Map<String,Float> travelsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        travelsList=new HashMap<>();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        totalDistance = findViewById(R.id.analytics_total_distance_text);
        listView = findViewById(R.id.analytics_list);
        database = FirebaseDatabase.getInstance();
        totalDistance.setText( getApplicationContext().getString(R.string.distance_traveled,0.0));
        DatabaseReference distanceState = database.getReference("deliverers/" + Welcome.getDbKey() + "/analytics/");
        distanceState.orderByKey().addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Map<String, Double> map = (Map<String, Double>) dataSnapshot.getValue();
                String key = dataSnapshot.getKey();
                long time = Integer.valueOf(dataSnapshot.getKey());
                count++;
                float distance = Float.valueOf(dataSnapshot.getValue().toString());
                totDistance+=distance/1000;
                Date d = new Date((long)time*1000);
                DateFormat f = new SimpleDateFormat("yyyy-MM-dd\tHH:mm");
                travelsList.put(f.format(d),distance/1000);

                if(count>=dataSnapshot.getChildrenCount()){
                    totalDistance.setText( getApplicationContext().getString(R.string.distance_traveled,totDistance));
                    List<HashMap<String, String>> listItems = new ArrayList<>();
                    SimpleAdapter adapter;
                    adapter = new SimpleAdapter(getApplicationContext(), listItems, R.layout.list_item,
                            new String[]{"First Line", "Second Line"},
                            new int[]{R.id.text1, R.id.text2});


                    Iterator it = travelsList.entrySet().iterator();
                    while (it.hasNext())
                    {
                        HashMap<String, String> resultsMap = new HashMap<>();
                        Map.Entry pair = (Map.Entry)it.next();
                        resultsMap.put("First Line", pair.getKey().toString());
                        DecimalFormat df = new DecimalFormat("#.#");

                        resultsMap.put("Second Line", df.format(pair.getValue())+" Km");
                        listItems.add(resultsMap);
                    }

                    listView.setAdapter(adapter);
                }




            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });




        //init();
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