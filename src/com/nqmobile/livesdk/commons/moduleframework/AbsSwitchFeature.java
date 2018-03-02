package com.nqmobile.livesdk.commons.moduleframework;

import java.util.Map;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;


/**
 * 抽象开关Feature
 * 
 * @author HouKangxi
 *
 */
public abstract class AbsSwitchFeature extends AbsFeature implements
		IUpdateActionHandler {
	ILogger NqLog = LoggerFactory.getLogger("AbsSwitchFeature");
	// ===========================================================
	// Constants
	// ===========================================================
	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================
	public AbsSwitchFeature() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean isEnabled() {
		return getModule().canEnabled();
	}

	protected abstract IModule getModule();

	@Override
	public int getStatus() {
		return 0;
	}

	@Override
	public IUpdateActionHandler getHandler() {
		return this;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	@Override
	public void enableFeature() {
		NqLog.d("enableFeature: "+getFeatureId());
		getModule().setEnabled(true);
	}

	@Override
	public void disableFeature() {
		NqLog.d("disableFeature: "+getFeatureId());
		getModule().setEnabled(false);
	}

	@Override
	public void upgradeAssets(Map<String, String> keyValues) {
	}

	@Override
	public void updatePreferences(Map<String, String> keyValues) {
	}

	@Override
	public void hasUpdate() {
	}
	
	@Override
	public void updateSearchConfig(Map<String, String> keyValues) {
	}

	@Override
	public boolean supportModuleLevelAction() {
		return false;
	}

}
