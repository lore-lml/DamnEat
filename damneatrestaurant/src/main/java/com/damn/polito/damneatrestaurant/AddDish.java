package com.damn.polito.damneatrestaurant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.Objects;

import static com.damn.polito.commonresources.Utility.BitMapToString;

public class AddDish extends AppCompatActivity {
    private ImageView dish_image;
    private ImageButton camera;
    private EditText name, description, availabity, price;
    private Button save;
    private Bitmap profImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dish);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        dish_image = findViewById(R.id.profile_image);
        name = findViewById(R.id.edit_name_dish);
        description = findViewById(R.id.edit_desc_dish);
        price = findViewById(R.id.edit_price_dish);
        availabity = findViewById(R.id.edit_availabity_dish);
        description = findViewById(R.id.edit_desc_dish);
        save = findViewById(R.id.edit_save_dish);

        //Imposta la funzione del bottone "SALVA"
        save.setOnClickListener(v->{
            setActivityResult();
            finish();
        });
    }

    private void setActivityResult() {
        setResult(RESULT_OK, getActivityResult());
    }

    private Intent getActivityResult() {
        Intent i = new Intent();
        i.putExtra("name", name.getText().toString().trim());
        i.putExtra("description", description.getText().toString().trim());
        i.putExtra("price", price.getText().toString().trim());
        i.putExtra("availabity", availabity.getText().toString().trim());
//        if(profImg != null){
//            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//            pref.edit().putString("profile", BitMapToString(profImg)).apply();
//        }
        return i;
    }
}
