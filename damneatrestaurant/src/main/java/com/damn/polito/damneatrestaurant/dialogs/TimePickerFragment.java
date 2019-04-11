package com.damn.polito.damneatrestaurant.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment{

    private EditText text;
    TimePickerDialog.OnTimeSetListener listener;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), listener, hour, min, true);
    }

    public void setOnTimeListener(TimePickerDialog.OnTimeSetListener listener) {
        this.listener = listener;
    }

    public void setEditText(EditText et) {
        text = et;
    }

    public EditText getEditText() {
        return text;
    }
}
