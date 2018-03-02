package com.nqmobile.livesdk.utils;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.nqmobile.livesdk.commons.info.CommonDefine;

public class CommonMethod {
    private static String SDCARD_PATH = null;

	public static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xFF & messageDigest[i]);
				if (hex.length() == 1)
					hex = 0 + hex;
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String encryptMD5(String str) {
		String md = md5(str);
		String a = md.substring(24, 26);
		String b = md.substring(26, 28);
		String c = md.substring(28, 30);
		String d = md.substring(30, 32);
		int aa = Integer.valueOf(a, 16) % 10;
		int bb = Integer.valueOf(b, 16) % 10;
		int cc = Integer.valueOf(c, 16) % 10;
		int dd = Integer.valueOf(d, 16) % 10;
		String result = String.valueOf(aa) + String.valueOf(bb)
				+ String.valueOf(cc) + String.valueOf(dd);
		return result;
	}
	
    public static String getIMEI(Context context) {
		TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = mTelephonyMgr.getDeviceId();

        if (TextUtils.isEmpty(imei)){
            imei = "000000000000000";
        }
		return imei;
	}
    
    public static String getIMSI(Context context) {
		TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = mTelephonyMgr.getSubscriberId();

        if (TextUtils.isEmpty(imsi)){
            imsi = "000000000000000";
        }
		return imsi;
	}
    

	public static String getPlatformLanguage() {
		Locale locale = Locale.getDefault();
		String languageCode = locale.getLanguage();
		if (languageCode.equals("en")) {
			return "1"; // 英文1
		} else if (languageCode.equals("zh")) {
			return "31";// 中文31
		} else {
			return "1";
		}
	}
	
	public static String getCountryCode() {
		Locale locale = Locale.getDefault();
		String countryCode = locale.getCountry();
		return countryCode;
	}
	
	public static String getLocalIpAddress(Context context) {  
		try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                    	// 4.0系统，有可能获取到类似fe80::a20b:baff:fec8:3f1a%wlan0的ip地址，根据是否包含":"过滤掉
 
                   	String ip = inetAddress.getHostAddress().toString();
                    	if (ip != null && ip.contains(":")) {
                    		continue;
                    	}
                    	return ip;
                    }
                }
            }
        } catch (Exception ex) {
        }
        return null;
    } 
	
	public static boolean hasActiveNetwork(Context context){
		ConnectivityManager mConnectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE); 
		// 检查网络连接，如果无网络可用，就不需要进行连网操作等    
		NetworkInfo info = mConnectivity.getActiveNetworkInfo();    
		if (info == null/* || !info.isAvailable()*/) {   
			return false;    
		} else {
			return true;
		}
	}
	
	public static String getApnName(Context context) {
		String apnName = "";
		try {
			ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = manager.getActiveNetworkInfo();
			if(ni != null) {
				apnName = ni.getTypeName();	
				switch(ni.getType()){
				case ConnectivityManager.TYPE_WIFI:
					apnName = "wifi"; // using wifi now
					break;
				default:
					apnName = getCurrentApnName(context);
				}
			}
		}
		catch (Exception e) {
		}
		
		return apnName;
	}
	
	public static String getCurrentApnName(Context context) {
		String name = "";
		Cursor c = context.getContentResolver().query(
				Uri.parse("content://telephony/carriers/preferapn"), null,
				null, null, null);
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			name = c.getString(c.getColumnIndex("apn"));
		}

		if (c != null) {
			c.close();
		}
		
		return name;
	}
	
	public static String getMcnc(Context context){
		int mcc = 0;
		try {
			TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  
			// 返回值MCC + MNC  
			String operator = mTelephonyManager.getNetworkOperator();  
			mcc = Integer.parseInt(operator.substring(0, 3));  
		} catch (Exception e) {
			e.printStackTrace();
		}  
        return String.valueOf(mcc);
	}

    
	
	/**
	 * 客户端分辨率
	 * @param context
	 * @return
	 */
	public static String getDisplaySize(Context context){
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;
		String strPM = "手机屏幕分辨率为:" + screenWidth +"x"+screenHeight;
        return strPM;
	}
	
	
	public static String getAppUrl(Context context) {
		int serverRegion = getServerRegion(context);
		return serverRegion == 0 ? CommonDefine.APP_CN_URL : CommonDefine.APP_EN_URL;
	}

	public static int getServerRegion(Context context) {
		return PreferenceDataHelper.getInstance(context).getIntValue(PreferenceDataHelper.KEY_SERVERREGION);
	}
	
	public static String getWeatherUrl(Context context) {
		int serverRegion = getServerRegion(context);
		return serverRegion == 0 ? CommonDefine.WEATHER_CN_URL : CommonDefine.WEATHER_EN_URL;
	}
	
	public static String getLauncherUrl(Context context) {
		int serverRegion = getServerRegion(context);
		return serverRegion == 0 ? CommonDefine.APP_CN_URL : CommonDefine.APP_EN_URL;
	}
	
	public static boolean isSDCardOK(Context context) {
		return !StringUtil.isNullOrEmpty(getSDcardPath(context));
	}

	public static String getSDcardPath(Context context) {
//		SDCARD_PATH = getSDcardPathFromPref(context);
		if(SDCARD_PATH == null){
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				SDCARD_PATH = Environment.getExternalStorageDirectory().toString();
			
			if(SDCARD_PATH != null)
				PreferenceDataHelper.getInstance(context).setStringValue(PreferenceDataHelper.KEY_SDCARD_PATH, SDCARD_PATH);
		}
		return SDCARD_PATH;
	}
	
	public static String getSDcardPathFromPref(Context context){
		String path = PreferenceDataHelper.getInstance(context).getStringValue(PreferenceDataHelper.KEY_SDCARD_PATH);
		if(path == null || TextUtils.isEmpty(path)) {
			return null;
		}
		return path;
	}
	
	

}
