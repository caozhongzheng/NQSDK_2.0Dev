package com.nqmobile.livesdk.modules.batterypush.features;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.batterypush.BatteryPushModule;
import com.nqmobile.livesdk.modules.font.FontModule;

/**
 * 字体开关Feature
 * 
 * @author johnson
 *
 */
public class BatterypushSwitchFeature extends AbsSwitchFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 128;

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================
	public BatterypushSwitchFeature() {
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
		return ModuleContainer.getInstance().getModuleByName(BatteryPushModule.MODULE_NAME);
	}

}
