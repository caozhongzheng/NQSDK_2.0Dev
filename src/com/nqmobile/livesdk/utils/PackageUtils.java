package com.nqmobile.livesdk.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.TextUtils;

import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.commons.receiver.LiveReceiver;

public class PackageUtils {
    public static List<String> getInstalledPackageNames(Context context) {
        List<String> result = new ArrayList<String>();

        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);

        for (PackageInfo info : packages) {
            result.add(info.packageName);
        }

        return result;
    }
    
    private static boolean checkInstallPermission(Context context) {
	    String permission = "android.permission.INSTALL_PACKAGES";
	    int res = context.checkCallingOrSelfPermission(permission);
	    return (res == PackageManager.PERMISSION_GRANTED);            
	}
    
    /**
     * 应用是否已安装
     *
     * @param packageName
     * @return
     */
	public static boolean isAppInstalled(Context context, String packageName) {
		boolean result = false;
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(packageName, 0);
			if (packageInfo != null) {// 暂时忽略versionCode
				result = true;
			}
		} catch (NameNotFoundException e) {
			// do nothing: the package is not installed
		} catch (Exception e) {
			NqLog.e(e);
		}
		return result;
	}
    /**
     * 获取应用名称
     *
     * @param packageName
     * @return
     */
    public static String getAppName(Context context,String packageName){
    	PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return info.loadLabel(pm).toString();
        } catch (Exception e) {
            NqLog.e(e);
        }
        return "";
    }
    public static String silentInstallApk(String apkAbsolutePath){
        String[] args = { "pm", "install", "-r", apkAbsolutePath };
        String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	FileUtil.closeStream(errIs);
        	FileUtil.closeStream(inIs);
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
    
    /**
     * 安装应用
     *
     * @param apkPath apk包的文件路径
     */
	public static void installApp(final Context context, final String apkPath) {
		if (TextUtils.isEmpty(apkPath)) {
			return;
		}
		
		if (checkInstallPermission(context)) {
			Intent intent = new Intent(LiveReceiver.ACTION_SILENT_INSTALL);
			intent.putExtra("apkPath", apkPath);
			context.sendBroadcast(intent);
		} else {
			File file = new File(apkPath);
			if (file.isFile() && file.exists()) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
				context.startActivity(intent);
			}
		}
	}
	
	/**
     * 启动应用
     *
     * @param pkgName 应用的包名
     * @author chenyanmin
     * @time 2014-1-23 上午11:39:38
     */
    public static void launchApp(Context context, String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return;
        }

        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
            context.startActivity(intent);
        } catch (Exception e) {
            NqLog.e(e);
        }
    }
    
    /*
     * 获取VersionName 
     */
	public static String getVersionName(Context context) 
	{  
	    try {  
	        PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);  
	        return pi.versionName;  
	    } catch (NameNotFoundException e) {  
	        NqLog.e(e);  
	    }  
	    
	    return "";
	}  
	
	
    /*
     * 是否是默认桌面  
     */
	public static boolean isPreferredLauncher(Context context, String packageName) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);
        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);
        // the packageName of your application
        List<ComponentName> preferredActivities = new ArrayList<ComponentName>();
        final PackageManager packageManager = (PackageManager) context.getPackageManager();
        // You can use name of your package here as third argument
        packageManager.getPreferredActivities(filters, preferredActivities, packageName);
        if (preferredActivities != null && preferredActivities.size() > 0) {
            return true;
        }
        return false;
    }
    
}
