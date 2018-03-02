package com.nqmobile.livesdk.commons;

import com.nqmobile.livesdk.utils.ProcessUtils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class ApplicationContext {
	private static Context sContext;
	private static boolean sIsLauncherReady;
	private static String sCurrentProcessName;
	private static Handler sHandler;

	public static Context getContext() {
		return sContext;
	}

	public static void setContext(Context context) {
		if (sContext != null || context == null) {
			return;
		}
		ApplicationContext.sContext = context.getApplicationContext();
		sCurrentProcessName = ProcessUtils.getCurrentProcessName(sContext);
		if (Looper.getMainLooper() == Looper.myLooper()){
			sHandler = new Handler();
		}
	}

	public static boolean isLauncherReady() {
		return sIsLauncherReady;
	}

	public static void setLauncherReady(boolean isLauncherReady) {
		sIsLauncherReady = isLauncherReady;
	}
	
	public static String getCurrentProcessName(){
		return sCurrentProcessName;
	}
	
	public static void runOnUiThread(Runnable runnable){
		if (sHandler != null){
			sHandler.post(runnable);
		}
	}
	
	public static void runOnUiThread(Runnable runnable, long delayMillis){
		if (sHandler != null){
			sHandler.postDelayed(runnable, delayMillis);
		}
	}
}
