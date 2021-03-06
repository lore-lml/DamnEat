package com.damn.polito.damneat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneat.dialogs.DialogType;
import com.damn.polito.damneat.dialogs.HandleDismissDialog;
import com.damn.polito.damneat.dialogs.SpinnerDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Cart extends AppCompatActivity implements HandleDismissDialog {

    private String time = "20:00";
    EditText note_et, time_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.order_summary);

        setContentView(R.layout.activity_cart);
        Intent i = getIntent();
        String list = i.getStringExtra("list");
        String name = i.getStringExtra("restaurant_name");
        String address = i.getStringExtra("restaurant_address");
        Double price = i.getDoubleExtra("price", -1);
        String price_list = i.getStringExtra("restaurant_dishprices");
        String ship = i.getStringExtra("restaurant_shipprice");
        int quantity = i.getIntExtra("restaurant_quantity", -1);

        TextView quantity_tv = findViewById(R.id.restaurant_dishnumber);
        TextView list_tv = findViewById(R.id.restaurant_dishlist);
        TextView list_price_tv = findViewById(R.id.restaurant_dishprice);
        TextView name_tv = findViewById(R.id.restaurant_name);
        TextView price_tv = findViewById(R.id.restaurant_totalprice);
        TextView address_tv = findViewById(R.id.restaurant_address);
        time_et = findViewById(R.id.restaurant_time);
        TextView ship_tv = findViewById(R.id.restaurant_shipprice);
        note_et = findViewById(R.id.restaurant_note);

        ImageView photo_image = findViewById(R.id.restaurant_image);


        String photo_bmp = i.getStringExtra("restaurant_photo");
        if(!photo_bmp.equals("NO_PHOTO")){
            Bitmap bmp = Utility.StringToBitMap(photo_bmp);

            photo_image.setImageBitmap(bmp);
        }
        quantity_tv.setText(String.valueOf(quantity));
        list_tv.setText(list);
        name_tv.setText(name);
        address_tv.setText(address);
        price_tv.setText(getString(R.string.order_price, price));
        list_price_tv.setText(price_list);
        ship_tv.setText(ship);

        time_et.setOnClickListener(v-> showSpinner());

        Button button = findViewById(R.id.confirm_button);
        button.setOnClickListener(v-> {
            setResultCart();
            finish();
        });
    }

    private void showSpinner() {
        FragmentManager fm = getSupportFragmentManager();
        SpinnerDialog spinner = new SpinnerDialog();
        spinner.show(fm, "Spinner Dialog");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setResultCart(){
        Intent i = new Intent();
        Log.d("result", note_et.getText().toString());
        i.putExtra("note", note_et.getText().toString());
        String time = time_et.getText().toString().trim();
        i.putExtra("time", time.isEmpty() ?
                "ASAP" : time);
        setResult(RESULT_OK, i);
    }

    @Override
    public void handleOnDismiss(DialogType type, String text) {
        if(type == DialogType.SpinnerDialog)
            time_et.setText(text);
    }
}
