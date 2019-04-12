package com.damn.polito.damneatrestaurant;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Objects;

import static com.damn.polito.commonresources.Utility.*;

public class AddDish extends AppCompatActivity {
    private ImageView dish_image;
    private ImageButton galley;
    private EditText name, description, availabity, price;
    private Button save;
    private Bitmap dishImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_dish);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        dish_image = findViewById(R.id.dish_image);
        name = findViewById(R.id.edit_name_dish);
        description = findViewById(R.id.edit_desc_dish);
        price = findViewById(R.id.edit_price_dish);
        availabity = findViewById(R.id.edit_availabity_dish);
        description = findViewById(R.id.edit_desc_dish);
        save = findViewById(R.id.edit_save_dish);
        galley = findViewById(R.id.btn_gallery);


        //Imposta la funzione del bottone "SALVA"
        save.setOnClickListener(v->{
            if(checkField()){
                setActivityResult();
                finish();
            }
        });

        galley.setOnClickListener(v-> itemGallery());
    }
    private void itemGallery(){
        if(!checkPermissionFromDevice(REQUEST_PERM_WRITE_EXTERNAL))
            requestPermission(REQUEST_PERM_WRITE_EXTERNAL, PERMISSION_CODE_WRITE_EXTERNAL);
        else
            pickFromGallery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == IMAGE_GALLERY_REQUEST) {
            final Bundle extras = data.getExtras();
            if (extras != null) {
                dishImg = extras.getParcelable("data");
                dish_image.setImageBitmap(dishImg);
            }else{
                copyAndCrop(data.getData());
            }
        }
        if(requestCode == CROP_REQUEST){
            final Bundle extras = data.getExtras();
            if (extras != null) {
                dishImg = extras.getParcelable("data");
                dish_image.setImageBitmap(dishImg);
            }else{
                displayImage(data.getData());
            }
        }
    }

    private void displayImage(Uri data) {
        try {
            dishImg = MediaStore.Images.Media.getBitmap(getContentResolver(), data);
            dish_image.setImageBitmap(dishImg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyAndCrop(Uri uri) {
        assert uri != null;
        Uri newUri = getImageUrlWithAuthority(this, Objects.requireNonNull(uri));
        if(newUri != null) {
            cropImage(newUri);
        }
    }

    private void cropImage(Uri uri) {
        try{
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(uri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra("outputX", 889);
            cropIntent.putExtra("outputY", 500);
            cropIntent.putExtra("aspectX", 16);
            cropIntent.putExtra("aspectY", 9);
            cropIntent.putExtra("scaleUpIfNeeded", true);
            cropIntent.putExtra("return-data", true);

            startActivityForResult(cropIntent,CROP_REQUEST);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
        }
    }

    private void pickFromGallery() {
        Intent intent = galleryIntent16_9();
        startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
    }

    private void setActivityResult() {
        setResult(RESULT_OK, getActivityResult());
    }

    private Intent getActivityResult() {
        Intent i = new Intent();
        i.putExtra("name", name.getText().toString().trim());
        i.putExtra("description", description.getText().toString().trim());
        i.putExtra("price", price.getText().toString().trim());
        i.putExtra("availabity", availabity.getText().toString().trim());
        if(dishImg != null){
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            pref.edit().putString("dish_photo", BitMapToString(dishImg)).apply();
        }
        return i;
    }
    private boolean checkPermissionFromDevice(String permission) {

        int result = ContextCompat.checkSelfPermission(this, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(final String permission, final int permission_code) {
        ActivityCompat.requestPermissions(this,new String[]{
                permission
        }, permission_code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case PERMISSION_CODE_WRITE_EXTERNAL:
                if(!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    Toast.makeText(getApplicationContext(), getString(R.string.permission_denied),
                            Toast.LENGTH_SHORT).show();
                else
                    pickFromGallery();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(checkChanges())
                    showWarning(this, checkField(), getActivityResult());
                else
                    this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(checkChanges())
            // Facciamo comparire il messagio solo se sono stati cambiati dei campi
            showWarning(this, checkField(), getActivityResult());
        else
            this.finish();
    }

    private boolean checkChanges() {
        String name = this.name.getText().toString();
        if(!name.equals(""))
            return true;

        String mail = this.description.getText().toString();
        if(!(mail.equals("")))
            return true;

        String description = this.price.getText().toString();
        if(!(description.equals("")))
            return true;

        String address = this.availabity.getText().toString();
        return (!(address.equals("")));

    }

    private boolean checkField() {
        String name = this.name.getText().toString();

        //Controllo sui campi vuoti
        if(name.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.empty_name), Toast.LENGTH_SHORT).show();
            this.name.requestFocus();
            return false;
        }
        String description = this.description.getText().toString();
        if(description.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.empty_desc), Toast.LENGTH_SHORT).show();
            this.description.requestFocus();
            return false;
        }

        String availabity = this.availabity.getText().toString();
        if(availabity.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.empty_availabity), Toast.LENGTH_SHORT).show();
            this.availabity.requestFocus();
            return false;
        }
        if(Integer.parseInt(availabity)<0){
            Toast.makeText(this, getString(R.string.availabity_too_low), Toast.LENGTH_SHORT).show();
            this.availabity.requestFocus();
            return false;
        }
        String price = this.price.getText().toString();
        if(price.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.empty_price), Toast.LENGTH_SHORT).show();
            this.price.requestFocus();
            return false;
        }
        if(Float.parseFloat(price)<=0){
            Toast.makeText(this, getString(R.string.price_too_low), Toast.LENGTH_SHORT).show();
            this.price.requestFocus();
            return false;
        }



        return true;
    }


}
