package com.nqmobile.livesdk.modules.installedrecommend.feature;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.installedrecommend.InstalledRecommendModule;


/**
 * 安装后关联推荐开关
 * 
 * @author liujiancheng
 *
 */
public class InstalledRecommendSwitchFeature extends AbsSwitchFeature {

	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 118;

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
				InstalledRecommendModule.MODULE_NAME);
	}

}
