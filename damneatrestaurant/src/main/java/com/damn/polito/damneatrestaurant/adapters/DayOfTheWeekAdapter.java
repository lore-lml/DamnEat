package com.damn.polito.damneatrestaurant.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.beans.DayOfTheWeek;
import com.damn.polito.damneatrestaurant.dialogs.TimePickerFragment;

import java.util.List;

public class DayOfTheWeekAdapter extends RecyclerView.Adapter<DayOfTheWeekAdapter.DayOfTheWeekHolder> {

    private List<DayOfTheWeek> days;
    private Context ctx;

    public DayOfTheWeekAdapter(List<DayOfTheWeek> days, Context ctx) {
        this.days = days;
        this.ctx = ctx;
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

        for(EditText et : holder.opening){
            et.setOnClickListener(v->{
                TimePickerFragment time = new TimePickerFragment();
                time.setTextView(et);
                time.show(((FragmentActivity)ctx).getSupportFragmentManager(), "time_picker");
            });
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public class DayOfTheWeekHolder extends RecyclerView.ViewHolder {
        private CardView parent;
        private TextView day;
        private Switch isClosed;
        private EditText[] opening;

        public DayOfTheWeekHolder(View itemView) {
            super(itemView);

            parent = itemView.findViewById(R.id.opening_dialog_root);
            day = itemView.findViewById(R.id.opening_dialog_day);
            opening = new EditText[4];
            isClosed = itemView.findViewById(R.id.opening_dialog_switch);
            opening[0] = itemView.findViewById(R.id.opening_dialog_open1);
            opening[1] = itemView.findViewById(R.id.opening_dialog_close1);
            opening[2] = itemView.findViewById(R.id.opening_dialog_open2);
            opening[3] = itemView.findViewById(R.id.opening_dialog_close2);
        }


    }
}
