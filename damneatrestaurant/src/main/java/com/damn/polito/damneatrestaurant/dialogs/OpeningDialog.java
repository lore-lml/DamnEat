package com.damn.polito.damneatrestaurant.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.damn.polito.commonresources.beans.DayOfTheWeek;
import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.adapters.DayOfTheWeekAdapter;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class OpeningDialog extends DialogFragment {

    private String[] daysList;
    List<DayOfTheWeek> days;
    private RecyclerView recyclerView;
    private DayOfTheWeekAdapter adapter;
    private FloatingActionButton fab;
    private String text;
    private boolean saved = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_opening, container, false);
        recyclerView = v.findViewById(R.id.recycler_day);
        recyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));

        days = load();
        assert days != null;
        if(days.isEmpty()) {
            initDays();
            for (String day : this.daysList)
                days.add(new DayOfTheWeek(day));
        }else
            recyclerView.smoothScrollToPosition(6);


        adapter = new DayOfTheWeekAdapter(days, getActivity());
        recyclerView.setAdapter(adapter);
        this.getDialog().setTitle("Opening");

        fab = v.findViewById(R.id.fab_day);
        fab.setOnClickListener(view -> saveAction());
        return v;
    }

    private void saveAction() {
        boolean ok = true;
        for(int i = 0; i<days.size(); i++){
            //Ritorna al massimo i due slot orari per quella giornata
            List<String> slots = adapter.getSlots(i);
            DayOfTheWeek d = days.get(i);

            //Se è null qualcosa è andato storto
            if(slots == null){
                Toast.makeText(getContext(), getString(R.string.invalid_slot, d.getDay()), Toast.LENGTH_LONG).show();
                ok = false;
                recyclerView.smoothScrollToPosition(i);
                break;
            }else if(slots.size() == 0){

                d.setClosed(true); //Se la lista è vuota, in quel giorno si è chiusi

            }else{ //Altrimenti è stato settato almeno uno slot orario
                String[] fs = slots.get(0).split("\\s+");
                days.get(i).setFirstTimeSlot(fs[0], fs[1]);
                if(slots.size() == 2) {
                    String[] ss = slots.get(1).split("\\s+");
                    d.setSecondTimeSlot(ss[0], ss[1]);
                }else{
                    d.setSecondTimeSlot("", "");
                }
            }
        }

        if(ok){
            saved = true;
            this.dismiss();
        }
    }

    /*private void store() {
        JSONArray array = new JSONArray();
        for(DayOfTheWeek d : days)
            array.put(d.toJson());

        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .putString("days", array.toString()).apply();
    }*/

    private List<DayOfTheWeek> load(){

        if(text != null && !text.isEmpty()){
            return DayOfTheWeek.listOfDays(text);
        }

        return new ArrayList<>();
    }

    private void initDays() {
        daysList = new String[7];
        daysList[0] = getString(R.string.lun_text);
        daysList[1] = getString(R.string.mar_text);
        daysList[2] = getString(R.string.mer_text);
        daysList[3] = getString(R.string.gio_text);
        daysList[4] = getString(R.string.ven_text);
        daysList[5] = getString(R.string.sab_text);
        daysList[6] = getString(R.string.dom_text);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(!saved){
            super.onDismiss(dialog);
            return;
        }

        Activity activity = getActivity();
        if(activity instanceof HandleDismissDialog){
            StringBuilder sb = new StringBuilder();
            for(DayOfTheWeek d : days)
                sb.append(d.toString()).append("\n");

            ((HandleDismissDialog) activity).handleOnDismiss(DialogType.Opening, sb.toString());
        }
    }

    public void setDaysText(String sOpening){
        text = sOpening;
    }
}
