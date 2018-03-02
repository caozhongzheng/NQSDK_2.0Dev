package com.nqmobile.livesdk.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class Tools {
	private static long lastClickTime;

	public static boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		if (time - lastClickTime < 500) {
			return true;
		}
		lastClickTime = time;
		return false;
	}
	
	public static boolean stringEquals(String str1, String str2) {
		if (str1 != null && str2 != null)
			return str1.trim().equals(str2.trim());
		else if (str1 != null || str2 != null)
			return false;
		else
			return false;
	}
	
	public static boolean stringEquals(String str1, int str2) {
		return stringEquals(str1, String.valueOf(str2));
	}
	
	public static boolean stringEquals(int str1, String str2) {
		return stringEquals(String.valueOf(str1), str2);
	}
	
	public static boolean stringEqualsIgnoreCase(String str1, String str2) {
		if (str1 != null && str2 != null)
			return str1.trim().equalsIgnoreCase(str2.trim());
		else if (str1 != null || str2 != null)
			return false;
		else
			return false;
	}

	public static boolean isEmpty(String str) {
		if (isNotNull(str)) {
			str = str.replaceAll("\n", "");
			str = str.replaceAll("\r", "");
			str = str.replaceAll("\t", "");
		}
		return isNull(str);
	}
	
	public static boolean isNotNull(String str) {
		return !isNull(str);
	}
	
	public static boolean isNull(String str) {
		if (str == null || str.trim().length() < 1
				|| str.toLowerCase().trim().equals("null"))
			return true;
		else
			return false;
	}
	
    public static int getVersionCode(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return info.versionCode;
        } catch (Exception e) {
        }
        return -1;
    }
}
