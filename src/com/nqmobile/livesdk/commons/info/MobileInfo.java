package com.nqmobile.livesdk.commons.info;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;

import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.utils.DisplayUtil;
import com.nqmobile.livesdk.utils.NQMultiCardUtil;

public class MobileInfo {
    private static final String NETWORK_TYPE_GSM = "gsm";
    private static final String NETWORK_TYPE_CDMA = "cdma";
    private static final String NETWORK_TYPE_WCDMA = "wcdma";
    
	private static String imsi = "";
	private static String imei = "";
	private static String lat = "";
	private static String lon = "";
    /** 手机SIM卡的运营商的国家码 **/
	private static String country = "";
    /** 手机SIM卡的运营商代码（MCC+MNC） **/
	private static String mcnc = "";
    /**  手机网络的运营商的国家码 **/
	private static String networkcountry = "";
    /** 手机网络的运营商代码（MCC+MNC） **/
	private static String networkmcnc = "";
	
	static {
		NqTest.init();
	}
	
	public static String getImsi(Context context) {
		if (TextUtils.isEmpty(imsi)){
			imsi = NQMultiCardUtil.getIMSI(context);
		}
		return imsi;
	}
	static void setImsi(String imsi) {
		MobileInfo.imsi = imsi;
	}
	
	public static String getImei(Context context) {
		if (TextUtils.isEmpty(imei)){
			imei = NQMultiCardUtil.getIMEI(context);
		} 
		return imei;
	}
	static void setImei(String imei) {
		MobileInfo.imei = imei;
	}
	
	public static String getLat() {
		return lat;
	}
	static void setLat(String lat) {
		MobileInfo.lat = lat;
	}
	
	public static String getLon() {
		return lon;
	}
	static void setLon(String lon) {
		MobileInfo.lon = lon;
	}
	
	public static String getCountry(Context context) {
		if (TextUtils.isEmpty(country)){
			country = tryGetSimCountry(context);
		}
		return country;
	}
	static void setCountry(String country) {
		MobileInfo.country = country;
	}
	
	public static String getMcnc(Context context) {
		if (TextUtils.isEmpty(mcnc)){
			mcnc = tryGetSimMcnc(context);
		}
		return mcnc;
	}
	static void setMcnc(String mcnc) {
		MobileInfo.mcnc = mcnc;
	}
	
	public static String getNetworkCountry(Context context) {
		if (TextUtils.isEmpty(networkcountry)) {
			networkcountry = tryGetNetworkCountry(context);
		}
		return networkcountry;
	}
	static void setNetworkCountry(String networkcountry) {
		MobileInfo.networkcountry = networkcountry;
	}
	
	public static String getNetworkMcnc(Context context) {
		if (TextUtils.isEmpty(networkmcnc)){
			networkmcnc = tryGetNetworkMcnc(context);
		}
		return networkmcnc;
	}
	static void setNetworkMcnc(String networkmcnc) {
		MobileInfo.networkmcnc = networkmcnc;
	}
	
	public static String getOSID() {
		int ver = Build.VERSION.SDK_INT;
		String osId = "";

		if (ver < 5) {
			osId = CommonDefine.ANDROID_OSID_15;
		} else if (ver < 11) {
			osId = CommonDefine.ANDROID_OSID_20;
		} else if (ver < 14) {
			osId = CommonDefine.ANDROID_OSID_30;
		} else {
			osId = CommonDefine.ANDROID_OSID_40;
		}

		return osId;
	}
	
	public static String getMobileLanguage(Context context){
		Locale locale = Locale.getDefault();
		return locale.getLanguage() + "-" + locale.getCountry();
	}
	
	/**
	 * 客户端分辨率Width
	 * @param context
	 * @return
	 */
	public static int getDisplayWidth(Context context){
        return DisplayUtil.getDisplayWidth(context);
	}
	
	/**
	 * 客户端分辨率Height
	 * @param context
	 * @return
	 */
	public static int getDisplayHeight(Context context){
		return DisplayUtil.getDisplayHeight(context);
	}
	
	public static String getDeviceName(){
		return Build.MANUFACTURER + "__" + Build.BRAND + "__" + Build.MODEL;//两个下划线分隔各字段
	}
	
	public static String getLocalMacAddress(Context context) {  
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
        WifiInfo info = wifi.getConnectionInfo();  
        return info.getMacAddress();  
    }
	
	public static String getNetworkType(Context context){
		String type = "";
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if(gprs == State.CONNECTED || gprs == State.CONNECTING){
			type = "GPRS";
		}
		
		if(wifi == State.CONNECTED || wifi == State.CONNECTING){
			type = "WIFI";
		}

		return type;
	}
	
	public static String getTimeZone(){
		Calendar calendar = Calendar.getInstance();
		TimeZone timeZone = calendar.getTimeZone();
		int a = timeZone.getRawOffset();
		int b = a / (1000*60*60);
		String c = String.valueOf(b);
		if (TextUtils.isEmpty(c)) {
			return "";
		} else {
			return c;
		}
	}
	
	public static String getCellid(Context context){
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int cellid = 0;
        try {
        	String networkType = getCellLocationType(context);
            if(networkType == NETWORK_TYPE_CDMA){
                CdmaCellLocation cdmalocation = (CdmaCellLocation) mTelephonyManager.getCellLocation();
                if (cdmalocation != null){
                	cellid = cdmalocation.getBaseStationId();
                }
            }else{
                GsmCellLocation gsmlocation = (GsmCellLocation) mTelephonyManager.getCellLocation();
                if (gsmlocation != null){
                	cellid = gsmlocation.getCid();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return String.valueOf(cellid);
    }

    private static String getCellLocationType(Context context){
        String networkType = "";
        TelephonyManager mM = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (mM.getNetworkType() == TelephonyManager.NETWORK_TYPE_CDMA
                || mM.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_A
                || mM.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_0
                || mM.getNetworkType() == TelephonyManager.NETWORK_TYPE_1xRTT
                || mM.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
            networkType = NETWORK_TYPE_CDMA;
        } else if (mM.getNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN){
            if (mM.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE
                    || mM.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS){
                networkType = NETWORK_TYPE_GSM;
            } else if (mM.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA
                    || mM.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA
                    || mM.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA
                    || mM.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS
                    ){
                networkType = NETWORK_TYPE_WCDMA;
            }
        }
        return networkType;
    }
	
	private static String tryGetSimCountry(Context context){
		String result = null;
		
		try {
			TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  
			result = mTelephonyManager.getSimCountryIso();  
			if (!TextUtils.isEmpty(result)){
				result =  result.toUpperCase(Locale.US);
			}
		} catch (Exception e) {
			NqLog.e(e);
		}  
        return result;
	}
	
	private static String tryGetSimMcnc(Context context){
		String result = null;
		
		try {
			TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  
			result = mTelephonyManager.getSimOperator();  
		} catch (Exception e) {
			NqLog.e(e);
		}  
        return result;
	}
	
	private static String tryGetNetworkCountry(Context context){
		String result = null;
		
		try {
			TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  
			result = mTelephonyManager.getNetworkCountryIso();  
			if (!TextUtils.isEmpty(result)){
				result =  result.toUpperCase(Locale.US);
			}
		} catch (Exception e) {
			NqLog.e(e);
		}  
        return result;
	}
	
	private static String tryGetNetworkMcnc(Context context){
		String result = null;
		
		try {
			TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  
			result = mTelephonyManager.getNetworkOperator();  
		} catch (Exception e) {
			NqLog.e(e);
		}  
        return result;
	}
}
