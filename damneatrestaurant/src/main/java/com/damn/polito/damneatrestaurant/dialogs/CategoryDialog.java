package com.damn.polito.damneatrestaurant.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.damn.polito.damneatrestaurant.R;

import java.util.ArrayList;

public class CategoryDialog extends DialogFragment {

    private ArrayList<CheckBox> cuisine;
    private ArrayList<CheckBox> dishes;
    private EditText otherCuisine, otherDish;
    private RadioGroup priceRange;
    private Button save;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        getDialog().setTitle(R.string.restaurant_category);

        initViews(v);

        save.setOnClickListener(view->{
            StringBuilder sb1 = new StringBuilder();
            int cnt = 0;
            for(CheckBox c : cuisine) {
                if (c.isChecked()) {
                    cnt++;
                    sb1.append(c.getText()).append(", ");
                }
            }
            if(cnt == 0){
                Toast.makeText(getContext(), "No cuisine selected", Toast.LENGTH_LONG).show();
                return;
            }
            for(CheckBox c : dishes){
                if(c.isChecked()){
                    sb1.append(c.getText()).append(", ");
                    /*TODO: finire scelta categorie*/
                }
            }
        });
    }

    private void initViews(View v) {
        cuisine = new ArrayList<>();
        dishes = new ArrayList<>();

        priceRange = v.findViewById(R.id.group_pricerange);
        save = v.findViewById(R.id.category_save);
        otherCuisine = v.findViewById(R.id.cuisine_new);
        otherDish = v.findViewById(R.id.dishes_new);
        otherCuisine.setVisibility(View.GONE);
        otherDish.setVisibility(View.GONE);

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

        cuisine.get(cuisine.size()-1).setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                otherCuisine.setVisibility(View.VISIBLE);
            }else{
                otherCuisine.setVisibility(View.GONE);
            }
        });

        dishes.get(dishes.size()-1).setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                otherDish.setVisibility(View.VISIBLE);
            }else{
                otherDish.setVisibility(View.GONE);
            }
        });
    }
}
