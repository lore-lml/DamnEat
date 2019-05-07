package com.damn.polito.commonresources;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetConnection {

    public static boolean haveInternetConnection(Context ctx) {

        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm == null) return false;

        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if(nInfo == null) return false;

        return nInfo.isConnected();
    }
}
