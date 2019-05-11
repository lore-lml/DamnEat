package com.damn.polito.damneatrestaurant.dialogs;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.damn.polito.damneatrestaurant.EditProfile;
import com.damn.polito.damneatrestaurant.R;

import java.util.ArrayList;

public class CategoryDialog extends DialogFragment {

    private ArrayList<CheckBox> cuisine;
    private ArrayList<CheckBox> dishes;
    private EditText otherCuisine, otherDish;
    private RadioGroup priceRange;
    private Button save;

    private String result;
    private boolean ok = false;

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

        if(result != null){
            initParams();
        }

        save.setOnClickListener(view->{
            StringBuilder sb1 = new StringBuilder();
            boolean otherCuisine = false;
            int cnt = 0;
            for(CheckBox c : cuisine) {
                if (c.isChecked()) {
                    cnt++;
                    if(c.getId() != R.id.box_other)
                        sb1.append(c.getText()).append(", ");
                    else
                        otherCuisine = true;
                }
            }
            if(cnt == 0){
                Toast.makeText(getContext(), "No cuisine selected", Toast.LENGTH_LONG).show();
                return;
            }
            if(otherCuisine)
                sb1.append(this.otherCuisine.getText().toString()).append(", ");

            boolean otherDish = false;
            for(CheckBox c : dishes){
                if(c.isChecked()){
                    if(c.getId() != R.id.box_otherdish)
                        sb1.append(c.getText()).append(", ");
                    else
                        otherDish = true;
                }
            }
            if(otherDish)
                sb1.append(this.otherDish.getText().toString()).append(", ");

            sb1.delete(sb1.length()-2, sb1.length()-1);
            sb1.append(" (");

            switch (priceRange.getCheckedRadioButtonId()){
                case R.id.radio_cheap:
                    sb1.append("€");
                    break;
                case R.id.radio_medium:
                    sb1.append("€€");
                    break;
                case R.id.radio_expensive:
                    sb1.append("€€€");
                    break;
                default:
                    sb1.append("?");
                    break;
            }
            sb1.append(")");
            result = sb1.toString();
            ok = true;
            this.dismiss();
        });
    }

    private void initParams() {
        assert result != null;

        String[] cat = result.split(",?\\s+");
        boolean found;
        int nDishes = 0;
        for (int i = 0; i < cat.length - 1; i++) {
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
                    otherCuisine.setText(cat[i]);
                }else{
                    dishes.get(dishes.size()-1).setChecked(true);
                    otherDish.setText(cat[i]);
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

    public void setCategories(String categories){
        this.result = categories;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(result == null || !ok) {
            super.onDismiss(dialog);
            return;
        }

        Activity activity = getActivity();
        if(activity instanceof HandleDismissDialog){
            ((HandleDismissDialog) activity).handleOnDismiss(DialogType.Categories, result);
        }
    }



}
