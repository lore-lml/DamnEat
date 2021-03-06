package com.damn.polito.damneatrestaurant;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.damneatrestaurant.dialogs.CategoryDialog;
import com.damn.polito.damneatrestaurant.dialogs.HandleDismissDialog;
import com.damn.polito.damneatrestaurant.dialogs.OpeningDialog;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import static com.damn.polito.commonresources.Utility.BitMapToString;
import static com.damn.polito.commonresources.Utility.CROP_REQUEST;
import static com.damn.polito.commonresources.Utility.IMAGE_GALLERY_REQUEST;
import static com.damn.polito.commonresources.Utility.PERMISSION_CODE_CAMERA;
import static com.damn.polito.commonresources.Utility.PERMISSION_CODE_WRITE_EXTERNAL;
import static com.damn.polito.commonresources.Utility.REQUEST_IMAGE_CAPTURE;
import static com.damn.polito.commonresources.Utility.REQUEST_PERM_CAMERA;
import static com.damn.polito.commonresources.Utility.REQUEST_PERM_WRITE_EXTERNAL;
import static com.damn.polito.commonresources.Utility.StringToBitMap;
import static com.damn.polito.commonresources.Utility.cameraIntent;
import static com.damn.polito.commonresources.Utility.galleryIntent;
import static com.damn.polito.commonresources.Utility.getImageUrlWithAuthority;
import static com.damn.polito.commonresources.Utility.showWarning;

public class EditProfile extends AppCompatActivity implements HandleDismissDialog {

    private ImageView profile;
    private ImageButton camera;
    private EditText name, mail, description, address, phone, opening, categories, shipPrice;
    private Button save;
    private Bitmap profImg;

