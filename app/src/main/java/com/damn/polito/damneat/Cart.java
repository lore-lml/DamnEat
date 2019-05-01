package com.damn.polito.damneat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class Cart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Intent i = getIntent();
        String list = i.getStringExtra("list");
        String name = i.getStringExtra("name");
        String address = i.getStringExtra("address");
        Double price = i.getDoubleExtra("price", -1);
        TextView list_tv = findViewById(R.id.editText_dishes);
        TextView name_tv = findViewById(R.id.editText_name);
        TextView price_tv = findViewById(R.id.editText_price);
        TextView address_tv = findViewById(R.id.editText_address);

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
