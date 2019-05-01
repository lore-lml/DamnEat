package com.damn.polito.damneat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.damn.polito.commonresources.Utility;

public class Cart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Intent i = getIntent();
        String list = i.getStringExtra("list");
        String name = i.getStringExtra("restaurant_name");
        String address = i.getStringExtra("restaurant_address");
        Double price = i.getDoubleExtra("price", -1);
        TextView list_tv = findViewById(R.id.editText_dishes);
        TextView name_tv = findViewById(R.id.editText_name);
        TextView price_tv = findViewById(R.id.editText_price);
        TextView address_tv = findViewById(R.id.editText_address);
        ImageView photo_image = findViewById(R.id.restaurant_image);
        String photo_bmp = i.getStringExtra("restaurant_photo");
        if(!photo_bmp.equals("NO_PHOTO")){
            Bitmap bmp = Utility.StringToBitMap(photo_bmp);

            photo_image.setImageBitmap(bmp);
        }
        list_tv.setText(list);

        name_tv.setText(name);
        address_tv.setText(address);
        price_tv.setText(String.format("%.2f", price) + "€");

        Button button = findViewById(R.id.confirm_button);
        button.setOnClickListener(v-> {
            setResult(RESULT_OK);
            finish();
        });
    }

}
