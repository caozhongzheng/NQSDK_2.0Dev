/**
 * 
 */
package com.nqmobile.livesdk.modules.lqwidget.features;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.lqwidget.LqWidgetModule;

/**
 * @author nq
 *
 */
public class LqWidgetFeature extends AbsSwitchFeature {

	@Override
	public int getFeatureId() {
		return 120;
	}

	@Override
	protected IModule getModule() {
		return ModuleContainer.getInstance().getModuleByName(LqWidgetModule.MODULE_NAME);
	}

}
