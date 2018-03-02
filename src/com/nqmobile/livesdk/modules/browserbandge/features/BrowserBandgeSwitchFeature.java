/**
 * 
 */
package com.nqmobile.livesdk.modules.browserbandge.features;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.browserbandge.BandgeManager;
import com.nqmobile.livesdk.modules.browserbandge.BrowserBandgeModule;
import com.nqmobile.livesdk.modules.browserbandge.model.Bandge;

/**
 * 浏览器角标开关
 * 
 * @author HouKangxi
 *
 */
public class BrowserBandgeSwitchFeature extends AbsSwitchFeature {

	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(BrowserBandgeModule.MODULE_NAME);
	
	private static final int FEATURE = 112;

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public int getFeatureId() {
		return FEATURE;
	}

	@Override
	public void disableFeature() {
		super.disableFeature();
		NqLog.e("mid");
		Bandge bandge = new Bandge();
		bandge.setJumpUrl("123");
		bandge.setLongExpirTime(0);
		bandge.setLongExpirTime(0);
		bandge.setStrId("122");
		bandge.setType(0);
		BandgeManager.getInstance(ApplicationContext.getContext())
				.updateBandge(bandge, 0);
	}

	@Override
	protected IModule getModule() {
		return ModuleContainer.getInstance().getModuleByName(
				BrowserBandgeModule.MODULE_NAME);
	}

}
