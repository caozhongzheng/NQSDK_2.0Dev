package com.nqmobile.livesdk.modules.font.features;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.font.FontModule;

/**
 * 字体开关Feature
 * 
 * @author liujiancheng
 *
 */
public class FontSwitchFeature extends AbsSwitchFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 7;

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================
	public FontSwitchFeature() {
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public int getFeatureId() {
		// TODO Auto-generated method stub
		return FEATURE;
	}

	@Override
	protected IModule getModule() {
		// TODO Auto-generated method stub
		return ModuleContainer.getInstance().getModuleByName(FontModule.MODULE_NAME);
	}

}
