/**
 * 
 */
package com.nqmobile.livesdk.modules.appstub.features;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.appstub.AppStubModule;

/**
 * 虚框应用开关Feature
 * 
 * @author HouKangxi
 * 
 */
public class AppStubSwicthFeature extends AbsSwitchFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	private final int FEATURE = 113;

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

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
				AppStubModule.MODULE_NAME);
	}

	@Override
	public String getVersionTag() {
		return "-1";//-1表示普通定期联网，0 Lite升级，1 Live升级， 2 普通安装。目前只会上传-1。
	}
	
	

}
