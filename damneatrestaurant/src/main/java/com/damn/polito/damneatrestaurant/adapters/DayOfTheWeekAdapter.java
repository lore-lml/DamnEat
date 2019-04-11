package com.damn.polito.damneatrestaurant.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.beans.DayOfTheWeek;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class DayOfTheWeekAdapter extends RecyclerView.Adapter<DayOfTheWeekAdapter.DayOfTheWeekHolder> {

    private List<DayOfTheWeek> days;
    private Context ctx;
    private ArrayList<String> timeSlot;

    public DayOfTheWeekAdapter(List<DayOfTheWeek> days, Context ctx) {
        this.days = days;
        this.ctx = ctx;
        timeSlot = new ArrayList<>();
        int[] half = new int[]{0,30};

        for (int i = 0; i<24 ; i++)
            for(int j : half){
                StringBuilder sb = new StringBuilder(String.format(Locale.getDefault(),"%2d", i));
                sb.append(":");
                sb.append(String.format(Locale.getDefault(), "%2d", j));
                timeSlot.add(sb.toString());
            }
    }

    @NonNull
    @Override
    public DayOfTheWeekHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.day_of_week_layout, viewGroup, false);
        return new DayOfTheWeekAdapter.DayOfTheWeekHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayOfTheWeekHolder holder, int position) {
        DayOfTheWeek day = days.get(position);
        holder.day.setText(day.getDay());
        holder.isClosed.setChecked(day.isClosed());
        holder.isClosed.setOnCheckedChangeListener((buttonView, isChecked) -> day.setClosed(isChecked));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public class DayOfTheWeekHolder extends RecyclerView.ViewHolder implements AdapterView.OnItemSelectedListener {
        private CardView parent;
        private TextView day;
        private Switch isClosed;
        private Spinner open1,open2,close1,close2;
        private String oh1, oh2, ch1, ch2;

        public DayOfTheWeekHolder(View itemView) {
            super(itemView);

            parent = itemView.findViewById(R.id.opening_dialog_root);
            day = itemView.findViewById(R.id.opening_dialog_day);
            isClosed = itemView.findViewById(R.id.opening_dialog_switch);
            open1 = itemView.findViewById(R.id.opening_dialog_open1);
            close1 = itemView.findViewById(R.id.opening_dialog_close1);
            open2 = itemView.findViewById(R.id.opening_dialog_open2);
            close2 = itemView.findViewById(R.id.opening_dialog_close2);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item, timeSlot);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            open1.setAdapter(adapter);
            open2.setAdapter(adapter);
            close1.setAdapter(adapter);
            close2.setAdapter(adapter);
            open1.setOnItemSelectedListener(this);
            close1.setOnItemSelectedListener(this);
            open2.setOnItemSelectedListener(this);
            close2.setOnItemSelectedListener(this);
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String text = parent.getItemAtPosition(position).toString();

            if(id == open1.getId()){
                oh1 = text;
            }else if(id == close1.getId()){
                ch1 = text;
            }else if(id == open2.getId()){
                oh2 = text;
            }else if(id == close2.getId()){
                ch2 = text;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}
