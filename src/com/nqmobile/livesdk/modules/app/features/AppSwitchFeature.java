/**
 * 
 */
package com.nqmobile.livesdk.modules.app.features;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.app.AppModule;

/**
 * 应用开关Feature
 * 
 * @author HouKangxi
 *
 */
public class AppSwitchFeature extends AbsSwitchFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 1;

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================
	public AppSwitchFeature() {
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public int getFeatureId() {
		return FEATURE;
	}

	protected IModule getModule() {
		return ModuleContainer.getInstance().getModuleByName(
				AppModule.MODULE_NAME);
	}
}
