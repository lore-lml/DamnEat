package com.damn.polito.commonresources;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class FirebaseLogin {
    private static List<AuthUI.IdpConfig> providers;
    public final static int REQUEST_CODE = 707;

    public static void init(){
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
    }

    public static void logout(Activity ctx){
        // logout
        AuthUI.getInstance()
                .signOut(ctx)
                .addOnCompleteListener(task -> {
                    //b.setEnabled(false);
                    clearData(ctx);
                    //sshownSignInOptions(ctx);
                }).addOnFailureListener(e -> Toast.makeText(ctx, ctx.getString(R.string.logout_error), Toast.LENGTH_LONG).show());
    }
    public static void shownSignInOptions(Activity ctx) {
        ctx.startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.Background)
                        .build(), REQUEST_CODE);
    }

    public static void storeData(FirebaseUser user, Context ctx){
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        pref.putString("dbkey", user.getUid());
        pref.apply();
    }

    public static void clearData(Context ctx){
        SharedPreferences.Editor pref = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        pref.remove("dbkey");
        pref.remove("clientaddress");
        pref.remove("clientname");
        pref.remove("clientphone");
        pref.remove("clientmail");
        pref.remove("address");
        pref.remove("name");
        pref.remove("phone");
        pref.remove("mail");
        pref.remove("description");
        pref.remove("opening");
        pref.remove("categories");
        pref.remove("shipprice");
        pref.remove("profile");
        pref.apply();
    }


}
