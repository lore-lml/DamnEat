package com.damn.polito.damneat.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.damn.polito.damneat.R;

import java.util.ArrayList;

public class FilterDialog extends DialogFragment {

    private ArrayList<CheckBox> cuisine;
    private ArrayList<CheckBox> dishes;
    private CheckBox[] priceRange;
    private ImageButton refresh;
    private Button save;

    private HandleDismissDialog from;
    private String categories;
    private String result;
    private boolean ok = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(from == null)
            throw new IllegalStateException("You must call setListener() before create show the dialog");

        View v = inflater.inflate(R.layout.dialog_filter, container, false);
        initViews(v);
        initParams();
        save.setOnClickListener(view-> saveAction());
        refresh.setOnClickListener(view -> refreshAction(v));
        return v;
    }

    private void refreshAction(View v) {
        for(CheckBox c : cuisine)
            c.setChecked(false);
        for(CheckBox c : dishes)
            c.setChecked(false);
        for(CheckBox c : priceRange)
            c.setChecked(false);
    }

    private void initParams() {
        if(categories == null || categories.isEmpty()) return;

        String[] cat = categories.split(",\\s?");
        if(cat.length == 0) return;

        boolean found;
        int nDishes = 0;
        for (int i = 0; i < cat.length; i++) {
            found = false;
            for(CheckBox c : cuisine) {
                if (c.getText().toString().equals(cat[i])) {
                    c.setChecked(true);
                    found = true;
                    break;
                }
            }
            if(found) continue;

            for(CheckBox c : dishes){
                if(c.getText().toString().equals(cat[i])) {
                    c.setChecked(true);
                    found = true;
                    nDishes++;
                    break;
                }
            }

            if(!found && !cat[i].contains("€")){
                //Essendo ordinati prima le cucine e poi i piatti e i le custom choice sono in ultima posizione, se il primo non trovato
                //capita quando non sono stati trovati ancora piatti allora è sicuramente una cucina
                if(nDishes == 0){
                    cuisine.get(cuisine.size()-1).setChecked(true);
                }else{
                    dishes.get(dishes.size()-1).setChecked(true);
                }
            }else if (cat[i].contains("€")){

                String priceRange = cat[i];
                switch (priceRange){
                    case "(€)":
                        this.priceRange[0].setChecked(true);
                        break;
                    case "(€€)":
                        this.priceRange[1].setChecked(true);
                        break;
                    case "(€€€)":
                        this.priceRange[2].setChecked(true);
                        break;
                }
            }
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


        for(CheckBox c : priceRange){
            if(c.isChecked())
                sb.append("(").append(c.getText()).append(")").append(",");
        }
        if(sb.length()>0)
            result = sb.substring(0, sb.length()-1);
        ok = true;
        dismiss();
    }

    private void initViews(View v) {
        cuisine = new ArrayList<>();
        dishes = new ArrayList<>();

        priceRange = new CheckBox[3];
        priceRange[0] = v.findViewById(R.id.box_cheap);
        priceRange[1] = v.findViewById(R.id.box_medium);
        priceRange[2] = v.findViewById(R.id.box_expensive);
        save = v.findViewById(R.id.category_save);
        refresh = v.findViewById(R.id.filter_refresh);

        cuisine.add(v.findViewById(R.id.box_italian));
        cuisine.add(v.findViewById(R.id.box_greek));
        cuisine.add(v.findViewById(R.id.box_mexican));
        cuisine.add(v.findViewById(R.id.box_japanese));
        cuisine.add(v.findViewById(R.id.box_chinese));

        dishes.add(v.findViewById(R.id.box_pizza));
        dishes.add(v.findViewById(R.id.box_sushi));
        dishes.add(v.findViewById(R.id.box_sandwich));
        dishes.add(v.findViewById(R.id.box_hamburger));
        dishes.add(v.findViewById(R.id.box_tacos));
        dishes.add(v.findViewById(R.id.box_starter));
        dishes.add(v.findViewById(R.id.box_meat));
    }

    public void setCategories(String categories){
        this.categories = categories;
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

        from.handleOnDismiss(DialogType.FilterDialog, result);
    }
}
