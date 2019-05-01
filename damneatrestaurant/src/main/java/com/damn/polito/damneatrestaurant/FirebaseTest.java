package com.damn.polito.damneatrestaurant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.damn.polito.commonresources.beans.Dish;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class FirebaseTest extends AppCompatActivity {
private String mail = "ste@gelato.it";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_test);
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        /*
        Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.dishes_empty);

        myRef.setValue(dish);
        */

        Dish dish = new Dish("ssss", "ciao", 66, 66);
        DatabaseReference myRef = database.getReference("prova/"+dish.getName());
        myRef.runTransaction(new Transaction.Handler(){
            @Override
            public Transaction.Result doTransaction (MutableData currentData){
                if(currentData.getKey()==null){
                    //no default value for data, set one
                    currentData.setValue(dish);
                    return Transaction.success(currentData);
                }
                else{
                    //perform the update operations on data or deny modification
                    //currentData.setValue(dish);
                    return Transaction.abort();
                }

            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot currentData){
                //this method will be called once with the result of the transaction
            }

        });




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
