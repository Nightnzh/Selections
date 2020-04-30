package com.boardtek.selection.net;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.Nullable;

/**
 * 網路更動 監聽廣播
 * <p>
 * 參考來源: https://blog.csdn.net/wuyou1336/article/details/51958150
 * 更新(API-29): https://stackoverflow.com/questions/57277759/getactivenetworkinfo-is-deprecated-in-api-29
 */

public class WifiReceiver extends BroadcastReceiver {

	//網路更動事件
	private OnNetworkChangeListener onNetworkChangeListener;
	public interface OnNetworkChangeListener {
		void onNetworkChange(boolean isConnected, @Nullable WifiInfo wifiInfo);
	}
	public void setOnNetworkChangeListener(OnNetworkChangeListener onNetworkChangeListener) {
		this.onNetworkChangeListener = onNetworkChangeListener;
	}

	//當網路更動(收到廣播)
	@Override
	public void onReceive(Context context, Intent intent) {
		WifiInfo wifiInfo = null;
		WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if (wifiManager != null) {
			wifiInfo = wifiManager.getConnectionInfo();
		}
		onNetworkChangeListener.onNetworkChange(isNetworkAvailable(context), wifiInfo);
	}

	//網路是否有連接，API:29更新
	public boolean isNetworkAvailable(Context context) {
		if(context == null) return false;

		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				// API >= 23
				NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
				if (capabilities != null) {
					if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) { //移動網路
						return true;
					} else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) { //wi-fi
						return true;
					}  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) { //乙太網路
						return true;
					}
				}
			} else {
				// API < 23 (29後被棄用)
				NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
				if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
					return true;
				}
			}
		}
		return false;
	}

}