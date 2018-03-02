/**
 * 
 */
package com.nqmobile.livesdk.modules.points.features;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.points.PointModule;

/**
 * 装机必备开关Feature
 * 
 * @author HouKangxi
 *
 */
public class PointSwitchFeature extends AbsSwitchFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	private final int FEATURE = 110;

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
				PointModule.MODULE_NAME);
	}

}
