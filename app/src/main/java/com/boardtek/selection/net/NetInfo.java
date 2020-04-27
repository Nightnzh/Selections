package com.boardtek.selection.net;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

public class NetInfo{

    private Application application;

    public NetInfo(Application application){
        this.application = application;
    }

    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo networkInfo =  cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
