package com.damn.polito.damneatrestaurant.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.damn.polito.commonresources.Utility;
import com.damn.polito.commonresources.beans.Deliverer;
import com.damn.polito.damneatrestaurant.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomInfoMarkerAdapter implements GoogleMap.InfoWindowAdapter{

    private final View mWindow;
    private Context mContext;
    private Deliverer deliverer;

    public CustomInfoMarkerAdapter(Context context, Deliverer deliverer) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_marker, null);
        this.deliverer = deliverer;
    }

    private void RenderWindowText (Marker marker, View view) {
        String title = marker.getTitle();
        TextView tvTitle = (TextView) view.findViewById(R.id.deliverer_name_marker);
        TextView tvDescriptiom = (TextView) view.findViewById(R.id.deliverer_description_marker);
        TextView tvPhone = (TextView) view.findViewById(R.id.deliverer_phone_number_marker);
        TextView tvDistance = (TextView) view.findViewById(R.id.deliverer_distance_marker);
        CircleImageView delivererImage = view.findViewById(R.id.deliverer_img_marker);
        Bitmap img = Utility.StringToBitMap(deliverer.getBitmapProf());

        if(img != null)
            delivererImage.setImageBitmap(img);
//        else
//            delivererImage.setImageResource(R.drawable.profile_sample);

//        if (!title.equals("")) {
//            tvTitle.setText(title);
//        }

//        String snippet = marker.getTitle();
//        TextView tvSnippet = (TextView) view.findViewById(R.id.deliverer_name_marker);

//        if (!snippet.equals("")) {
//            tvSnippet.setText(snippet);
//        }

        tvTitle.setText(deliverer.getName());
        tvDescriptiom.setText(deliverer.getDescription());
        tvPhone.setText(deliverer.getPhone());
        tvDistance.setText(mContext.getString(R.string.distance_km, (double)deliverer.distance()/1000));

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
