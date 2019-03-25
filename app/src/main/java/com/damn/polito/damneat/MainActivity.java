package com.damn.polito.damneat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    public static final int CUSTOMER = 1;
    public static final int RESTAURANT = 2;
    public static final int DELIVERER = 3;

    private Button customer, restaurant, deliverer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        customer = findViewById(R.id.customer);
        restaurant = findViewById(R.id.restaurant);
        deliverer = findViewById(R.id.deliverer);

        customer.setOnClickListener(v-> {
            Intent intent = new Intent(this, Profile.class);
            intent.putExtra("type", CUSTOMER);
            startActivity(intent);
        });

        restaurant.setOnClickListener(v-> {
            Intent intent = new Intent(this, Profile.class);
            intent.putExtra("type", RESTAURANT);
            startActivity(intent);
        });

        deliverer.setOnClickListener(v-> {
            Intent intent = new Intent(this, Profile.class);
            intent.putExtra("type", DELIVERER);
            startActivity(intent);
        });
    }
}
