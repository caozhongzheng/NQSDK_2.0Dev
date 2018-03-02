package com.nqmobile.livesdk.modules.defaultlauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.receiver.BootCompletedEvent;
import com.nqmobile.livesdk.commons.receiver.LauncherResumeEvent;
import com.nqmobile.livesdk.utils.PackageUtils;


public class DefaultLauncherManager extends AbsManager {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(DefaultLauncherModule.MODULE_NAME);

	// ===========================================================
	// Fields
	// ===========================================================
	private static DefaultLauncherManager sInstance;

	private DefaultLauncherPreference mPreference;

	private Context mContext;

	private boolean mReboot;

	// ===========================================================
	// Constructors
	// ===========================================================
	private DefaultLauncherManager() {
		mContext = ApplicationContext.getContext();
		mPreference = DefaultLauncherPreference.getInstance();
    }

    public synchronized static DefaultLauncherManager getInstance() {
        if (sInstance == null) {
            sInstance = new DefaultLauncherManager();
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

		EventBus.getDefault().register(this);
	}

	// ===========================================================
	// Methods
	// ===========================================================
	public void onEvent(BootCompletedEvent event){
		NqLog.i("onEvent: BootCompletedEvent");
		mReboot = true;
		
		Context context = ApplicationContext.getContext();
		String packageName = context.getPackageName();
		
		boolean isDefault = PackageUtils.isPreferredLauncher(context, packageName);
		if (!isDefault) {
			PackageManager pm = context.getPackageManager();
			Intent intent = pm.getLaunchIntentForPackage(packageName);
			if (intent != null) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				NqLog.i("start Live Launcher");
			}
		}
	}

	public void onEvent(LauncherResumeEvent event){
		mContext.sendBroadcast(new Intent("com.nqmobile.action.FORCE_HIDE"));
		long lastTime = mPreference.getLastShowDialogTime();
		long lastGuideTime = mPreference.getLastGuideLauncherTime();
		Log.i("gqf","reboot="+mReboot);
		if (!KP.a(mContext)) {
			if (System.currentTimeMillis() - lastGuideTime > 7 * DateUtils.DAY_IN_MILLIS && mReboot) {
				Log.i("gqf", "guideLuancher start!");
				mReboot = false;
				mPreference.setLastGuideLauncherTime(System.currentTimeMillis());
				KP.a(mContext, true);
			} else {
				if (System.currentTimeMillis() - lastTime > DateUtils.DAY_IN_MILLIS) {
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							Intent i = new Intent(mContext, SetLauncherActivity.class);
							i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							mContext.startActivity(i);
						}
					}, 3 * 1000L);
				}
			}
		}
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
