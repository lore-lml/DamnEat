package com.damn.polito.damneatrestaurant.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.adapters.DayOfTheWeekAdapter;
import com.damn.polito.damneatrestaurant.beans.DayOfTheWeek;

import java.util.ArrayList;
import java.util.List;

public class OpeningDialog extends DialogFragment {

    private RecyclerView recyclerView;
    private DayOfTheWeekAdapter adapter;
    private FloatingActionButton fab;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_opening, container, false);
        recyclerView = v.findViewById(R.id.recycler_day);
        recyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));

        List<DayOfTheWeek> days = new ArrayList<>();
        days.add(new DayOfTheWeek("LUN"));
        days.add(new DayOfTheWeek("MAR"));
        days.add(new DayOfTheWeek("MER"));
        days.add(new DayOfTheWeek("GIO"));
        days.add(new DayOfTheWeek("VEN"));
        days.add(new DayOfTheWeek("SAB"));
        days.add(new DayOfTheWeek("DOM"));

        adapter = new DayOfTheWeekAdapter(days, v.getContext());
        recyclerView.setAdapter(adapter);

        this.getDialog().setTitle("Opening");
        return v;
    }
}
