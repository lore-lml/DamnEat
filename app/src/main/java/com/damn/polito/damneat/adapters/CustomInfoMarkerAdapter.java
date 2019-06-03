package com.damn.polito.damneat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.damn.polito.damneat.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomInfoMarkerAdapter implements GoogleMap.InfoWindowAdapter{

    private final View mWindow;
    private String name, description;
    private Bitmap photo;
    private Double distance;
    private Context ctx;

    public CustomInfoMarkerAdapter(Context context, String name, Bitmap photo, Double distance) {
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_marker, null);
        this.name = name;
        this.photo = photo;
        this.description = "";
        ctx = context;
        this.distance = distance;
    }

    public CustomInfoMarkerAdapter(Context context, String name, Bitmap photo, String description) {
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_marker, null);
        this.name = name;
        this.description = description;
        this.photo = photo;
    }

    private void RenderWindowText (Marker marker, View view) {
        String title = marker.getTitle();
        TextView tvTitle = (TextView) view.findViewById(R.id.name_marker);
        TextView tvDescriptiom = (TextView) view.findViewById(R.id.description_marker);
        TextView tvDistance = (TextView) view.findViewById(R.id.marker_distance);
        CircleImageView photoCard = view.findViewById(R.id.img_marker);

        if(photo != null)
            photoCard.setImageBitmap(this.photo);

        tvTitle.setText(name);
        if (description.equals("")) tvDescriptiom.setText(R.string.del);
        else tvDescriptiom.setText(description);


        if(distance == null)
            tvDistance.setVisibility(View.GONE);
        else{
            double d = distance/1000;
            if((int)d > 0)
                tvDistance.setText(ctx.getString(R.string.distance_km, d));
            else
                tvDistance.setText(ctx.getString(R.string.distance_meter, (int)((double)this.distance)));
        }


    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        RenderWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        RenderWindowText(marker, mWindow);
        return mWindow;
    }
}
