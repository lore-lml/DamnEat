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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.damn.polito.damneat.R;

import java.util.ArrayList;

public class FilterDialog extends DialogFragment {

    private ArrayList<CheckBox> cuisine;
    private ArrayList<CheckBox> dishes;
    private RadioGroup priceRange;
    private Button save;

    private HandleDismissDialog listener;
    private String categories;
    private String result;
    private boolean ok = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(listener == null)
            throw new RuntimeException("You have to call setListener() method before show the sort dialog");

        View v = inflater.inflate(R.layout.dialog_filter, container, false);
        initViews(v);
        initParams();
        save.setOnClickListener(view-> saveAction());

        return v;
    }

    private void initParams() {
        if(categories == null) return;

        String[] cat = categories.split(",\\s?");
        if(cat.length == 0) return;

        boolean found;
        int nDishes = 0;
        for (int i = 0; i < cat.length; i++) {
            found = false;
            for(CheckBox c : cuisine) {
                if (c.getId() != R.id.box_other && c.getText().toString().equals(cat[i])) {
                    c.setChecked(true);
                    found = true;
                    break;
                }
            }
            if(found) continue;

            for(CheckBox c : dishes){
                if(c.getId() != R.id.box_otherdish && c.getText().toString().equals(cat[i])) {
                    c.setChecked(true);
                    found = true;
                    nDishes++;
                    break;
                }
            }

            if(!found){
                //Essendo ordinati prima le cucine e poi i piatti e i le custom choice sono in ultima posizione, se il primo non trovato
                //capita quando non sono stati trovati ancora piatti allora è sicuramente una cucina
                if(nDishes == 0){
                    cuisine.get(cuisine.size()-1).setChecked(true);
                }else{
                    dishes.get(dishes.size()-1).setChecked(true);
                }
            }
        }

        String priceRange = cat[cat.length-1];
        switch (priceRange){
            case "(€)":
                ((RadioButton)this.priceRange.getChildAt(0)).setChecked(true);
                break;
            case "(€€)":
                ((RadioButton)this.priceRange.getChildAt(1)).setChecked(true);
                break;
            case "(€€€)":
                ((RadioButton)this.priceRange.getChildAt(2)).setChecked(true);
                break;
        }
    }

    private void saveAction() {
        StringBuilder sb = new StringBuilder();
        for(CheckBox c : cuisine)
            if(c.isChecked())
                sb.append(c.getText().toString().trim()).append(",");


        for(CheckBox c : dishes)
            if(c.isChecked())
                sb.append(c.getText().toString().trim()).append(",");


        switch (priceRange.getCheckedRadioButtonId()){
            case R.id.radio_cheap:
                sb.append("(€)");
                break;
            case R.id.radio_medium:
                sb.append("(€€)");
                break;
            case R.id.radio_expensive:
                sb.append("(€€€)");
                break;
            default:
                if(sb.length()!=0)
                    sb.deleteCharAt(sb.length()-1);
                break;
        }

        result = sb.toString();
        ok = true;
        dismiss();
    }

    private void initViews(View v) {
        cuisine = new ArrayList<>();
        dishes = new ArrayList<>();

        priceRange = v.findViewById(R.id.group_pricerange);
        save = v.findViewById(R.id.category_save);

        cuisine.add(v.findViewById(R.id.box_italian));
        cuisine.add(v.findViewById(R.id.box_greek));
        cuisine.add(v.findViewById(R.id.box_mexican));
        cuisine.add(v.findViewById(R.id.box_japanese));
        cuisine.add(v.findViewById(R.id.box_chinese));
        cuisine.add(v.findViewById(R.id.box_other));

        dishes.add(v.findViewById(R.id.box_pizza));
        dishes.add(v.findViewById(R.id.box_sushi));
        dishes.add(v.findViewById(R.id.box_sandwich));
        dishes.add(v.findViewById(R.id.box_hamburger));
        dishes.add(v.findViewById(R.id.box_tacos));
        dishes.add(v.findViewById(R.id.box_starter));
        dishes.add(v.findViewById(R.id.box_meat));
        dishes.add(v.findViewById(R.id.box_otherdish));
    }

    public void setListener(HandleDismissDialog listener){
        if(listener == null)
            throw new RuntimeException("The fragment you passed is null");
        this.listener = listener;
    }

    public void setCategories(String categories){
        this.categories = categories;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(!ok) {
            super.onDismiss(dialog);
            return;
        }

        listener.handleOnDismiss(DialogType.FilterDialog, result);
    }
}
