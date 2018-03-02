package com.nqmobile.livesdk.modules.apprate;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.text.format.DateUtils;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.receiver.LauncherResumeEvent;
import com.nqmobile.livesdk.commons.system.SystemFacade;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PackageUtils;


public class AppRateManager extends AbsManager {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(AppRateModule.MODULE_NAME);

	// ===========================================================
	// Fields
	// ===========================================================
	private static AppRateManager sInstance;

	private AppRatePreference mPreference;
	private SystemFacade mSystemFacade;

	private String mLastVersionCode;
	private int mDisplayCount;
	private long mInitTime;

	private boolean mIsDefaultLauncher;

	// ===========================================================
	// Constructors
	// ===========================================================
	private AppRateManager() {
		mPreference = AppRatePreference.getInstance();
		mSystemFacade = SystemFacadeFactory.getSystem();
    }

    public synchronized static AppRateManager getInstance() {
        if (sInstance == null) {
            sInstance = new AppRateManager();
        }
        return sInstance;
    }
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void init() {
		NqLog.i("init");
		
		Context context = ApplicationContext.getContext();
		String currentVersionName = PackageUtils.getVersionName(context);
		
		mLastVersionCode = mPreference.getVersionName();
		mDisplayCount = mPreference.getDisplayCount();
		mInitTime = mPreference.getInitTime();
		
		if (currentVersionName!=null && !currentVersionName.equals(mLastVersionCode)) {
			NqLog.i("new versionCode = "+currentVersionName);
			mDisplayCount = 0;
			mPreference.setDisplayCount(mDisplayCount);
			
			long now = mSystemFacade.currentTimeMillis();
			mInitTime = now;
			mPreference.setInitTime(now);
			
			mPreference.setVersionName(currentVersionName);
		}
		
		mIsDefaultLauncher = PackageUtils.isPreferredLauncher(context, context.getPackageName());
		
		NqLog.i("mDisplayCount="+mDisplayCount+",mLastVersionCode="+mLastVersionCode+",currentVersionCode="+currentVersionName+",mInitTime="+mInitTime+",mIsDefaultLauncher="+mIsDefaultLauncher);
		
		EventBus.getDefault().register(this);
	}

	// ===========================================================
	// Methods
	// ===========================================================
	public void onEvent(LauncherResumeEvent event){
		NqLog.i("LauncherResumeEvent");
		
		if (mDisplayCount > 0){
			NqLog.i("mDisplayCount="+mDisplayCount);
			return;
		}
		
		Context context = ApplicationContext.getContext();
		if (!NetworkUtils.isConnected(context)){
			return;
		}
		
		boolean needDisplay = false;
		
		boolean lastIsDefaultLauncher = mIsDefaultLauncher;
		mIsDefaultLauncher = PackageUtils.isPreferredLauncher(context, context.getPackageName());
		if (!lastIsDefaultLauncher && mIsDefaultLauncher){
			NqLog.i("change to DefaultLauncher");
			needDisplay = true;
		}
		
		if (SystemClock.elapsedRealtime() < 3 * DateUtils.MINUTE_IN_MILLIS){
			return;//刚开机时，不要弹框，避免弹框导致启动过程停顿住
		}
		
		long now = mSystemFacade.currentTimeMillis();
		long interval = Math.abs(now-mInitTime);
		if (interval > 1*DateUtils.DAY_IN_MILLIS){
			NqLog.i("after one day");
			needDisplay = true;
		}
		
		if (!needDisplay){
			return;
		}
		
		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				Context context = ApplicationContext.getContext();
				Intent intent = new Intent(context, AppRateActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				NqLog.i("start AppRateActivity");
				
				mPreference.setDisplayCount(mDisplayCount);
			}
			
		};
		mDisplayCount = mDisplayCount+1;//预先设置mDisplayCount，避免快速多次回到桌面的情况
		ApplicationContext.runOnUiThread(runnable, 1500);
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
