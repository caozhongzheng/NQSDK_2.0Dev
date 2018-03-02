/**
 * 
 */
package com.nqmobile.livesdk.utils;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author chenyanmin
 * @time 2014-2-26 上午8:41:17
 */
public class NetworkUtils {
	// 网络类型
	public static final int NETWORK_UNKNOWN = 0;
	public static final int NETWORK_WIFI = 1;
	public static final int NETWORK_MOBILE = 2;

	public static boolean isConnected(Context context) {
		boolean result = false;

		NetworkInfo ni = SystemFacadeFactory.getSystem(context).getActiveNetworkInfo();
		if (ni != null && ni.isConnected()) {
			result = true;
		}

		return result;
	}

	public static boolean isWifi(Context context) {
        Integer networkType = SystemFacadeFactory.getSystem(context).getActiveNetworkType();
        if (networkType != null && networkType.intValue() == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else {
            return false;
        }
	}
}
