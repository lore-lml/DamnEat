package com.damn.polito.damneat.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.damn.polito.commonresources.beans.DayOfTheWeek;
import com.damn.polito.damneat.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class SpinnerDialog extends DialogFragment {

    private Spinner spinner;
    private Button save;
    private String opening;
    private Calendar instance;

    private boolean ok = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        opening = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString("rest_opening", null);
        assert opening != null;
        return inflater.inflate(R.layout.dialog_spinner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().setTitle(R.string.dialog_time);

        spinner = view.findViewById(R.id.spinner);
        save = view.findViewById(R.id.spinner_save);


        instance = Calendar.getInstance();
        int weekday = getDayOfWeek(instance.get(Calendar.DAY_OF_WEEK));
        DayOfTheWeek day = DayOfTheWeek.listOfDays(opening).get(weekday);
        List<String> hours = getSpinnerHours(day);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, hours);

        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(adapter);

        save.setOnClickListener(v->{
            ok = true;
            dismiss();
        });
    }

    private List<String> getSpinnerHours(DayOfTheWeek day) {
        List<String> hours = new ArrayList<>();
        if(instance == null) return hours;

        if(day.getFirstTimeSlot() != null)
            hours.addAll(getHourInRange(day.getFirstOpenTime(), day.getFirstCloseTime()));
        if(day.getSecondTimeSlot() != null && hours.size() == 0)
            hours.addAll(getHourInRange(day.getSecondOpenTime(), day.getSecondCloseTime()));

        return hours;
    }

    private List<String> getHourInRange(String open, String close) {
        List<String> hours = new ArrayList<>();
        int myHour = instance.get(Calendar.HOUR_OF_DAY);
        int myMinute = instance.get(Calendar.MINUTE);

        String[] openTime = open.split(":");
        int openHour = Integer.valueOf(openTime[0]);
        int openMin = Integer.valueOf(openTime[1]);

        if(myHour < openHour) return hours;
        if(myHour == openHour && myMinute < openMin) return hours;

        String[] closeTime = close.split(":");
        int closeHour = Integer.valueOf(closeTime[0]);
        int closeMin = Integer.valueOf(closeTime[1]);

        //Calcolo differenza di ore tra chiusura e apertura
        int diffHour = closeHour - openHour < 0 ? 24-(closeHour-openHour)*-1 : closeHour - openHour;
        int diffMin = closeMin - openMin;
        if(diffMin < 0){
            diffHour--;
            diffMin = 60 - diffMin;
        }

        int tmpHour = openHour + diffHour;
        int tmpMin = openMin + diffMin;
        if(tmpMin >= 60){
            tmpMin %= 60;
            tmpHour++;
        }

        if(myHour > tmpHour) return hours;
        if(myHour == tmpHour && myMinute > tmpMin) return hours;

        for(int i = myHour+1; i<tmpHour ; i++){
            hours.add("" +(i%24)+":00");
            hours.add("" +(i%24)+":30");
        }

        if(60 - myMinute < 25)
            hours.remove(0);

        return hours;
    }

    private int getDayOfWeek(int englishVersion){
        if(englishVersion-2 < 0)
            return englishVersion +5;

        return englishVersion-2;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(!ok) {
            super.onDismiss(dialog);
            return;
        }
        Activity activity = getActivity();
        assert activity != null;
        ((HandleDismissDialog) activity).handleOnDismiss(DialogType.SpinnerDialog, (String)spinner.getSelectedItem());
    }
}
