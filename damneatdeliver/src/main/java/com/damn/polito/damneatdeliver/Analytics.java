package com.damn.polito.damneatdeliver;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneatdeliver.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static com.damn.polito.commonresources.Utility.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Analytics extends AppCompatActivity implements OnChartValueSelectedListener, OnChartGestureListener{

    private TextView totalDistance;
    private float totDistance;
    private LineChart mChart;
    private static final String TAG= "Analytics";
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        totalDistance = findViewById(R.id.analytics_total_distance_text);
        mChart = (LineChart) findViewById(R.id.distance_chart);
        mChart.setOnChartGestureListener(Analytics.this);
        mChart.setOnChartValueSelectedListener(Analytics.this);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);
        database = FirebaseDatabase.getInstance();
        ArrayList<Entry> yValues = new ArrayList<>();
//        yValues.add(new Entry(0,60f));
//        yValues.add(new Entry(1,50f));
//        yValues.add(new Entry(2,80f));
//        yValues.add(new Entry(3,90f));
//        yValues.add(new Entry(4,40f));
//        yValues.add(new Entry(5,30f));
//        yValues.add(new Entry(6,80f));
//        yValues.add(new Entry(7,66f));
//
        //GET VALUES FROM FIREBASE
        yValues.clear();
        totalDistance.setText( getApplicationContext().getString(R.string.distance_traveled,0.0));
        DatabaseReference distanceState = database.getReference("deliverers/" + Welcome.getDbKey() + "/analytics/");
        distanceState.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Map<String, Double> map = (Map<String, Double>) dataSnapshot.getValue();
                String key = dataSnapshot.getKey();
                int time = Integer.valueOf(dataSnapshot.getKey());
                float distance = Float.valueOf(dataSnapshot.getValue().toString());
                totDistance+=distance/1000;
                yValues.add(new Entry(yValues.size()+1,distance/1000));

                LineDataSet set1 = new LineDataSet(yValues,"Data Set 1");
                set1.setFillAlpha(110);

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(set1);
                set1.setColor(Color.RED);

                LineData data = new LineData(dataSets);

                mChart.setData(data);
                mChart.setDrawGridBackground(false);

                totalDistance.setText( getApplicationContext().getString(R.string.distance_traveled,totDistance));
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

    private void init() {

    }




    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i(TAG,"onChartGestureStart: X: "+ me.getX()+"Y: "+me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i(TAG,"onChartGestureEnd: "+ lastPerformedGesture);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i(TAG,"onChartLongPressed: ");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i(TAG,"onChartDoubleTapped: ");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i(TAG,"onChartSingleTapped");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i(TAG,"onChartFling: veloX: "+velocityX+" veloY: "+ velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i(TAG,"onChartScale: scaleX: "+ scaleX + "scaleY: " +scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i(TAG,"onChartTranslate: dX: "+dX +"dY: "+dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i(TAG,"onValueSelected: " + e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i(TAG,"onNothingSelected");
    }
}