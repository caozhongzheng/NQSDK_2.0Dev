package com.nqmobile.livesdk.commons.mydownloadmanager.features;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadModule;
import com.nqmobile.livesdk.modules.batterypush.BatteryPushModule;
import com.nqmobile.livesdk.modules.font.FontModule;

/**
 * 字体开关Feature
 * 
 * @author johnson
 *
 */
public class MyDownloadSwitchFeature extends AbsSwitchFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 129;

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================
	public MyDownloadSwitchFeature() {
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public int getFeatureId() {
		return FEATURE;
	}

	@Override
	protected IModule getModule() {
		return ModuleContainer.getInstance().getModuleByName(MyDownloadModule.MODULE_NAME);
	}

}
