package com.damn.polito.damneat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Profile extends AppCompatActivity {

    private String defaultValue;
    private ImageView profileImage;
    private TextView name, mail, description, address;
    private TextView profAddress;
    private int type=1;
    private boolean editable = false, empty = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        defaultValue = getString(R.string.nullText);

        profileImage = findViewById(R.id.profile_image);
        name = findViewById(R.id.editText_name);
        mail = findViewById(R.id.editText_email);
        description = findViewById(R.id.editText_desc);
        address = findViewById(R.id.editText_address);
        profAddress = findViewById(R.id.profile_address);

        type = getIntent().getIntExtra("type", 1);

        initActivity();
    }

    private void initActivity() {

        //Setta il testo corretto e/o nasconde la view dell'indirizzo
        switch (type){
            case MainActivity.RESTAURANT:
                profAddress.setText(getString(R.string.profile_restaurant_address));
                break;
            case MainActivity.DELIVERER:
                profAddress.setVisibility(View.GONE);
                address.setVisibility(View.GONE);
                break;
            default:
                profAddress.setText(getString(R.string.profile_address));
                break;
        }

        //Se precedentemente salvate, ricarica le informazioni del profilo
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        List<String> values = new ArrayList<>(pref.getStringSet(typeId(),new HashSet<>()));

        if(!values.isEmpty()){
            name.setText(stringOrDefault(values.get(0)));
            mail.setText(stringOrDefault(values.get(1)));
            description.setText(stringOrDefault(values.get(2)));
            if(type != MainActivity.DELIVERER)
                address.setText(stringOrDefault(values.get(3)));
            empty = false;
        }
    }

    private void editProfile() {
        //Crea il corretto intent per l'apertura dell'activity EditProfile
        Intent intent = new Intent(this, EditProfile.class);
        intent.putExtra("type", type);

        //Se il profilo esisteva, passa le informazioni a EditProfile
        if(!empty){
            intent.putExtra("name", name.getText().toString().trim());
            intent.putExtra("mail", mail.getText().toString().trim());
            intent.putExtra("description", description.getText().toString().trim());
            intent.putExtra("address", address.getText().toString().trim());
        }
        startActivityForResult(intent, 1);
    }

    private String typeId(){
        switch (type){
            case MainActivity.CUSTOMER:
                return "customer";
            case MainActivity.RESTAURANT:
                return "restaurant";
            case MainActivity.DELIVERER:
                return "deliverer";
            default:
                return null;
        }
    }

    private String stringOrDefault(String s){
        return (s.trim().isEmpty() || s == null) ? defaultValue : s;
    }

    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK){
            String name = data.getStringExtra("name");
            String mail = data.getStringExtra("mail");
            String description = data.getStringExtra("description");

            Set<String> values = new LinkedHashSet<>();
            values.add(name);
            values.add(mail);
            values.add(description);

            if(type != MainActivity.DELIVERER) {
                String address = data.getStringExtra("address");
                values.add(address);
                this.address.setText(address);
            }
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            pref.edit().putStringSet(typeId(), values).apply();

            this.name.setText(name);
            this.mail.setText(mail);
            this.description.setText(description);

            empty = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.item_edit:
                editProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
