package com.nqmobile.livesdk.modules.stat.feature;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.stat.StatModule;
import com.nqmobile.livesdk.modules.stat.StatPreference;

public class UploadLogSwitchFeature extends AbsSwitchFeature {
	
	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 125;
	
	// ===========================================================
	// Fields
	// ===========================================================
	private StatPreference mPreference;
	
	// ===========================================================
	// Constructor
	// ===========================================================
	public UploadLogSwitchFeature() {
		mPreference = StatPreference.getInstance();
	};
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public int getFeatureId() {
		return FEATURE;
	}

	@Override
	protected IModule getModule() {
		return ModuleContainer.getInstance().getModuleByName(StatModule.MODULE_NAME);
	}

	@Override
	public boolean isEnabled() {
		return mPreference.isUploadLogEnabled();
	}
	
	@Override
	public void enableFeature() {
		mPreference.setUploadLogEnabled(true);
	}
	
	@Override
	public void disableFeature() {
		mPreference.setUploadLogEnabled(false);		
	}
}
