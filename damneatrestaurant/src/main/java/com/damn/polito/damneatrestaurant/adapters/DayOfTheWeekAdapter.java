package com.damn.polito.damneatrestaurant.adapters;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.beans.DayOfTheWeek;
import com.damn.polito.damneatrestaurant.dialogs.TimePickerFragment;

import java.util.ArrayList;
import java.util.List;

public class DayOfTheWeekAdapter extends RecyclerView.Adapter<DayOfTheWeekAdapter.DayOfTheWeekHolder> {

    private List<DayOfTheWeek> days;
    private FragmentActivity ctx;
    private SparseArray<DayOfTheWeekHolder> holders;

    public DayOfTheWeekAdapter(List<DayOfTheWeek> days, FragmentActivity ctx) {
        this.days = days;
        this.ctx = ctx;
        holders = new SparseArray<>();
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
        holders.append(position, holder);

        holder.day.setText(day.getDay());
        holder.isClosed.setChecked(day.isClosed());
        holder.setClosed(day.isClosed());
        holder.setTimeSlots(day);

        holder.isClosed.setOnCheckedChangeListener((buttonView, isChecked) -> {
            day.setClosed(isChecked);
            holder.setClosed(isChecked);
        });

        holder.refresh.setOnClickListener(v-> holder.refresh());



        for(EditText et : holder.opening){
            et.setOnClickListener(v->{
                holder.time.setOnTimeListener(holder);
                holder.time.setEditText(et);
                holder.time.show(ctx.getSupportFragmentManager(), "time_picker");
            });
        }
    }

    public List<String> getSlots(int position){
        DayOfTheWeekHolder holder = holders.get(position);
        if(holder == null) return null;

        // Se in quel giorno il ristorante è chiuso torno una lista vuota
        if(holder.isClosed.isChecked()) return new ArrayList<>();

        List<String> slots = new ArrayList<>();
        String fs = holder.getFirstSlot();
        if(fs == null) return null;

        slots.add(fs);
        String ss = holder.getSecondSlot();
        if(ss != null)
            slots.add(ss);
        return slots;
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public class DayOfTheWeekHolder extends RecyclerView.ViewHolder implements TimePickerDialog.OnTimeSetListener {

        private CardView parent;
        private ImageButton refresh;
        private TextView day;
        private Switch isClosed;
        private EditText[] opening;
        private TimePickerFragment time;

        public DayOfTheWeekHolder(View itemView) {
            super(itemView);

            time = new TimePickerFragment();
            parent = itemView.findViewById(R.id.opening_dialog_root);
            refresh = itemView.findViewById(R.id.opening_refresh);
            day = itemView.findViewById(R.id.opening_dialog_day);
            opening = new EditText[4];
            isClosed = itemView.findViewById(R.id.opening_dialog_switch);
            opening[0] = itemView.findViewById(R.id.opening_dialog_open1);
            opening[1] = itemView.findViewById(R.id.opening_dialog_close1);
            opening[2] = itemView.findViewById(R.id.opening_dialog_open2);
            opening[3] = itemView.findViewById(R.id.opening_dialog_close2);

        }

        public void refresh(){
            enableFirstSlot();
            opening[0].setText(ctx.getString(R.string.first_text));
            opening[1].setText(ctx.getString(R.string.slot_text));
            disableSecondSlot();
            isClosed.setChecked(false);
        }

        public void disableFirstSlot(){
            for(int i = 0; i<2; i++){
                opening[i].setEnabled(false);
                opening[i].setTextColor(ctx.getResources().getColor(R.color.colorDisabled));
            }
            opening[0].setText(ctx.getString(R.string.first_text));
            opening[1].setText(ctx.getString(R.string.slot_text));
        }

        public void disableSecondSlot(){
            for(int i = 2; i<4; i++){
                opening[i].setEnabled(false);
                opening[i].setTextColor(ctx.getResources().getColor(R.color.colorDisabled));
            }
            opening[2].setText(ctx.getString(R.string.second_text));
            opening[3].setText(ctx.getString(R.string.slot_text));
        }

        public void enableFirstSlot(){
            for(int i = 0; i<2; i++){
                opening[i].setEnabled(true);
                opening[i].setTextColor(ctx.getResources().getColor(R.color.colorPrimaryDark));
            }
        }

        public void enableSecondSlot(){
            for(int i = 2; i<4; i++){
                opening[i].setEnabled(true);
                opening[i].setTextColor(ctx.getResources().getColor(R.color.colorPrimaryDark));
            }
        }

        public void setClosed(boolean closed){
            if(closed){
                disableFirstSlot();
                disableSecondSlot();
            }else
                enableFirstSlot();

        }

        public String getFirstSlot(){
            String fs = opening[0].getText().toString();
            if(!fs.matches(Utility.Regex.TIME)) return null;

            String ss = opening[1].getText().toString();
            if(!ss.matches(Utility.Regex.TIME))return null;

            return fs + " " + ss;
        }

        public String getSecondSlot(){
            String fs = opening[2].getText().toString();
            if(!fs.matches(Utility.Regex.TIME)) return null;

            String ss = opening[3].getText().toString();
            if(!ss.matches(Utility.Regex.TIME))return null;

            return fs + " " + ss;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            EditText text = time.getEditText();

            StringBuilder sb = new StringBuilder(String.format("%d", hourOfDay));
            sb.append(":");
            sb.append(String.format("%02d", minute));
            text.setText(sb.toString());
            text.setTextColor(ctx.getResources().getColor(R.color.colorPrimaryDark));

            if(text.equals(opening[0]) || text.equals(opening[1])){
                if(opening[0].getText().toString().matches(Utility.Regex.TIME)
                && opening[1].getText().toString().matches(Utility.Regex.TIME)){
                    enableSecondSlot();
                }
            }
        }

        public void setTimeSlots(DayOfTheWeek day) {
            String open1,open2,close1,close2;
            open1 = day.getFirstOpenTime();
            close1 = day.getFirstCloseTime();
            if(open1 != null && close1 != null){
                opening[0].setText(open1);
                opening[1].setText(close1);
            }
            open2 = day.getSecondOpenTime();
            close2 = day.getSecondCloseTime();
            if(open2 != null && close2 != null){
                opening[2].setText(open2);
                opening[3].setText(close2);
            }
        }
    }
}
