/**
 * 
 */
package com.nqmobile.livesdk.modules.mustinstall.features;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.mustinstall.MustInstallModule;

/**
 * 装机必备开关Feature
 * 
 * @author HouKangxi
 *
 */
public class MustInstallSwitchFeature extends AbsSwitchFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	private final int FEATURE = 101;

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public int getFeatureId() {
		return FEATURE;
	}

	@Override
	protected IModule getModule() {
		return ModuleContainer.getInstance().getModuleByName(
				MustInstallModule.MODULE_NAME);
	}

}
