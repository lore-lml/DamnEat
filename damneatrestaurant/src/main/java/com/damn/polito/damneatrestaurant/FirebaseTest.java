package com.damn.polito.damneatrestaurant;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.damn.polito.damneatrestaurant.beans.Dish;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseTest extends AppCompatActivity {
private String mail = "ste@gelato.it";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_test);
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.dishes_empty);
        Dish dish = new Dish("sss", "kkkkk", 66, 66);
        myRef.setValue(dish);
//        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference db_restaurant = database.child("ristoranti").child(mail);
//
//        init_firebase(database, db_restaurant);
//
//        DatabaseReference  db_dishes = db_restaurant.child("piatti");
//        DatabaseReference  db_dishes_otd = db_restaurant.child("piatti_del_giorno");
//        DatabaseReference  db_info = db_restaurant.child("profile_info");

    }
//
//    private void init_firebase(DatabaseReference database, DatabaseReference db_restaurant){
//        database.child("ristoranti").setValue(mail);
//        db_restaurant.setValue("piatti");
//        db_restaurant.setValue("piatti_del_giorno");
//        db_restaurant.setValue("profile_info");
//
//    }
}
