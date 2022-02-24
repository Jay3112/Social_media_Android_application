package com.example.jaypatel.homoheart;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Chk_Network {
    public boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean resultCon = activeNetworkInfo!= null && activeNetworkInfo.isConnected();
        return resultCon;
    }
}
