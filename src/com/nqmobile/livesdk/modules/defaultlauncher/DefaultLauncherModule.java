package com.nqmobile.livesdk.modules.defaultlauncher;

import java.util.List;

import android.text.format.DateUtils;
import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;

/**
 * @author chenyanmin
 *
 * 2015年2月1日
 */
public class DefaultLauncherModule extends AbsModule {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(DefaultLauncherModule.MODULE_NAME);
	
	public static final String MODULE_NAME = "DefaultLauncher";
	
	// ===========================================================
	// Fields
	// ===========================================================
	private DefaultLauncherPreference mPreference;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	public DefaultLauncherModule() {
		mPreference = DefaultLauncherPreference.getInstance();
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void init() {
		DefaultLauncherManager.getInstance().init();
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public boolean canEnabled() {
		return mPreference.isDefaultLauncherEnable();
	}

	@Override
	public void onAppFirstInit(boolean enabled) {
		long time = System.currentTimeMillis() + 5* DateUtils.MINUTE_IN_MILLIS - DateUtils.DAY_IN_MILLIS;
		long guidetime = System.currentTimeMillis() + 5 * DateUtils.MINUTE_IN_MILLIS - DateUtils.DAY_IN_MILLIS * 7;
		mPreference.setLastShowDialogTime(time);
		mPreference.setLastGuideLauncherTime(guidetime);
	}

	@Override
	public List<IFeature> getFeatures() {
		return null;
	}

	@Override
	public List<IDataTable> getTables() {
		return null;
	}

	@Override
	protected void onEnabled(boolean enabled) {
		NqLog.i("enabled = "+ enabled);
		mPreference.setDefaultLauncherEnable(enabled);
		DefaultLauncherManager mgr = DefaultLauncherManager.getInstance();
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
