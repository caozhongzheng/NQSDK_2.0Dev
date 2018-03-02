package com.nqmobile.livesdk.modules.apprate;

import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;

/**
 * @author chenyanmin
 *
 * 2015年1月28日
 */
public class AppRateModule extends AbsModule {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(AppRateModule.MODULE_NAME);
	
	public static final String MODULE_NAME = "AppRate";
	
	// ===========================================================
	// Fields
	// ===========================================================
	private AppRatePreference mPreference;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	public AppRateModule() {
		mPreference = AppRatePreference.getInstance();
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void init() {
		AppRateManager.getInstance().init();
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public boolean canEnabled() {
		return mPreference.isAppRateEnable();
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
		mPreference.setAppRateEnable(enabled);
		AppRateManager mgr = AppRateManager.getInstance();
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
