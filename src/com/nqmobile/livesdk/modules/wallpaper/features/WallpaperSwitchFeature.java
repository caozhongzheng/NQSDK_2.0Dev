/**
 * 
 */
package com.nqmobile.livesdk.modules.wallpaper.features;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperModule;

/**
 * 壁纸开关
 * 
 * @author HouKangxi
 *
 */
public class WallpaperSwitchFeature extends AbsSwitchFeature {

	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 3;

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
				WallpaperModule.MODULE_NAME);
	}

}
