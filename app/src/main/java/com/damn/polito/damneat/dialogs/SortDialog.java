package com.damn.polito.damneat.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.damn.polito.damneat.R;
import com.damn.polito.damneat.fragments.RestaurantFragment;

public class SortDialog extends DialogFragment {

    private RadioGroup radioGroup;
    private HandleDismissDialog from;
    private boolean ok = false;
    private RestaurantFragment.SortType sortType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(from == null)
            throw new IllegalStateException("You must call setListener() before create show the dialog");
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
            case PriceDesc:
                ((RadioButton)radioGroup.getChildAt(1)).setChecked(true);
                break;
            case PriceAsc:
                ((RadioButton)radioGroup.getChildAt(2)).setChecked(true);
                break;
            case MostRated:
                ((RadioButton)radioGroup.getChildAt(3)).setChecked(true);
                break;
        }
    }

    public void setListener(HandleDismissDialog from) {
        if(from == null)
            throw new IllegalArgumentException("You cannot pass a null listener");
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
                result = RestaurantFragment.SortType.Alpha.toString();
                break;
            case R.id.radio_asc:
                result = RestaurantFragment.SortType.PriceAsc.toString();
                break;
            case R.id.radio_desc:
                result = RestaurantFragment.SortType.PriceDesc.toString();
                break;
            case R.id.radio_mostrated:
                result = RestaurantFragment.SortType.MostRated.toString();
                break;
        }

        from.handleOnDismiss(DialogType.SortDialog, result);
    }

    public void setSortType(RestaurantFragment.SortType sortType) {
        if(sortType == null) return;
        this.sortType = sortType;
    }
}
