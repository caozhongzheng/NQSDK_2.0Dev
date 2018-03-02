package com.nqmobile.livesdk.commons.info;

import android.content.Context;
import android.text.TextUtils;

import com.nqmobile.livesdk.commons.ApplicationContext;

public class ProcessInfo {
    public static final String PROCESS_STORE = "com.nqmobile.live";
    public static final String PROCESS_PROVIDER = "com.nqmobile.live.provider";
    
	public static boolean isStoreProcess(Context context){
		String curProcessName = ApplicationContext.getCurrentProcessName();
		return PROCESS_STORE.equals(curProcessName);
	}
	
	public static boolean isProviderProcess(Context context){
		String curProcessName = ApplicationContext.getCurrentProcessName();
		return PROCESS_PROVIDER.equals(curProcessName);
	}
	
	public static boolean isLauncherProcess(Context context){
		String curProcessName = ApplicationContext.getCurrentProcessName();
		String launcherProcessName = context.getApplicationInfo().processName;
		if (TextUtils.isEmpty(launcherProcessName)){
			launcherProcessName = context.getPackageName();
		}
		return launcherProcessName.equals(curProcessName);
	}

}
