package com.damn.polito.damneat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class Login extends AppCompatActivity {

    List<AuthUI.IdpConfig> providers;
    private final int REQUEST_CODE = 101;
    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        b = findViewById(R.id.btn_sign_out);
        b.setOnClickListener(v->{
                    // logout
            AuthUI.getInstance()
                    .signOut(Login.this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            b.setEnabled(false);
                            shownSignInOptions();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
            //startActivity(new Intent(this, Welcome.class))

        );

        //init providers
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
        shownSignInOptions();

    }

    private void shownSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.Background)
                        .build(), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode==RESULT_OK){
                //get user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //show email on toast
                Toast.makeText(this, user.getEmail().toString() + "   " + user.getUid(), Toast.LENGTH_LONG).show();
                //set button signout
                b.setEnabled(true);

            }
            else{
                Toast.makeText(this, response.getError().getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
