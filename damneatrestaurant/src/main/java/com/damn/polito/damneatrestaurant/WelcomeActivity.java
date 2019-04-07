package com.damn.polito.damneatrestaurant;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_reservations:
                    //mTextMessage.setText(R.string.title_home);
//                    Intent intent1 = new Intent(WelcomeActivity.this, DishActivity.class);
//                    startActivity(intent1);
                    return true;
                case R.id.nav_menu:
                    /*mTextMessage.setText(R.string.title_dashboard);*/
                    Intent intent2 = new Intent(WelcomeActivity.this,OrderActivity.class);
                    startActivity(intent2);
                    return true;
                case R.id.nav_profile:
                    //mTextMessage.setText(R.string.title_notifications);
                    Intent intent3 = new Intent(WelcomeActivity.this,Profile.class);
                    startActivity(intent3);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dishes_fragment);

        Intent intent1 = new Intent(WelcomeActivity.this, DishActivity.class);
        startActivity(intent1);

//        mTextMessage = (TextView) findViewById(R.id.message);
//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setSelectedItemId(R.id.nav_menu);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

}