    // VARIABILI PER VERIFICARE SE SONO STATE EFFETTUATE MODIFICHE
    private String sName, sMail, sDesc, sAddress, sPhone, sOpening, sCategories, sShipPrice;
    private Bitmap profImgPrec;
    private boolean addressFound = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.edit_profile);

        profile = findViewById(R.id.profile_image);
        camera = findViewById(R.id.btn_camera);
        name = findViewById(R.id.edit_name);
        mail = findViewById(R.id.edit_mail);
        phone = findViewById(R.id.edit_phone);
        description = findViewById(R.id.edit_desc);
        address = findViewById(R.id.edit_address);
        opening = findViewById(R.id.edit_opening);
        save = findViewById(R.id.edit_save);
        categories = findViewById(R.id.edit_category);
        shipPrice = findViewById(R.id.edit_shipprice);

        init();
    }

    private void init() {
        //Recupera le informazioni passate da ProfileFragment
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Intent intent = getIntent();

        sName = intent.getStringExtra("name");
        sMail = intent.getStringExtra("mail");
        sPhone = intent.getStringExtra("phone");

        if(sPhone != null && !sPhone.isEmpty())
            sPhone = sPhone.substring(4);

        sDesc = intent.getStringExtra("description");
        sAddress = intent.getStringExtra("address");
        sOpening = intent.getStringExtra("opening");
        sCategories = intent.getStringExtra("categories");
        sShipPrice = intent.getStringExtra("shipprice");
        if(sShipPrice == null || sShipPrice.equals(getString(R.string.price_free)))
            sShipPrice = "";
        String bitmapString = pref.getString("profile", null);
        if(bitmapString != null) {
            profImg = StringToBitMap(bitmapString);
            profImgPrec = profImg;
            pref.edit().remove("profile").apply();
        }

        name.setText(sName);
        mail.setText(sMail);
        phone.setText(sPhone);
        description.setText(sDesc);
        address.setText(sAddress);
        opening.setText(sOpening);
        categories.setText(sCategories);
        shipPrice.setText(sShipPrice);
        if(profImg != null){
            profile.setImageBitmap(profImg);
        }


        //Imposta la funzione del bottone "SALVA"
        save.setOnClickListener(v->{
            if(address.hasFocus())
                addressFound = checkAddress();
            if(checkField()){
                setActivityResult();
                finish();
            }
        });

        camera.setOnClickListener(v->{
            PopupMenu pop = new PopupMenu(this, camera);
            pop.getMenuInflater().inflate(R.menu.context_camera_menu, pop.getMenu());
            pop.setOnMenuItemClickListener(item->{
                switch (item.getItemId()){
                    case R.id.item_snap:
                        itemCamera();
                        return true;
                    case R.id.item_gallery:
                        itemGallery();
                        return true;
                    default:
                        return super.onContextItemSelected(item);
                }
            });
            pop.show();
        });

        opening.setOnClickListener(v->{
            FragmentManager fm = getSupportFragmentManager();
            OpeningDialog opening = new OpeningDialog();
            opening.setDaysText(this.opening.getText().toString());
            opening.show(fm, "Opening Dialog");
        });

        categories.setOnClickListener(v->{
            FragmentManager fm = getSupportFragmentManager();
            CategoryDialog category = new CategoryDialog();
            category.setCategories(this.categories.getText().toString());
            category.show(fm, "Category Dialog");
        });

        address.setOnFocusChangeListener((view, isFocused) -> {
            if(!isFocused)
                addressFound = checkAddress();
        });
    }

    private boolean checkAddress() {
        boolean outcome = false;
        Geocoder geocoder = new Geocoder(this, Locale.ITALY);
        String address = this.address.getText().toString();

        try {
            if(geocoder.getFromLocationName(address, 1).size() > 0)
                outcome = true;

        } catch (IOException ignored) {}

        this.address.setCompoundDrawables( null, null, getProperAddressDrawable(outcome), null );
        return outcome;
    }

    private Drawable getProperAddressDrawable(boolean outcome){
        Drawable image = outcome ? getDrawable(R.drawable.ic_check_green) : getDrawable(R.drawable.ic_close);
        assert image != null;
        int h = image.getIntrinsicHeight();
        int w = image.getIntrinsicWidth();
        image.setBounds( 0, 0, w, h );
        return image;
    }

    private void itemCamera() {
        if(!checkPermissionFromDevice(REQUEST_PERM_CAMERA))
            requestPermission(REQUEST_PERM_CAMERA, PERMISSION_CODE_CAMERA);
        else
            photoShot();
    }

    private void photoShot() {
        Intent intent = cameraIntent();
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void itemGallery(){
        if(!checkPermissionFromDevice(REQUEST_PERM_WRITE_EXTERNAL))
            requestPermission(REQUEST_PERM_WRITE_EXTERNAL, PERMISSION_CODE_WRITE_EXTERNAL);
        else
            pickFromGallery();
    }

    private void pickFromGallery() {
        Intent intent = galleryIntent();
        startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
    }

    private void setActivityResult(){
        setResult(RESULT_OK, getActivityResult());
    }

    private Intent getActivityResult(){
        Intent i = new Intent();
        i.putExtra("name", name.getText().toString().trim());
        i.putExtra("mail", mail.getText().toString().trim());
        i.putExtra("phone", getString(R.string.phone_prefix) + " " +phone.getText().toString().trim());
        i.putExtra("description", description.getText().toString().trim());
        i.putExtra("address", address.getText().toString().trim());
        i.putExtra("opening", opening.getText().toString().trim());
        i.putExtra("categories", categories.getText().toString().trim());

        String price = shipPrice.getText().toString().trim();
        if(price.isEmpty())
            price = getString(R.string.price_free);
        i.putExtra("shipprice", price);
        if(checkChanges())
            i.putExtra("hasChanged", true);
        if(profImg != null){
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            pref.edit().putString("profile", BitMapToString(profImg)).apply();
        }

        return i;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == IMAGE_GALLERY_REQUEST) {
            final Bundle extras = data.getExtras();
            if (extras != null) {
                profImg = extras.getParcelable("data");
                profile.setImageBitmap(profImg);
            }else{
                copyAndCrop(data.getData());
            }
        }
        if(requestCode == CROP_REQUEST){
            final Bundle extras = data.getExtras();
            if (extras != null) {
                profImg = extras.getParcelable("data");
                profile.setImageBitmap(profImg);
            }else{
                displayImage(data.getData());
            }
        }
    }

    private void displayImage(Uri data) {
        try {
            profImg = MediaStore.Images.Media.getBitmap(getContentResolver(), data);
            profile.setImageBitmap(profImg);
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
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("scaleUpIfNeeded", true);
            cropIntent.putExtra("return-data", true);

            startActivityForResult(cropIntent,CROP_REQUEST);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
        }
    }

    private boolean checkField() {
        String name = this.name.getText().toString();

        //Controllo sui campi vuoti
        if(name.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.empty_name), Toast.LENGTH_SHORT).show();
            this.name.requestFocus();
            return false;
        }

        //Controlla se la stringa inserita sia una mail valida
        String mail = this.mail.getText().toString();
        String regex = Utility.Regex.MAIL;
        if(mail.trim().toLowerCase().isEmpty()){
            Toast.makeText(this, getString(R.string.empty_mail), Toast.LENGTH_SHORT).show();
            this.mail.requestFocus();
            return false;
        }
        else if(!mail.matches(regex)){
            Toast.makeText(this, getString(R.string.invalid_mail), Toast.LENGTH_SHORT).show();
            this.mail.requestFocus();
            return false;
        }

        String phone = this.phone.getText().toString().trim();
        if(phone.isEmpty()){
            Toast.makeText(this, getString(R.string.empty_phone), Toast.LENGTH_SHORT).show();
            this.phone.requestFocus();
            return false;
        }else if(!phone.matches(Utility.Regex.MOBILE_PHONE) && !phone.matches(Utility.Regex.TELEPHONE)){
            Toast.makeText(this, getString(R.string.invalid_phone), Toast.LENGTH_SHORT).show();
            this.phone.requestFocus();
            return false;
        }

        String description = this.description.getText().toString();
        if(description.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.empty_desc), Toast.LENGTH_SHORT).show();
            this.description.requestFocus();
            return false;
        }

        String address = this.address.getText().toString();
        if(address.trim().isEmpty()){
            this.address.requestFocus();
            Toast.makeText(this, getString(R.string.empty_address), Toast.LENGTH_SHORT).show();
            return false;
        }else if(!addressFound){
            this.address.requestFocus();
            return false;
        }

        String opening = this.opening.getText().toString();
        if(opening.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.empty_opening), Toast.LENGTH_SHORT).show();
            this.opening.requestFocus();
            return false;
        }

        String categories = this.categories.getText().toString();
        if(categories.trim().isEmpty()){
            Toast.makeText(this, getString(R.string.empty_categories), Toast.LENGTH_SHORT).show();
            this.categories.requestFocus();
            return false;
        }

        return true;
    }

    private boolean checkChanges() {
        if(!addressFound)
            return false;

        String name = this.name.getText().toString();
        if(!name.equals(sName))
            return true;

        String mail = this.mail.getText().toString();
        if(!(mail.equals(sMail)))
            return true;

        String phone = this.phone.getText().toString();
        if(!(phone.equals(sPhone)))
            return true;

        String description = this.description.getText().toString();
        if(!(description.equals(sDesc)))
            return true;

        String address = this.address.getText().toString();
        if(!(address.equals(sAddress)))
            return true;

        String opening = this.opening.getText().toString();
        if(!(opening.equals(sOpening)))
            return true;

        String categories = this.categories.getText().toString();
        if(!(categories.equals(sCategories)))
            return true;

        String shipPrice = this.shipPrice.getText().toString();
        if(!(shipPrice.equals(sShipPrice)))
            return true;

        if(profImg == null) return false;
        return (!(profImg.equals(profImgPrec)));

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
            case PERMISSION_CODE_CAMERA:
                if(!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    Toast.makeText(getApplicationContext(), getString(R.string.permission_denied),
                            Toast.LENGTH_SHORT).show();
                else
                    photoShot();

                break;

            case PERMISSION_CODE_WRITE_EXTERNAL:
                if(!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    Toast.makeText(getApplicationContext(), getString(R.string.permission_denied),
                            Toast.LENGTH_SHORT).show();
                else
                    pickFromGallery();
        }
    }

    @Override
    public void onBackPressed() {
        if(checkChanges()) {
            // Facciamo comparire il messagio solo se sono stati cambiati dei campi
            if (address.hasFocus())
                addressFound = checkAddress();
            showWarning(this, checkField(), getActivityResult());
        }
        else {
            setResult(RESULT_CANCELED);
            this.finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(profImg != null)
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("profile", BitMapToString(profImg)).apply();
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String bitmap = pref.getString("profile", null);
        if(bitmap != null) {
            profImg = StringToBitMap(bitmap);
            profile.setImageBitmap(profImg);
            pref.edit().remove("profile").apply();
        }
    }

    @Override
    public void handleOnDismiss(com.damn.polito.damneatrestaurant.dialogs.DialogType type, String text) {
        switch (type){
            case Opening:
                opening.setText(text);
                break;
            case Categories:
                categories.setText(text);
                break;
        }
    }
}

