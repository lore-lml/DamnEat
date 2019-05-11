package com.damn.polito.damneatrestaurant.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.damn.polito.damneatrestaurant.R;
import com.damn.polito.damneatrestaurant.FindDelivererActivity;

public class SortDialog extends AppCompatDialogFragment {

    private RadioGroup radioGroup;
    private Fragment from;
    private boolean ok = false;
    private FindDelivererActivity.SortType sortType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(from == null)
            throw new RuntimeException("You have to call setFragment() method before show the sort dialog");
        this.getDialog().setTitle(R.string.sort);
        return inflater.inflate(R.layout.dialog_sort, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        radioGroup = view.findViewById(R.id.sort_group);

        initRadio();

        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            ok = true;
            dismiss();
        });
    }

    private void initRadio() {
        if(sortType == null) return;
        switch (sortType){
            case Alpha:
                ((RadioButton)radioGroup.getChildAt(0)).setChecked(true);
                break;
            case Closer:
                ((RadioButton)radioGroup.getChildAt(1)).setChecked(true);
                break;
            case Rating:
                ((RadioButton)radioGroup.getChildAt(2)).setChecked(true);
                break;
            case TotDeliver:
                ((RadioButton)radioGroup.getChildAt(3)).setChecked(true);
                break;
        }
    }

    public void setFragment(Fragment from) {
        this.from = from;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(!ok) {
            super.onDismiss(dialog);
            return;
        }
        String result = "";
        switch (radioGroup.getCheckedRadioButtonId()){
            case R.id.radio_alpha:
                result = FindDelivererActivity.SortType.Alpha.toString();
                break;
            case R.id.radio_asc:
                result = FindDelivererActivity.SortType.Closer.toString();
                break;
            case R.id.radio_desc:
                result = FindDelivererActivity.SortType.Rating.toString();
                break;
            case R.id.radio_mostrated:
                result = FindDelivererActivity.SortType.TotDeliver.toString();
                break;
        }

        ((HandleDismissDialog)from).handleOnDismiss(DialogType.SortDialog, result);
    }

    public void setSortType(FindDelivererActivity.SortType sortType) {
        if(sortType == null) return;
        this.sortType = sortType;
    }
}
